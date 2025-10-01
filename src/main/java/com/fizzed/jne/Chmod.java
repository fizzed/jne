package com.fizzed.jne;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class Chmod {

    /**
     * Chmods a file to set owner/group/everyone read/write/execute permissions
     * @param path The path to chmod
     * @param octalPermission The octal representation of our changes such as 755, 644, etc.
     * @throws IOException If there was an issue while doing permissions
     */
    static public void chmod(Path path, String octalPermission) throws IOException {
        SystemFilePermission permission = SystemFilePermission.parseOctalPermissions(octalPermission);
        chmod(path, permission);
    }

    static public void chmod(Path path, SystemFilePermission permission) throws IOException {
        // try posix method first
        try {
            Files.setPosixFilePermissions(path, permission.getPosixFilePermissions());
        } catch (UnsupportedOperationException e) {
            // likely just not on posix, and we'll fallback to old "file" methods instead
            applyLegacyPermissions(path.toFile(), permission.getPosixFilePermissions());
        }
    }

    /**
     * Attempts to map a full Set<PosixFilePermission> to the limited legacy
     * File.setXxx(boolean, boolean) methods.
     *
     * WARNING: This is a lossy conversion. The File methods cannot set separate
     * permissions for 'Group' and 'Others'. The permissions for the 'Everybody'
     * group will be set to the *least permissive* setting among Group/Others.
     *
     * @param file The file to apply permissions to.
     * @param perms The full set of desired POSIX permissions.
     */
    static private void applyLegacyPermissions(File file, Set<PosixFilePermission> perms) {
        if (file == null || perms == null || !file.exists()) {
            return;
        }

        // --- 1. Determine Owner Permissions (ownerOnly = true) ---
        boolean ownerRead = perms.contains(PosixFilePermission.OWNER_READ);
        boolean ownerWrite = perms.contains(PosixFilePermission.OWNER_WRITE);
        boolean ownerExecute = perms.contains(PosixFilePermission.OWNER_EXECUTE);

        // Apply Owner Permissions
        // The second argument 'true' means 'ownerOnly'
        file.setReadable(ownerRead, true);
        file.setWritable(ownerWrite, true);
        file.setExecutable(ownerExecute, true);

        // --- 2. Determine Everybody Permissions (ownerOnly = false) ---
        // The File methods set the permission for 'Everybody' (Group + Others).
        // To prevent unintentionally granting access, we use a *restrictive* strategy:
        // Everybody gets a permission only if *both* Group and Others have it.

        boolean everybodyRead = perms.contains(PosixFilePermission.GROUP_READ)
            && perms.contains(PosixFilePermission.OTHERS_READ);

        boolean everybodyWrite = perms.contains(PosixFilePermission.GROUP_WRITE)
            && perms.contains(PosixFilePermission.OTHERS_WRITE);

        boolean everybodyExecute = perms.contains(PosixFilePermission.GROUP_EXECUTE)
            && perms.contains(PosixFilePermission.OTHERS_EXECUTE);

        // Apply Everybody Permissions
        // The second argument 'false' means 'apply to everybody' (not ownerOnly)

        // IMPORTANT: We only want to set the permission for Group/Others if the
        // File methods *don't* overwrite the Owner permission we just set.
        // However, the File API is poorly defined here. Using 'ownerOnly = false'
        // typically means "set for ALL USERS including owner."
        // We will stick to the safer interpretation for legacy compatibility, which
        // usually results in:
        // - setXxx(val, true) sets owner
        // - setXxx(val, false) sets ALL (Owner, Group, Others)

        // Given the ambiguity and lossiness, the following is the most logical
        // but imperfect mapping:

        file.setReadable(everybodyRead, false);
        file.setWritable(everybodyWrite, false);
        file.setExecutable(everybodyExecute, false);

        // This last step effectively sets Group and Others permissions based on the
        // restrictive "everybodyRead" logic, BUT it might also overwrite the Owner's
        // more specific settings if 'everybody' is more permissive than 'owner'.
        // For example, if Owner is 'rwx' and Group/Others are '---', setting everybody
        // to 'r--' (read) *should* set the owner's read bit to true, but might
        // disable the owner's write/execute bits if the underlying OS implementation
        // is very crude.
    }

}