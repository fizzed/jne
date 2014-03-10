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
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author joelauer
 */
public class JNE {
    
    static public final String SYSPROP_DEBUG = "jne.debug";
    static public final String SYSPROP_RESOURCE_PREFIX = "jne.resource.prefix";
    static public final String SYSPROP_EXTRACT_DIR = "jne.extract.dir";
    static public final String SYSPROP_CLEANUP_EXTRACTED = "jne.cleanup.extracted";
    static public final String SYSPROP_X32_EXE_FALLBACK = "jne.x32.exe.fallback";
    
    public static enum FindType {
        EXECUTABLE,
        LIBRARY,
        FILE
    }
    
    public static class Options {
        
        private String resourcePrefix;
        private File extractDir;
        private boolean x32ExecutableFallback;
        private boolean cleanupExtracted;
        
        public Options() {
            // defaults
            this.resourcePrefix = System.getProperty(SYSPROP_RESOURCE_PREFIX, "/jne");
            this.extractDir = getSystemPropertyAsFile(SYSPROP_EXTRACT_DIR, null);
            this.x32ExecutableFallback = getSystemPropertyAsBoolean(SYSPROP_X32_EXE_FALLBACK, true);
            this.cleanupExtracted = getSystemPropertyAsBoolean(SYSPROP_CLEANUP_EXTRACTED, true);
        }

        public String getResourcePrefix() {
            return resourcePrefix;
        }

        /**
         * Sets the prefix of the resource to being search from. Defaults to
         * "/jne".
         * @param resourcePrefix The prefix of the resource to search from
         */
        public void setResourcePrefix(String resourcePrefix) {
            this.resourcePrefix = resourcePrefix;
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

        public boolean isX32ExecutableFallback() {
            return x32ExecutableFallback;
        }

        /**
         * If an executable is not found on an x64 platform whether a fallback
         * search will occur for an x32 executable. Defaults to true.
         * @param x32ExecutableFallback If an x32 executable will be searched for
         *      on an x64 platform if an x64 version is not found.
         */
        public void setX32ExecutableFallback(boolean x32ExecutableFallback) {
            this.x32ExecutableFallback = x32ExecutableFallback;
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
        public void setCleanupExtracted(boolean cleanupExtracted) {
            this.cleanupExtracted = cleanupExtracted;
        }
        
        public String createExecutableName(String name, OS os) {
            // adjust executable name for windows
            if (os == OS.WINDOWS) {
                return name + ".exe";
            } else {
                return name;
            }
        }

        public String createLibraryName(String name, OS os, Integer majorVersion, Integer minorVersion, Integer revisionVersion) {
            // adjust executable name for windows
            if (os == OS.WINDOWS) {
                return name + ".dll";
            } else if (os == OS.LINUX) {
                // build up the name of the file we want to load
                String soname = "lib" + name + ".so";
                if (majorVersion != null) {
                    soname += "." + majorVersion;
                    if (minorVersion != null) {
                        soname += "." + minorVersion;
                        if (revisionVersion != null) {
                            soname += "." + revisionVersion;
                        }  
                    }
                }            
                return soname;
            } else if (os == OS.MAC) {
                return "lib" + name + ".dylib";
            } else {
                return name;
            }
        }
        
        public String createResourcePath(OS os, Arch arch, String name) {
            StringBuilder s = new StringBuilder();
            s.append(getResourcePrefix());
            s.append("/");
            s.append(os.name().toLowerCase());
            s.append("/");
            // only append arch if its not null and not any...
            if (arch != null && arch != Arch.ANY) {
                s.append(arch.name().toLowerCase());
                s.append("/");
            }
            s.append(name);
            return s.toString();
        }
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
    
    static public boolean DEBUG = getSystemPropertyAsBoolean(SYSPROP_DEBUG, false);
    static private final int TEMP_DIR_ATTEMPTS = 100;
    static private File _tempDir;
    static public Options DEFAULT_OPTIONS = new Options();
    static private ConcurrentHashMap<File,String> jarVersionHashes = new ConcurrentHashMap<File,String>();
 
    /**
     * Finds (extracts if necessary) a named executable for the runtime operating system
     * and architecture. The executable should be a regular Java resource at
     * the path /jne/[os]/[arch]/[exe]. The name of the file will be automatically
     * adjusted for the target platform. For example, on Windows, to find the "cat"
     * application, this method will actually search for "cat.exe".
     * @param name The executable name you would normally type on the command-line.
     *      For example, "cat" or "ping" would search for "ping.exe" on windows and "ping" on linux/mac.
     * @param findType The type of file to find. For example, on Windows, searching
     *      for an EXECUTABLE will result in a search of "name.exe". 
     * @return The executable file or null if no executable found.
     * @throws NativeExecutableException Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public File findExecutable(String name) throws IOException, NativeExecutableException {
        return findExecutable(name, DEFAULT_OPTIONS);
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
    synchronized static public File findExecutable(String name, Options options) throws IOException, NativeExecutableException {
        // get current os and arch
        OS os = OS.getOS();
        Arch arch = Arch.getArch();
        
        String fileName = options.createExecutableName(name, os);
        
        // always search for specific arch first
        File f = find(fileName, options, os, arch);
        
        // for x64 fallback to x86 if an exe was not found
        if (f == null && options.isX32ExecutableFallback() && arch == Arch.X64) {
            f = find(fileName, options, os, Arch.X32);
        }
        
        return f;
    }
    
    
    synchronized static public File findLibrary(String name) {
        return findLibrary(name, null, null);
    }
    
    
    synchronized static public File findLibrary(String name, Integer majorVersion) {
        return findLibrary(name, null, majorVersion);
    }
    
    
    synchronized static public File findLibrary(String name, Options options, Integer majorVersion) {
        // get current os and arch
        OS os = OS.getOS();
        Arch arch = Arch.getArch();
        
        if (options == null) {
            options = DEFAULT_OPTIONS;
        }
        
        // file name to try and find/extract (only support major for now since linux 99% of the time links to major)
        String fileName = options.createLibraryName(name, os, majorVersion, null, null);
        
        try {
            // always search for specific arch first
            return find(fileName, options, os, arch);
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(e.getMessage());
        } catch (NativeExecutableException e) {
            throw new UnsatisfiedLinkError(e.getMessage());
        }
    }
    
    
    /**
     * <p>
     * Loads a dynamic library. Attempts to find (extracts if necessary)
     * a named library for the runtime operating system and architecture.  If
     * the library was found as a resource and/or extracted, it will then be
     * loaded via System.load().  If the library was not found as a resource,
     * this method will simply fallback to System.loadLibrary(). Thus, this
     * method should be safe as a drop-in replacement for calls to System.loadLibrary().
     * </p>
     * <p>
     * If including the library as a Java resource, the resource path will be
     * /jne/[os]/[arch]/[lib]. The name of the file will be automatically
     * adjusted for the target platform. For example, on Windows, to find the "cat"
     * library, this method will search for "cat.dll". On Linux, to find
     * the "cat" library, this method will search for "libcat.so". On Mac, to
     * find the "cat" library, this method will search for "libcat.dylib".
     * </p>
     * @param name The library name to find and load
     * @throws UnsatisfiedLinkError Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public void loadLibrary(String name) {
        loadLibrary(name, null, null);
    }
    
    
    synchronized static public void loadLibrary(String name, Integer majorVersion) {
        loadLibrary(name, null, majorVersion);
    }
    
    
    /**
     * <p>
     * Loads a dynamic library. Attempts to find (extracts if necessary)
     * a named library for the runtime operating system and architecture.  If
     * the library was found as a resource and/or extracted, it will then be
     * loaded via System.load().  If the library was not found as a resource,
     * this method will simply fallback to System.loadLibrary(). Thus, this
     * method should be safe as a drop-in replacement for calls to System.loadLibrary().
     * </p>
     * <p>
     * If including the library as a Java resource, the resource path will be
     * /jne/[os]/[arch]/[lib]. The name of the file will be automatically
     * adjusted for the target platform. For example, on Windows, to find the "cat"
     * library, this method will search for "cat.dll". On Linux, to find
     * the "cat" library, this method will search for "libcat.so". On Mac, to
     * find the "cat" library, this method will search for "libcat.dylib".
     * </p>
     * @param name The library name to find and load
     * @param options The options to use when finding the library. If null then
     *      the default options will be used.
     * @throws UnsatisfiedLinkError Thrown if a runtime exception occurs while
     *      finding or extracting the executable.
     */
    synchronized static public void loadLibrary(String name, Options options, Integer majorVersion) {
        // search for specific library
        File f = null;
        try {
            f = findLibrary(name, options, majorVersion);
        } catch (Exception e) {
            debug("JNE: exception while finding library: " + e.getMessage());
            throw new UnsatisfiedLinkError("Unable to cleanly find (or extract) library [" + name + "] as resource");
        }
        
        // temporarily prepend library path to load library if found
        if (f != null) {
            //System.load(f.getAbsolutePath());
            debug("JNE: modifying java.library.path to load [" + f + "]");
            try {
                String initialLibraryPath = System.getProperty("java.library.path");
                try {
                    // prepend out path to library (so they are loaded first)
                    setJavaLibraryPath(f.getParentFile().getAbsolutePath() + ":" + initialLibraryPath);
                    System.loadLibrary(name);
                } finally {
                    // set java.library.path back to the original value
                    setJavaLibraryPath(initialLibraryPath);
                }
            } catch (IllegalAccessException e) {
                throw new UnsatisfiedLinkError(e.getMessage());
            }
        } else {
            debug("JNE: falling back to System.loadLibrary(" + name + ")");
            // fallback to java method
            System.loadLibrary(name);
        }
        
        debug("JNE: library [" + name + "] loaded!");
    }
    
    
    /**
     * Set the java.library.path System variable.
     * ADAPTED FROM: http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
     */
    static private void setJavaLibraryPath(String path) throws IllegalAccessException {
	try {
	    System.setProperty("java.library.path", path);
	    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
	    fieldSysPath.setAccessible(true);
	    fieldSysPath.set(null, null);
	    debug("New java.library.path = "+System.getProperty("java.library.path"));
        } catch (SecurityException e) {
            throw e;
	} catch (Exception e) {
	    debug("This shouldn't happen. sys_paths is always present in ClassLoader");
	}
    }

    
    /**
     * Underlying method used by findExecutable and loadLibrary to find and
     * extract executables as needed. Although public, it's NOT recommended
     * to use this method unless you know what you're doing.
     * @param name
     * @param findType
     * @param options
     * @param os
     * @param arch
     * @return
     * @throws IOException
     * @throws NativeExecutableException 
     */
    synchronized static public File find(String fileName, Options options, OS os, Arch arch) throws IOException, NativeExecutableException {
        if (options == null) {
            options = DEFAULT_OPTIONS;
        }
        
        if (os == null || os == OS.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect operating system (e.g. Windows)");
        }
        
        if (arch == null || arch == Arch.UNKNOWN) {
            throw new NativeExecutableException("Unable to detect hardware architecture (e.g. x86)");
        }
        
        if (DEBUG) {
            debug("JNE: finding fileName [" + fileName + "] os [" + os + "] arch [" + arch + "]...");
        }
        
        //String resourcePath = options.getResourcePrefix() + "/" + os.name().toLowerCase() + "/" + arch.name().toLowerCase() + "/" + name;
        String resourcePath = options.createResourcePath(os, arch, fileName);
        
        debug("JNE: finding resource [" + resourcePath + "]");
        
        URL url = JNE.class.getResource(resourcePath);
        if (url == null) {
            debug("JNE: resource [" + resourcePath + "] not found");
            return null;
        }
        
        // support for "file" and "jar"
        debug("JNE: resource found @ " + url);
        if (url.getProtocol().equals("jar")) {
            debug("JNE: resource in jar; extracting file if necessary...");
            
            // in the case of where the app specifies an extract directory and
            // does not request deleteOnExit we need a way to detect if the 
            // executables changed from the previous app run -- we do this with
            // a very basic "hash" for an extracted resource. We basically combine
            // the path of the jar and manifest version of when the exe was extracted
            String versionHash = getJarVersionHashForResource(url);
            debug("JNE: version hash [" + versionHash + "]");
            
            // where should we extract the executable?
            File d = options.getExtractDir();
            if (d == null) {
                d = getOrCreateTempDirectory(options.isCleanupExtracted());
            } else {
                // does the extract dir exist?
                if (!d.exists()) {
                    d.mkdirs();
                }
                if (!d.isDirectory()) {
                    throw new NativeExecutableException("Extract dir [" + d + "] is not a directory");
                }
            }
            
            debug("JNE: using dir [" + d + "]");
            
            // create both exe and hash files
            File exeFile = new File(d, fileName);
            File exeHashFile = new File(exeFile.getAbsolutePath() + ".hash");
            
            // if file already exists verify its hash
            if (exeFile.exists()) {
                debug("JNE: file already exists; verifying if hash matches");
                // verify the version hash still matches
                if (!exeHashFile.exists()) {
                    // hash file missing -- we will force a new extract to be safe
                    exeFile.delete();
                } else {
                    // hash file exists, verify it matches what we expect
                    String existingHash = readFileToString(exeHashFile);
                    if (existingHash == null || !existingHash.equals(versionHash)) {
                        debug("JNE: hash mismatch; deleting files; will freshly extract file");
                        // hash mismatch -- will force an overwrite of both files
                        exeFile.delete();
                        exeHashFile.delete();
                    } else {
                        debug("JNE: hash matches; will use existing file");
                        // hash match (exeFile and exeHashFile are both perrrrfect)
                        //System.out.println("exe already extracted AND hash matched -- reusing same exe");
                        return exeFile;
                    }
                }
            }
            
            // does exe already exist? (previously extracted)
            if (!exeFile.exists()) {
                try {
                    debug("JNE: extracting [" + url + "] to [" + exeFile + "]...");
                    extractTo(url, exeFile);
                    
                    // set file to "executable"
                    debug("JNE: setting to executable");
                    exeFile.setExecutable(true);
                    
                    // create corrosponding hash file
                    debug("JNE: writing hash file");
                    writeStringToFile(exeHashFile, versionHash);
                    
                    // schedule files for deletion?
                    if (options.isCleanupExtracted()) {
                        debug("JNE: scheduling file and hash for delete on exit");
                        exeFile.deleteOnExit();
                        exeHashFile.deleteOnExit();
                    }
                } catch (IOException e) {
                    debug("JNE: failed to extract file");
                    throw new NativeExecutableException("Unable to cleanly extract executable from jar", e);
                }
            }
            
            debug("JNE: returning [" + exeFile + "]");
            return exeFile;
        } else if (url.getProtocol().equals("file")) {
            debug("JNE: resource in file");
            try {
                File exeFile = new File(url.toURI());
                if (!exeFile.canExecute()) {
                    debug("JNE: setting file to executable");
                    if (!exeFile.setExecutable(true)) {
                        debug("JNE: unable to cleanly set file to executable");
                        throw new NativeExecutableException("Executable was found but it cannot be set to execute [" + exeFile.getAbsolutePath() + "]");
                    }
                }
                debug("JNE: returning [" + exeFile + "]");
                return exeFile;
            } catch (URISyntaxException e) {
                debug("JNE: uri syntax error");
                throw new NativeExecutableException("Unable to create executable file from uri", e);
            }
        } else {
            throw new NativeExecutableException("Unsupported executable resource protocol [" + url.getProtocol() + "]");
        }
    }
    
    static private void debug(String line) {
        if (DEBUG) {
            System.out.println(line);
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
