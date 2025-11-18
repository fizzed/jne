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

    /**
     * Determines and returns the file extension commonly used for executable files
     * on the current operating system of the machine.
     * This method internally checks the operating system to infer the appropriate
     * file extension, such as ".exe" for Windows or an empty string for Unix-based systems.
     *
     * Example usage:
     * A Windows operating system will return ".exe".
     * A Linux operating system may return an empty string as executables usually do not have specific extensions.
     *
     * @return A string representing the executable file extension specific to the operating system.
     *         Returns ".exe" for Windows, an empty string for Unix-based systems, or other extensions
     *         specific to different operating systems.
     */
    public String getExecutableFileExtension() {
        this.checkOperatingSystem();
        return getExecutableFileExtension(this.operatingSystem);
    }

    /**
     * Determines the executable file extension for a given operating system.
     * The method currently supports returning the extension for the Windows operating system.
     *
     * Example usage:
     *
     * OperatingSystem os = OperatingSystem.WINDOWS;
     * String extension = getExecutableFileExtension(os);
     * // extension would be ".exe"
     *
     * @param os the operating system for which the executable file extension is to be retrieved
     * @return the executable file extension as a string for the given operating system,
     *         or null if the operating system is not supported
     */
    static public String getExecutableFileExtension(OperatingSystem os) {
        if (os == OperatingSystem.WINDOWS) {
            return ".exe";
        }
        return null;
    }

    /**
     * Resolves the full executable file name based on the provided name and the operating system in use.
     * This method first ensures the operating system is checked or set, then resolves the full path
     * or any necessary extension for the given name of the executable. Commonly used to handle
     * platform-specific variations in executable names (e.g., appending ".exe" on Windows).
     *
     * @param name the base name of the executable file without any platform-specific modifications
     *             or extensions (e.g., "program").
     * @return the resolved executable file name, potentially including a file extension or path
     *         specific to the detected operating system (e.g., "program.exe" on Windows, "program" on Unix).
     */
    public String resolveExecutableFileName(String name) {
        this.checkOperatingSystem();
        return resolveExecutableFileName(this.operatingSystem, name);
    }

    /**
     * Resolves the executable file name for the specified operating system by appending
     * the appropriate executable file extension if one exists. If the operating system
     * does not have a specific executable file extension, the provided name is returned as is.
     *
     * @param os the operating system for which the executable file name needs to be resolved.
     * @param name the base name of the executable file, which should not be null.
     * @return the resolved executable file name, which may include an operating system-specific
     *         extension or remain unchanged if no extension is applicable.
     */
    static public String resolveExecutableFileName(OperatingSystem os, String name) {
        Objects.requireNonNull(name, "name was null");

        final String ext = getExecutableFileExtension(os);

        if (ext != null) {
            return name + ext;
        }

        return name;
    }

    /**
     * Determines and returns the appropriate file extension for library files
     * based on the operating system of the environment where the application is running.
     * It first checks the current operating system using {@code checkOperatingSystem()}
     * and then delegates to {@code getLibraryFileExtension(String operatingSystem)}
     * to retrieve the corresponding extension.
     *
     * @return a {@code String} representing the file extension for the library files
     *         on the current operating system. For example, it may return ".dll" for Windows,
     *         ".so" for Linux, or ".dylib" for macOS.
     */
    public String getLibraryFileExtension() {
        this.checkOperatingSystem();
        return getLibraryFileExtension(this.operatingSystem);
    }

    /**
     * Determines the appropriate file extension for dynamic library files based on the specified operating system.
     * The method returns file extensions specific to the operating system, such as ".dll" for Windows,
     * ".so" for Linux, and ".dylib" for macOS. If the operating system is unrecognized, the method returns null.
     *
     * @param os The operating system for which the library file extension is being determined. Must not be null.
     * @return A string representing the file extension for dynamic library files. This could be:
     *         ".dll" for Windows, ".so" for Linux-based systems (Linux, FreeBSD, OpenBSD, Solaris), or ".dylib" for macOS.
     *         Returns null if the operating system is not recognized.
     */
    static public String getLibraryFileExtension(OperatingSystem os) {
        switch (os) {
            case WINDOWS:
                return ".dll";
            case LINUX:
            case FREEBSD:
            case OPENBSD:
            case NETBSD:
            case DRAGONFLYBSD:
            case ANDROID:
            case AIX:
            case SOLARIS:
                return ".so";
            case MACOS:
                return ".dylib";
            default:
                return null;
        }
    }

    /**
     * Resolves the complete file name for a dynamic library based on the target operating system
     * and the provided base library name. This method uses the current instance's operating system
     * to determine the appropriate file extension and appends it to the given library name.
     *
     * @param name The base name of the library without any file extension. Must not be null.
     * @return A string representing the full library file name with the correct file extension appended.
     *         The result may vary depending on the operating system, e.g., "name.dll" for Windows,
     *         "libname.so" for Linux, or "libname.dylib" for macOS. If the operating system is
     *         unrecognized, the base name is returned unchanged.
     * @throws NullPointerException If the name parameter is null.
     */
    public String resolveLibraryFileName(String name) {
        this.checkOperatingSystem();
        return resolveLibraryFileName(this.operatingSystem, name);
    }

    /**
     * Resolves the appropriate dynamic library file name for a given operating system and library base name.
     * The method appends the correct file extension to the library's base name based on the specified operating system.
     *
     * @param os The operating system for which the library file name is being resolved. Must not be null.
     * @param name The base name of the library without any file extension. Must not be null.
     * @return A string representing the complete library file name, including the appropriate file extension
     *         (e.g., ".dll" for Windows, "lib" + ".so" for Linux, and "lib" + ".dylib" for macOS).
     *         If the operating system is unrecognized, the base name is returned unchanged.
     * @throws NullPointerException If the name parameter is null.
     */
    static public String resolveLibraryFileName(OperatingSystem os, String name) {
        Objects.requireNonNull(name, "name was null");

        switch (os) {
            case WINDOWS:
                return name + ".dll";
            case LINUX:
            case ANDROID:
            case FREEBSD:
            case OPENBSD:
            case NETBSD:
            case DRAGONFLYBSD:
            case AIX:
            case SOLARIS:
                return "lib" + name + ".so";
            case MACOS:
                return "lib" + name + ".dylib";
            default:
                return name;
        }
    }

    /**
     * Converts the current hardware architecture, operating system, and ABI (Application Binary Interface)
     * into a Rust target triple string. The Rust target triple format adheres to the structure:
     * {@code <arch><sub>-<vendor>-<os>-<abi/env>}.
     *
     * This method determines the appropriate architecture prefix and vendor/OS/ABI suffix
     * based on the specified hardware architecture, operating system, and ABI of the current instance.
     * If an unsupported hardware architecture or operating system is encountered, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @return The Rust target triple string combining architecture, vendor, operating system, and ABI.
     *         For example: {@code x86_64-unknown-linux-gnu}.
     * @throws IllegalArgumentException If the hardware architecture or operating system is unsupported
     *                                   or if either of them is null.
     */
    public String toRustTarget() {
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
            case ARMHF:
                arch = "armv7";
                break;
            case ARMEL:
                arch = "arm";
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
                } else if (this.hardwareArchitecture == HardwareArchitecture.ARMHF) {
                    vendorOsEnv = "unknown-linux-gnueabihf";
                } else if (this.hardwareArchitecture == HardwareArchitecture.ARMEL) {
                    vendorOsEnv = "unknown-linux-gnueabi";
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
            case NETBSD:
                vendorOsEnv = "unknown-netbsd";
                break;
            case DRAGONFLYBSD:
                vendorOsEnv = "unknown-dragonflybsd";
                break;
            case SOLARIS:
                vendorOsEnv = "sun-solaris";
                break;
            default:
                throw new IllegalArgumentException("Unsupported rust target for operating system " + this.operatingSystem + " (if it should be valid please add to " + this.getClass().getCanonicalName() + ")");
        }

        return arch + "-" + vendorOsEnv;
    }

    public String toAutoConfTarget() {
        this.checkHardwareArchitecture();
        this.checkOperatingSystem();

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
                arch = "riscv64";
                break;
            case ARMHF:
            case ARMEL:
                arch = "arm";
                break;
            default:
                throw new IllegalArgumentException("Unsupported autoconf target for hardware architecture " + this.hardwareArchitecture + " (if it should be valid please add to " + this.getClass().getCanonicalName() + ")");
        }

        switch (this.operatingSystem) {
            case WINDOWS:
                if (abi == ABI.GNU) {
                    vendorOsEnv = "w64-mingw32";
                } else {
                    // default is msvc
//                    vendorOsEnv = "pc-windows-msvc";
                    throw new IllegalArgumentException("Unsupported autoconf target for windows ABI of msvc (did you mean the gnu ABI?)");
                }
                break;
            case LINUX:
                if (abi == ABI.MUSL) {
                    vendorOsEnv = "linux-musl";
                } else if (this.hardwareArchitecture == HardwareArchitecture.ARMHF) {
                    vendorOsEnv = "linux-gnueabihf";
                } else if (this.hardwareArchitecture == HardwareArchitecture.ARMEL) {
                    vendorOsEnv = "linux-gnueabi";
                } else {
                    // default is glibc/gnu
                    vendorOsEnv = "linux-gnu";
                }
                break;
            case MACOS:
                vendorOsEnv = "apple-darwin";
                break;
            /*case FREEBSD:
                vendorOsEnv = "unknown-freebsd";
                break;
            case OPENBSD:
                vendorOsEnv = "unknown-openbsd";
                break;
            case SOLARIS:
                vendorOsEnv = "sun-solaris";
                break;*/
            default:
                throw new IllegalArgumentException("Unsupported autoconf target for operating system " + this.operatingSystem + " (if it should be valid please add to " + this.getClass().getCanonicalName() + ")");
        }

        return arch + "-" + vendorOsEnv;
    }

    public String toJneOsAbi() {
        this.checkOperatingSystem();
        return toJneOsAbi(this.operatingSystem, this.abi, null);
    }

    public String toJneArch() {
        this.checkHardwareArchitecture();
        return this.hardwareArchitecture.name().toLowerCase();
    }

    public String toJneTarget() {
        return this.toJneOsAbi() + "-" + this.toJneArch();
    }

    static private String toJneOsAbi(OperatingSystem os, ABI abi, String osAlias) {
        validateAbi(os, abi);

        if (os == OperatingSystem.LINUX) {
            // special case for linux, with an ABI
            if (abi == ABI.MUSL) {
                if (osAlias != null) {
                    return osAlias.toLowerCase() + "_" + abi.name().toLowerCase();
                } else {
                    return os.name().toLowerCase() + "_" + abi.name().toLowerCase();
                }
            }
        } else if (os == OperatingSystem.WINDOWS) {
            // special case for windows, with an ABI
            if (abi == ABI.GNU) {
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

    static private void validateAbi(OperatingSystem os, ABI abi) {
        boolean invalidAbi = false;
        if (abi != null && abi != ABI.DEFAULT) {
            if (os == OperatingSystem.LINUX) {
                // linux can use musl or gnu
                switch (abi) {
                    case GNU:
                    case MUSL:
                        break;
                    default:
                        invalidAbi = true;
                        break;
                }
            } else if (os == OperatingSystem.WINDOWS) {
                // windows can use msvc or gnu
                switch (abi) {
                    case GNU:
                    case MSVC:
                        break;
                    default:
                        invalidAbi = true;
                        break;
                }
            } else {
                invalidAbi = true;
            }
        }
        if (invalidAbi) {
            throw new IllegalArgumentException("ABI " + abi + " is not valid for operating system " + os);
        }
    }

    /**
     * Converts a hardware architecture to its JNE (Java Native Execution) compatible
     * string representation. If a hardware architecture alias is provided, it returns
     * the alias in lowercase. Otherwise, it returns the name of the
     * hardware architecture in lowercase.
     *
     * @param hardwareArchitecture The hardware architecture to be converted.
     *                             Must not be null.
     * @param hardwareArchitectureAlias An alias for the hardware architecture,
     *                                  which, if provided, takes precedence over
     *                                  the hardwareArchitecture parameter.
     * @return A string representing the hardware architecture or its alias in lowercase.
     */
    static public String toJneArch(HardwareArchitecture hardwareArchitecture, String hardwareArchitectureAlias) {
        if (hardwareArchitectureAlias != null) {
            return hardwareArchitectureAlias.toLowerCase();
        } else {
            return hardwareArchitecture.name().toLowerCase();
        }
    }

    /**
     * Resolves and generates a list of resource paths based on the provided resource prefix
     * and resource name. The generated paths include combinations of operating system aliases,
     * architectural aliases, and the specified name, formatted hierarchically.
     *
     * @param resourcePrefix The prefix for the resource path. This typically represents the base
     *                       directory or identifier for the resources.
     * @param name The name of the resource to be resolved in the paths.
     *             This value is always appended to the generated paths.
     * @return A list of strings where each string represents a resolved resource path. The paths
     *         incorporate combinations of operating system and hardware architecture details.
     */
    public List<String> resolveResourcePaths(String resourcePrefix, String name) {
        final List<String> jneOsAbis = new ArrayList<>();

        if (this.operatingSystem != null) {
            jneOsAbis.add(toJneOsAbi(this.operatingSystem, this.abi, null));
            if (this.operatingSystem.getAliases() != null) {
                for (String alias : this.operatingSystem.getAliases()) {
                    jneOsAbis.add(toJneOsAbi(this.operatingSystem, this.abi, alias));
                }
            }
        } else {
            jneOsAbis.add(null);
        }

        final List<String> jneArchs = new ArrayList<>();

        if (this.hardwareArchitecture != null) {
            jneArchs.add(toJneArch(this.hardwareArchitecture, null));
            if (this.hardwareArchitecture.getAliases() != null) {
                for (String alias : this.hardwareArchitecture.getAliases()) {
                    jneArchs.add(toJneArch(this.hardwareArchitecture, alias));
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

    /**
     * Creates and returns a {@link NativeTarget} instance based on the specified operating system,
     * hardware architecture, and application binary interface (ABI).
     *
     * @param os The operating system of the target. Must not be null.
     * @param arch The hardware architecture of the target. Must not be null.
     * @param abi The application binary interface (ABI) for the target. Must not be null.
     * @return A new {@link NativeTarget} object with the specified operating system, hardware architecture,
     *         and ABI.
     * @throws IllegalArgumentException If any of the parameters are null.
     */
    static public NativeTarget of(OperatingSystem os, HardwareArchitecture arch, ABI abi) {
        return new NativeTarget(os, arch, abi);
    }

    /**
     * Detects and returns a {@link NativeTarget} instance representing the current system's
     * operating system, hardware architecture, and application binary interface (ABI).
     * This method gathers system information by utilizing the {@link PlatformInfo} utility
     * to identify the underlying components of the environment.
     *
     * @return A {@link NativeTarget} object containing the detected operating system,
     *         hardware architecture, and ABI based on the current platform.
     */
    static public NativeTarget detect() {
        final OperatingSystem os = PlatformInfo.detectOperatingSystem();
        final HardwareArchitecture arch = PlatformInfo.detectHardwareArchitecture();
        final ABI abi = PlatformInfo.detectAbi(os);
        return new NativeTarget(os, arch, abi);
    }

    /**
     * Detects a {@link NativeTarget} by parsing the provided text. This method attempts to identify
     * the operating system, hardware architecture, and ABI (Application Binary Interface) from the
     * given input text by matching it against known names, aliases, and additional identifiers.
     *
     * @param text The input text to analyze for operating system, architecture, and ABI information.
     *             Must not be null or empty.
     * @return A {@link NativeTarget} object containing the detected operating system, hardware
     *         architecture, and ABI. If the input text does not match any known values for these
     *         attributes, the corresponding components in the {@link NativeTarget} may be null.
     * @throws IllegalArgumentException If the input text is null or empty.
     */
    static public NativeTarget detectFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("JNE target was null or empty");
        }

        // normalize text to lowercase to make it easier to check
        String lowerText = text.toLowerCase();

        OperatingSystem detectedOs = null;
        for (OperatingSystem os : OperatingSystem.values()) {
            if (lowerText.contains(os.name().toLowerCase())) {
                detectedOs = os;
                break;
            }
            if (os.getAliases() != null) {
                for (String alias : os.getAliases()) {
                    if (lowerText.contains(alias.toLowerCase())) {
                        detectedOs = os;
                        break;
                    }
                }
            }
        }

        // if no os detected yet, try the extra aliases now
        if (detectedOs == null) {
            for (OperatingSystem os : OperatingSystem.values()) {
                if (os.getExtraAliases() != null) {
                    for (String extraAlias : os.getExtraAliases()) {
                        if (lowerText.contains(extraAlias.toLowerCase())) {
                            detectedOs = os;
                            break;
                        }
                    }
                }
            }
        }

        HardwareArchitecture detectedArch = null;
        for (HardwareArchitecture arch : HardwareArchitecture.values()) {
            if (lowerText.contains(arch.name().toLowerCase())) {
                detectedArch = arch;
                break;
            }
            if (arch.getAliases() != null) {
                for (String alias : arch.getAliases()) {
                    if (lowerText.contains(alias.toLowerCase())) {
                        detectedArch = arch;
                        break;
                    }
                }
            }
        }

        if (detectedArch == null) {
            for (HardwareArchitecture arch : HardwareArchitecture.values()) {
                if (arch.getExtraAliases() != null) {
                    for (String extraAlias : arch.getExtraAliases()) {
                        if (lowerText.contains(extraAlias.toLowerCase())) {
                            detectedArch = arch;
                            break;
                        }
                    }
                }
            }
        }

        ABI detectedABI = null;
        for (ABI abi : ABI.values()) {
            if (lowerText.contains(abi.name().toLowerCase())) {
                detectedABI = abi;
                break;
            }
        }

        return NativeTarget.of(detectedOs, detectedArch, detectedABI);
    }

    static public NativeTarget fromJneTarget(String jneTarget) {
        if (jneTarget == null || jneTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("JNE target was null or empty");
        }

        final int hyphenPos = jneTarget.indexOf("-");
        if (hyphenPos < 2 || hyphenPos >= jneTarget.length()-2) {
            throw new IllegalArgumentException("JNE target [" + jneTarget + "] was not of format <os+abi>/arch such as linux-x64 or windows-x64");
        }

        final String osAbiStr = jneTarget.substring(0, hyphenPos);
        final String archStr = jneTarget.substring(hyphenPos+1);
        final String osStr;
        final String abiStr;

        // of osAbi contains an underscore, this means it has an abi
        final int underscorePos = osAbiStr.indexOf("_");
        if (underscorePos > 1) {
            // we have an abi
            osStr = osAbiStr.substring(0, underscorePos);
            abiStr = osAbiStr.substring(underscorePos+1);
        } else {
            // we do not have an abi
            osStr = osAbiStr;
            abiStr = null;
        }

        // resolve all of 'em now
        final OperatingSystem os = OperatingSystem.resolve(osStr);
        final HardwareArchitecture arch = HardwareArchitecture.resolve(archStr);
        final ABI abi = ABI.resolve(abiStr);

        // validate we resolved all of them
        if (os == null) {
            throw new IllegalArgumentException("JNE target [" + jneTarget + "] with an unsupported operating system [" + osStr + "]");
        }
        if (arch == null) {
            throw new IllegalArgumentException("JNE target [" + jneTarget + "] with an unsupported hardware architecture [" + archStr + "]");
        }
        if (abiStr != null && abi == null) {
            throw new IllegalArgumentException("JNE target [" + jneTarget + "] with an unsupported abi [" + abiStr + "]");
        }

        // validate abi is valid
        validateAbi(os, abi);

        return new NativeTarget(os, arch, abi);
    }

}
