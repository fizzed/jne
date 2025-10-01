package com.fizzed.jne;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class SystemFilePermission {

    private final String octalPermission;
    private final String symbolicPermission;
    private final Set<PosixFilePermission> posixFilePermissions;

    private SystemFilePermission(String octalPermission, String symbolicPermission) {
        this.octalPermission = octalPermission;
        this.symbolicPermission = symbolicPermission;
        this.posixFilePermissions = PosixFilePermissions.fromString(octalPermission);
    }

    public String getOctalPermission() {
        return this.octalPermission;
    }

    public String getSymbolicPermission() {
        return this.symbolicPermission;
    }

    public Set<PosixFilePermission> getPosixFilePermissions() {
        return this.posixFilePermissions;
    }

    @Override
    public String toString() {
        return this.octalPermission;
    }

    static public SystemFilePermission parseOctalPermissions(String octalPermission) {
        // must be 3 chars in length, only digits
        if (octalPermission == null || !octalPermission.matches("^[0-7]{3}$")) {
            throw new IllegalArgumentException("Invalid file octal permission format '" + octalPermission + "' (must be 3 digits in length and only 0-7 such as 755)");
        }

        // build a 9 char string to represent this
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < octalPermission.length(); i++) {
            int octalDigit = Character.digit(octalPermission.charAt(i), 10);
            /*Octal Digit	Permission Value Sum	Symbolic Permission	Description
            0	0 + 0 + 0	---	No permissions (none)
            1	0 + 0 + 1	--x	Execute only
            2	0 + 2 + 0	-w-	Write only
            3	0 + 2 + 1	-wx	Write and Execute
            4	4 + 0 + 0	r--	Read only
            5	4 + 0 + 1	r-x	Read and Execute
            6	4 + 2 + 0	rw-	Read and Write
            7	4 + 2 + 1	rwx	Read, Write, and Execute (full)*/
            switch (octalDigit) {
                case 7:
                    sb.append("rwx");
                    break;
                case 6:
                    sb.append("rw-");
                    break;
                case 5:
                    sb.append("r-x");
                    break;
                case 4:
                    sb.append("r--");
                    break;
                case 3:
                    sb.append("-wx");
                    break;
                case 2:
                    sb.append("-w-");
                    break;
                case 1:
                    sb.append("--x");
                    break;
                case 0:
                    sb.append("---");
                    break;
                // The check at the start of the method handles anything outside 0-7,
                // so a 'default' case for error handling is optional here but good practice
                // if the initial check were removed.
            }
        }

        return new SystemFilePermission(octalPermission, sb.toString());
    }

}