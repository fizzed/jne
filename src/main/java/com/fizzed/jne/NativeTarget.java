package com.fizzed.jne;

public class NativeTarget {

    private final OperatingSystem operatingSystem;
    private final HardwareArchitecture hardwareArchitecture;
    private final LinuxLibC linuxLibC;

    private NativeTarget(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, LinuxLibC linuxLibC) {
        this.operatingSystem = operatingSystem;
        this.hardwareArchitecture = hardwareArchitecture;
        this.linuxLibC = linuxLibC;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public LinuxLibC getLinuxLibC() {
        return linuxLibC;
    }

    public String getLibraryFileExtension() {
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
        // rust targets are a triple: <arch><sub>-<vendor>-<sys>-<abi>
        String arch = null;
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
        }

        String vendorSysAbi = null;
        switch (this.operatingSystem) {
            case WINDOWS:
                vendorSysAbi = "pc-windows-msvc";
                break;
            case LINUX:
                if (linuxLibC == LinuxLibC.MUSL) {
                    vendorSysAbi = "unknown-linux-musl";
                } else {
                    vendorSysAbi = "unknown-linux-gnu";
                }
                break;
            case MACOS:
                vendorSysAbi = "apple-darwin";
                break;
            case FREEBSD:
                vendorSysAbi = "unknown-freebsd";
                break;
            case OPENBSD:
                vendorSysAbi = "unknown-openbsd";
                break;
            case SOLARIS:
                
        }
    }

    static public NativeTarget of(OperatingSystem operatingSystem, HardwareArchitecture hardwareArchitecture, LinuxLibC linuxLibC) {
        return new NativeTarget(operatingSystem, hardwareArchitecture, linuxLibC);
    }

}