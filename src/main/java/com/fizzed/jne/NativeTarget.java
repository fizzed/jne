package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reference for how this target generally works: <a href="https://llvm.org/doxygen/Triple_8h_source.html">https://llvm.org/doxygen/Triple_8h_source.html</a>
 * Configuration names are strings in the canonical form:
 *   ARCHITECTURE-VENDOR-OPERATING_SYSTEM
 * Or
 *   ARCHITECTURE-VENDOR-OPERATING_SYSTEM-ENVIRONMENT
 */
public class NativeTarget {

    private final OperatingSystem operatingSystem;
    private final HardwareArchitecture hardwareArchitecture;
    private final ABI abi;

    private NativeTarget(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, ABI abi) {
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.abi = abi;
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

    private void checkHardwareArchitecture() {
        if (this.hardwareArchitecture == null) {
            throw new IllegalArgumentException("Hardware architecture was null");
        }
    }

    private void checkOperatingSystem() {
        if (this.operatingSystem == null) {
            throw new IllegalArgumentException("Operating system was null");
        }
    }

    public String getExecutableFileExtension() {
        this.checkOperatingSystem();

        switch (this.operatingSystem) {
            case WINDOWS:
                return ".exe";
            default:
                return null;
        }
    }

    public String resolveExecutableFileName(String name) {
        Objects.requireNonNull(name, "name was null");
        this.checkOperatingSystem();

        final String ext = this.getExecutableFileExtension();

        if (ext != null) {
            return name + ext;
        }

        return name;
    }

    public String getLibraryFileExtension() {
        this.checkOperatingSystem();

        switch (this.operatingSystem) {
            case WINDOWS:
                return ".dll";
            case LINUX:
            case FREEBSD:
            case OPENBSD:
            case SOLARIS:
                return ".so";
            case MACOS:
                return ".dylib";
            default:
                return null;
        }
    }

    public String resolveLibraryFileName(String name) {
        Objects.requireNonNull(name, "name was null");
        this.checkOperatingSystem();

        switch (this.operatingSystem) {
            case WINDOWS:
                return name + ".dll";
            case LINUX:
            case FREEBSD:
            case OPENBSD:
            case SOLARIS:
                return "lib" + name + ".so";
            case MACOS:
                return "lib" + name + ".dylib";
            default:
                return name;
        }
    }

    public String resolveRustTarget() {
        this.checkHardwareArchitecture();
        this.checkOperatingSystem();

        // rust targets are a triple: <arch><sub>-<vendor>-<os>-<abi/env>
        String arch = null;
        String vendorOsEnv = null;

        switch (this.hardwareArchitecture) {
            case X64:
                arch = "x86_64";
                break;
            case X32:
                arch = "i686";
                break;
            case ARM64:
                arch = "aarch64";
                break;
            case RISCV64:
                arch = "riscv64gc";
                break;
            default:
                throw new IllegalArgumentException("Unsupported rust target for hardware architecture " + this.hardwareArchitecture + " (if it should be valid please add to " + this.getClass().getCanonicalName() + ")");
        }

        switch (this.operatingSystem) {
            case WINDOWS:
                if (abi == ABI.GNU) {
                    vendorOsEnv = "pc-windows-gnu";
                } else {
                    // default is msvc
                    vendorOsEnv = "pc-windows-msvc";
                }
                break;
            case LINUX:
                if (abi == ABI.MUSL) {
                    vendorOsEnv = "unknown-linux-musl";
                } else {
                    // default is glibc/gnu
                    vendorOsEnv = "unknown-linux-gnu";
                }
                break;
            case MACOS:
                vendorOsEnv = "apple-darwin";
                break;
            case FREEBSD:
                vendorOsEnv = "unknown-freebsd";
                break;
            case OPENBSD:
                vendorOsEnv = "unknown-openbsd";
                break;
            case SOLARIS:
                vendorOsEnv = "sun-solaris";
                break;
            default:
                throw new IllegalArgumentException("Unsupported rust target for operating system " + this.operatingSystem + " (if it should be valid please add to " + this.getClass().getCanonicalName() + ")");
        }

        return arch + "-" + vendorOsEnv;
    }

    static public String resolveJneOperatingSystemABI(OperatingSystem os, ABI abi, String osAlias) {
        // special case for linux, with an ABI
        if (os == OperatingSystem.LINUX) {
            if (abi == ABI.MUSL) {
                if (osAlias != null) {
                    return osAlias.toLowerCase() + "_" + abi.name().toLowerCase();
                } else {
                    return os.name().toLowerCase() + "_" + abi.name().toLowerCase();
                }
            }
        }

        if (osAlias != null) {
            return osAlias.toLowerCase();
        } else {
            return os.name().toLowerCase();
        }
    }

    static public String resolveJneHardwareArchitecture(HardwareArchitecture hardwareArchitecture, String hardwareArchitectureAlias) {
        if (hardwareArchitectureAlias != null) {
            return hardwareArchitectureAlias.toLowerCase();
        } else {
            return hardwareArchitecture.name().toLowerCase();
        }
    }

    public List<String> resolveResourcePaths(String resourcePrefix, String name) {
        final List<String> jneOsAbis = new ArrayList<>();

        if (this.operatingSystem != null) {
            jneOsAbis.add(resolveJneOperatingSystemABI(this.operatingSystem, this.abi, null));
            if (this.operatingSystem.getAliases() != null) {
                for (String alias : this.operatingSystem.getAliases()) {
                    jneOsAbis.add(resolveJneOperatingSystemABI(this.operatingSystem, this.abi, alias));
                }
            }
        } else {
            jneOsAbis.add(null);
        }

        final List<String> jneArchs = new ArrayList<>();

        if (this.hardwareArchitecture != null) {
            jneArchs.add(resolveJneHardwareArchitecture(this.hardwareArchitecture, null));
            if (this.hardwareArchitecture.getAliases() != null) {
                for (String alias : this.hardwareArchitecture.getAliases()) {
                    jneArchs.add(resolveJneHardwareArchitecture(this.hardwareArchitecture, alias));
                }
            }
        } else {
            jneArchs.add(null);
        }

        final List<String> resourcePaths = new ArrayList<>();

        for (String jneOsAbi : jneOsAbis) {
            for (String jneArch : jneArchs) {
                StringBuilder s = new StringBuilder();
                s.append(resourcePrefix);
                // append jneOsAbi if not null (if its null then its an "any" kind of lookup)
                if (jneOsAbi != null) {
                    s.append("/");
                    s.append(jneOsAbi);
                }
                // append jneArch if not null (if its null then its an "any" kind of lookup)
                if (jneArch != null) {
                    s.append("/");
                    s.append(jneArch);
                }
                // always append the name that we're looking for
                s.append("/");
                s.append(name);
                resourcePaths.add(s.toString());
            }
        }

        return resourcePaths;
    }

    static public NativeTarget of(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, ABI abi) {
        return new NativeTarget(operatingSystem, hardwareArchitecture, abi);
    }

}
