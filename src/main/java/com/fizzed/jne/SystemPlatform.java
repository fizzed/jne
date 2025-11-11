package com.fizzed.jne;

import com.fizzed.jne.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

public class SystemPlatform {
    static private final Logger log = LoggerFactory.getLogger(SystemPlatform.class);

    final private OperatingSystem operatingSystem;
    final private HardwareArchitecture hardwareArchitecture;
    final private ABI abi;
    final private String name;
    final private String prettyName;
    final private SemanticVersion version;
    final private SemanticVersion kernelVersion;
    final private String uname;

    public SystemPlatform(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, ABI abi, String name, String prettyName, SemanticVersion version, SemanticVersion kernelVersion, String uname) {
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.abi = abi;
        this.name = name;
        this.prettyName = prettyName;
        this.version = version;
        this.kernelVersion = kernelVersion;
        this.uname = uname;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public ABI getAbi() {
        return abi;
    }

    public String getName() {
        return name;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public SemanticVersion getVersion() {
        return version;
    }

    public SemanticVersion getKernelVersion() {
        return kernelVersion;
    }

    public String getUname() {
        return this.uname;
    }

    static public SystemPlatform detect(SystemExecutor systemExecutor) {
        // we should now be able to detect the operating system and architecture
        OperatingSystem operatingSystem = null;
        HardwareArchitecture hardwareArchitecture = null;
        ABI abi = null;
        String name = null;
        String prettyName = null;
        SemanticVersion version = null;
        SemanticVersion kernelVersion = null;
        Uname uname = null;

        // try uname first (if that fails, we are likely on windows)
        try {
            log.debug("Trying 'uname -a' to detect system platform...");
            final String unameOutput = systemExecutor.execProcess("uname", "-a");
            try {
                uname = Uname.parse(unameOutput);
            } catch (Exception ex) {
                log.warn("Unable to parse 'uname -a' output: {}", ex.getMessage());
                uname = null;
            }
        } catch (Exception e) {
            log.trace("Unable to execute 'uname -a' to detect system platform: {}", e.getMessage());
            uname = null;
        }

        // if uname fails, we should try to see if we're on windows
        if (uname == null) {
            // on windows, we can grab a better version via the registry via 2 queries
            try {
                log.debug("Trying windows registry to detect system platform...");
                WindowsRegistry windowsRegistryCurrentVersion = WindowsRegistry.queryCurrentVersion(systemExecutor);
                WindowsRegistry windowsRegistrySystemEnvironment = WindowsRegistry.querySystemEnvironmentVariables(systemExecutor);
                WindowsRegistry windowsRegistryComputerName = WindowsRegistry.queryComputerName(systemExecutor);

                // we are going to mimic 'uname -a' by combining the keys together
                // [Sysname] [Nodename] [Release] [Version] [Machine]
                // Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC x86_64 GNU/Linux
                // FreeBSD bmh-build-x64-freebsd15-1 15.0-ALPHA1 FreeBSD 15.0-ALPHA1 #0 stable/15-n280099-0b3d82579a01 amd64
                // windows version numbers are pretty complicated, we'll safely try to get the values we need
                // This key was introduced in Windows 10
                int majorVersion = ofNullable(windowsRegistryCurrentVersion.get("CurrentMajorVersionNumber"))
                    .map(Integer::parseInt)
                    .orElse(-1);
                // This key was introduced in Windows 10
                int minorVersion = ofNullable(windowsRegistryCurrentVersion.get("CurrentMinorVersionNumber"))
                    .map(Integer::parseInt)
                    .orElse(-1);
                // Windows 7 has this, and it's the most important value. For Windows 7 with Service Pack 1, this value is 7601
                int buildVersion = ofNullable(windowsRegistryCurrentVersion.get("CurrentBuildNumber"))
                    .map(Integer::parseInt)
                    .orElse(-1);
                // Windows 7, 8, and 8.1 all reported their version as 6.x (e.g., Windows 7 was 6.1, Windows 8.1 was 6.3) to maintain application compatibility.
                final String currentVersion = windowsRegistryCurrentVersion.get("CurrentVersion");

                // While ProductName is almost always correct, there was a known issue during the initial release of Windows 11
                // where systems that were upgraded from Windows 10 sometimes retained the ProductName value of Windows 10 Pro
                String productName =  windowsRegistryCurrentVersion.get("ProductName");           // e.g. Windows 10 Pro, Windows Server 2022 Standard, Windows Server 2019 Datacenter

                // Windows 10 and 11: Yes. This key is present and provides the user-friendly "feature update" version, like 23H2 (for Windows 11) or 22H2 (for Windows 10).
                String displayVersion =  windowsRegistryCurrentVersion.get("DisplayVersion");     // e.g. 25H2

                final String computerName = windowsRegistryComputerName.get("ComputerName");
                final String processorArchitecture =  windowsRegistrySystemEnvironment.get("PROCESSOR_ARCHITECTURE");

                // any build number >= 22000 is windows 11, we can fix the product name "bug"
                if (buildVersion >= 22000) {
                    productName = productName.replace(" 10 ", " 11 ");
                }

                // append the feature pack if that key exists
                if (displayVersion != null) {
                    productName += " (" + displayVersion + ")";
                }

                // let's build out a "kernel version" we can actually use
                final String kernelVersionString;
                if (majorVersion > 0) {
                    kernelVersionString = majorVersion + "." + minorVersion + "." + buildVersion;
                } else {
                    kernelVersionString = currentVersion + "." + buildVersion;
                }

                // to build out an "os version" string, we need to fix the major versions
                final String osVersionString;
                if (buildVersion >= 22000 && majorVersion == 10) {
                    osVersionString = "11" + "." + minorVersion + "." + buildVersion;
                } else if (majorVersion > 0) {
                    // majorVersion was only introduced in windows 10, but this could also handle a windows 12?
                    osVersionString = majorVersion + "." + minorVersion + "." + buildVersion;
                } else if (majorVersion < 0) {
                    // this indicates we are on something other than windows 10, like 7, 8.1, etc.
                    // Windows 7 was 6.1, Windows 8.1 was 6.3
                    if ("6.3".equals(currentVersion)) {
                        osVersionString =  "8.1." + buildVersion;
                    } else if ("6.2".equals(currentVersion)) {
                        osVersionString =  "8.0." + buildVersion;
                    } else {
                        osVersionString =  "7.0." + buildVersion;
                    }
                } else {
                    // all else fails, we'll default to this value
                    osVersionString = currentVersion;
                }

                final String unameString = "Windows " + computerName + " " + osVersionString + " " + productName + " " + processorArchitecture;

                log.debug("Generated windows os version={}, uname-{}", osVersionString, unameString);

                uname = Uname.parse(unameString);
                kernelVersion = SemanticVersion.parse(kernelVersionString);
                name = "Windows";
                prettyName = productName;
            } catch (Exception e) {
                log.trace("Unable to query windows registry to detect system platform: {}", e.getMessage());
            }
        }

        // with uname, we can detect os & arch
        if (uname != null) {
            try {
                NativeTarget nativeTarget = NativeTarget.detectFromText(uname.getSysname() + " " + uname.getOperatingSystem() + " " + uname.getMachine() + " " + uname.getHardwarePlatform());
                operatingSystem = nativeTarget.getOperatingSystem();
                hardwareArchitecture = nativeTarget.getHardwareArchitecture();
                try {
                    version = SemanticVersion.parse(uname.getVersion());
                } catch (Exception ex) {
                    log.warn("Unable to parse 'uname -a' VERSION: {}", ex.getMessage());
                }
                if (name == null) {
                    name = uname.getSysname();
                }
                if (version != null && prettyName == null) {
                    prettyName = name + " " + version;
                }
            } catch (Exception e) {
                log.warn("Unable to detect operating system from 'uname -a' output: {}", e.getMessage());
            }
        }

        // if we have a version, on these platforms it's really the kernel version
        if (operatingSystem == OperatingSystem.LINUX || operatingSystem == OperatingSystem.MACOS) {
            kernelVersion = version;
            version = null;
        }

        // if there is an /etc/os-release file, we can get a lot of what we need out of it
        OsReleaseFile osReleaseFile = null;
        // only try this on platforms we know have it
        if (operatingSystem == OperatingSystem.LINUX || operatingSystem == OperatingSystem.FREEBSD) {
            try {
                String osReleaseFileOutput = systemExecutor.catFile("/etc/os-release");
                osReleaseFile = OsReleaseFile.parse(osReleaseFileOutput);
                name = osReleaseFile.getName();
                prettyName = osReleaseFile.getPrettyName();
                try {
                    version = SemanticVersion.parse(osReleaseFile.getVersionId());
                } catch (Exception ex) {
                    log.warn("Unable to parse /etc/os-release VERSION_ID: {}", ex.getMessage());
                }
            } catch (Exception e) {
                log.trace("Unable to read /etc/os-release file: {}", e.getMessage());
            }
        }

        // on macos, we can grab a better version
        if (operatingSystem == OperatingSystem.MACOS) {
            MacSwVers swVers = null;
            try {
                String swVersOutput = systemExecutor.execProcess("sw_vers");
                swVers = MacSwVers.parse(swVersOutput);
                name = swVers.getProductName();
                prettyName = swVers.getProductName() + " " + swVers.getProductVersion();
                version = SemanticVersion.parse(swVers.getProductVersion());
            } catch (Exception e) {
                log.trace("Unable to execute 'sw_ver' to detect system platform: {}", e.getMessage());
            }
        }

        return new SystemPlatform(operatingSystem, hardwareArchitecture, abi, name, prettyName, version, kernelVersion, uname.getSource());
    }

}