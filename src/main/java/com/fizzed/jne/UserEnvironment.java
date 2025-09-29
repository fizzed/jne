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

import com.fizzed.jne.internal.EtcPasswd;
import com.fizzed.jne.internal.MacDscl;
import com.fizzed.jne.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fizzed.jne.internal.Utils.trimToNull;

/**
 * The key properties of a "user environment" that effect installing/running apps, etc.
 */
public class UserEnvironment {
    static private final Logger log = LoggerFactory.getLogger(UserEnvironment.class);

    private String user;
    private Boolean elevated;
    private Path homeDir;
    private Integer userId;
    private Integer groupId;
    private String displayName;
    private Path shell;
    private ShellType shellType;

    public String getUser() {
        return user;
    }

    public Boolean getElevated() {
        return elevated;
    }

    public Path getHomeDir() {
        return homeDir;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Path getShell() {
        return shell;
    }

    public ShellType getShellType() {
        return shellType;
    }

    static public UserEnvironment detectEffective() {
        return detect(false);
    }

    static public UserEnvironment detectLogical() {
        return detect(true);
    }

    static private UserEnvironment detect(boolean logical) {
        final UserEnvironment userEnvironment = new UserEnvironment();

        // 1. detect which user we are running as (this is most linux/unix/mac systems)
        String user = trimToNull(System.getenv("USER"));
        // on windows, this usually is USERNAME
        if (user == null) {
            user = System.getenv("USERNAME");
        }

        // 2. detect if running with elevated privileges (e.g. sudo/doas) since that will effect what "user" we
        // think we are running as
        String elevatedUser = trimToNull(System.getenv("SUDO_USER"));
        if (elevatedUser == null) {
            elevatedUser = trimToNull(System.getenv("DOAS_USER"));
        }

        // NOTE: on windows, we could try to detect if a process has elevated privileges or not, but that's a lot of
        // work just to set a boolean in our user environment, so we'll skip it for now

        // 3. are going to proceed with the effective or logical user?
        userEnvironment.user = user;
        userEnvironment.elevated = false;
        if (logical && elevatedUser != null) {
            userEnvironment.user = elevatedUser;
            userEnvironment.elevated = true;
        }

        // 3. try to detect home directory, shell for a specific user
        detectHomeAndShellType(userEnvironment);

        return userEnvironment;
    }


    static private void detectHomeAndShellType(UserEnvironment userEnvironment) {
        // first, if we're on a linux/unix/bsd environment, /etc/passwd will have what we want
        final EtcPasswd etcPasswd = EtcPasswd.detect();
        if (etcPasswd != null) {
            final EtcPasswd.Entry entry = etcPasswd.findEntryByUserName(userEnvironment.user);
            if (entry != null) {
                log.debug("Using /etc/passwd for detecting user environment for {}", userEnvironment.user);
                userEnvironment.homeDir = Paths.get(entry.getHome());
                userEnvironment.userId = entry.getUserId();
                userEnvironment.groupId = entry.getGroupId();
                userEnvironment.displayName = entry.getName();
                userEnvironment.shell = Paths.get(entry.getShell());
                userEnvironment.shellType = ShellType.detectFromBin(entry.getShell());
                return;
            }
        }

        // second, we now need to know what operating system we're on so we can detect things faster
        final OperatingSystem os = PlatformInfo.detectOperatingSystem();

        // we may be on a macos, which requires using (dscl . -read /Users/builder) to determine a shell present
        if (os == OperatingSystem.MACOS) {
            try {
                MacDscl macDscl = MacDscl.readByUser(userEnvironment.user);
                if (macDscl != null) {
                    log.debug("Using dscl output for detecting user environment for {}", userEnvironment.user);
                    userEnvironment.homeDir = macDscl.getHomeDir();
                    userEnvironment.userId = macDscl.getUniqueId();
                    userEnvironment.groupId = macDscl.getPrimaryGroupId();
                    userEnvironment.displayName = macDscl.getRealName();
                    userEnvironment.shell = macDscl.getShell();
                    userEnvironment.shellType = ShellType.detectFromBin(macDscl.getShell().toString());
                    return;
                }
            } catch (Exception e) {
                log.error("Unable to cleanly read dscl output", e);
            }
        }

        if (os == OperatingSystem.WINDOWS) {
            log.debug("Using windows env vars for user environment for {}", userEnvironment.user);

            // environment variables (regardless of whether we are elevated or not) will setup everything nicely
            final String homeDrive = trimToNull(System.getenv("HOMEDRIVE"));
            final String homePath = trimToNull(System.getenv("HOMEPATH"));
            if (homeDrive != null && homePath != null) {
                userEnvironment.homeDir = Paths.get(homeDrive + homePath);
            }

            // on windows we are going to simplify things a bit as a user does not really pick their shell
            // however, if powershell exists, we will make that the user's preferred shell, otherwise it'll be cmd.exe
            final Path powershellExe = Utils.which("powershell.exe");
            if (powershellExe != null) {
                userEnvironment.shell = powershellExe;
                userEnvironment.shellType = ShellType.PS;
            } else {
                final Path cmdExe = Utils.which("cmd.exe");
                if (cmdExe != null) {
                    userEnvironment.shell = cmdExe;
                    userEnvironment.shellType = ShellType.CMD;
                }
            }
        }
    }

}
