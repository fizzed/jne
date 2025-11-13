package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2022 Fizzed, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fizzed.jne.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class PlatformInfo {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfo.class);

    final private OperatingSystem operatingSystem;
    final private HardwareArchitecture hardwareArchitecture;
    final private String name;
    final private String displayName;
    final private SemanticVersion version;
    final private SemanticVersion kernelVersion;
    final private String uname;
    final private LibC libC;
    final private SemanticVersion libCVersion;

    private PlatformInfo(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, String name, String displayName, SemanticVersion version, SemanticVersion kernelVersion, String uname, LibC libC, SemanticVersion libCVersion) {
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.name = name;
        this.displayName = displayName;
        this.version = version;
        this.kernelVersion = kernelVersion;
        this.uname = uname;
        this.libC = libC;
        this.libCVersion = libCVersion;
    }

    public OperatingSystem getOperatingSystem() {
        return this.operatingSystem;
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return this.hardwareArchitecture;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public SemanticVersion getVersion() {
        return this.version;
    }

    public SemanticVersion getKernelVersion() {
        return this.kernelVersion;
    }

    public String getUname() {
        return this.uname;
    }

    public LibC getLibC() {
        return this.libC;
    }

    public SemanticVersion getLibCVersion() {
        return this.libCVersion;
    }

    //
    // Static Detection Methods
    //

    public enum Detect {
        VERSION,
        LIBC,
        ALL
    }

    /**
     * IMPORTANT: This method does a fresh "detect" every time it's called, which is somewhat expensive. For faster
     * local-only detection, you may find other static methods in this class faster and will return cached results.
     *
     * Detects the platform details of the operating system and hardware architecture of the host system
     * by utilizing the provided {@link SystemExecutor} for executing system-level commands. The method
     * tries to determine the platform by executing standard commands (like 'uname') and by querying
     * platform-specific information such as the Windows Registry if necessary.
     *
     * @param detects
     * @return a {@link PlatformInfo} object containing detailed information about the platform,
     *         including the operating system, hardware architecture, kernel version, and other properties.
     *         If the platform cannot be determined, the returned object may contain null values or default configurations.
     */
    static public PlatformInfo detect(Detect... detects) {
        return detect(SystemExecutor.LOCAL, detects);
    }

    /**
     * IMPORTANT: This method does a fresh "detect" every time it's called, which is somewhat expensive. For faster
     * local-only detection, you may find other static methods in this class faster and will return cached results.
     *
     * Detects the platform details of the operating system and hardware architecture of the host system
     * by utilizing the provided {@link SystemExecutor} for executing system-level commands. The method
     * tries to determine the platform by executing standard commands (like 'uname') and by querying
     * platform-specific information such as the Windows Registry if necessary.
     *
     * @param systemExecutor the instance of {@link SystemExecutor} used to execute commands on the host system.
     *                        It is essential for gathering system information to deduce the operating system
     *                        and hardware details.
     * @return a {@link PlatformInfo} object containing detailed information about the platform,
     *         including the operating system, hardware architecture, kernel version, and other properties.
     *         If the platform cannot be determined, the returned object may contain null values or default configurations.
     */
    static public PlatformInfo detect(SystemExecutor systemExecutor, Detect... detects) {
        final long startTime = System.currentTimeMillis();

        // we should now be able to detect the operating system and architecture
        final Set<Detect> detectSet = EnumSet.copyOf(Arrays.asList(detects));
        OperatingSystem operatingSystem = null;
        HardwareArchitecture hardwareArchitecture = null;
        String name = null;
        String displayName = null;
        SemanticVersion version = null;
        SemanticVersion kernelVersion = null;
        Uname uname = null;
        LibC libC = null;
        SemanticVersion libCVersion = null;

        // try uname first (if that fails, we are likely on windows)
        try {
            log.debug("Trying 'uname -a' to detect system platform...");
            final String unameOutput = systemExecutor.execProcess(asList(0), "uname", "-a");
            try {
                uname = Uname.parse(unameOutput);
            } catch (Exception ex) {
                log.warn("Unable to parse 'uname -a' output: {}", ex.getMessage());
                uname = null;
            }
        } catch (Exception e) {
            log.debug("Unable to execute 'uname -a' to detect system platform: {}", e.getMessage());
            uname = null;
        }

        // if uname fails, we should try to see if we're on windows, or someone may have installed "cygwin" or "msys"
        // on windows, and we may actually want to try the registry as well
        if (uname == null || (uname.getSource().toLowerCase().contains("cygwin") || uname.getSource().toLowerCase().contains("msys"))) {
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
                displayName = productName;
            } catch (Exception e) {
                log.debug("Unable to query windows registry to detect system platform: {}", e.getMessage());
            }
        }

        // with uname, we can detect os & arch
        if (uname != null) {
            try {
                log.debug("Detecting os & arch from 'uname -a' output...");

                // try first with limited fields
                NativeTarget nativeTarget = NativeTarget.detectFromText(uname.getSysname() + " " + uname.getOperatingSystem() + " " + uname.getMachine() + " " + uname.getHardwarePlatform());
                operatingSystem = nativeTarget.getOperatingSystem();
                hardwareArchitecture = nativeTarget.getHardwareArchitecture();

                if (operatingSystem == null) {
                    // try with the whole thing
                    nativeTarget = NativeTarget.detectFromText(uname.getSource());
                    operatingSystem = nativeTarget.getOperatingSystem();
                }

                if (hardwareArchitecture == null) {
                    // try with the whole thing
                    nativeTarget = NativeTarget.detectFromText(uname.getSource());
                    hardwareArchitecture = nativeTarget.getHardwareArchitecture();
                }

                try {
                    version = SemanticVersion.parse(uname.getVersion());
                } catch (Exception ex) {
                    log.warn("Unable to parse 'uname -a' VERSION: {}", ex.getMessage());
                }
                if (name == null) {
                    name = uname.getSysname();
                }
                if (version != null && displayName == null) {
                    displayName = name + " " + version;
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
            log.debug("Trying /etc/os-release to detect platform info...");
            try {
                String osReleaseFileOutput = systemExecutor.catFile("/etc/os-release");
                osReleaseFile = OsReleaseFile.parse(osReleaseFileOutput);
                name = osReleaseFile.getName();
                // if the version isn't null, we'll use that to build our display name
                if (osReleaseFile.getVersion() != null) {
                    displayName = name + " " + osReleaseFile.getVersion();
                } else if (osReleaseFile.getPrettyName() != null) {
                    displayName = osReleaseFile.getPrettyName();
                } else if (osReleaseFile.getVersionId() != null) {
                    displayName = name + " " + osReleaseFile.getVersionId();
                } else {
                    // just fallback to the name
                    displayName = name;
                }

                try {
                    version = SemanticVersion.parse(osReleaseFile.getVersionId());
                } catch (Exception ex) {
                    log.warn("Unable to parse /etc/os-release VERSION_ID: {}", ex.getMessage());
                }
            } catch (Exception e) {
                log.debug("Unable to read /etc/os-release file: {}", e.getMessage());
            }
        }

        // on macos, we can grab a better version
        if (operatingSystem == OperatingSystem.MACOS) {
            log.debug("Trying macos 'sw_vers' to detect platform info...");
            MacSwVers swVers = null;
            try {
                String swVersOutput = systemExecutor.execProcess("sw_vers");
                swVers = MacSwVers.parse(swVersOutput);
                name = swVers.getProductName();
                displayName = swVers.getProductName() + " " + swVers.getProductVersion();
                version = SemanticVersion.parse(swVers.getProductVersion());
                // see if we can make it even prettier, with its name
                final String versionName = MacReleases.getVersionName(version.getMajor(), version.getMinor());
                if (versionName != null) {
                    displayName += " (" + versionName + ")";
                }
            } catch (Exception e) {
                log.warn("Unable to execute 'sw_ver' to detect system platform: {}", e.getMessage());
            }
        }

        if (detectSet.contains(Detect.ALL) || detectSet.contains(Detect.LIBC)) {
            if (operatingSystem == OperatingSystem.LINUX) {
                // let's try to detect the libc version
                LibCResult libcResult = detectLibC(systemExecutor);
                if (libcResult != null) {
                    libC = libcResult.getLibC();
                    libCVersion = libcResult.getVersion();
                }
            }
        }

        log.debug("Completed detecting platform info in {} ms", (System.currentTimeMillis() - startTime));

        // did it work?
        if (operatingSystem == null || hardwareArchitecture == null) {
            throw new IllegalStateException("Unable to detect platform details (both os and arch were null)");
        }

        return new PlatformInfo(operatingSystem, hardwareArchitecture, name, displayName, version, kernelVersion, ofNullable(uname).map(Uname::getSource).orElse(null), libC, libCVersion);
    }

    static private LibCResult detectLibC(SystemExecutor systemExecutor) {
        LibC libC = null;
        SemanticVersion version = null;

        // ldd /bin/ls is a technique that apparently works well for GLIBC or MUSL
        try {
            log.debug("Trying to detect libc version via 'ldd /bin/ls' output...");
            final String lddOutput = systemExecutor.execProcess("ldd", "/bin/ls");
            final LibCs.PathResult pathResult = LibCs.parsePath(lddOutput);

            if (pathResult != null && pathResult.getLibC() != null) {
                libC = pathResult.getLibC();
                // we now literally execute the .so and it'll print out version info that we can parse
                try {
                    log.debug("Detected libc {} with path {} (will now try to detect version...)", pathResult.getLibC(), pathResult.getPath());
                    final String libcOutput = systemExecutor.execProcess(Collections.emptyList(), pathResult.getPath());
                    final String versionString = LibCs.parseVersion(libcOutput);
                    if (versionString != null) {
                        version = SemanticVersion.parse(versionString);
                    }
                } catch (Exception ex) {
                    log.warn("Unable to execute {} to detect libc version: {}", pathResult.getPath(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Unable to execute 'ldd' to detect libc version: {}", e.getMessage());
        }

        if (libC != null) {
            return new LibCResult(libC, version);
        }

        return null;
    }

    /**
     * Detects minimal platform-specific information, specifically the operating system
     * and hardware architecture, of the host system. Unlike more comprehensive detection
     * methods, this method focuses on essential details by returning a basic {@link PlatformInfo}
     * object populated only with the operating system and hardware architecture information.
     *
     * This method internally delegates tasks to {@link #detectOperatingSystem()} and
     * {@link #detectHardwareArchitecture()} for retrieving the necessary details.
     * Other platform properties such as name, version, and kernel details are left
     * uninitialized or set to null.
     *
     * Example usage:
     * - This method is intended to be used in situations where only basic platform
     *   information (such as OS and architecture) is required, and a full platform
     *   detection process is unnecessary.
     * - For environments where detailed information is needed (e.g., kernel version
     *   or library details), consider using {@link #detect)}.
     *
     * @return a {@link PlatformInfo} object containing only the operating system
     *         and hardware architecture details of the host system. Other properties
     *         in the returned object are set to null or default values.
     */
    static public PlatformInfo detectBasic() {
        return new PlatformInfo(detectOperatingSystem(), detectHardwareArchitecture(), null, null, null, null, null, detectLinuxLibC(), null);
    }

    //
    // Operating System Detection
    //

    static private final MemoizedInitializer<OperatingSystem> operatingSystemRef = new MemoizedInitializer<>();

    static public OperatingSystem detectOperatingSystem() {
        return operatingSystemRef.once(new MemoizedInitializer.Initializer<OperatingSystem>() {
            @Override
            public OperatingSystem init() {
                return doDetectOperatingSystem();
            }
        });
    }

    static OperatingSystem doDetectOperatingSystem() {
        final String osName = System.getProperty("os.name");

        final long now = System.currentTimeMillis();
        log.trace("Trying to detect operating system via system property [{}]", osName);

        final OperatingSystem operatingSystem = detectOperatingSystemFromValues(osName);

        if (operatingSystem != null) {
            log.debug("Detected operating system {} (in {} ms)", operatingSystem, (System.currentTimeMillis() - now));
        } else {
            log.warn("Unable to detect operating system (in {} ms)", (System.currentTimeMillis() - now));
        }

        return operatingSystem;
    }

    static OperatingSystem detectOperatingSystemFromValues(String osName) {
        return OperatingSystem.resolve(osName);
    }

    //
    // Hardware Architecture Detection
    //

    static private final MemoizedInitializer<HardwareArchitecture> hardwareArchitectureRef = new MemoizedInitializer<>();

    static public HardwareArchitecture detectHardwareArchitecture() {
        return hardwareArchitectureRef.once(new MemoizedInitializer.Initializer<HardwareArchitecture>() {
            @Override
            public HardwareArchitecture init() {
                return doDetectHardwareArchitecture();
            }
        });
    }

    static HardwareArchitecture doDetectHardwareArchitecture() {
        final String osArch = System.getProperty("os.arch");
        // armhf vs. armel is hard to detect in many cases
        // https://github.com/bytedeco/javacpp/pull/123/commits/642b6d9823a290488e8c4dd8f579cf3e414ab3b3
        final String abiType = System.getProperty("sun.arch.abi");
        final String bootLibPath = System.getProperty("sun.boot.library.path", "").toLowerCase();
        final LinuxDetectedFilesResult linuxMappedFilesResult = detectLinuxMappedFiles();

        final long now = System.currentTimeMillis();

        final HardwareArchitecture hardwareArchitecture = detectHardwareArchitectureFromValues(
                osArch, abiType, bootLibPath, linuxMappedFilesResult);

        if (hardwareArchitecture != null) {
            log.debug("Detected hardware architecture {} (in {} ms)", hardwareArchitecture, (System.currentTimeMillis() - now));
        } else {
            log.warn("Unable to detect hardware architecture (in {} ms)", (System.currentTimeMillis() - now));
        }

        return hardwareArchitecture;
    }

    static HardwareArchitecture detectHardwareArchitectureFromValues(
            String osArch,
            String abiType,
            String bootLibPath,
            LinuxDetectedFilesResult linuxMappedFilesResult) {

        log.trace("Trying to detect hardware architecture via sysprops arch={}, abi={}, bootpath={}", osArch, abiType, bootLibPath);

        if (osArch != null) {
            osArch = osArch.toLowerCase();
            abiType = abiType != null ? abiType.toLowerCase() : "none";
            bootLibPath = bootLibPath != null ? bootLibPath.toLowerCase() : "none";

            // delegate most of the lookup to the HW enum
            HardwareArchitecture hardwareArchitecture = HardwareArchitecture.resolve(osArch);
            if (hardwareArchitecture != null) {
                return hardwareArchitecture;
            }

            /*if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                return HardwareArchitecture.X64;
            } else if (osArch.contains("i386") || osArch.contains("i686") || osArch.contains("x86")) {
                return HardwareArchitecture.X32;
            } else if (osArch.contains("aarch64")) {
                return HardwareArchitecture.ARM64;
            } else if (osArch.contains("armv7l")) {
                return HardwareArchitecture.ARMHF;
            } else*/ if (osArch.contains("arm") || osArch.contains("aarch32")) {
                // unfortunately, this arch is used for ARMEL vs ARMHF, we can leverage the mapped files on linux to help differentiate
                log.trace("System property arch [{}] is ambiguous, will try a few workarounds", osArch);
                // abitype? e.g. gnueabihf
                if ("gnueabihf".equals(abiType)) {
                    return HardwareArchitecture.ARMHF;
                }
                // boot lib path?
                if (bootLibPath.contains("armhf") || bootLibPath.contains("aarch32hf")) {
                    return HardwareArchitecture.ARMHF;
                }

                // now check for soft-float (which is likely less common)
                if ("gnueabi".equals(abiType)) {
                    return HardwareArchitecture.ARMEL;
                }
                if (bootLibPath.contains("armsf") || bootLibPath.contains("aarch32sf")) {
                    return HardwareArchitecture.ARMEL;
                }

                // linux mapped files?
                if (linuxMappedFilesResult != null && linuxMappedFilesResult.getArch() != null && linuxMappedFilesResult.getArch() != null) {
                    return linuxMappedFilesResult.getArch();
                }
                // the most common is likely hard float
            } /*else if (osArch.contains("riscv64")) {
                return HardwareArchitecture.RISCV64;
            } else if (osArch.contains("s390x")) {
                return HardwareArchitecture.S390X;
            } else if (osArch.contains("ppc64le")) {
                return HardwareArchitecture.PPC64LE;
            } else if (osArch.contains("mips64el") || osArch.contains("mips64le")) {
                return HardwareArchitecture.MIPS64LE;
            }*/
        }
        return null;
    }

    //
    // Binary Environment Detection
    //

    static private final ConcurrentHashMap<OperatingSystem,ABI> abiRefs = new ConcurrentHashMap<>();

    static public ABI detectAbi(OperatingSystem os) {
        return abiRefs.computeIfAbsent(os, PlatformInfo::doDetectAbi);
    }

    static ABI doDetectAbi(OperatingSystem os) {
        final long now = System.currentTimeMillis();

        ABI abi = null;

        if (os == OperatingSystem.LINUX) {
            final LibC libC = detectLinuxLibC();
            switch (ofNullable(libC).orElse(LibC.GLIBC)) {
                case MUSL:
                    abi = ABI.MUSL;
                    break;
                case GLIBC:
                    abi = ABI.GNU;
                    break;
            }
        }

        if (abi == null) {
            abi = ABI.DEFAULT;
        }

        log.debug("Detected {} abi {} (in {} ms)", os, abi, (System.currentTimeMillis() - now));

        return abi;
    }

    //
    // LibC Detection
    //

    static private final MemoizedInitializer<LibC> linuxLibCRef = new MemoizedInitializer<>();

    static public LibC detectLinuxLibC() {
        return linuxLibCRef.once(new MemoizedInitializer.Initializer<LibC>() {
            @Override
            public LibC init() {
                // step 1: use /proc/self/mapped_files available in newer/some kernels to see what libs are loaded
                LinuxDetectedFilesResult detectedFilesResult = detectLinuxMappedFiles();

                if (detectedFilesResult != null && detectedFilesResult.getLibc() != null) {
                    return detectedFilesResult.getLibc();
                }

                // step 2: search /lib/ directory for MUSL and/or architecture
                detectedFilesResult = detectLinuxLibFiles();

                if (detectedFilesResult != null && detectedFilesResult.getLibc() != null) {
                    return detectedFilesResult.getLibc();
                }

                // if detectedFilesResult is null, we probably aren't even on linux
                if (detectedFilesResult == null) {
                    return null;
                }

                // fallback: we will assume this is GLIBC
                log.debug("Will assume we are running on GLIBC");
                return LibC.GLIBC;
            }
        });
    }

    static public class LinuxDetectedFilesResult {
        private LibC libc;
        private HardwareArchitecture arch;

        public LibC getLibc() {
            return libc;
        }

        public void setLibc(LibC libc) {
            this.libc = libc;
        }

        public HardwareArchitecture getArch() {
            return arch;
        }

        public void setArch(HardwareArchitecture arch) {
            this.arch = arch;
        }
    }

    static private final MemoizedInitializer<LinuxDetectedFilesResult> libFilesResultRef = new MemoizedInitializer<>();
    static private final MemoizedInitializer<LinuxDetectedFilesResult> mappedFilesResultRef = new MemoizedInitializer<>();

    static public LinuxDetectedFilesResult detectLinuxLibFiles() {
        return libFilesResultRef.once(new MemoizedInitializer.Initializer<LinuxDetectedFilesResult>() {
            @Override
            public LinuxDetectedFilesResult init() {
                return doDetectLinuxLibFiles();
            }
        });
    }

    static LinuxDetectedFilesResult doDetectLinuxLibFiles() {
        // only do this on linux
        final OperatingSystem os = detectOperatingSystem();
        if (os != OperatingSystem.LINUX) {
            // skip doing this on anything other than linux
            return null;
        }

        // issue: https://github.com/facebook/rocksdb/issues/9956
        // some version of the kernel are missing mapped files, and its potentially slow as well
        final long now = System.currentTimeMillis();
        final Path libDir = Paths.get("/lib/");
        log.trace("Trying to detect libc/arch via [{}]", libDir);
        final LinuxDetectedFilesResult result = new LinuxDetectedFilesResult();
        try {
            if (Files.exists(libDir)) {
                final File[] mapFiles = libDir.toFile().listFiles();
                if (mapFiles != null) {
                    for (File mapFile : mapFiles) {
                        try {
                            log.trace("Analyzing file {}", mapFile);
                            final String name = mapFile.getName().toLowerCase();

                            if (name.contains("musl")) {
                                // only try detecting this once
                                if (result.getLibc() == null) {
                                    log.debug("Detected libc MUSL via /lib dir strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setLibc(LibC.MUSL);
                                }
                            }

                            if (name.contains("armhf") || name.contains("arm-linux-gnueabihf")) {
                                // only try detecting this once
                                if (result.getArch() != HardwareArchitecture.ARMHF) {
                                    log.debug("Detected hardware architecture ARMHF via /lib dir strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setArch(HardwareArchitecture.ARMHF);
                                }
                            } else if (name.contains("armel") || name.contains("arm-linux-gnueabi")) {
                                // only try detecting this once
                                if (result.getArch() != HardwareArchitecture.ARMEL) {
                                    log.debug("Detected hardware architecture ARMEL via /lib dir strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setArch(HardwareArchitecture.ARMEL);
                                }
                            }
                        } catch (Exception e) {
                            // ignore this
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore it
        }

        return result;
    }

    static public LinuxDetectedFilesResult detectLinuxMappedFiles() {
        return mappedFilesResultRef.once(new MemoizedInitializer.Initializer<LinuxDetectedFilesResult>() {
            @Override
            public LinuxDetectedFilesResult init() {
                return doDetectLinuxMappedFiles();
            }
        });
    }

    static LinuxDetectedFilesResult doDetectLinuxMappedFiles() {
        // only do this on linux
        final OperatingSystem os = detectOperatingSystem();
        if (os != OperatingSystem.LINUX) {
            // skip doing this on anything other than linux
            return null;
        }

        // NOTE: this was only added in linux v3.3+, it will not work below that
        // https://github.com/dmlc/xgboost/issues/7915
        // helpful technique discovered from https://github.com/xerial/sqlite-jdbc/blob/master/src/main/java/org/sqlite/util/OSInfo.java
        // can be used for both ARMEL vs ARMHF detection, as well as GLIBC vs. MUSL
        final long now = System.currentTimeMillis();
        final Path mapFilesDir = Paths.get("/proc/self/map_files");
        log.trace("Trying to detect libc/arch via [{}]", mapFilesDir);
        final LinuxDetectedFilesResult result = new LinuxDetectedFilesResult();
        boolean possiblyFoundGlibc = false;
        try {
            if (Files.exists(mapFilesDir)) {
                final File[] mapFiles = mapFilesDir.toFile().listFiles();
                if (mapFiles != null) {
                    for (File mapFile : mapFiles) {
                        try {
                            final String _realMapFilePath = realpath(mapFile.toPath());
                            log.trace("Analyzing file {}", _realMapFilePath);
                            final String realMapFilePath = _realMapFilePath.toLowerCase();

                            if (realMapFilePath.contains("musl")) {
                                // only try detecting this once
                                if (result.getLibc() == null) {
                                    log.debug("Detected libc MUSL via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setLibc(LibC.MUSL);
                                }
                            } else if (realMapFilePath.contains("/libc")) {
                                possiblyFoundGlibc = true;
                            }

                            if (realMapFilePath.contains("armhf") || realMapFilePath.contains("arm-linux-gnueabihf")) {
                                // only try detecting this once
                                if (result.getArch() != HardwareArchitecture.ARMHF) {
                                    log.debug("Detected hardware architecture ARMHF via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setArch(HardwareArchitecture.ARMHF);
                                }
                            } else if (realMapFilePath.contains("armel") || realMapFilePath.contains("arm-linux-gnueabi")) {
                                // only try detecting this once
                                if (result.getArch() != HardwareArchitecture.ARMEL) {
                                    log.debug("Detected hardware architecture ARMEL via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
                                    result.setArch(HardwareArchitecture.ARMEL);
                                }
                            }
                        } catch (IOException e) {
                            // ignore this
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore it
        }

        if (possiblyFoundGlibc) {
            log.debug("Detected libc GLIBC via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
            result.setLibc(LibC.GLIBC);
        }

        if (result.getLibc() == null) {
            log.debug("Unable to detect libc via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
        }

        return result;
    }

    static private String realpath(Path file) throws IOException {
        try {
            return file.toRealPath().toString();
        } catch (IOException ioe1) {
            // on alpine linux, this operation was "not permitted", but this worked instead
            try {
                return Files.readSymbolicLink(file).toString();
            } catch (IOException ioe2) {
                // ignore this particular error
            }
            throw ioe1;
        }
    }

}
