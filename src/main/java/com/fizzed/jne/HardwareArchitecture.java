package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2017 Fizzed, Inc
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

/**
 * Architecture of hardware & operating system.
 */
public enum HardwareArchitecture {

    X32("x32", new String[] { "i386", "i586", "i686" }, new String[] { "x86" }),
    X64("x64", new String[] { "x86_64", "amd64" }),   // also known as amd64 or x86_64
    ARMEL("armel", null, new String[] { "arm32v5", "arm32v6" }),                        // ARMEL, ARM 32-bit SF v6, v7, v5
    ARMHF("armhf", null, new String[] { "arm32v7", "armv7l" }),                        // ARMHF stands for "ARM hard float", and is the name given to a Debian port for ARM processors (armv7+) that have hardware floating point support, which is found on most modern 32-bit ARM boards
    ARM64("arm64", new String[] { "aarch64" }, new String[] { "arm64v8" }),          // used by docker
    RISCV64("riscv64", null, new String[] { "riscv64gc" }),                    // used by llvm
    MIPS64LE("mips64le", new String[] { "mips64el" }),
    S390X("s390x", null),
    PPC64LE("ppc64le", null),       // Introduced with the POWER8 processor and is the focus for modern OpenPOWER architecture. Uses little-endian byte ordering, where the least significant byte of a word is stored at the lowest memory address.
    PPC64("ppc64", null);           // Associated with older 64-bit PowerPC processors like the PowerPC 970 (used in the PowerMac G5) and earlier POWER series chips.

    private final String descriptor;
    private final String[] aliases;
    private final String[] extraAliases;

    HardwareArchitecture(String descriptor, String[] aliases) {
        this(descriptor, aliases, null);
    }

    HardwareArchitecture(String descriptor, String[] aliases, String[] extraAliases) {
        this.descriptor = descriptor;
        this.aliases = aliases;
        this.extraAliases = extraAliases;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String[] getExtraAliases() {
        return extraAliases;
    }

    static public HardwareArchitecture resolve(String value) {
        for (HardwareArchitecture arch : HardwareArchitecture.values()) {
            if (arch.name().equalsIgnoreCase(value)) {
                return arch;
            }
            if (arch.aliases != null) {
                for (String alias : arch.aliases) {
                    if (alias.equalsIgnoreCase(value)) {
                        return arch;
                    }
                }
            }
            if (arch.extraAliases != null) {
                for (String alias : arch.extraAliases) {
                    if (alias.equalsIgnoreCase(value)) {
                        return arch;
                    }
                }
            }
        }
        return null;
    }

}