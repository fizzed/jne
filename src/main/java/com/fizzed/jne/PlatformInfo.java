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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class PlatformInfo {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfo.class);

    //
    // Operating System Detection
    //

    static private final AtomicReference<OperatingSystem> operatingSystemRef = new AtomicReference<>();

    static public OperatingSystem detectOperatingSystem() {
        // double lock prevention of only detecting this one time
        OperatingSystem operatingSystem = operatingSystemRef.get();

        if (operatingSystem == null) {
            synchronized (PlatformInfo.class) {
                // another thread may have won out detecting, so we check it again
                operatingSystem = operatingSystemRef.get();
                if (operatingSystem == null) {
                    operatingSystem = doDetectOperatingSystem();
                    operatingSystemRef.set(operatingSystem);
                }
            }
        }

        return operatingSystem;
    }

    static OperatingSystem doDetectOperatingSystem() {
        final String osName = System.getProperty("os.name");

        final long now = System.currentTimeMillis();
        log.trace("Trying to detect operating system via system property [{}]", osName);

        final OperatingSystem operatingSystem = detectOperatingSystemFromValues(osName);

        if (operatingSystem != OperatingSystem.UNKNOWN) {
            log.debug("Detected operating system {} in {} ms", operatingSystem, (System.currentTimeMillis() - now));
        } else {
            log.warn("Unable to detect operating system in {} ms", (System.currentTimeMillis() - now));
        }

        return operatingSystem;
    }

    static OperatingSystem detectOperatingSystemFromValues(String osName) {
        if (osName != null) {
            osName = osName.toLowerCase();
            if (osName.contains("windows")) {
                return OperatingSystem.WINDOWS;
            } else if (osName.contains("mac") || osName.contains("darwin")) {
                return OperatingSystem.MACOS;
            } else if (osName.contains("linux")) {
                return OperatingSystem.LINUX;
            } else if (osName.contains("sun") || osName.contains("solaris")) {
                return OperatingSystem.SOLARIS;
            } else if (osName.contains("freebsd")) {
                return OperatingSystem.FREEBSD;
            } else if (osName.contains("openbsd")) {
                return OperatingSystem.OPENBSD;
            }
        }
        return OperatingSystem.UNKNOWN;
    }

    //
    // Hardware Architecture Detection
    //

    static private final AtomicReference<HardwareArchitecture> hardwareArchitectureRef = new AtomicReference<>();

    static public HardwareArchitecture detectHardwareArchitecture() {
        // double lock prevention of only detecting this one time
        HardwareArchitecture hardwareArchitecture = hardwareArchitectureRef.get();

        if (hardwareArchitecture == null) {
            synchronized (PlatformInfo.class) {
                // another thread may have won out detecting, so we check it again
                hardwareArchitecture = hardwareArchitectureRef.get();
                if (hardwareArchitecture == null) {
                    hardwareArchitecture = doDetectHardwareArchitecture();
                    hardwareArchitectureRef.set(hardwareArchitecture);
                }
            }
        }

        return hardwareArchitecture;
    }

    static HardwareArchitecture doDetectHardwareArchitecture() {
        final String osArch = System.getProperty("os.arch");
        final LinuxMappedFilesResult linuxMappedFilesResult = detectLinuxMappedFiles();

        final long now = System.currentTimeMillis();
        log.trace("Trying to detect hardware architecture via system property [{}]", osArch);

        HardwareArchitecture hardwareArchitecture = detectHardwareArchitectureFromValues(osArch, linuxMappedFilesResult);

        if (hardwareArchitecture != HardwareArchitecture.UNKNOWN) {
            log.debug("Detected operating system {} in {} ms", hardwareArchitecture, (System.currentTimeMillis() - now));
        } else {
            log.warn("Unable to detect operating system in {} ms", (System.currentTimeMillis() - now));
        }

        return hardwareArchitecture;
    }

    static public HardwareArchitecture detectHardwareArchitectureFromValues(String osArch, LinuxMappedFilesResult linuxMappedFilesResult) {
        if (osArch != null) {
            osArch = osArch.toLowerCase();
            if (osArch.contains("amd64") || osArch.contains("x86_64")) {
                return HardwareArchitecture.X64;
            } else if (osArch.contains("i386") || osArch.contains("i686") || osArch.contains("x86")) {
                return HardwareArchitecture.X32;
            } else if (osArch.contains("aarch64")) {
                return HardwareArchitecture.ARM64;
            } else if (osArch.contains("arm") || osArch.contains("aarch32")) {
                // unfortunately, this arch is used for ARMEL vs ARMHF, we can leverage the mapped files on linux to help differentiate
                log.trace("System property arch [{}] is ambiguous, will try linux mapped files", osArch);
                if (linuxMappedFilesResult != null && linuxMappedFilesResult.getArch() != null) {
                    return linuxMappedFilesResult.getArch();
                }
            } else if (osArch.contains("riscv64")) {
                return HardwareArchitecture.RISCV64;
            } else if (osArch.contains("s390x")) {
                return HardwareArchitecture.S390X;
            } else if (osArch.contains("ppc64le")) {
                return HardwareArchitecture.PPC64LE;
            } else if (osArch.contains("mips64el") || osArch.contains("mips64le")) {
                return HardwareArchitecture.MIPS64LE;
            }
        }
        return HardwareArchitecture.UNKNOWN;
    }

    //
    // LibC Detection
    //

    static private final AtomicReference<LinuxLibC> linuxLibCRef = new AtomicReference<>();

    static public LinuxLibC detectLinuxLibC() {
        // double lock prevention of only detecting this one time
        LinuxLibC linuxLibC = linuxLibCRef.get();

        if (linuxLibC == null) {
            synchronized (PlatformInfo.class) {
                // another thread may have won out detecting, so we check it again
                linuxLibC = linuxLibCRef.get();
                if (linuxLibC == null) {
                    linuxLibC = detectLinuxMappedFiles().getLibc();
                    linuxLibCRef.set(linuxLibC);
                }
            }
        }

        return linuxLibC;
    }


    static public class LinuxMappedFilesResult {
        private LinuxLibC libc;
        private HardwareArchitecture arch;

        public LinuxLibC getLibc() {
            return libc;
        }

        public void setLibc(LinuxLibC libc) {
            this.libc = libc;
        }

        public HardwareArchitecture getArch() {
            return arch;
        }

        public void setArch(HardwareArchitecture arch) {
            this.arch = arch;
        }
    }

    static private final AtomicReference<LinuxMappedFilesResult> mappedFilesResultRef = new AtomicReference<>();

    static public LinuxMappedFilesResult detectLinuxMappedFiles() {
        // double lock prevention of only detecting this one time
        LinuxMappedFilesResult result = mappedFilesResultRef.get();

        if (result == null) {
            synchronized (PlatformInfo.class) {
                // another thread may have won out detecting, so we check it again
                result = mappedFilesResultRef.get();
                if (result == null) {
                    result = doDetectLinuxMappedFiles();
                    mappedFilesResultRef.set(result);
                }
            }
        }

        return result;
    }

    static LinuxMappedFilesResult doDetectLinuxMappedFiles() {
        // helpful technique discovered from https://github.com/xerial/sqlite-jdbc/blob/master/src/main/java/org/sqlite/util/OSInfo.java
        // can be used for both ARMEL vs ARMHF detection, as well as GLIBC vs. MUSL
        final long now = System.currentTimeMillis();
        final Path mapFilesDir = Paths.get("/proc/self/map_files");
        log.trace("Trying to detect libc/arch via [{}]", mapFilesDir);
        final LinuxMappedFilesResult result = new LinuxMappedFilesResult();
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
                                log.debug("Detected MUSL libc in {} ms", (System.currentTimeMillis() - now));
                                result.setLibc(LinuxLibC.MUSL);
                            } else if (realMapFilePath.contains("/libc")) {
                                possiblyFoundGlibc = true;
                            }

                            if (realMapFilePath.contains("armhf") || realMapFilePath.contains("arm-linux-gnueabihf")) {
                                log.debug("Detected ARMHF in {} ms", (System.currentTimeMillis() - now));
                                result.setArch(HardwareArchitecture.ARMHF);
                            } else if (realMapFilePath.contains("armel")) {
                                log.debug("Detected ARMEL in {} ms", (System.currentTimeMillis() - now));
                                result.setArch(HardwareArchitecture.ARMEL);
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
            log.debug("Detected GLIBC libc in {} ms", (System.currentTimeMillis() - now));
            //return LinuxLibC.GLIBC;
            result.setLibc(LinuxLibC.GLIBC);
        }

        if (result.getLibc() == null) {
            log.warn("Unable to detect libc in {} ms", (System.currentTimeMillis() - now));
            result.setLibc(LinuxLibC.UNKNOWN);
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
