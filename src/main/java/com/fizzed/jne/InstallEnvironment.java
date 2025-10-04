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

import com.fizzed.jne.internal.ShellBuilder;
import com.fizzed.jne.internal.Utils;
import com.fizzed.jne.internal.WindowsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.fizzed.jne.internal.Utils.*;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

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
                    Chmod.chmod(dir, "700");
                } else {
                    Chmod.chmod(dir, "755");
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
        // to simplify method, null values swapped with empty lists
        paths = ofNullable(paths).orElseGet(Collections::emptyList);
        vars = ofNullable(vars).orElseGet(Collections::emptyList);
        final ShellType shellType = this.userEnvironment.getShellType();

        // filter paths requested down to the actual list (removing well-known paths)
        final List<EnvPath> filteredPaths = this.filterWellKnownEnvPaths(paths);

        // make sure we are installing stuff
        if (filteredPaths.isEmpty() && vars.isEmpty()) {
            return; // nothing to actually do, entirely skip install
        }

        // check we're not installing any vars that are prohibited
        final List<EnvVar> blacklistedVars = this.findBlacklistedEnvVars(vars);

        if (blacklistedVars != null && !blacklistedVars.isEmpty()) {
            throw new IOException("The following environment variables are prohibited from installation: " + blacklistedVars);
        }

        // an empty line for logging what we install
        log.info("");

        if (this.operatingSystem == OperatingSystem.WINDOWS) {
            log.info("Installing the {} environment by modifying the windows registry with the following:", this.scope.toString().toLowerCase());
            log.info("");

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
                    log.info(" set {}={}", var.getName(), var.getValue());
                } else {
                    log.info(" set {}={} (skipped as this already was present)", var.getName(), var.getValue());
                }
            }

            // PATH is set the same way, we will need to read the current value, append/prepend if the path does not yet exist
            // if we modify the PATH multiple times we need to keep track of the entire change
            String pathValueInRegistry = currentEnvInRegistry.get("PATH");
            for (EnvPath path : filteredPaths) {
                final boolean exists = Utils.searchEnvPath(pathValueInRegistry, path.getValue());
                if (!exists) {
                    final List<String> envVarCmd =  new ArrayList<>(asList("setx", "PATH"));
                    if (path.isPrepend()) {
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
                    log.info(" added to path {}", path.getValue());
                } else {
                    log.info(" added to path {} (skipped as this was already present)", path.getValue());
                }
            }

            log.info("");

            // for windows, we are now done
            return;
        }


        // on macos, system-scoped env installs will have PATH setup in a special way, but env vars will be setup
        // first, we will generate the lines we will either put into a .sh file or tack onto something like ~/.bashrc
        if (this.operatingSystem == OperatingSystem.MACOS && this.scope == EnvScope.SYSTEM) {

            final List<String> shellLines = new ArrayList<>();
            for (EnvPath path : filteredPaths) {
                // thre is no prepend/append support for macos
                shellLines.add(path.getValue().toString());
            }

            final Path targetFile = Paths.get("/etc/paths.d").resolve(this.unitName);

            writeLinesToFile(targetFile, shellLines, false);
            this.logEnvWritten(targetFile, shellLines, false);

            // now we will proceed and allow ZSH, etc. to be setup, but no need to do any env paths, so we'll clear those out
            filteredPaths.clear();
        }


        // for bash, we can setup system and user with the same shell syntax, they just go into different files, where
        // the global one will be truncated and the per-user one needs some smart appending
        if (shellType == ShellType.BASH) {

            // for base, usually its /usr/local/etc/profile.d, then /etc/profile.d, then /etc/profile, then ~/.bashrc
            // we'll first default the location to ~/.bashrc, but if we find the other two, we'll use them instead'
            boolean append = true;
            Path targetFile = this.userEnvironment.getHomeDir().resolve(".bashrc");
            if (this.scope == EnvScope.SYSTEM) {
                final Path usrLocalEtcProfileDotDir = Paths.get("/usr/local/etc/profile.d");
                final Path etcProfileDotDir = Paths.get("/etc/profile.d");
                final Path etcProfileFile = Paths.get("/etc/profile");

                if (Files.exists(usrLocalEtcProfileDotDir) && Files.isDirectory(usrLocalEtcProfileDotDir)) {
                    targetFile = usrLocalEtcProfileDotDir.resolve(this.unitName + ".sh");
                    append = false;
                } else if (Files.exists(etcProfileDotDir) && Files.isDirectory(etcProfileDotDir)) {
                    targetFile = etcProfileDotDir.resolve(this.unitName + ".sh");
                    append = false;
                } else if (Files.exists(etcProfileFile)) {
                    targetFile = etcProfileFile;
                } else {
                    log.warn("Unable to locate system-wide profile file for BASH, will use ~/.bashrc instead");
                }
            }

            final List<String> shellLines = new ArrayList<>();
            for (EnvVar var : vars) {
                // even though we're BASH, the global profile using BOURNE shell syntax
                shellLines.add(new ShellBuilder(ShellType.SH).exportEnvVar(var));
            }
            for (EnvPath path : filteredPaths) {
                // even though we're BASH, the global profile using BOURNE shell syntax
                shellLines.add(new ShellBuilder(ShellType.SH).addEnvPath(path));
            }

            // if we're appending we need to filter the lines
            List<String> filteredShellLines = shellLines;
            if (append) {
                filteredShellLines = filterLinesIfPresentInFile(targetFile, shellLines);
            }

            writeLinesToFile(targetFile, filteredShellLines, append);
            this.logEnvWritten(targetFile, shellLines, append);

            return;     // we are done with bash setup
        }

        if (shellType == ShellType.ZSH) {

            // since system-wide paths were already installed above, everything nicely now goes into the same file
            final Path targetFile = this.userEnvironment.getHomeDir().resolve(".zprofile");

            final List<String> shellLines = new ArrayList<>();
            for (EnvVar var : vars) {
                shellLines.add(new ShellBuilder(shellType).exportEnvVar(var));
            }
            for (EnvPath path : filteredPaths) {
                shellLines.add(new ShellBuilder(shellType).addEnvPath(path));
            }

            final List<String> filteredShellLines = filterLinesIfPresentInFile(targetFile, shellLines);

            writeLinesToFile(targetFile, filteredShellLines, true);
            this.logEnvWritten(targetFile, shellLines, true);

            return;     // we are done with zsh setup
        }

        if (shellType == ShellType.CSH) {

            Path targetFile = this.userEnvironment.getHomeDir().resolve(".cshrc");
            if (this.scope == EnvScope.SYSTEM) {
                final Path etcCshrcFile = Paths.get("/etc/csh.cshrc");
                if (Files.exists(etcCshrcFile)) {
                    targetFile = etcCshrcFile;
                }
            }

            final List<String> shellLines = new ArrayList<>();
            for (EnvVar var : vars) {
                shellLines.add(new ShellBuilder(shellType).exportEnvVar(var));
            }
            for (EnvPath path : filteredPaths) {
                shellLines.add(new ShellBuilder(shellType).addEnvPath(path));
            }

            final List<String> filteredShellLines = filterLinesIfPresentInFile(targetFile, shellLines);

            writeLinesToFile(targetFile, filteredShellLines, true);
            this.logEnvWritten(targetFile, shellLines, true);

            return;     // we are done with csh setup
        }

        if (shellType == ShellType.KSH) {

            // for ksh, usually its /etc/profile.d/file.sh, then /etc/profile, then ~/.profile -- but on most openbsd
            // systems, /etc/profile.d and /etc/profile do not exist :-( -- so we'll check for them here first
            // we'll first default the location to ~/.profile, but if we find the other two, we'll use them instead'
            boolean append = true;
            Path targetFile = this.userEnvironment.getHomeDir().resolve(".profile");
            if (this.scope == EnvScope.SYSTEM) {
                final Path etcProfileDotDir = Paths.get("/etc/profile.d");
                final Path etcProfileFile = Paths.get("/etc/profile");
                if (Files.exists(etcProfileDotDir) && Files.isDirectory(etcProfileDotDir)) {
                    targetFile = etcProfileDotDir.resolve(this.unitName + ".sh");
                    append = false;
                } else if (Files.exists(etcProfileFile)) {
                    targetFile = etcProfileFile;
                } else {
                    log.warn("Unable to locate system-wide profile file for KSH, will use ~/.profile instead");
                }
            }

            final List<String> shellLines = new ArrayList<>();
            for (EnvVar var : vars) {
                shellLines.add(new ShellBuilder(shellType).exportEnvVar(var));
            }
            for (EnvPath path : filteredPaths) {
                shellLines.add(new ShellBuilder(shellType).addEnvPath(path));
            }

            List<String> filteredShellLines = shellLines;
            if (append) {
                filteredShellLines = filterLinesIfPresentInFile(targetFile, shellLines);
            }

            writeLinesToFile(targetFile, filteredShellLines, append);
            this.logEnvWritten(targetFile, shellLines, append);

            return;     // we are done with ksh setup
        }

        // hmm... we don't actually know how to handle this shell type
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.warn("");
        log.warn("Unable to install environment for shell type {} (we don't have support for it yet)", this.userEnvironment.getShellType());
        log.warn("");
        log.warn("You will need to install the following environment variables and paths yourself:");
        log.warn("");
        for (EnvVar var : vars) {
            log.warn("  set var: {}={}", var.getName(), var.getValue());
        }
        for (EnvPath path : paths) {
            log.warn("  add path: {}", path.getValue());
        }
        log.warn("");
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    }

    private void logEnvWritten(Path file, List<String> shellLines, boolean append) {
        final String appendStr = append ? "appending (or confirming already present)" : "writing";
        log.info("Installed the {} {} environment by {} the following lines to {}:", this.scope.toString().toLowerCase(), this.userEnvironment.getShellType().toString().toLowerCase(), appendStr, file);
        log.info("");
        for (String line : shellLines) {
            log.info("  {}", line);
        }
        log.info("");
    }

    private List<EnvPath> filterWellKnownEnvPaths(List<EnvPath> paths) {
        if (paths == null) {
            return null;
        }

        final Set<Path> wellKnownSystemPaths = this.wellKnownEnvPaths();
        final List<EnvPath> filteredPaths = new ArrayList<>();
        for (EnvPath path : paths) {
            if (!wellKnownSystemPaths.contains(path.getValue())) {
                filteredPaths.add(path);
            }
        }
        return filteredPaths;
    }

    private Set<Path> wellKnownEnvPaths() {
        // these are paths that should never be installed as environment paths since they are well-known, and the
        // chance of duplication is extremely high
        final Set<Path> wellKnownPaths = new HashSet<>();
        wellKnownPaths.add(Paths.get("/bin"));
        wellKnownPaths.add(Paths.get("/usr/bin"));
        wellKnownPaths.add(Paths.get("/usr/sbin"));
        wellKnownPaths.add(Paths.get("/usr/local/bin"));
        wellKnownPaths.add(Paths.get("/usr/local/sbin"));
        wellKnownPaths.add(Paths.get("C:\\Windows\\System32"));
        return wellKnownPaths;
    }

    private List<EnvVar> findBlacklistedEnvVars(List<EnvVar> vars) {
        if (vars == null) {
            return null;
        }

        final Set<String> blacklistedEnvVars = this.blacklistedEnvVars();
        final List<EnvVar> matched = new ArrayList<>();
        for (EnvVar var : vars) {
            if (blacklistedEnvVars.contains(var.getName())) {
                matched.add(var);
            }
        }
        return matched;
    }

    private Set<String> blacklistedEnvVars() {
        // these are env vars that should never be installed
        final Set<String> blacklistedVars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        blacklistedVars.add("PATH");
        return blacklistedVars;
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
