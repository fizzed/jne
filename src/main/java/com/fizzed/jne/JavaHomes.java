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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JavaHomes {
    static private final Logger log = LoggerFactory.getLogger(JavaHome.class);

    static public JavaHome fromDirectory(Path javaHomeDir) throws IOException {
        if (!Files.isDirectory(javaHomeDir)) {
            throw new FileNotFoundException("Java home directory " + javaHomeDir + " does not exist");
        }

        final OperatingSystem thisOs = PlatformInfo.detectOperatingSystem();

        // Test #1: bin/java exists?
        final String javaExeFileName = NativeTarget.resolveExecutableFileName(thisOs, "java");
        final Path javaExeFile = javaHomeDir.resolve("bin").resolve(javaExeFileName);

        if (!Files.isRegularFile(javaExeFile)) {
            throw new FileNotFoundException("Java executable " + javaExeFile + " was not found in " + javaHomeDir);
        }

        // Test #2: sometimes we can find a bin/java, especially if we're dealing with a directory simply on the PATH
        // we need to make sure this directory "looks" like a typical java home, with a "lib" dir, etc.
        final Path javaLibDir = javaHomeDir.resolve("lib");
        if (!Files.isDirectory(javaLibDir)) {
            throw new FileNotFoundException("Java lib directory " + javaLibDir + " was not found in " + javaHomeDir);
        }

        final String javacExeFileName = NativeTarget.resolveExecutableFileName(thisOs, "javac");
        final Path _javacExeFile = javaHomeDir.resolve("bin").resolve(javacExeFileName);
        final Path javacExeFile;
        if (Files.isRegularFile(_javacExeFile)) {
            javacExeFile = _javacExeFile;
        } else {
            javacExeFile = null;
        }

        JavaVersion version = null;
        String vendor = null;
        OperatingSystem operatingSystem = null;
        HardwareArchitecture hardwareArchitecture = null;
        ABI abi = null;
        Map<String,String> releaseProperties = null;

        // Test #3: release file w/ java home (it contains valuable info)
        final Path releaseFile = javaHomeDir.resolve("release");
        if (Files.isRegularFile(releaseFile)) {
            releaseProperties = JavaHomes.readReleaseProperties(releaseFile);

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
                // TODO: need special handling for "arm"
            }

            vendor = releaseProperties.get("IMPLEMENTOR");
        } else {
            throw new FileNotFoundException("Java release file " + releaseFile + " was not found in " + javaHomeDir);
        }

        return new JavaHome(javaHomeDir, javaExeFile, javacExeFile, operatingSystem, hardwareArchitecture, abi, vendor, version, releaseProperties);
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
            } catch (FileNotFoundException e) {
                // not a jvm
                log.info("  was NOT a java home");
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