package com.fizzed.jne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fizzed.jne.internal.Utils.trimToNull;

public class InstallEnvironment {
    static private final Logger log = LoggerFactory.getLogger(InstallEnvironment.class);

    // e.g. jdk, maven
    private String unitName;
    // e.g. OpenJDK 21, Apache Maven, etc.
    private String applicationName;
    private OperatingSystem operatingSystem;
    // e.g. /usr/local, C:\Program Files, etc.
    private Path applicationRootDir;
    // e.g. /usr, C:\Windows\system32
    private Path systemRootDir;
    // e.g. /usr/local, C:\Opt
    private Path localSystemRootDir;

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public InstallEnvironment setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public String getUnitName() {
        return unitName;
    }

    public InstallEnvironment setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public InstallEnvironment setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public Path getApplicationRootDir() {
        return applicationRootDir;
    }

    public InstallEnvironment setApplicationRootDir(Path applicationRootDir) {
        this.applicationRootDir = applicationRootDir;
        return this;
    }

    public Path getSystemRootDir() {
        return systemRootDir;
    }

    public InstallEnvironment setSystemRootDir(Path systemRootDir) {
        this.systemRootDir = systemRootDir;
        return this;
    }

    public Path getLocalSystemRootDir() {
        return localSystemRootDir;
    }

    public InstallEnvironment setLocalSystemRootDir(Path localSystemRootDir) {
        this.localSystemRootDir = localSystemRootDir;
        return this;
    }

    // these are dynamic based on the roots

    public Path getApplicationDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return applicationRootDir.resolve(this.applicationName);
        } else {
            return applicationRootDir.resolve(this.unitName);
        }
    }

    public Path getSystemBinDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return this.systemRootDir;
        } else {
            return this.systemRootDir.resolve("bin");
        }
    }

    public Path getLocalApplicationDir() {
        return this.localSystemRootDir.resolve(this.unitName);
    }

    public Path getLocalSystemBinDir() {
        return this.localSystemRootDir.resolve("bin");
    }

    static public InstallEnvironment detect(String applicationName, String unitName) {
        if (applicationName == null || unitName == null) {
            throw new IllegalArgumentException("applicationName and unitName must not be null");
        }
        // unitName must all be lowercase, can include a hypen or underscore, but no whitespace
        if (!unitName.matches("^[a-z0-9\\-_]+$")) {
            throw new IllegalArgumentException("unitName must be all lowercase, can include a hypen or underscore, but no whitespace");
        }

        final OperatingSystem os = PlatformInfo.detectOperatingSystem();

        final InstallEnvironment ie = new InstallEnvironment();
        ie.operatingSystem = os;
        ie.applicationName = applicationName;
        ie.unitName = unitName;

        if (os == OperatingSystem.LINUX) {
            ie.localSystemRootDir = Paths.get("/usr/local");
            ie.systemRootDir = Paths.get("/usr");
            // this is sort of debatable, some apps puts stuff in /opt, but many others will put it in /usr/local
            // opt: Used for installing optional, add-on application software packages, especially those not managed
            // by the system's package manager. Each package often resides in its own subdirectory, like /opt/someapp
            ie.applicationRootDir = Paths.get("/opt");
        } else if (os == OperatingSystem.FREEBSD || os == OperatingSystem.OPENBSD || os == OperatingSystem.NETBSD || os == OperatingSystem.DRAGONFLYBSD) {
            ie.localSystemRootDir = Paths.get("/usr/local");
            ie.systemRootDir = Paths.get("/usr");
            // unlike linux, freebsd puts stuff in /usr/local
            ie.applicationRootDir = ie.localSystemRootDir;
        } else if (os == OperatingSystem.WINDOWS) {
            String programFiles = trimToNull(System.getenv("ProgramFiles"));
            String systemRoot = trimToNull(System.getenv("SystemRoot"));
            String systemDrive = trimToNull(System.getenv("SystemDrive"));

            if (programFiles == null) {
                // let's just assume the standard place
                programFiles = "C:\\Program Files";
                log.warn("Windows ProgramFiles environment variable not set, assuming {}", programFiles);
            }

            if (systemRoot == null) {
                if (systemDrive != null) {
                    systemRoot = systemDrive + "\\Windows";
                }
                if (systemRoot == null) {
                    // assume the standard place once again
                    systemRoot = "C:\\Windows";
                    log.warn("Windows SystemRoot environment variable not set, assuming {}", systemRoot);
                }
            }

            ie.applicationRootDir = Paths.get(programFiles);
            ie.systemRootDir = Paths.get(systemRoot).resolve("system32");
            // we will take the drive of ProgramFiles, and create an Opt there to install things to
            ie.localSystemRootDir = ie.applicationRootDir.resolve("..").resolve("Opt").normalize();
        }

        return ie;
    }

}