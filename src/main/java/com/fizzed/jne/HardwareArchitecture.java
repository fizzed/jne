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
    
    ANY,
    UNKNOWN,
    X32,
    X64,
    ARM32,
    ARM64,
    SPARC;
    
    public static HardwareArchitecture detect() {
        return parseSystemProperty(System.getProperty("os.arch"));
    }
    
    public static HardwareArchitecture parseSystemProperty(String value) {
        if (value != null) {
            value = value.toLowerCase();
            if (value.contains("amd64") || value.contains("x86_64")) {
                return X64;
            } else if (value.contains("i386") || value.contains("x86")) {
                return X32;
            } else if (value.contains("aarch64")) {
                return ARM64;
            } else if (value.contains("aarch32")) {
                return ARM32;
            } else if (value.contains("sparc")) {
                return SPARC;
            }
        }
        return UNKNOWN;
    }

}
