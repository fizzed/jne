package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
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

import com.fizzed.jne.internal.Utils;
import com.fizzed.jne.internal.WindowsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fizzed.jne.internal.Utils.joinIfDelimiterMissing;
import static com.fizzed.jne.internal.Utils.trimToNull;
import static java.util.Arrays.asList;

public class InstallEnvironment {
    static private final Logger log = LoggerFactory.getLogger(InstallEnvironment.class);

    private final UserEnvironment userEnvironment;
    private final EnvScope scope;
    private final OperatingSystem operatingSystem;
    // e.g. jdk, maven
    private String unitName;
    // e.g. OpenJDK 21, Apache Maven, etc.
    private String applicationName;
    // e.g. /usr/local, C:\Program Files, etc.
    private Path applicationRootDir;
    // e.g. /usr, C:\Windows\system32
    private Path systemRootDir;
    // e.g. /opt, C:\Opt
    private Path optRootDir;
    // e.g. /usr/local, C:\Opt
    private Path localRootDir;

    private InstallEnvironment(UserEnvironment userEnvironment, EnvScope scope, OperatingSystem operatingSystem) {
        this.userEnvironment = userEnvironment;
        this.scope = scope;
        this.operatingSystem = operatingSystem;
    }

    public UserEnvironment getUserEnvironment() {
        return userEnvironment;
    }

    public EnvScope getScope() {
        return scope;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public String getUnitName() {
        return unitName;
    }

    /*public InstallEnvironment setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }*/

    public String getApplicationName() {
        return applicationName;
    }

    /*public InstallEnvironment setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }*/

    public Path getApplicationRootDir() {
        return applicationRootDir;
    }

    /*public InstallEnvironment setApplicationRootDir(Path applicationRootDir) {
        this.applicationRootDir = applicationRootDir;
        return this;
    }*/

    public Path getSystemRootDir() {
        return systemRootDir;
    }

    /*public InstallEnvironment setSystemRootDir(Path systemRootDir) {
        this.systemRootDir = systemRootDir;
        return this;
    }*/

    public Path getOptRootDir() {
        return optRootDir;
    }

    /*public InstallEnvironment setOptRootDir(Path optRootDir) {
        this.optRootDir = optRootDir;
        return this;
    }*/

    public Path getLocalRootDir() {
        return localRootDir;
    }

    /*public InstallEnvironment setLocalRootDir(Path localRootDir) {
        this.localRootDir = localRootDir;
        return this;
    }*/

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

    public Path resolveLocalApplicationDir(boolean createIfMissing) throws IOException {
        return this.resolveDir(this.getLocalApplicationDir(), createIfMissing);
    }

    public Path getOptApplicationDir() {
        return this.optRootDir.resolve(this.unitName);
    }

    public Path resolveOptApplicationDir(boolean createIfMissing) throws IOException {
        return this.resolveDir(this.getOptApplicationDir(), createIfMissing);
    }

    public Path getLocalBinDir() {
        return this.localRootDir.resolve("bin");
    }

    public Path resolveLocalBinDir(boolean createIfMissing) throws IOException {
        return this.resolveDir(this.getLocalBinDir(), createIfMissing);
    }

    public Path getLocalShareDir() {
        return this.localRootDir.resolve("share");
    }

    public Path resolveLocalShareDir(boolean createIfMissing) throws IOException {
        return this.resolveDir(this.getLocalShareDir(), createIfMissing);
    }

    private Path resolveDir(Path dir, boolean createIfMissing) throws IOException {
        if (Files.exists(dir)) {
            if (!Files.isWritable(dir)) {
                throw new IOException("Directory " + dir + " exists but is not writable (perhaps you need to run as sudo or fix permissions?)");
            }
        } else {
            if (createIfMissing) {
                Files.createDirectories(dir);
                if (this.scope == EnvScope.USER) {
                    // if USER scope, make sure its xrw only for our user
                    dir.toFile().setExecutable(true, true);
                    dir.toFile().setWritable(true, true);
                    dir.toFile().setReadable(true, true);
                } else {
                    dir.toFile().setExecutable(true, true);
                    dir.toFile().setWritable(true, false);
                    dir.toFile().setReadable(true, true);
                }
            } else {
                throw new IOException("Directory " + dir + " does not exist (perhaps you need to create it first then re-run your command?)");
            }
        }
        return dir;
    }

    public void installEnv(List<EnvPath> paths) throws IOException, InterruptedException {
        this.installEnv(paths, Collections.emptyList());
    }

    public void installEnv(List<EnvPath> paths, List<EnvVar> vars) throws IOException, InterruptedException {

        if (this.operatingSystem == OperatingSystem.WINDOWS) {
            // we are going to query the user OR system env vars via registry
            final Map<String,String> currentEnvInRegistry;
            if (this.scope == EnvScope.USER) {
                currentEnvInRegistry = WindowsRegistry.queryUserEnvironmentVariables();
            } else {
                currentEnvInRegistry = WindowsRegistry.querySystemEnvironmentVariables();
            }

            // shell is irrelevant on windows, env vars and PATH are setup in the registry
            // e.g. setx MY_VARIABLE "MyValue" OR setx MY_VARIABLE "MyValue" /M
            for (EnvVar var : vars) {
                final boolean exists = Utils.searchEnvVar(currentEnvInRegistry, var.getName(), var.getValue());
                if (!exists) {
                    final List<String> envVarCmd = new ArrayList<>(asList("setx", var.getName(), var.getValue()));
                    if (this.scope == EnvScope.SYSTEM) {
                        // we just tack on a /M to make it system-wide
                        envVarCmd.add("/M");
                    }
                    Utils.execAndGetOutput(envVarCmd);
                    log.info("Installed environment variable {} (with {} scope)", var, scope);
                } else {
                    log.info("Skipped installing environment variable {} (it already exists)", var);
                }
            }

            // PATH is set the same way, we will need to read the current value, append/prepend if the path does not yet exist
            // if we modify the PATH multiple times we need to keep track of the entire change
            String pathValueInRegistry = currentEnvInRegistry.get("PATH");
            for (EnvPath path : paths) {
                final boolean exists = Utils.searchEnvPath(pathValueInRegistry, path.getValue());
                if (!exists) {
                    final List<String> envVarCmd =  new ArrayList<>(asList("setx", "PATH"));
                    if (path.getPrepend()) {
                        pathValueInRegistry = joinIfDelimiterMissing(path.getValue().toString(), pathValueInRegistry, ";");
                    } else {
                        pathValueInRegistry = joinIfDelimiterMissing(pathValueInRegistry, path.getValue().toString(), ";");
                    }
                    // we will set the ENTIRE value again
                    envVarCmd.add(pathValueInRegistry);
                    if (this.scope == EnvScope.SYSTEM) {
                        // we just tack on a /M to make it system-wide
                        envVarCmd.add("/M");
                    }
                    Utils.execAndGetOutput(envVarCmd);
                    log.info("Installed environment path {} (with {} scope)", path, scope);
                } else {
                    log.info("Skipped installing environment path {} (it already exists)", path);
                }
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
        return detect(applicationName, unitName, EnvScope.SYSTEM);
    }

    static public InstallEnvironment detect(String applicationName, String unitName, EnvScope scope) {
        return detect(applicationName, unitName, scope, UserEnvironment.detectLogical());
    }

    static public InstallEnvironment detect(String applicationName, String unitName, EnvScope scope, UserEnvironment userEnvironment) {
        if (applicationName == null || unitName == null) {
            throw new IllegalArgumentException("applicationName and unitName must not be null");
        }

        // unitName must all be lowercase, can include a hypen or underscore, but no whitespace
        if (!unitName.matches("^[a-z0-9\\-_]+$")) {
            throw new IllegalArgumentException("unitName must be all lowercase, can include a hypen or underscore, but no whitespace");
        }

        final OperatingSystem os = PlatformInfo.detectOperatingSystem();

        final InstallEnvironment ie = new InstallEnvironment(userEnvironment, scope, os);
        ie.applicationName = applicationName;
        ie.unitName = unitName;

        if (scope == EnvScope.USER) {
            // regardless of operating system, user-specific installs all go to same place
            final UserEnvironment ue = UserEnvironment.detectLogical();
            ie.localRootDir = ue.getHomeDir().resolve(".local");
            ie.systemRootDir = ie.localRootDir;
            ie.applicationRootDir = ue.getHomeDir().resolve("Applications");
            ie.optRootDir = ie.localRootDir;
        } else if (os == OperatingSystem.LINUX) {
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

}
