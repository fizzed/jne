package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JavaHomes {
    static private final Logger log = LoggerFactory.getLogger(JavaHome.class);

    public interface ReleasePropertiesProvider {

        boolean asFallbackOnly();

        Map<String,String> apply(Path javaHomeDir, Path javaExeFile) throws IOException;

    }

    static public final ReleasePropertiesProvider EXECUTE_JAVA_VERSION_RELEASE_PROPERTIES_PROVIDER = new ReleasePropertiesProvider() {

        @Override
        public boolean asFallbackOnly() {
            return true;
        }

        @Override
        public Map<String, String> apply(Path javaHomeDir, Path javaExeFile) throws IOException {
            // otherwise, we could do "java -version" to try and detect it
            try {
                String versionOutput = executeJavaVersion(javaExeFile);

                return readJavaVersionOutput(versionOutput);
            } catch (Exception e) {
                throw new IOException("Unable to execute -version command on " + javaExeFile, e);
            }
        }
    };

    static public final ReleasePropertiesProvider CURRENT_JVM_RELEASE_PROPERTIES_PROVIDER = new ReleasePropertiesProvider() {

        @Override
        public boolean asFallbackOnly() {
            return false;
        }

        @Override
        public Map<String, String> apply(Path javaHomeDir, Path javaExeFile) throws IOException {
            final Map<String,String> releaseProperties = new HashMap<>();

            // java.version -> 11.0.17                      java.version -> 1.8.0_352
            // java.runtime.version -> 11.0.17+8-LTS
            // java.vendor -> Azul Systems, Inc.            java.vendor -> Azul Systems, Inc.
            // java.vm.vendor -> Azul Systems, Inc.
            // java.vendor.version -> Zulu11.60+19-CA
            // os.name -> Linux
            // os.arch -> amd64
            // java.vendor.url -> http://www.azul.com/
            // java.version.date -> 2022-10-18
            String javaVersion = System.getProperty("java.version");
            if (javaVersion != null) {
                releaseProperties.put("JAVA_VERSION", javaVersion);
            }

            String javaVendor = System.getProperty("java.vendor");
            if (javaVendor != null) {
                releaseProperties.put("IMPLEMENTOR", javaVendor);
            }

            String javaVendorVersion = System.getProperty("java.vendor.version");
            if (javaVendorVersion != null) {
                releaseProperties.put("IMPLEMENTOR_VERSION", javaVendorVersion);
            }

            String osName = System.getProperty("os.name");
            if (osName != null) {
                releaseProperties.put("OS_NAME", osName);
            }

            String osArch = System.getProperty("os.arch");
            if (osArch != null) {
                releaseProperties.put("OS_ARCH", osArch);
            }

            return releaseProperties;
        }
    };

    static public JavaHome fromDirectory(Path javaHomeDir) throws IOException {
        return fromDirectory(javaHomeDir, false);
    }

    static public JavaHome fromDirectory(Path javaHomeDir, boolean requireReleaseFile) throws IOException {
        return fromDirectory(javaHomeDir, requireReleaseFile, EXECUTE_JAVA_VERSION_RELEASE_PROPERTIES_PROVIDER);
    }

    static public JavaHome fromDirectory(Path javaHomeDir, boolean requireReleaseFile, ReleasePropertiesProvider releasePropertiesFallbackProvider) throws IOException {
        if (!Files.isDirectory(javaHomeDir)) {
            throw new FileNotFoundException("Java home directory " + javaHomeDir + " does not exist");
        }

        final OperatingSystem thisOs = PlatformInfo.detectOperatingSystem();

        // Test #1: bin/java exists?
        final String javaExeFileName = NativeTarget.resolveExecutableFileName(thisOs, "java");
        Path javaExeFile = javaHomeDir.resolve("bin").resolve(javaExeFileName);

        if (!Files.isRegularFile(javaExeFile)) {
            throw new FileNotFoundException("Java executable " + javaExeFile + " was not found in " + javaHomeDir);
        }

        // For old Java 8, sometimes we're in the "jre" directory, where we really need to probe the directory above
        if ("jre".equalsIgnoreCase(javaHomeDir.getFileName().toString())) {
            Path jdkHomeDir = javaHomeDir.getParent();
            Path javaExeFileAlt = jdkHomeDir.resolve("bin").resolve(javaExeFileName);
            if (Files.isRegularFile(javaExeFileAlt)) {
                javaHomeDir = jdkHomeDir;
                javaExeFile = javaExeFileAlt;
            }
        }

        // Test #2: sometimes we can find a bin/java, especially if we're dealing with a directory simply on the PATH
        // we need to make sure this directory "looks" like a typical java home, with a "lib" dir, etc.
        final Path javaLibDir = javaHomeDir.resolve("lib");
        if (!Files.isDirectory(javaLibDir)) {
            throw new FileNotFoundException("Java lib directory " + javaLibDir + " was not found in " + javaHomeDir);
        }

        // Test #3: must also have jmods and/or a jre
        final Path jmodsDir = javaHomeDir.resolve("jmods");
        if (!Files.isDirectory(jmodsDir)) {
            final Path jreDir = javaHomeDir.resolve("jre");
            if (!Files.isDirectory(jreDir)) {
                final Path rtJarFile = javaLibDir.resolve("rt.jar");
                if (!Files.exists(rtJarFile)) {
                    throw new FileNotFoundException("Java jmods/jre directory not found in " + javaHomeDir);
                }
            }
        }

        final String javacExeFileName = NativeTarget.resolveExecutableFileName(thisOs, "javac");
        Path javacExeFile = javaHomeDir.resolve("bin").resolve(javacExeFileName);
        if (!Files.isRegularFile(javacExeFile)) {
            javacExeFile = null;
        }

        final String nativeImageExeFileName = NativeTarget.resolveExecutableFileName(thisOs, "native-image");
        Path nativeImageExeFile = javaHomeDir.resolve("bin").resolve(nativeImageExeFileName);
        if (!Files.isRegularFile(nativeImageExeFile)) {
            nativeImageExeFile = null;
        }

        JavaVersion version = null;
        String vendor = null;
        String implementorVersion = null;
        OperatingSystem operatingSystem = null;
        HardwareArchitecture hardwareArchitecture = null;
        ABI abi = null;
        Map<String,String> releaseProperties = null;
        JavaDistribution distro;

        // Test #3: release file w/ java home (it contains valuable info)
        final Path releaseFile = javaHomeDir.resolve("release");
        if (Files.isRegularFile(releaseFile)) {
            releaseProperties = JavaHomes.readReleaseProperties(releaseFile);
        }

        if (releaseProperties == null) {
            if (requireReleaseFile) {
                throw new FileNotFoundException("Java release file " + releaseFile + " was not found in " + javaHomeDir);
            }
            if (releasePropertiesFallbackProvider != null) {
                releaseProperties = releasePropertiesFallbackProvider.apply(javaHomeDir, javaExeFile);
            }
        } else if (releasePropertiesFallbackProvider != null && !releasePropertiesFallbackProvider.asFallbackOnly()) {
            Map<String,String> extraReleaseProperties = releasePropertiesFallbackProvider.apply(javaHomeDir, javaExeFile);
            // merge those into the existing
            if (extraReleaseProperties != null) {
                Map<String, String> finalReleaseProperties = releaseProperties;
                extraReleaseProperties.forEach((k, v) -> {
                    if (!finalReleaseProperties.containsKey(k)) {
                        finalReleaseProperties.put(k, v);
                    }
                });
            }
        }

        if (releasePropertiesFallbackProvider != null && (releaseProperties == null || !releasePropertiesFallbackProvider.asFallbackOnly())) {
            releaseProperties = releasePropertiesFallbackProvider.apply(javaHomeDir, javaExeFile);
        }

        String releaseJavaVersion = releaseProperties.get("JAVA_VERSION");
        if (releaseJavaVersion != null) {
            version = JavaVersion.parse(releaseJavaVersion);
        }

        String releaseOs = releaseProperties.get("OS_NAME");
        if (releaseOs != null) {
            operatingSystem = OperatingSystem.resolve(releaseOs);
        }

        String releaseArch = releaseProperties.get("OS_ARCH");
        if (releaseArch != null) {
            hardwareArchitecture = HardwareArchitecture.resolve(releaseArch);
            // special handling for "arm"
            if (hardwareArchitecture == null) {
                if ("arm".equalsIgnoreCase(releaseArch)) {
                    String sunArchAbi = releaseProperties.get("SUN_ARCH_ABI");
                    if ("gnueabihf".equalsIgnoreCase(sunArchAbi)) {
                        hardwareArchitecture = HardwareArchitecture.ARMHF;
                    } else if ("gnueabi".equalsIgnoreCase(sunArchAbi)) {
                        hardwareArchitecture = HardwareArchitecture.ARMEL;
                    }
                }
            }
        }

        String releaseLibc = releaseProperties.get("LIBC");
        if (releaseLibc != null) {
            abi = ABI.resolve(releaseLibc);
        }

        vendor = releaseProperties.get("IMPLEMENTOR");
        implementorVersion = releaseProperties.get("IMPLEMENTOR_VERSION");

        // try to parse distribution from a few different values
        distro = JavaDistribution.resolve(vendor);
        if (distro == null) {
            distro = JavaDistribution.resolve(implementorVersion);
            if (distro == null) {
                distro = JavaDistribution.resolve(javaHomeDir.toString());
            }
        }

        return new JavaHome(javaHomeDir, javaExeFile, javacExeFile, nativeImageExeFile, operatingSystem, hardwareArchitecture, abi, vendor, distro, version, releaseProperties);
    }

    static public Map<String,String> readReleaseProperties(Path releaseFile) throws IOException {
        final List<String> lines = Files.readAllLines(releaseFile);
        final Map<String,String> nameValues = new HashMap<>();
        for (String line : lines) {
            // IMPLEMENTOR="Azul Systems, Inc."
            int equalsPos = line.indexOf('=');
            if (equalsPos > 2 && equalsPos < line.length()-2) {
                String name = line.substring(0, equalsPos);
                String value;
                // does it need unquoted?
                if (line.charAt(equalsPos+1) == '"' && line.charAt(line.length()-1) == '"') {
                    value = line.substring(equalsPos+2, line.length()-1);
                } else {
                    value = line.substring(equalsPos+1);
                }
                nameValues.put(name, value);
            }
        }
        return nameValues;
    }

    static public String executeJavaVersion(Path javaExeFile) throws IOException, InterruptedException {
        final Process process = new ProcessBuilder()
            .command(javaExeFile.toString(), "-version")
            .redirectErrorStream(true)
            .start();

        // we do not need the input stream
        process.getOutputStream().close();

        // read all the output
        final StringBuilder output = new StringBuilder();
        final byte[] buf = new byte[1024];
        try (InputStream input = process.getInputStream()) {
            int read = 1;
            while (read > 0) {
                read = input.read(buf);
                if (read > 0) {
                    output.append(new String(buf, 0, read, StandardCharsets.UTF_8));
                }
            }
        }

        // the exit value MUST be zero
        process.waitFor(5, TimeUnit.SECONDS);

        if (process.exitValue() != 0) {
            throw new IOException("Version command failed with exit value " + process.exitValue());
        }

        return output.toString();
    }

    static public Map<String,String> readJavaVersionOutput(String versionOutput) throws IOException {
        final String[] lines = versionOutput.split("\n");
        final Map<String,String> nameValues = new HashMap<>();

        // first line should have the version
        String line1 = lines[0];
        int doubleQuoteStartPos = line1.indexOf('"');
        if (doubleQuoteStartPos > 0) {
            int doubleQuoteEndPos = line1.indexOf('"', doubleQuoteStartPos+1);
            if (doubleQuoteEndPos > doubleQuoteStartPos) {
                String version = line1.substring(doubleQuoteStartPos+1, doubleQuoteEndPos);
                nameValues.put("JAVA_VERSION", version.trim());
            }
        }

        // second line should have the implementer version
        if (lines.length > 1) {
            String line2 = lines[1];
            int implStartPos = line2.toLowerCase().indexOf("runtime environment ");
            if (implStartPos > 0) {
                int implEndPos = line2.indexOf(" (build", implStartPos+1);
                if (implEndPos > implStartPos+23) {
                    String implementerVersion = line2.substring(implStartPos+20, implEndPos);

                    // remove ( and ) from it?
                    if (implementerVersion.charAt(0) == '(' && implementerVersion.charAt(implementerVersion.length()-1) == ')') {
                        implementerVersion = implementerVersion.substring(1, implementerVersion.length()-1);
                    }

                    nameValues.put("IMPLEMENTOR_VERSION", implementerVersion.trim());
                }
            }
        }

        return nameValues;
    }

    static public List<JavaHome> detect() throws Exception {
        final NativeTarget nativeTarget = NativeTarget.detect();

        log.info("Detected operating system {}", nativeTarget.getOperatingSystem());

        final Set<Path> maybeJavaHomes = new LinkedHashSet<>();

        // this JVM's "java.home" it is executing with
        locateJavaHomeFromThisJvm(maybeJavaHomes);

        // the JAVA_HOME environment variable
        locateJavaHomeFromJavaHomeEnvVar(maybeJavaHomes);

        // the PATH environment variable will contain "bin" directories of the java home, or symlinked javas
        locateJavaHomesFromPathEnvVar(maybeJavaHomes, nativeTarget);

        switch (nativeTarget.getOperatingSystem()) {
            case LINUX:
                // Ubuntu 22.04 (e.g. /usr/lib/jvm/zulu21.28.85-ca-jdk21.0.0-linux_x64)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("/usr/lib/jvm"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("/usr/java"), ".*");
                break;
            case MACOS:
                final Path contentsHomeDir = Paths.get("Contents/Home");
                // e.g. /Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("/Library/Java/JavaVirtualMachines"), ".*", contentsHomeDir);
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("/System/Library/Java/JavaVirtualMachines"), ".*", contentsHomeDir);
                // e.g. /Library/Internet Plug-Ins/Java*/Contents/Home
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("/Library/Internet Plug-Ins"), "Java.*", contentsHomeDir);
                break;
            case WINDOWS:
                // Azul Zulu (e.g. C:\Program Files\Zulu\zulu-17)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\Zulu"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\Zulu"), ".*");
                // Eclipse Adoptium/Temerin (e.g. C:\Program Files (x86)\Eclipse Adoptium\jdk-8.0.392.8-hotspot)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\Eclipse Adoptium"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\Eclipse Adoptium"), ".*");
                // BellSoft/Liberica (e.g. C:\Program Files\BellSoft\LibericaJDK-11)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\BellSoft"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\BellSoft"), ".*");
                // Amazon Corretto (e.g. C:\Program Files (x86)\Amazon Corretto\jdk1.8.0_392 OR C:\Program Files (x86)\Amazon Corretto\jre8)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\Amazon Corretto"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\Amazon Corretto"), ".*");
                // Microsoft (e.g. C:\Program Files\Microsoft\jdk-21.0.1.12-hotspot)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\Microsoft"), "jdk.*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\Microsoft"), "jdk.*");
                // SAP Machine (e.g. C:\Program Files\SapMachine\JDK\17)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\SapMachine\\JDK"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\SapMachine\\JDK"), ".*");
                // IBM Semeru (e.g. C:\Program Files\Semeru\jdk-18.0.2.9-openj9)
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files\\Semeru"), ".*");
                locateJavaHomesFromDir(maybeJavaHomes, Paths.get("C:\\Program Files (x86)\\Semeru"), ".*");
                break;
            case FREEBSD:
            case OPENBSD:
                Path usrLocalPath = Paths.get("/usr/local");
                // FreeBSD (e.g. /usr/local/openjdk11)
                locateJavaHomesFromDir(maybeJavaHomes, usrLocalPath, "openjdk.*");
                // OpenBSD (e.g. /usr/local/jdk-17)
                locateJavaHomesFromDir(maybeJavaHomes, usrLocalPath, "jdk.*");
                break;
        }

        final List<JavaHome> javaHomes = new ArrayList<>();

        // detect if the possible java home IS a java home
        for (final Path maybeJavaHome : maybeJavaHomes) {
            log.info("detectJavaHome: {}", maybeJavaHome);
            JavaHome javaHome = null;
            try {
                javaHome = JavaHomes.fromDirectory(maybeJavaHome);
                javaHomes.add(javaHome);
            } catch (Exception e) {
                // not a jvm
                log.info("  was NOT a java home", e);
            }
        }

        return javaHomes;
    }

    static private void locateJavaHomeFromThisJvm(Set<Path> maybeJavaHomes) throws IOException {
        String javaHomeSysProp = System.getProperty("java.home");
        if (javaHomeSysProp != null) {
            Path p = Paths.get(javaHomeSysProp);
            log.info("locateJavaHomeFromThisJvm: {}", javaHomeSysProp);
            // before we do 2 system calls, let's check if we already have it
            if (!maybeJavaHomes.contains(p)) {
                if (Files.isDirectory(p)) {
                    // note: we want the real java home, not a symlinked version of it
                    p = p.toRealPath();
                    if (!maybeJavaHomes.contains(p)) {
                        log.info("  adding possible java home {}", p);
                        maybeJavaHomes.add(p);
                    } else {
                        log.info("  skipping, already present java home {}", p);
                    }
                }
            } else {
                log.info("  skipping, already present java home {}", p);
            }
        }
    }

    static private void locateJavaHomeFromJavaHomeEnvVar(Set<Path> maybeJavaHomes) throws IOException {
        String javaHomeEnvVar = System.getenv("JAVA_HOME");
        if (javaHomeEnvVar != null && !javaHomeEnvVar.isEmpty()) {
            Path p = Paths.get(javaHomeEnvVar);
            log.info("locateJavaHomeFromJavaHomeEnvVar: {}", javaHomeEnvVar);
            // before we do 2 system calls, let's check if we already have it
            if (!maybeJavaHomes.contains(p)) {
                if (Files.isDirectory(p)) {
                    // note: we want the real java home, not a symlinked version of it
                    p = p.toRealPath();
                    if (!maybeJavaHomes.contains(p)) {
                        log.info("  adding possible java home {}", p);
                        maybeJavaHomes.add(p);
                    } else {
                        log.info("  skipping, already present java home {}", p);
                    }
                }
            } else {
                log.info("  skipping, already present java home {}", p);
            }
        }
    }

    static private void locateJavaHomesFromPathEnvVar(Set<Path> maybeJavaHomes, NativeTarget nativeTarget) throws IOException {
        // what will the java executable on this native platform be named?
        final String javaExeFileName = nativeTarget.resolveExecutableFileName("java");

        final String pathEnvVar = System.getenv("PATH");
        if (pathEnvVar != null && !pathEnvVar.isEmpty()) {
            // split path on path separator
            final String[] paths = pathEnvVar.split(File.pathSeparator);
            for (String path : paths) {
                log.info("locateJavaHomesFromPathEnvVar: {}", path);

                // does the path contain a java executable?
                final Path javaExeFile = Paths.get(path).resolve(javaExeFileName);
                if (Files.exists(javaExeFile)) {
                    // follow it if symlinked, to get the real path
                    Path p = javaExeFile.toRealPath().resolve("../..").normalize();
                    if (!maybeJavaHomes.contains(p)) {
                        maybeJavaHomes.add(p);
                        log.info("  adding possible java home {}", p);
                    } else {
                        log.info("  skipping, already present java home {}", p);
                    }
                } else {
                    log.info("  skipping, no {} present", javaExeFileName);
                }
            }
        }
    }

    static private void locateJavaHomesFromDir(Set<Path> maybeJavaHomes, Path searchDir, String nameRegex) throws IOException {
        locateJavaHomesFromDir(maybeJavaHomes, searchDir, nameRegex, null);
    }

    static private void locateJavaHomesFromDir(Set<Path> maybeJavaHomes, Path searchDir, String nameRegex, Path subDir) throws IOException {
        log.info("locateJavaHomesFromDir: {} with regex {}", searchDir, nameRegex);

        // does the search dir even exist?
        if (!Files.isDirectory(searchDir)) {
            log.info("  skipping, dir does not exist (or is a file)");
            return;
        }

        final Pattern namePattern = Pattern.compile(nameRegex);

        try (Stream<Path> paths = Files.list(searchDir)) {
            paths.forEach(p -> {
                // only dirs
                if (Files.isDirectory(p)) {
                    String name = p.getFileName().toString();
                    if (namePattern.matcher(name).matches()) {
                        // follow it if symlinked, to get the real path (so we can check if we already have it)
                        try {
                            p = p.toRealPath();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }

                        // are we in yet another subDir?
                        if (subDir != null) {
                            p = p.resolve(subDir);
                            if (!Files.isDirectory(p)) {
                                return;     // this isn't valid then
                            }
                        }

                        if (!maybeJavaHomes.contains(p)) {
                            log.info("  adding possible java home {}", p);
                            maybeJavaHomes.add(p);
                        } else {
                            log.info("  skipping, already present java home {}", p);
                        }
                    } else {
                        log.info("  skipping, name {} does not match regex", name);
                    }
                }
            });
        }
    }

}