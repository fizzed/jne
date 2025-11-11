package com.fizzed.jne;

import com.fizzed.jne.internal.OsReleaseFile;
import com.fizzed.jne.internal.SystemExecutor;
import com.fizzed.jne.internal.Uname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPlatform {
    static private final Logger log = LoggerFactory.getLogger(SystemPlatform.class);

    final private OperatingSystem operatingSystem;
    final private HardwareArchitecture hardwareArchitecture;
    final private ABI abi;
    final private String name;
    final private String prettyName;
    final private SemanticVersion version;
    final private SemanticVersion kernelVersion;

    public SystemPlatform(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, ABI abi, String name, String prettyName, SemanticVersion version, SemanticVersion kernelVersion) {
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.abi = abi;
        this.name = name;
        this.prettyName = prettyName;
        this.version = version;
        this.kernelVersion = kernelVersion;
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


    static public SystemPlatform detect(SystemExecutor systemExecutor) {
        // try uname first (if that fails, we are likely on windows)
        Uname uname;
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

        // we should now be able to detect the operating system and architecture
        OperatingSystem operatingSystem = null;
        HardwareArchitecture hardwareArchitecture = null;
        ABI abi = null;
        String name = null;
        String prettyName = null;
        SemanticVersion version = null;
        SemanticVersion kernelVersion = null;


        // with uname, we can detect os & arch
        if (uname != null) {
            try {
                NativeTarget nativeTarget = NativeTarget.detectFromText(uname.getOperatingSystem() + " " + uname.getMachine() + " " + uname.getHardwarePlatform());
                operatingSystem = nativeTarget.getOperatingSystem();
                hardwareArchitecture = nativeTarget.getHardwareArchitecture();
                try {
                    version = SemanticVersion.parse(uname.getVersion());
                } catch (Exception ex) {
                    log.warn("Unable to parse 'uname -a' VERSION: {}", ex.getMessage());
                }
                // we can default name and build a pretty name from the uname output
                name = uname.getOperatingSystem();
                if (version != null) {
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

        return new SystemPlatform(operatingSystem, hardwareArchitecture, abi, name, prettyName, version, kernelVersion);
    }

}