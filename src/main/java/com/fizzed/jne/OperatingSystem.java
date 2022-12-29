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
    
    ANY(null),
    UNKNOWN(null),
    WINDOWS(null),
    MACOS(new String[] { "osx" }),
    LINUX(null),
    FREEBSD(null),
    OPENBSD(null),
    SOLARIS(null);

    private final String[] aliases;

    OperatingSystem(String[] aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return aliases;
    }

}