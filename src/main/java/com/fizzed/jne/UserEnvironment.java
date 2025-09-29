package com.fizzed.jne;

import com.fizzed.jne.internal.EtcPasswd;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fizzed.jne.internal.Utils.trimToNull;

/**
 * The key properties of a "user environment" that effect installing/running apps, etc.
 */
public class UserEnvironment {

    private String user;
    private Boolean elevated;
    private Path homeDir;
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

        // 1. detect which user we are running as
        String user = trimToNull(System.getenv("USER"));

        // 2. detect if running with elevated privileges (e.g. sudo/doas) since that will effect what "user" we
        // think we are running as
        String elevatedUser = trimToNull(System.getenv("SUDO_USER"));
        if (elevatedUser == null) {
            elevatedUser = trimToNull(System.getenv("DOAS_USER"));
        }

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
                userEnvironment.homeDir = Paths.get(entry.getHome());
                userEnvironment.shellType = ShellType.detectFromBin(entry.getShell());
                return; // we are done
            }
        }

        // we now need to know what operating system we're on so we can detect things faster
        final OperatingSystem os = PlatformInfo.detectOperatingSystem();

        // second, we may be on a macos, which requires using (dscl . -read /Users/builder) to determine a shell present
        if (os == OperatingSystem.MACOS) {

        }
    }

    /*static public ShellType detectShellType() {
        


        String shellPath = null;

        // NOTE: sometimes if running as "sudo", the shell the user will normally have will be changed just
        // for sudo.  A more reliable method turns out to be "echo $0"??
        String sudoUser = System.getenv("SUDO_USER");
        //System.out.println("sudoUser env: " + sudoUser);

        // or DOAS_USER on openbsd
        if (sudoUser == null) {
            sudoUser = System.getenv("DOAS_USER");
            //System.out.println("doasUser env: " + sudoUser);
        }

        if (sudoUser != null) {
            // looks like we are running as sudo, investigate /etc/passwd to see the default shell for the user
            final Path etcPasswd = Paths.get("/etc/passwd");

            if (Files.exists(etcPasswd)) {
                try {
                    byte[] etcPasswdBytes = Files.readAllBytes(etcPasswd);
                    String etcPasswdString = new String(etcPasswdBytes, StandardCharsets.UTF_8);
                    String[] lines = etcPasswdString.split("\n");
                    for (String line : lines) {
                        String[] parts = line.split(":");
                        if (parts.length == 7 && sudoUser.equals(parts[0])) {
                            shellPath = parts[6];
                            break;
                        }
                    }
                } catch (IOException e) {
                    // ignore this error, will continue with later detection
                }
            }

            // if we're running as sudo and we still do not have a shell, if we're on a mac assume zsh?
            // the correct way is to actually use "dsl" utility
            // dscl . -read /Users/builder
            if (NativeTarget.detect().getOperatingSystem() == OperatingSystem.MACOS) {
                shellPath = "zsh";
            }
        }

        if (shellPath == null) {
            // fallback to the shell variable (which hopefully matches the default)
            shellPath = System.getenv("SHELL");
        }

        if (shellPath != null) {
            if (shellPath.endsWith("bash")) {
                return Shell.BASH;
            } else if (shellPath.endsWith("zsh")) {
                return Shell.ZSH;
            } else if (shellPath.endsWith("csh")) {
                return Shell.CSH;
            } else if (shellPath.endsWith("ksh")) {
                return Shell.KSH;
            }
        }

        // we were not able to detect it
        return null;
    }*/

}