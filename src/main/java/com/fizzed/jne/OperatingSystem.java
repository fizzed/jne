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
    
    ANY,
    UNKNOWN,
    WINDOWS,
    OSX,
    LINUX,
    FREEBSD,
    OPENBSD,
    SOLARIS;
    
    public static OperatingSystem detect() {
        return parseSystemProperty(System.getProperty("os.name"));
    }
    
    public static OperatingSystem parseSystemProperty(String value) {
        if (value != null) {
            value = value.toLowerCase();
            if (value.contains("windows")) {
                return WINDOWS;
            } else if (value.contains("mac") || value.contains("darwin")) {
                return OSX;
            } else if (value.contains("linux")) {
                return LINUX;
            } else if (value.contains("sun") || value.contains("solaris")) {
                return SOLARIS;
            } else if (value.contains("freebsd")) {
                return FREEBSD;
            } else if (value.contains("OPENBSD")) {
                return OPENBSD;
            }
        }
	return UNKNOWN;
    }

}
