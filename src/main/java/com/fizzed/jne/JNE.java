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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNE {

    static private final Logger log = LoggerFactory.getLogger(JNE.class);

    static private File TEMP_DIRECTORY;
    static private final ConcurrentHashMap<File, String> JAR_VERSION_HASHES = new ConcurrentHashMap<>();

    /**
     * Finds (extracts if necessary) a named executable for the runtime
     * operating system and architecture. The executable should be a regular
     * Java resource at the path /jne/[os]/[arch]/[exe]. The name of the file
     * will be automatically adjusted for the target platform. For example, on
     * Windows, to find the "cat" application, this method will actually search
     * for "cat.exe".
     *
     * @param name The executable name you would normally type on the
     * command-line. For example, "cat" or "ping" would search for "ping.exe" on
     * windows and "ping" on linux/mac.
     * @return The executable file or null if no executable found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findExecutable(String name) throws IOException {
        return findExecutable(name, null, null);
    }

    /**
     * Finds (extracts if necessary) a named executable for the runtime
     * operating system and architecture. The executable should be a regular
     * Java resource at the path /jne/[os]/[arch]/[exe]. The name of the file
     * will be automatically adjusted for the target platform. For example, on
     * Windows, to find the "cat" application, this method will actually search
     * for "cat.exe".
     *
     * @param name The executable name you would normally type on the
     * command-line. For example, "cat" or "ping" would search for "ping.exe" on
     * windows and "ping" on linux/mac.
     * @param targetName The executable name you would like the resource (if
     * found) to be named on extract.
     * @return The executable file or null if no executable found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findExecutable(String name, String targetName) throws IOException {
        return findExecutable(name, targetName, null);
    }

    /**
     * Finds (or extracts) a named executable for the runtime operating system
     * and architecture. The executable should be a regular Java resource at the
     * path /jne/[os]/[arch]/[exe].
     *
     * @param name The executable name you would normally type on the
     * command-line. For example, "cat" or "ping" would search for "ping.exe" on
     * windows and "ping" on linux/mac.
     * @param options The options to use when finding an executable. If null
     * then the default options will be used.
     * @return The executable file or null if no executable found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findExecutable(String name, Options options) throws IOException {
        return findExecutable(name, null, options);
    }

    /**
     * Finds (or extracts) a named executable for the runtime operating system
     * and architecture. The executable should be a regular Java resource at the
     * path /jne/[os]/[arch]/[exe].
     *
     * @param name The executable name you would normally type on the
     * command-line. For example, "cat" or "ping" would search for "ping.exe" on
     * windows and "ping" on linux/mac.
     * @param targetName The executable name you would like the resource (if
     * found) to be named on extract.
     * @param options The options to use when finding an executable. If null
     * then the default options will be used.
     * @return The executable file or null if no executable found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findExecutable(String name, String targetName, Options options) throws IOException {
        if (options == null) {
            options = Options.DEFAULT;
        }

        final NativeTarget nativeTarget = resolveNativeTarget(options);

        String fileName = nativeTarget.resolveExecutableFileName(name);

        String targetFileName = null;

        if (targetName != null) {
            targetFileName = nativeTarget.resolveExecutableFileName(targetName);
        }

        // always search for specific arch first
        File file = find(fileName, targetFileName, options, nativeTarget.getOperatingSystem(), nativeTarget.getHardwareArchitecture(), nativeTarget.getAbi());

        // for x64 fallback to x86 if an exe was not found
        if (file == null && options.isX32ExecutableFallback() && options.getHardwareArchitecture() == HardwareArchitecture.X64) {
            file = find(fileName, targetFileName, options, nativeTarget.getOperatingSystem(), HardwareArchitecture.X32, nativeTarget.getAbi());
        }

        return file;
    }

    /**
     * Same as findExecutable but throws an exception if the executable was not
     * found.
     */
    synchronized static public File requireExecutable(String name) throws IOException {
        return requireExecutable(name, null, null);
    }

    /**
     * Same as findExecutable but throws an exception if the executable was not
     * found.
     */
    synchronized static public File requireExecutable(String name, Options options) throws IOException {
        return requireExecutable(name, null, options);
    }

    /**
     * Same as findExecutable but throws an exception if the executable was not
     * found.
     */
    synchronized static public File requireExecutable(String name, String targetName, Options options) throws IOException {
        File file = findExecutable(name, targetName, options);
        if (file == null) {
            throw new ResourceNotFoundException("Resource executable " + name + " not found");
        }
        return file;
    }

    synchronized static public File findLibrary(String name) {
        return findLibrary(name, null);
    }

    synchronized static public File findLibrary(String name, Options options) {
        if (options == null) {
            options = Options.DEFAULT;
        }

        final NativeTarget nativeTarget = resolveNativeTarget(options);

        // file name to try and find/extract
        String fileName = nativeTarget.resolveLibraryFileName(name);

        try {
            // always search for specific arch first
            return find(fileName, null, options, nativeTarget.getOperatingSystem(), nativeTarget.getHardwareArchitecture(), nativeTarget.getAbi());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(e.getMessage());
        }
    }

    /**
     * <p>
     * Loads a dynamic library. Attempts to find (extracts if necessary) a named
     * library for the runtime operating system and architecture. If the library
     * was found as a resource and/or extracted, it will then be loaded via
     * System.load(). If the library was not found as a resource, this method
     * will simply fallback to System.loadLibrary(). Thus, this method should be
     * safe as a drop-in replacement for calls to System.loadLibrary().
     * </p>
     * <p>
     * If including the library as a Java resource, the resource path will be
     * /jne/[os]/[arch]/[lib]. The name of the file will be automatically
     * adjusted for the target platform. For example, on Windows, to find the
     * "cat" library, this method will search for "cat.dll". On Linux, to find
     * the "cat" library, this method will search for "libcat.so". On Mac, to
     * find the "cat" library, this method will search for "libcat.dylib".
     * </p>
     *
     * @param name The library name to find and load
     * @throws UnsatisfiedLinkError Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public void loadLibrary(String name) {
        loadLibrary(name, null);
    }

    /**
     * <p>
     * Loads a dynamic library. Attempts to find (extracts if necessary) a named
     * library for the runtime operating system and architecture. If the library
     * was found as a resource and/or extracted, it will then be loaded via
     * System.load(). If the library was not found as a resource, this method
     * will simply fallback to System.loadLibrary(). Thus, this method should be
     * safe as a drop-in replacement for calls to System.loadLibrary().
     * </p>
     * <p>
     * If including the library as a Java resource, the resource path will be
     * /jne/[os]/[arch]/[lib]. The name of the file will be automatically
     * adjusted for the target platform. For example, on Windows, to find the
     * "cat" library, this method will search for "cat.dll". On Linux, to find
     * the "cat" library, this method will search for "libcat.so". On Mac, to
     * find the "cat" library, this method will search for "libcat.dylib".
     * </p>
     *
     * @param name The library name to find and load
     * @param options The options to use when finding the library. If null then
     * the default options will be used.
     * @throws UnsatisfiedLinkError Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public void loadLibrary(String name, Options options) {
        // search for specific library
        File f = null;
        try {
            f = findLibrary(name, options);
        } catch (Exception e) {
            log.debug("Exception while finding library: {}", e.getMessage());
            throw new UnsatisfiedLinkError("Unable to cleanly find (or extract) library [" + name + "] as resource");
        }

        // temporarily prepend library path to load library if found
        if (f != null) {
            // since loading of dependencies of a library cannot dynamically happen
            // and the user would be required to provide a valid LD_LIBRARY_PATH when
            // launching the java process -- we don't need to do use loadLibrary
            // and can just tell it to load a specific library file
            String libraryPath = f.getAbsolutePath();
            log.trace("System.load({})", libraryPath);
            System.load(libraryPath);
            log.debug("Loaded library [{}] @ {}", name, libraryPath);
        } else {
            log.trace("Falling back to System.loadLibrary(" + name + ")");
            // fallback to java method
            System.loadLibrary(name);
            log.debug("Loaded library [{}]", name);
        }
    }

    /**
     * Finds (or extracts) a named file. Will first attempt to locate the file
     * for the runtime operating system and architecture, then fallback to just
     * the runtime operating system, and finally fallback to the resource
     * prefix. For example, a file named "resource.txt" running on a JVM on x64
     * linux would search the following 3 resource paths:
     *
     * /jne/linux/x64/resource.txt /jne/linux/resource.txt /jne/resource.txt
     *
     * @param name The file name to find or extract.
     * @return The file or null if no file found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findFile(String name) throws IOException {
        return JNE.findFile(name, null);
    }

    /**
     * Finds (or extracts) a named file. Will first attempt to locate the file
     * for the runtime operating system and architecture, then fallback to just
     * the runtime operating system, and finally fallback to the resource
     * prefix. For example, a file named "resource.txt" running on a JVM on x64
     * linux would search the following 3 resource paths:
     *
     * /jne/linux/x64/resource.txt /jne/linux/resource.txt /jne/resource.txt
     *
     * @param name The file name to find or extract.
     * @param options The options to use when finding an executable. If null
     * then the default options will be used.
     * @return The file or null if no file found.
     * @throws java.io.IOException
     * @throws ExtractException Thrown if a runtime exception occurs while
     * finding or extracting the executable.
     */
    synchronized static public File findFile(String name, Options options) throws IOException {
        if (options == null) {
            options = Options.DEFAULT;
        }

        final NativeTarget nativeTarget = resolveNativeTarget(options);

        // 1. try with os & arch
        File file = JNE.find(name, name, options, nativeTarget.getOperatingSystem(), nativeTarget.getHardwareArchitecture(), nativeTarget.getAbi());

        // 2. try with os & any arch
        if (file == null) {
            file = JNE.find(name, name, options, nativeTarget.getOperatingSystem(), null, nativeTarget.getAbi());
        }

        // 3. try with os & any arch
        if (file == null) {
            file = JNE.find(name, name, options, null, null, null);
        }

        return file;
    }

    /**
     * Same as findFile but throws an exception if the file was not found.
     */
    synchronized static public File requireFile(String name) throws IOException {
        return JNE.requireFile(name, null);
    }

    /**
     * Same as findFile but throws an exception if the file was not found.
     */
    synchronized static public File requireFile(String name, Options options) throws IOException {
        File file = findFile(name, options);
        if (file == null) {
            throw new ResourceNotFoundException("Resource file " + name + " not found");
        }
        return file;
    }

    /**
     * Underlying method used by findExecutable and loadLibrary to find and
     * extract executables as needed. Although public, it's NOT recommended to
     * use this method unless you know what you're doing.
     *
     * @param fileName
     * @param targetFileName
     * @param options
     * @param os
     * @param arch
     * @return
     * @throws IOException
     * @throws ExtractException
     */
    synchronized static public File find(String fileName, String targetFileName, Options options, OperatingSystem os, HardwareArchitecture arch, ABI abi) throws IOException {
        if (options == null) {
            options = Options.DEFAULT;
        }

        // a null os and arch now indicate an "any"
        /*if (os == null || os == OperatingSystem.UNKNOWN) {
            throw new ExtractException("Unable to detect operating system (e.g. Windows)");
        }

        if (arch == null || arch == HardwareArchitecture.UNKNOWN) {
            throw new ExtractException("Unable to detect hardware architecture (e.g. x86)");
        }*/

        if (targetFileName == null) {
            targetFileName = fileName;
        }

        log.trace("Finding fileName [" + fileName + "] targetFileName [" + targetFileName + "] os [" + os + "] arch [" + arch + "] abi [" + abi + "]...");

        // Full matrix of os + arch resources we will search for, in prioritized order
        final NativeTarget nativeTarget = NativeTarget.of(os, arch, abi);
        final List<String> resourcePaths = nativeTarget.resolveResourcePaths(options.getResourcePrefix(), fileName);
        URL url = null;
        for (String resourcePath : resourcePaths) {
            log.trace("Finding resource [" + resourcePath + "]");

            url = JNE.class.getResource(resourcePath);
            if (url != null) {
                break;      // we are done
            }
        }

        if (url == null) {
            log.debug("Unable to locate any resource of {}", resourcePaths);
            return null;
        }

        // support for "file" and "jar"
        log.trace("Resource found @ " + url);

        if (url.getProtocol().equals("jar")) {
            log.trace("Resource in jar; extracting file if necessary...");

            // in the case of where the app specifies an extract directory and
            // does not request deleteOnExit we need a way to detect if the 
            // executables changed from the previous app run -- we do this with
            // a very basic "hash" for an extracted resource. We basically combine
            // the path of the jar and manifest version of when the exe was extracted
            String versionHash = getJarVersionHashForResource(url);
            log.trace("Version hash [" + versionHash + "]");

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
                    throw new ExtractException("Extract dir [" + d + "] is not a directory");
                }
            }

            log.trace("Using dir [" + d + "]");

            // create both target exe and hash files
            File exeFile = new File(d, targetFileName);
            File exeHashFile = new File(exeFile.getAbsolutePath() + ".hash");

            // if file already exists verify its hash
            if (exeFile.exists()) {
                log.trace("File already exists; verifying if hash matches");
                // verify the version hash still matches
                if (!exeHashFile.exists()) {
                    // hash file missing -- we will force a new extract to be safe
                    exeFile.delete();
                } else {
                    // hash file exists, verify it matches what we expect
                    String existingHash = readFileToString(exeHashFile);
                    if (existingHash == null || !existingHash.equals(versionHash)) {
                        log.trace("Hash mismatch; deleting files; will freshly extract file");
                        // hash mismatch -- will force an overwrite of both files
                        exeFile.delete();
                        exeHashFile.delete();
                    } else {
                        log.trace("Hash matches; will use existing file");
                        // hash match (exeFile and exeHashFile are both perrrrfect)
                        //System.out.println("exe already extracted AND hash matched -- reusing same exe");
                        return exeFile;
                    }
                }
            }

            // does exe already exist? (previously extracted)
            if (!exeFile.exists()) {
                try {
                    log.trace("Extracting [" + url + "] to [" + exeFile + "]...");
                    extractTo(url, exeFile);

                    // set file to "executable"
                    log.trace("Setting to executable");
                    exeFile.setExecutable(true);

                    // create corrosponding hash file
                    log.trace("Writing hash file");
                    writeStringToFile(exeHashFile, versionHash);

                    // schedule files for deletion?
                    if (options.isCleanupExtracted()) {
                        log.trace("Scheduling file and hash for delete on exit");
                        exeFile.deleteOnExit();
                        exeHashFile.deleteOnExit();
                    }
                } catch (IOException e) {
                    log.debug("Failed to extract file: {}", e.getMessage());
                    throw new ExtractException("Unable to cleanly extract executable from jar", e);
                }
            }

            log.trace("Returning [" + exeFile + "]");
            return exeFile;
        } else if (url.getProtocol().equals("file")) {
            log.trace("Resource in file");
            try {
                File exeFile = new File(url.toURI());
                if (!exeFile.canExecute()) {
                    log.trace("Setting file to executable");
                    if (!exeFile.setExecutable(true)) {
                        log.debug("Unable to cleanly set file to executable");
                        throw new ExtractException("Executable was found but it cannot be set to execute [" + exeFile.getAbsolutePath() + "]");
                    }
                }
                log.trace("Returning [" + exeFile + "]");
                return exeFile;
            } catch (URISyntaxException e) {
                log.debug("URL syntax error");
                throw new ExtractException("Unable to create executable file from uri", e);
            }
        } else {
            throw new ExtractException("Unsupported executable resource protocol [" + url.getProtocol() + "]");
        }
    }

    static private void extractTo(URL url, File file) throws IOException {
        final InputStream in = url.openStream();
        try {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false))) {
                int len;
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) > -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    static private String readFileToString(File file) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > -1) {
                result.append(new String(buf, 0, len, "UTF-8"));
            }
        }
        return result.toString();
    }

    static private void writeStringToFile(File file, String s) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file, false)) {
            os.write(s.getBytes("UTF-8"));
            os.flush();
        }
    }

    static private String getJarVersionHashForResource(URL resource) throws IOException {
        // get the file that points to the underlying jar for this resource
        File jarFile = JarUtil.getJarFileForResource(resource);

        if (JAR_VERSION_HASHES.containsKey(jarFile)) {
            return JAR_VERSION_HASHES.get(jarFile);
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

            JAR_VERSION_HASHES.put(jarFile, hash);

            return hash;
        }
    }

    /**
     * Attempts to create a temporary directory that did not exist previously.
     */
    static private File getOrCreateTempDirectory(boolean deleteOnExit) throws ExtractException {
        // return the single instance if already created
        if ((TEMP_DIRECTORY != null) && TEMP_DIRECTORY.exists()) {
            return TEMP_DIRECTORY;
        }

        // use jvm supplied temp directory in case multiple jvms compete
//        try {
//            Path tempDirectory = Files.createTempDirectory("jne.");
//            File tempDirectoryAsFile = tempDirectory.toFile();
//            if (deleteOnExit) {
//                tempDirectoryAsFile.deleteOnExit();
//            }
//            return tempDirectoryAsFile;
//        } catch (IOException e) {
//            throw new ExtractException("Unable to create temporary dir", e);
//        }
        
        // use unique name to avoid race conditions
        try {
            Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path tempDirectory = baseDir.resolve("jne." + UUID.randomUUID().toString());
            Files.createDirectories(tempDirectory);
            File tempDirectoryAsFile = tempDirectory.toFile();
            if (deleteOnExit) {
                tempDirectoryAsFile.deleteOnExit();
            }
            // save temp directory so its only extracted once
            TEMP_DIRECTORY = tempDirectoryAsFile;
            return TEMP_DIRECTORY;
        } catch (IOException e) {
            throw new ExtractException("Unable to create temporary dir", e);
        }
    }

    static private OperatingSystem resolveOperatingSystem(Options options) {
        if (options != null && options.getOperatingSystem() != null) {
            return options.getOperatingSystem();
        }
        return PlatformInfo.detectOperatingSystem();
    }

    static private HardwareArchitecture resolveHardwareArchitecture(Options options) {
        if (options != null && options.getHardwareArchitecture() != null) {
            return options.getHardwareArchitecture();
        }
        return PlatformInfo.detectHardwareArchitecture();
    }

    static private ABI resolveAbi(Options options) {
        // to resolve abi, we need the os
        final OperatingSystem os = resolveOperatingSystem(options);

        if (options != null && options.getAbi() != null) {
            return options.getAbi();
        }

        return PlatformInfo.detectAbi(os);
    }

    static private NativeTarget resolveNativeTarget(Options options) {
        final OperatingSystem os = resolveOperatingSystem(options);
        final HardwareArchitecture arch = resolveHardwareArchitecture(options);
        final ABI abi = resolveAbi(options);
        return NativeTarget.of(os, arch, abi);
    }

}