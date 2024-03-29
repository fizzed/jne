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

import java.io.File;

public class Options {

    static public final Options DEFAULT = new Options();
    
    static public final String SYSPROP_DEBUG = "jne.debug";
    static public final String SYSPROP_RESOURCE_PREFIX = "jne.resource.prefix";
    static public final String SYSPROP_EXTRACT_DIR = "jne.extract.dir";
    static public final String SYSPROP_CLEANUP_EXTRACTED = "jne.cleanup.extracted";
    static public final String SYSPROP_X32_EXE_FALLBACK = "jne.x32.exe.fallback";
    
    private HardwareArchitecture hardwareArchitecture;
    private OperatingSystem operatingSystem;
    private ABI abi;
    private String resourcePrefix;
    private File extractDir;
    private boolean x32ExecutableFallback;
    private boolean cleanupExtracted;

    public Options() {
        // only if you need to override the auto detected values!
        this.operatingSystem = null;
        this.hardwareArchitecture = null;
        this.abi = null;
        this.resourcePrefix = System.getProperty(SYSPROP_RESOURCE_PREFIX, "/jne");
        this.extractDir = getSystemPropertyAsFile(SYSPROP_EXTRACT_DIR, null);
        this.x32ExecutableFallback = getSystemPropertyAsBoolean(SYSPROP_X32_EXE_FALLBACK, false);
        this.cleanupExtracted = getSystemPropertyAsBoolean(SYSPROP_CLEANUP_EXTRACTED, true);
    }

    public HardwareArchitecture getHardwareArchitecture() {
        return hardwareArchitecture;
    }

    public Options setHardwareArchitecture(HardwareArchitecture hardwareArchitecture) {
        this.hardwareArchitecture = hardwareArchitecture;
        return this;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public Options setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public ABI getAbi() {
        return abi;
    }

    public Options setAbi(ABI abi) {
        this.abi = abi;
        return this;
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    /**
     * Sets the prefix of the resource to being search from. Defaults to
     * "/jne".
     * @param resourcePrefix The prefix of the resource to search from
     */
    public Options setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
        return this;
    }

    public File getExtractDir() {
        return extractDir;
    }

    /**
     * Sets the directory an executable will be extracted to.  If
     * null, a one-time use temporary directory will be created and used
     * for extracted executables. Defaults to null.
     * @param extractDir The directory to extract files to
     */
    public Options setExtractDir(File extractDir) {
        this.extractDir = extractDir;
        return this;
    }

    public boolean isX32ExecutableFallback() {
        return x32ExecutableFallback;
    }

    /**
     * If an executable is not found on an x64 platform whether a fallback
     * search will occur for an x32 executable. Defaults to true.
     * @param x32ExecutableFallback If an x32 executable will be searched for
     *      on an x64 platform if an x64 version is not found.
     */
    public Options setX32ExecutableFallback(boolean x32ExecutableFallback) {
        this.x32ExecutableFallback = x32ExecutableFallback;
        return this;
    }

    public boolean isCleanupExtracted() {
        return cleanupExtracted;
    }

    /**
     * Sets whether extracted files will be scheduled for deletion on VM
     * exit via (File.deleteOnExit()). Defaults to true.
     * @param cleanupExtracted  If true then extracted files will be scheduled
     *      for delete on VM exit.
     */
    public Options setCleanupExtracted(boolean cleanupExtracted) {
        this.cleanupExtracted = cleanupExtracted;
        return this;
    }

    static private File getSystemPropertyAsFile(String key, File defaultValue) {
        String v = System.getProperty(key);
        if (v != null && !v.equals("")) {
            return new File(v);
        } else {
            return defaultValue;
        }
    }
    
    static private boolean getSystemPropertyAsBoolean(String key, boolean defaultValue) {
        String v = System.getProperty(key);
        if (v != null) {
            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("1")) {
                return true;
            } else if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("0")) {
                return false;
            } else {
                throw new IllegalArgumentException("Invalid boolean value for system property [" + key + "]");
            }
        } else {
            return defaultValue;
        }
    }
    
}
