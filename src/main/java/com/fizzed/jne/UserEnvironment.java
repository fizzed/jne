package com.fizzed.jne;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UserEnvironment {




    static public ShellType detectShellType() {
        


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
    }

}