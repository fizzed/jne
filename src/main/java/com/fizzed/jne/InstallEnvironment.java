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
    // e.g. /usr/local, C:\Program Files, etc.
    private Path applicationDir;
    // e.g. /usr, C:\Windows\system32
    private Path systemDir;
    // e.g. /usr/local, C:\Opt
    private Path localSystemDir;

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

    public Path getApplicationDir() {
        return applicationDir;
    }

    public InstallEnvironment setApplicationDir(Path applicationDir) {
        this.applicationDir = applicationDir;
        return this;
    }

    public Path getSystemDir() {
        return systemDir;
    }

    public InstallEnvironment setSystemDir(Path systemDir) {
        this.systemDir = systemDir;
        return this;
    }

    public Path getLocalSystemDir() {
        return localSystemDir;
    }

    public InstallEnvironment setLocalSystemDir(Path localSystemDir) {
        this.localSystemDir = localSystemDir;
        return this;
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
        ie.applicationName = applicationName;
        ie.unitName = unitName;

        if (os == OperatingSystem.WINDOWS) {
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

            ie.applicationDir = Paths.get(programFiles);
            ie.systemDir = Paths.get(systemRoot).resolve("system32");

            // we will take the drive of ProgramFiles, and create an Opt there to install things to
            ie.localSystemDir = ie.applicationDir.resolve("..").resolve("Opt").normalize();
        }

        return ie;
    }

}