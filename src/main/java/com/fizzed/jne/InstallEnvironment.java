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

import com.fizzed.jne.internal.*;
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

    /**
     * Retrieves the application directory based on the operating system and application-specific properties.
     *
     * On Windows, this method resolves the directory by appending the application name to the root directory.
     * On macOS, it appends the application name with a ".app" extension to the root directory.
     * On other operating systems, it resolves the directory by appending a unit name to the root directory.
     *
     * @return the resolved application directory as a {@code Path}, based on the current operating system.
     */
    public Path getApplicationDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return applicationRootDir.resolve(this.applicationName);
        } else if (operatingSystem == OperatingSystem.MACOS) {
            return applicationRootDir.resolve(this.applicationName + ".app");
        } else {
            return applicationRootDir.resolve(this.unitName);
        }
    }

    /**
     * Retrieves the system's binary directory path based on the operating system.
     * On Windows, it returns the root system directory. On other operating systems,
     * it appends "bin" to the root system directory.
     *
     * @return the path to the system's binary directory. For example:
     *         - On Windows: it may return something like "C:\Windows\System32"".
     *         - On Linux/Unix-based systems: it may return something like "/usr/bin",
     *           assuming the root directory is "/usr".
     */
    public Path getSystemBinDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return this.systemRootDir;
        } else {
            return this.systemRootDir.resolve("bin");
        }
    }

    /**
     * Retrieves the system share directory path based on the operating system.
     * On Windows, it returns the root system directory. For other operating systems,
     * it appends "share" to the root system directory path.
     *
     * @return the path to the system's shared directory. On Windows, this is the root system directory;
     *         on other operating systems, this is the root system directory followed by "share".
     */
    public Path getSystemShareDir() {
        if (operatingSystem == OperatingSystem.WINDOWS) {
            return this.systemRootDir;
        } else {
            return this.systemRootDir.resolve("share");
        }
    }

    /**
     * Retrieves the local application directory path by resolving the unit name
     * against the local root directory.
     *
     * This method combines the local root directory path with the unit name to return
     * a specific application directory path.
     *
     * @return a {@code Path} object representing the resolved local application directory
     * based on the root directory and the unit name.
     */
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

    /**
     * Installs the environment using the specified list of environment paths.
     * This method initializes or installs required resources associated with the
     * given environment paths. It delegates to another method to handle further
     * installation configuration with additional parameters if necessary.
     *
     * Example usage:
     *   installEnv(paths);
     *
     * @param paths the list of environment paths to be installed. Each path in the
     *              list represents a specific resource or configuration required
     *              for the environment setup.
     * @throws IOException if an I/O error occurs during the installation process.
     * @throws InterruptedException if the thread executing the process is interrupted.
     */
    public void installEnv(List<EnvPath> paths) throws IOException, InterruptedException {
        this.installEnv(paths, Collections.emptyList());
    }

    /**
     * Installs environment paths (e.g., system `PATH`) and variables into the user's
     * or system's environment configuration depending on the specified scope. The method
     * adapts to different operating systems (e.g., Windows, macOS, Linux) and shells
     * (e.g., BASH, ZSH, CSH).
     *
     * Changes may include updating the Windows registry, modifying shell profile files
     * (e.g., .bashrc, .zshrc), or writing to system-wide configuration paths.
     *
     * Throws an exception if certain restricted environment variables are attempted to be installed.
     *
     * Example usage: Installing a custom PATH or environment variable for a user on a UNIX-like system.
     *
     * @param paths A list of {@code EnvPath} objects representing paths to be added to the environment variable PATH.
     *              Each path can specify whether it should be prepended or appended.
     * @param vars  A list of {@code EnvVar} objects representing the environment variables to be added to the environment.
     *              Each variable includes a name and value.
     * @throws IOException If there is an error modifying files (e.g., shell profiles), accessing the registry,
     *                     or attempting to install prohibited variables.
     * @throws InterruptedException If any required processes (e.g., executing external commands) are interrupted.
     */
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

        if (this.operatingSystem == OperatingSystem.WINDOWS) {
            log.info("Installing the {} environment by modifying the windows registry with the following:", this.scope.toString().toLowerCase());
            log.info("");

            // we are going to query the user OR system env vars via registry
            final WindowsRegistry currentEnvInRegistry;
            try {
                if (this.scope == EnvScope.USER) {
                    currentEnvInRegistry = WindowsRegistry.queryUserEnvironmentVariables(new SystemExecutorLocal());
                } else {
                    currentEnvInRegistry = WindowsRegistry.querySystemEnvironmentVariables(new SystemExecutorLocal());
                }
            } catch (Exception e) {
                throw new IOException("Unable to query environment variables for " + this.scope.toString().toLowerCase() + " scope", e);
            }

            // shell is irrelevant on windows, env vars and PATH are setup in the registry
            // e.g. setx MY_VARIABLE "MyValue" OR setx MY_VARIABLE "MyValue" /M
            for (EnvVar var : vars) {
                final boolean exists = Utils.searchEnvVar(currentEnvInRegistry.getValues(), var.getName(), var.getValue());
                if (!exists) {
                    final List<String> envVarCmd = new ArrayList<>(asList("setx", var.getName(), var.getValue()));
                    if (this.scope == EnvScope.SYSTEM) {
                        // we just tack on a /M to make it system-wide
                        envVarCmd.add("/M");
                    }
                    //Utils.execAndGetOutput(envVarCmd);
                    try {
                        SystemExecutor.LOCAL.execProcess(envVarCmd);
                    } catch (Exception e) {
                        throw new IOException("Unable to set environment variable", e);
                    }
                    log.info("  set {}={}", var.getName(), var.getValue());
                } else {
                    log.info("  set {}={} (skipped as this already was present)", var.getName(), var.getValue());
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
                    //Utils.execAndGetOutput(envVarCmd);
                    try {
                        SystemExecutor.LOCAL.execProcess(envVarCmd);
                    } catch (Exception e) {
                        throw new IOException("Unable to set environment variable", e);
                    }
                    log.info("  {} PATH {}", path.isPrepend() ? "prepend" : "append", path.getValue());
                } else {
                    log.info("  {} PATH {} (skipped as this was already present)", path.isPrepend() ? "prepend" : "append", path.getValue());
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
                // there is no prepend/append support for macos
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

            // even though we're BASH, the global profile using BOURNE shell syntax
            final List<String> shellLines = this.buildShellLines(ShellType.SH, vars, filteredPaths);

            writeLinesToFileWithSectionBeginAndEndLines(targetFile, shellLines, append);

            this.logEnvWritten(targetFile, shellLines, append);

            return;     // we are done with bash setup
        }

        if (shellType == ShellType.ZSH) {

            // since system-wide paths were already installed above, everything nicely now goes into the same file
            final Path targetFile = this.userEnvironment.getHomeDir().resolve(".zprofile");

            final List<String> shellLines = this.buildShellLines(shellType, vars, filteredPaths);

            writeLinesToFileWithSectionBeginAndEndLines(targetFile, shellLines, true);

            this.logEnvWritten(targetFile, shellLines, true);

            return;     // we are done with zsh setup
        }

        if (shellType == ShellType.CSH || shellType == ShellType.TCSH) {

            // if we're a CSH then you'd want your stuff in .cshrc, but if using TCSH, it generally will only load
            // your stuff from .tcshrc -- however both shells look for global stuff in /etc/csh.cshrc
            Path targetFile = this.userEnvironment.getHomeDir().resolve(".cshrc");
            if (shellType == ShellType.TCSH) {
                targetFile = this.userEnvironment.getHomeDir().resolve(".tcshrc");
            }
            if (this.scope == EnvScope.SYSTEM) {
                final Path etcCshrcFile = Paths.get("/etc/csh.cshrc");
                if (Files.exists(etcCshrcFile)) {
                    targetFile = etcCshrcFile;
                }
            }

            final List<String> shellLines = this.buildShellLines(shellType, vars, filteredPaths);

            writeLinesToFileWithSectionBeginAndEndLines(targetFile, shellLines, true);

            this.logEnvWritten(targetFile, shellLines, true);

            return;     // we are done with csh setup
        }

        if (shellType == ShellType.KSH || shellType == ShellType.ASH || shellType == ShellType.SH) {

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

            final List<String> shellLines = this.buildShellLines(shellType, vars, filteredPaths);

            writeLinesToFileWithSectionBeginAndEndLines(targetFile, shellLines, append);

            this.logEnvWritten(targetFile, shellLines, append);

            return;     // we are done with ksh setup
        }

        // hmm... we don't actually know how to handle this shell type
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.warn("");
        log.warn("Unable to install environment for shell type {} (we don't have support for it yet)", shellType);
        log.warn("");
        log.warn("You will need to install the following environment variables and paths yourself.");
        log.warn("Here is some POSIX-compliant example code:");
        log.warn("");
        final ShellBuilder shellBuilder = new ShellBuilder(ShellType.SH);
        for (EnvVar var : vars) {
            log.warn("  {}", shellBuilder.exportEnvVar(var));
        }
        for (EnvPath path : paths) {
            log.warn("  {}", shellBuilder.addEnvPath(path));
        }
        log.warn("");
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private List<String> buildShellLines(ShellType shellType, List<EnvVar> vars, List<EnvPath> paths) {
        final List<String> shellLines = new ArrayList<>();
        final ShellBuilder shellBuilder = new ShellBuilder(shellType);

        shellLines.addAll(shellBuilder.sectionBegin(this.unitName));

        if (vars != null) {
            for (EnvVar var : vars) {
                shellLines.add(shellBuilder.exportEnvVar(var));
            }
        }

        if (paths != null) {
            for (EnvPath path : paths) {
                shellLines.add(shellBuilder.addEnvPath(path));
            }
        }

        shellLines.addAll(shellBuilder.sectionEnd(this.unitName));

        return shellLines;
    }

    private void logEnvWritten(Path file, List<String> shellLines, boolean append) {
        final String appendStr = append ? "appending/replacing" : "writing";
        log.info("Installed the {} {} environment by {} the following to {}:", this.scope.toString().toLowerCase(), this.userEnvironment.getShellType().toString().toLowerCase(), appendStr, file);
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

    /**
     * Detects and returns an {@code InstallEnvironment} object based on the provided application name
     * and unit name. This method uses a default environment scope of {@code EnvScope.SYSTEM}.
     * It configures the installation environment by identifying the operating system and setting up
     * application installation paths and properties accordingly.
     *
     * @param applicationName the name of the application. This should not be null and is used for
     *                        determining application-specific installation paths.
     * @param unitName the unique identifier for the application unit. This should be in lowercase,
     *                 should not include spaces, and may contain letters, numbers, hyphens, or underscores.
     * @return an {@code InstallEnvironment} object preconfigured with operating system details,
     *         application name, unit name, and a default system-wide scope. The object includes paths
     *         and properties tied to application installation.
     *
     * Example Usage:
     * - If the application name is "MyApp" and the unit name is "my_app", this method might return an
     *   {@code InstallEnvironment} object configured for a system-wide installation, with directories
     *   such as {@code /usr/local/MyApp} or equivalent system paths depending on the OS.
     */
    static public InstallEnvironment detect(String applicationName, String unitName) {
        return detect(applicationName, unitName, EnvScope.SYSTEM);
    }

    /**
     * Detects and returns an {@code InstallEnvironment} object based on the provided application name,
     * unit name, and environmental scope. This method utilizes the logical user environment
     * automatically detected by the system to configure application installation paths and parameters
     * appropriately for the underlying operating system and scope.
     *
     * @param applicationName the name of the application. This should not be null and is used for identifying
     *                        and setting application-specific installation paths.
     * @param unitName the unique identifier for the application unit. This should be in lowercase, not include
     *                 spaces, and may comprise letters, numbers, hyphens, or underscores. It is used for
     *                 determining the installation directory structure.
     * @param scope the scope of the environment, specifying whether the installation is user-specific
     *              ({@code EnvScope.USER}) or system-wide ({@code EnvScope.SYSTEM}). This affects where
     *              application-specific files and directories are created.
     * @return an {@code InstallEnvironment} object that is initialized based on the detected operating
     *         system, application name, unit name, and the provided scope. The returned object contains
     *         configuration details such as installation directories and environment-specific properties.
     *
     * Example Usage:
     * - For a user-specific environment:
     *   When {@code scope} is {@code EnvScope.USER}, the returned {@code InstallEnvironment} object may
     *   include directories like {@code ~/.local} for Linux.
     *
     * - For a system-wide installation:
     *   When {@code scope} is {@code EnvScope.SYSTEM}, the directories in the returned {@code InstallEnvironment}
     *   object may correspond to paths such as {@code /usr/local} or {@code C:\Program Files}.
     */
    static public InstallEnvironment detect(String applicationName, String unitName, EnvScope scope) {
        return detect(applicationName, unitName, scope, UserEnvironment.detectLogical());
    }

    /**
     * Detects the installation environment based on the provided application name, unit name,
     * scope, and user environment details. This method identifies the operating system
     * and adjusts the installation paths and configurations accordingly.
     *
     * @param applicationName the name of the application. This value must not be null.
     * @param unitName the unique, lowercase identifier for the application unit.
     *                 It must not include spaces, and can contain letters, numbers, hyphens, or underscores.
     * @param scope the scope of the environment, either {@code EnvScope.SYSTEM} or {@code EnvScope.USER}.
     * @param userEnvironment the user's environment details used for determining user-specific installation paths.
     * @return an {@code InstallEnvironment} object configured with details specific to the detected
     *         operating system, the provided scope, and user environment.
     * @throws IllegalArgumentException if {@code applicationName} or {@code unitName} is null, or
     *                                  if {@code unitName} does not follow naming rules.
     *
     * Example Usage:
     * - For a user-specific environment:
     *   Calling this method with scope set to {@code EnvScope.USER} will return an {@code InstallEnvironment}
     *   object where installation directories may point to user-specific paths such as {@code ~/.local} for Linux.
     *
     * - For a system-wide installation on macOS:
     *   Calling this method with scope set to {@code EnvScope.SYSTEM} may set directories like
     *   {@code /usr/local} or {@code /Application}.
     */
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
