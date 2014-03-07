/*
 * Copyright 2014 mfizz.
 *
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
 */
package com.mfizz.jne;

/*
 * #%L
 * mfz-jne
 * %%
 * Copyright (C) 2012 - 2014 mfizz
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joelauer
 */
public class JNE {
    
    public static class Options {
        
        private File extractDir;
        private boolean x86FallbackEnabled;
        private boolean deleteExtractedOnExit;
        
        public Options() {
            this.extractDir = null;
            this.x86FallbackEnabled = true;
            this.deleteExtractedOnExit = true;
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
        public void setExtractDir(File extractDir) {
            this.extractDir = extractDir;
        }

        public boolean isX86FallbackEnabled() {
            return x86FallbackEnabled;
        }

        /**
         * If an executable is not found on an x64 platform whether a fallback
         * search will occur for an x86 executable. Defaults to true.
         * @param x86FallbackEnabled If an x86 will be searched for on an x64
         *      platform if an x64 version is not found.
         */
        public void setX86FallbackEnabled(boolean x86FallbackEnabled) {
            this.x86FallbackEnabled = x86FallbackEnabled;
        }

        public boolean isDeleteExtractedOnExit() {
            return deleteExtractedOnExit;
        }

        /**
         * Sets whether extracted files will be scheduled for deletion on VM
         * exit via (File.deleteOnExit()). Defaults to true.
         * @param deleteExtractedOnExit  If true files scheduled for delete on
         *      VM exit.
         */
        public void setDeleteExtractedOnExit(boolean deleteExtractedOnExit) {
            this.deleteExtractedOnExit = deleteExtractedOnExit;
        }
        
    }
    
    private static final int TEMP_DIR_ATTEMPTS = 100;
    private static File _tempDir;
    public static Options DEFAULT_OPTIONS = new Options();
    private static ConcurrentHashMap<File,String> jarVersionHashes = new ConcurrentHashMap<File,String>();
 
    /**
     * Finds (or extracts) a named executable for the runtime operating system
     * and architecture. The executable should be a regular Java resource at
     * the path /jne/[os]/[arch]/[exe].
     * @param name The executable name you would normally type on the command-line.
     *      For example, "cat" or "ping" would search for "ping.exe" on windows and "ping" on linux/mac.
     * @return The executable file or null if no executable found.
     * @throws NativeExecutableException Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public File find(String name) throws IOException, NativeExecutableException {
        return find(name, DEFAULT_OPTIONS);
    }
    
    /**
     * Finds (or extracts) a named executable for the runtime operating system
     * and architecture. The executable should be a regular Java resource at
     * the path /jne/[os]/[arch]/[exe].
     * @param name The executable name you would normally type on the command-line.
     *      For example, "cat" or "ping" would search for "ping.exe" on windows and "ping" on linux/mac.
     * @param options The options to use when finding an executable. If null then
     *      the default options will be used.
     * @return The executable file or null if no executable found.
     * @throws NativeExecutableException Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public File find(String name, Options options) throws IOException, NativeExecutableException {
        // get current os and arch
        OS os = OS.getOS();
        Arch arch = Arch.getArch();
        
        // always search for specific arch first
        File f = doFind(name, os, arch, options);
        
        // for x64 fallback to x86 if an exe was not found
        if (f == null && options.isX86FallbackEnabled() && arch == Arch.X64) {
            f = doFind(name, os, Arch.X86, options);
        }
        
        return f;
    }
    
    static private File doFind(String name, OS os, Arch arch, Options options) throws IOException, NativeExecutableException {
        if (options == null) {
            options = DEFAULT_OPTIONS;
        }
        
        if (os == null || os == OS.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect operating system (e.g. Windows)");
        }
        
        if (arch == null || arch == Arch.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect hardware architecture (e.g. x86)");
        }
        
        // adjust executable name for windows
        String exeName = name;
        if (os == OS.WINDOWS) {
            exeName += ".exe";
        }
        
        String resourcePath = "/jne/" + os.name().toLowerCase() + "/" + arch.name().toLowerCase() + "/" + exeName;
    
        URL url = JNE.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        
        // support for "file" and "jar"
        if (url.getProtocol().equals("jar")) {
            // in the case of where the app specifies an extract directory and
            // does not request deleteOnExit we need a way to detect if the 
            // executables changed from the previous app run -- we do this with
            // a very basic "hash" for an extracted resource. We basically combine
            // the path of the jar and manifest version of when the exe was extracted
            String versionHash = getJarVersionHashForResource(url);
            
            // where should we extract the executable?
            File d = options.getExtractDir();
            if (d == null) {
                d = getOrCreateTempDirectory(options.isDeleteExtractedOnExit());
            }
            
            // create both exe and hash files
            File exeFile = new File(d, exeName);
            File exeHashFile = new File(exeFile.getAbsolutePath() + ".hash");
            
            // if we aren't using a 1-time temp dir then verify the exe hash matches
            if (options.getExtractDir() != null && exeFile.exists()) {
                // verify the version hash still matches
                if (!exeHashFile.exists()) {
                    // hash file missing -- we will force a new extract to be safe
                    exeFile.delete();
                } else {
                    // hash file exists, verify it matches what we expect
                    String existingHash = readFileToString(exeHashFile);
                    if (existingHash == null || !existingHash.equals(versionHash)) {
                        // hash mismatch -- will force an overwrite of both files
                        exeFile.delete();
                        exeHashFile.delete();
                    } else {
                        // hash match (exeFile and exeHashFile are both perrrrfect)
                        //System.out.println("exe already extracted AND hash matched -- reusing same exe");
                        return exeFile;
                    }
                }
            }
            
            // does exe already exist? (previously extracted)
            if (!exeFile.exists()) {
                try {
                    extractTo(url, exeFile);
                    
                    // set file to "executable"
                    exeFile.setExecutable(true);
                    
                    // create corrosponding hash file
                    writeStringToFile(exeHashFile, versionHash);
                    
                    // schedule files for deletion?
                    if (options.isDeleteExtractedOnExit()) {
                        exeFile.deleteOnExit();
                        exeHashFile.deleteOnExit();
                    }
                } catch (IOException e) {
                    throw new NativeExecutableException("Unable to cleanly extract executable from jar", e);
                }
            }
            
            return exeFile;
        } else if (url.getProtocol().equals("file")) {
            try {
                File exeFile = new File(url.toURI());
                if (!exeFile.canExecute()) {
                    if (!exeFile.setExecutable(true)) {
                        throw new NativeExecutableException("Executable was found but it cannot be set to execute [" + exeFile.getAbsolutePath() + "]");
                    }
                }
                return exeFile;
            } catch (URISyntaxException e) {
                throw new NativeExecutableException("Unable to create executable file from uri", e);
            }
        } else {
            throw new NativeExecutableException("Unsupported executable resource protocol [" + url.getProtocol() + "]");
        }
    }
    
    static private void extractTo(URL url, File file) throws IOException {
        final InputStream in = url.openStream();
        try {
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false));
            try {
                int len;
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) > -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    static private String readFileToString(File file) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > -1) {
                result.append(new String(buf, 0, len, "UTF-8"));
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return result.toString();
    }
    
    static private void writeStringToFile(File file, String s) throws IOException {
        FileOutputStream os = new FileOutputStream(file, false);
        try {
            os.write(s.getBytes("UTF-8"));
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }
    
    static private String getJarVersionHashForResource(URL resource) throws IOException {
        // get the file that points to the underlying jar for this resource
        File jarFile = JarUtil.getJarFileForResource(resource);
        
        if (jarVersionHashes.containsKey(jarFile)) {
            return jarVersionHashes.get(jarFile);
        } else {
            // calculate new hash for jar
            String manifestVersion = JarUtil.getManifestVersionNumber(jarFile);
            
            StringBuilder hashBuilder = new StringBuilder();
            hashBuilder.append("file:");
            hashBuilder.append(jarFile.getAbsolutePath());
            hashBuilder.append("|last_modified:");
            hashBuilder.append(jarFile.lastModified());
            hashBuilder.append("|version:");
            hashBuilder.append(manifestVersion);
            
            String hash = hashBuilder.toString();
            
            jarVersionHashes.put(jarFile, hash);
            
            return hash;
        }
    }
    
    
    /**
     * Attempts to create a temporary directory that did not exist previously.
     */
    static private File getOrCreateTempDirectory(boolean deleteOnExit) throws NativeExecutableException {
        // return the single instance if already created
        if (_tempDir != null) {
            return _tempDir;
        }
        
	File baseDir = new File(System.getProperty("java.io.tmpdir"));
	String baseName = System.currentTimeMillis() + "-";
	
	for (int counter = 0; counter < 100; counter++) {
	    File d = new File(baseDir, baseName + counter);
	    if (d.mkdir()) {
                // schedule this directory to be deleted on exit
                if (deleteOnExit) {
                    d.deleteOnExit();
                }
                _tempDir = d;
		return d;
	    }
	}
        
	throw new NativeExecutableException("Failed to create temporary directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }
    
}
