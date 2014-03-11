package com.mfizz.jne;

/*
 * #%L
 * ch-jni-loader
 * %%
 * Copyright (C) 2012 - 2013 Cloudhopper by Twitter
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
 * 
 * @author garth
 */
public enum OS {
    
    UNKNOWN,
    WINDOWS,
    OSX,
    LINUX,
    SOLARIS;
    
    public static OS getOS() {
        String osName = System.getProperty("os.name");
        
        if (osName != null) {
            osName = osName.toLowerCase();
            if (osName.contains("windows")) {
                return WINDOWS;
            } else if (osName.contains("mac") || osName.contains("darwin")) {
                return OSX;
            } else if (osName.contains("linux")) {
                return LINUX;
            } else if (osName.contains("sun") || osName.contains("solaris")) {
                return SOLARIS;
            }
        }
        
	return UNKNOWN;
    }

}
