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
 * Operating system that Java is running on.
 */
public enum OperatingSystem {

    WINDOWS("Windows", null, new String[] { "win" }),
    MACOS("MacOS", new String[] { "osx", "darwin" }, new String[] { "mac" }),
    LINUX("Linux", null, null),
    FREEBSD("FreeBSD", null),
    OPENBSD("OpenBSD", null),
    NETBSD("NetBSD", null),
    DRAGONFLYBSD("DragonFlyBSD", null),
    SOLARIS("Solaris", new String[] { "sun" }, new String[] { "sunos" }),
    AIX("AIX", null),
    ANDROID("Android", null);

    private final String descriptor;
    private final String[] aliases;
    private final String[] extraAliases;

    OperatingSystem(String descriptor, String[] aliases) {
        this(descriptor, aliases, null);
    }

    OperatingSystem(String descriptor, String[] aliases, String[] extraAliases) {
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

    static public OperatingSystem resolve(String value) {
        if (value == null) {
            return null;
        }

        value = value.toLowerCase();

        if (value.contains("windows")) {
            return OperatingSystem.WINDOWS;
        } else if (value.contains("mac") || value.contains("darwin")) {
            return OperatingSystem.MACOS;
        }

        for (OperatingSystem os : OperatingSystem.values()) {
            if (os.name().equalsIgnoreCase(value)) {
                return os;
            }
            if (os.aliases != null) {
                for (String alias : os.aliases) {
                    if (alias.equalsIgnoreCase(value)) {
                        return os;
                    }
                }
            }
            if (os.extraAliases != null) {
                for (String extraAlias : os.extraAliases) {
                    if (extraAlias.equalsIgnoreCase(value)) {
                        return os;
                    }
                }
            }
        }

        return null;
    }

}