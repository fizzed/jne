package com.fizzed.jne.internal;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
            applyWindowsAclWithAdminOverride(path, permission.getPosixFilePermissions());
            // likely just not on posix, and we'll fallback to old "file" methods instead
            //applyLegacyPermissions(path.toFile(), permission.getPosixFilePermissions());
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

    /**
     * Maps a Set<PosixFilePermission> to a Windows ACL list, with the following strategy:
     * 1. Grants the File Owner the corresponding POSIX permissions.
     * 2. Ignores the POSIX Group permissions.
     * 3. Grants the 'Others' permissions to the 'Everyone' principal.
     * 4. Grants the local 'Administrators' group Full Control.
     *
     * @param path The file or directory path.
     * @param posixPerms The set of desired POSIX permissions (Owner and Others used).
     * @throws IOException if an I/O error occurs or the ACL view is not supported.
     */
    static private void applyWindowsAclWithAdminOverride(Path path, Set<PosixFilePermission> posixPerms)
        throws IOException, UnsupportedOperationException {

        AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (aclView == null) {
            throw new UnsupportedOperationException("AclFileAttributeView is not supported on this file system.");
        }

        UserPrincipalLookupService lookupService = path.getFileSystem().getUserPrincipalLookupService();
        List<AclEntry> aclEntries = new ArrayList<AclEntry>();

        // --- 1. Lookup Principals ---
        UserPrincipal owner = Files.getOwner(path);

        // The Windows local Administrators group
        GroupPrincipal administratorsGroup;
        try {
            administratorsGroup = lookupService.lookupPrincipalByGroupName("Administrators");
        } catch (UserPrincipalNotFoundException e) {
            throw new IOException("Could not find the 'Administrators' group principal.", e);
        }

        // 'Others' mapping
        UserPrincipal everyonePrincipal;
        try {
            everyonePrincipal = lookupService.lookupPrincipalByName("EVERYONE@");
        } catch (UserPrincipalNotFoundException e) {
            everyonePrincipal = lookupService.lookupPrincipalByName("Everyone");
        }


        // --- 2. Define Permissions for each relevant identity (Owner, Others) ---
        boolean isDirectory = Files.isDirectory(path);

        // a) Owner Permissions
        Set<AclEntryPermission> ownerAclPerms = mapPosixToAclPermissions(posixPerms, "OWNER", isDirectory);

        // b) Others Permissions
        Set<AclEntryPermission> othersAclPerms = mapPosixToAclPermissions(posixPerms, "OTHERS", isDirectory);

        // c) Administrator FULL CONTROL
        Set<AclEntryPermission> adminFullControl = new HashSet<AclEntryPermission>();

        // Full Control includes all specific permissions
        adminFullControl.add(AclEntryPermission.READ_ACL);
        adminFullControl.add(AclEntryPermission.WRITE_ACL);
        adminFullControl.add(AclEntryPermission.READ_ATTRIBUTES);
        adminFullControl.add(AclEntryPermission.WRITE_ATTRIBUTES);
        adminFullControl.add(AclEntryPermission.SYNCHRONIZE);
        adminFullControl.add(AclEntryPermission.DELETE);
        adminFullControl.add(AclEntryPermission.WRITE_OWNER);
        adminFullControl.add(AclEntryPermission.READ_DATA);
        adminFullControl.add(AclEntryPermission.EXECUTE);
        adminFullControl.add(AclEntryPermission.WRITE_DATA);
        adminFullControl.add(AclEntryPermission.APPEND_DATA);

        // Directory-only permissions are still part of Full Control
        adminFullControl.add(AclEntryPermission.DELETE_CHILD);
        adminFullControl.add(AclEntryPermission.LIST_DIRECTORY);
        adminFullControl.add(AclEntryPermission.ADD_FILE);
        adminFullControl.add(AclEntryPermission.ADD_SUBDIRECTORY);

        // --- 3. Build ACL Entries ---

        // 1. Administrators Group (FULL CONTROL)
        AclEntry adminEntry = AclEntry.newBuilder()
            .setType(AclEntryType.ALLOW)
            .setPrincipal(administratorsGroup)
            .setPermissions(adminFullControl)
            .setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT)
            .build();
        aclEntries.add(adminEntry);

        // 2. File Owner (POSIX Owner mapping)
        if (!ownerAclPerms.isEmpty()) {
            AclEntry ownerEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(ownerAclPerms)
                .setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT)
                .build();
            aclEntries.add(ownerEntry);
        }

        // 3. Others (POSIX Others mapping)
        if (!othersAclPerms.isEmpty()) {
            AclEntry everyoneEntry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(everyonePrincipal)
                .setPermissions(othersAclPerms)
                .setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT)
                .build();
            aclEntries.add(everyoneEntry);
        }

        // --- 4. Apply the new ACL list ---
        aclView.setAcl(aclEntries);
    }

    // --- Helper Method ---

    private static Set<AclEntryPermission> mapPosixToAclPermissions(
        Set<PosixFilePermission> posixPerms, String principalType, boolean isDirectory) {

        // Use HashSet as a generic, standard implementation of Set
        Set<AclEntryPermission> aclPermissions = new HashSet<>();

        // Lookup strings for PosixFilePermission
        final String readPerm = principalType + "_READ";
        final String writePerm = principalType + "_WRITE";
        final String execPerm = principalType + "_EXECUTE";

        boolean read = posixPerms.contains(PosixFilePermission.valueOf(readPerm));
        boolean write = posixPerms.contains(PosixFilePermission.valueOf(writePerm));
        boolean execute = posixPerms.contains(PosixFilePermission.valueOf(execPerm));

        if (read) {
            aclPermissions.add(AclEntryPermission.READ_DATA);
            aclPermissions.add(AclEntryPermission.READ_ATTRIBUTES);
            aclPermissions.add(AclEntryPermission.SYNCHRONIZE);
            if (isDirectory) aclPermissions.add(AclEntryPermission.LIST_DIRECTORY);
        }

        if (write) {
            aclPermissions.add(AclEntryPermission.WRITE_DATA);
            aclPermissions.add(AclEntryPermission.APPEND_DATA);
            aclPermissions.add(AclEntryPermission.WRITE_ATTRIBUTES);
            if (isDirectory) {
                aclPermissions.add(AclEntryPermission.ADD_FILE);
                aclPermissions.add(AclEntryPermission.ADD_SUBDIRECTORY);
            }
        }

        if (execute) {
            aclPermissions.add(AclEntryPermission.EXECUTE);
        }

        return aclPermissions;
    }

}
