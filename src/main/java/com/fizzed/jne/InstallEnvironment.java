package com.fizzed.jne;

import com.fizzed.jne.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.fizzed.jne.internal.Utils.trimToNull;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

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
    // e.g. /opt, C:\Opt
    private Path optRootDir;
    // e.g. /usr/local, C:\Opt
    private Path localRootDir;

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

    public Path getOptRootDir() {
        return optRootDir;
    }

    public InstallEnvironment setOptRootDir(Path optRootDir) {
        this.optRootDir = optRootDir;
        return this;
    }

    public Path getLocalRootDir() {
        return localRootDir;
    }

    public InstallEnvironment setLocalRootDir(Path localRootDir) {
        this.localRootDir = localRootDir;
        return this;
    }

    // these are dynamic based on the roots

    public Path getApplicationDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return applicationRootDir.resolve(this.applicationName);
        } else if (operatingSystem == OperatingSystem.MACOS) {
            return applicationRootDir.resolve(this.applicationName + ".app");
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

    public Path getSystemShareDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return this.systemRootDir;
        } else {
            return this.systemRootDir.resolve("share");
        }
    }

    public Path getLocalApplicationDir() {
        return this.localRootDir.resolve(this.unitName);
    }

    public Path getOptApplicationDir() {
        return this.optRootDir.resolve(this.unitName);
    }

    public Path getLocalBinDir() {
        return this.localRootDir.resolve("bin");
    }

    public Path getLocalShareDir() {
        return this.localRootDir.resolve("share");
    }

    private void installEnv(UserEnvironment userEnvironment, EnvScope scope, List<EnvVar> vars, List<EnvPath> paths) throws Exception {
        // some possible locations we will use
//        final ShellType shellType = userEnvironment.getShellType();
//        final Path homeDir = userEnvironment.getHomeDir();

        if (this.operatingSystem == OperatingSystem.WINDOWS) {
            // shell is irrelevant on windows, env vars and PATH are setup in the registry
            // e.g. setx MY_VARIABLE "MyValue" OR setx MY_VARIABLE "MyValue" /M
            for (EnvVar var : vars) {
                if (scope == EnvScope.SYSTEM) {
                    Utils.execAndGetOutput(asList("setx", var.getName(), var.getValue(), "/M"));
                } else {
                    Utils.execAndGetOutput(asList("setx", var.getName(), var.getValue()));
                }
                log.info("Installed {} environment variable {} (with {} scope)",  this.unitName, var, scope);
            }
        }



        /*final Path bashEtcProfileDir = Paths.get("/etc/profile.d");
        final Path bashEtcLocalProfileDir = Paths.get("/usr/local/etc/profile.d");

        // linux and freebsd share the same strategy, just different locations
        if (shellType == ShellType.BASH && (
            (nativeTarget.getOperatingSystem() == OperatingSystem.LINUX && Files.exists(bashEtcProfileDir))
                || nativeTarget.getOperatingSystem() == OperatingSystem.FREEBSD)) {

            Path targetDir = bashEtcProfileDir;

            if (nativeTarget.getOperatingSystem() == OperatingSystem.FREEBSD) {
                targetDir = bashEtcLocalProfileDir;
                // on freebsd, we need to make sure the local profile dir exists
                if (!Files.exists(targetDir)) {
                    mkdir(targetDir)
                        .parents()
                        .verbose()
                        .run();
                    // everyone needs to be able to read & execute
                    this.chmodBinFile(targetDir);
                }
            }

            final Path targetFile = targetDir.resolve(env.getApplication() + ".sh");

            // build the shell file
            final StringBuilder sb = new StringBuilder();
            for (EnvVar var : env.getVars()) {
                sb.append("export ").append(var.getName()).append("=\"").append(var.getValue()).append("\"\n");
            }
            for (EnvPath path : env.getPaths()) {
                sb.append("export PATH=\"").append(path.getValue()).append(":$PATH\"\n");
            }

            // overwrite the existing file (if its present)
            Files.write(targetFile, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Installed {} environment for {} to {}", shellType, env.getApplication(), targetFile);
            log.info("");
            log.info("Usually a REBOOT is required for this system-wide profile to be activated...");

        } else if (shellType == ShellType.ZSH && nativeTarget.getOperatingSystem() == OperatingSystem.MACOS) {

            final Path pathsDir = Paths.get("/etc/paths.d");
            final Path pathFile = pathsDir.resolve(env.getApplication());

            // build the path file
            final StringBuilder sb = new StringBuilder();
            for (EnvPath path : env.getPaths()) {
                sb.append(path.getValue()).append("\n");
            }

            // overwrite the existing file (if its present)
            Files.write(pathFile, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Installed {} path for {} to {}", shellType, env.getApplication(), pathFile);

            // environment vars are more tricky, they need to be appended to ~/.zprofile
            final Path profileFile = homeDir.resolve(".zprofile");
            final List<String> profileFileLines = readFileLines(profileFile);

            for (EnvVar var : env.getVars()) {
                // this is the line we want to have present
                String line = "export " + var.getName() + "=\"" + var.getValue() + "\"";
                appendLineIfNotExists(profileFileLines, profileFile, line);
            }

            log.info("Usually a REBOOT is required for this system-wide profile to be activated...");

        } else if (shellType == ShellType.CSH) {
            final Path profileFile = homeDir.resolve(".cshrc");
            final List<String> profileFileLines = readFileLines(profileFile);

            // append env vars first
            for (EnvVar var : env.getVars()) {
                // this is the line we want to have present
                String line = "setenv " + var.getName() + " \"" + var.getValue() + "\"";
                appendLineIfNotExists(profileFileLines, profileFile, line);
            }

            for (EnvPath path : env.getPaths()) {
                // this is the line we want to have present
                String line = "setenv PATH \"" + path.getValue() + ":${PATH}\"";
                appendLineIfNotExists(profileFileLines, profileFile, line);
            }

            log.info("Usually LOGOUT/LOGIN is required for this profile to be activated...");

        } else if (shellType == ShellType.KSH) {
            final Path profileFile = homeDir.resolve(".profile");
            final List<String> profileFileLines = readFileLines(profileFile);

            // append env vars first
            for (EnvVar var : env.getVars()) {
                // this is the line we want to have present
                String line = "export " + var.getName() + "=\"" + var.getValue() + "\"";
                appendLineIfNotExists(profileFileLines, profileFile, line);
            }

            for (EnvPath path : env.getPaths()) {
                // this is the line we want to have present
                String line = "export PATH=\"" + path.getValue() + ":$PATH\"";
                appendLineIfNotExists(profileFileLines, profileFile, line);
            }

            log.info("Usually LOGOUT/LOGIN is required for this profile to be activated...");
        }*/
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
            ie.localRootDir = Paths.get("/usr/local");
            ie.systemRootDir = Paths.get("/usr");
            // this is sort of debatable, some apps puts stuff in /opt, but many others will put it in /usr/local
            // opt: Used for installing optional, add-on application software packages, especially those not managed
            // by the system's package manager. Each package often resides in its own subdirectory, like /opt/someapp
            ie.applicationRootDir = Paths.get("/opt");
            ie.optRootDir = ie.applicationRootDir;
        } else if (os == OperatingSystem.FREEBSD || os == OperatingSystem.OPENBSD || os == OperatingSystem.NETBSD || os == OperatingSystem.DRAGONFLYBSD) {
            ie.localRootDir = Paths.get("/usr/local");
            ie.systemRootDir = Paths.get("/usr");
            // unlike linux, freebsd puts stuff in /usr/local, which also is like /opt
            ie.applicationRootDir = ie.localRootDir;
            ie.optRootDir = ie.applicationRootDir;
        } else if (os == OperatingSystem.MACOS) {
            ie.localRootDir = Paths.get("/usr/local");
            ie.systemRootDir = Paths.get("/usr");
            ie.applicationRootDir = Paths.get("/Applications");
            ie.optRootDir = Paths.get("/opt");
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
            ie.localRootDir = ie.applicationRootDir.resolve("..").resolve("Opt").normalize();
            ie.optRootDir = ie.localRootDir;
        }

        return ie;
    }

    static public enum EnvScope {
        SYSTEM,
        USER;
    }

    static public class EnvVar {

        final private String name;
        final private String value;

        public EnvVar(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public EnvVar(String name, Path value) {
            this(name, value.toAbsolutePath().toString());
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.name + "=" + this.value;
        }
    }

    static public class EnvPath {

        final private boolean prepend;
        final private Path value;

        public EnvPath(boolean prepend, Path value) {
            this.prepend = prepend;
            this.value = value;
        }

        public boolean getPrepend() {
            return prepend;
        }

        public Path getValue() {
            return value;
        }

    }

}