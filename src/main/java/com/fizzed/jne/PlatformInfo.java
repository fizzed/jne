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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformInfo {
    static private final Logger log = LoggerFactory.getLogger(PlatformInfo.class);

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
            final LinuxLibC linuxLibC = detectLinuxLibC();
            if (linuxLibC != null) {
                switch (linuxLibC) {
                    case MUSL:
                        abi = ABI.MUSL;
                        break;
                    case GLIBC:
                    case UNKNOWN:
                        abi = ABI.GNU;
                        break;
                }
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

    static private final MemoizedInitializer<LinuxLibC> linuxLibCRef = new MemoizedInitializer<>();

    static public LinuxLibC detectLinuxLibC() {
        return linuxLibCRef.once(new MemoizedInitializer.Initializer<LinuxLibC>() {
            @Override
            public LinuxLibC init() {
                // step 1: use /proc/self/mapped_files available in newer/some kernels to see what libs are loaded
                LinuxDetectedFilesResult detectedFilesResult = detectLinuxMappedFiles();

                if (detectedFilesResult != null && detectedFilesResult.getLibc() != null && detectedFilesResult.getLibc() != LinuxLibC.UNKNOWN) {
                    return detectedFilesResult.getLibc();
                }

                // step 2: search /lib/ directory for MUSL and/or architecture
                detectedFilesResult = detectLinuxLibFiles();

                if (detectedFilesResult != null && detectedFilesResult.getLibc() != null && detectedFilesResult.getLibc() != LinuxLibC.UNKNOWN) {
                    return detectedFilesResult.getLibc();
                }

                // if detectedFilesResult is null, we probably aren't even on linux
                if (detectedFilesResult == null) {
                    return null;
                }

                // fallback: we will assume this is GLIBC
                log.debug("Will assume we are running on GLIBC");
                return LinuxLibC.GLIBC;
            }
        });
    }

    static public class LinuxDetectedFilesResult {
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
                                    result.setLibc(LinuxLibC.MUSL);
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
                                    result.setLibc(LinuxLibC.MUSL);
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
            result.setLibc(LinuxLibC.GLIBC);
        }

        if (result.getLibc() == null) {
            log.debug("Unable to detect libc via mapped files strategy (in {} ms)", (System.currentTimeMillis() - now));
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
