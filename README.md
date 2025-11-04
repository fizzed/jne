# Java Native Environment by Fizzed

[![Maven Central](https://img.shields.io/maven-central/v/com.fizzed/jne?style=flat-square)](https://mvnrepository.com/artifact/com.fizzed/jne)

## Automated Testing

The following Java versions and platforms are tested using GitHub workflows:

[![Java 8](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java8.yaml?branch=master&label=Java%208&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java8.yaml)
[![Java 11](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java11.yaml?branch=master&label=Java%2011&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java11.yaml)
[![Java 17](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java17.yaml?branch=master&label=Java%2017&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java17.yaml)
[![Java 21](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java21.yaml?branch=master&label=Java%2021&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java21.yaml)
[![Java 25](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java25.yaml?branch=master&label=Java%2025&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java25.yaml)

[![Linux x64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java8.yaml?branch=master&label=Linux%20x64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java8.yaml)
[![MacOS arm64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/macos-arm64.yaml?branch=master&label=MacOS%20arm64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/macos-arm64.yaml)
[![Windows x64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/windows-x64.yaml?branch=master&label=Windows%20x64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/windows-x64.yaml)

The following platforms are tested using the [Fizzed, Inc.](http://fizzed.com) build system:

[![FreeBSD x64](https://img.shields.io/badge/FreeBSD_x64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![FreeBSD arm64](https://img.shields.io/badge/FreeBSD_arm64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux arm64](https://img.shields.io/badge/Linux_arm64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux armhf](https://img.shields.io/badge/Linux_armhf-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux riscv64](https://img.shields.io/badge/Linux_riscv64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux MUSL x64](https://img.shields.io/badge/Linux_MUSL_x64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux MUSL arm64](https://img.shields.io/badge/Linux_MUSL_arm64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Linux MUSL riscv64](https://img.shields.io/badge/Linux_MUSL_riscv64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![MacOS x64](https://img.shields.io/badge/MacOS_x64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![OpenBSD x64](https://img.shields.io/badge/OpenBSD_x64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![OpenBSD arm64](https://img.shields.io/badge/OpenBSD_arm64-passing-brightgreen?style=flat-square)](buildx-results.txt)
[![Windows arm64](https://img.shields.io/badge/Windows_arm64-passing-brightgreen?style=flat-square)](buildx-results.txt)

## Overview

Java library (8, 11, 17, 21 etc) for working with "native" resources on the JVM.  Utilities for finding & extracting
OS and hardware architecture dependent files, executables, and libraries (e.g. including libraries as resources in JARs),
as well as utilities for detecting installed JDKs, or calculating the "targets" of native compilation.

This is a battle-tested library w/ extensive test coverage and automated CI across various operating systems, versions,
and architectures.

Includes the following features:

- JNE class: helps to find, extract, and load OS, ABI, and hardware architecture dependent files, executables, and libraries
- NativeTarget class: helps to compile native code by determining various "targets" for Rust, C/C++, or for CI frameworks
- JavaHome, JavaHomes classes: helps to detect JDKs installed on the local system, as well as versions, distribution, JDK vs. JRE, etc.
- Support for Linux, Windows, MacOS, FreeBSD, OpenBSD operating systems
- Support for x64, x32, arm64, armhf, armel, riscv64 hardware architecture
- Support for GNU & MUSL abis
- Support for multiple operating systems and architectures so that a single jar dependency can support them all.
- Support for finding executables (e.g. cat or cat.exe)
- Support for finding libraries (e.g. sample.dll/libsample.dylib/libsample.so)
- Support for finding generic files (e.g. movie.swf)
- Use a one-time temporary directory for extracted executables (thus same apps running multiple instances get their own executable copy)
- Specify a directory to extract executables to (useful for single instance daemons).
- Specify if executables should be deleted on VM exit. If disabled and an extracted directory is specified, then a "hash" is calculated for an extracted executable so that if the next run of the app has a dependency change then the latest executable will be used.
- Optional fallback to x86 executables on x64 platforms where an x64-specific executable is not found/included.  Useful in the case where an x86 executable is good for either architecture and you want to save space by not including both versions in your JAR.
- Utility classes for double-locking, safe loading of libraries.

## Sponsorship & Support

![](https://cdn.fizzed.com/github/fizzed-logo-100.png)

Project by [Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

**Developing and maintaining opensource projects requires significant time.** If you find this project useful or need
commercial support, we'd love to chat. Drop us an email at [ping@fizzed.com](mailto:ping@fizzed.com)

Project sponsors may include the following benefits:

- Priority support (outside of Github)
- Feature development & roadmap
- Priority bug fixes
- Privately hosted continuous integration tests for their unique edge or use cases

## OS/Architecture Dependent Extraction at Runtime

JNE can helps with finding, extracting, and using os and architecture dependent files (executables, libraries, and/or
other files) that are packaged as resources within jar files. Extensive support and testing for operating system, libc
(e.g. glibc vs. musl), and hardware architecture detection.

Allows them to be easily included as part of a Java application and intelligently extracted for use at runtime. This library makes
it easy to build your own custom "bin" directory based on the runtime operating
system and architecture.  You can package .exe and .dll/.so resources within
jars and then use something like maven for dependency management.

Here is how it works. At runtime, Java let's you find resources in directories
and/or jars (if they are included on the classpath).  Let's say you wanted to load
at runtime a shared object (.dll, .so, .dylib) that was packaged inside a .jar

   JNE.loadLibrary("mylib");

The library would search for the resource using the following resource path

    /jne/<os>/<arch>/<lib>

Or let's say you wanted to call an external "cat" executable.  With a properly packaged resource on the classpath
this executable can found with

    File catExeFile = JNE.findExecutable("cat");

If found the resource would be intelligently extracted to a temporary directory so it
can be executed.

    /tmp/1394227238992-0/cat

Where "os" would be something such as "windows", "macos", or "linux" and "arch" could
be "x32" or "x64". If we were running on Linux with a 64-bit operating system
then the library would search for "/jne/linux/x64/cat". If found and contained
within a jar file then this executable would be extracted to either a specific
or temporary directory and returned as a File object. This File object can then
be included as the first argument to a Process or ProcessBuilder object. There
are other options as well (e.g. fallback to x86 resources on x64 platforms) so
please see features below.

## Operating Systems

All popular operating systems are supported, along with special care for MUSL-based operating systems
such as Alpine Linux.

| OS         | Description                        |
|------------|------------------------------------|
| linux      |                                    |
| linux_musl | Such as alpine linux               |
| macos      | Also supports osx in resource path |
| windows    |                                    |
| freebsd    |                                    |
| openbsd    |                                    |
| solaris    |                                    |

## Hardware Architecture

Since this library targets finding or loading libraries for use within a JVM, the
supported hardware architectures match what you'd typically find JDK distributors
call their architectures.

| Arch     | Description                                                                | Docker          |
|----------|----------------------------------------------------------------------------|-----------------|
| x32      | 32-bit i386, i486, i686, etc.                                              | i386/debian     |
| x64      | 64-bit. Can also use x86_64, amd64 in resource path                        |                 |
| armel    | 32-bit armv4, armv5, armv6 w/ soft float support. E.g. Raspberry Pi 1      | arm32v5/debian  |
| armhf    | 32-bit armv7 w/ hard float support. E.g. Raspberry Pi 2                    | arm32v7/debian  |
| arm64    | 64-bit. Can also use aarch64 in resource path. E.g. Mac M1, Raspberry 3, 4 | arm64v8/ubuntu  |
| mips64le | 64-bit mips                                                                | mips64le/debian |
| riscv64  | 64-bit risc-v                                                              | riscv64/ubuntu  |
| s390x    |                                                                            | s390x/debian    |
| ppc64le  |                                                                            | ppc64le/debian  |

## Usage

Published to maven central use the following

```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>jne</artifactId>
    <version>4.6.2</version>
</dependency>
```

To safely load a library one time at application startup

```java
/**
 * Custom safe run once loading of native libs.
 */
public class CustomLoader {

  static private final MemoizedRunner loader = new MemoizedRunner();

  static public void loadLibrary() {
    loader.once(() -> {
      JNE.loadLibrary("jtokyocabinet");
    });
  }

}
```

To find a JDK 21 on your local system with a specific distribution:

```java
final JavaHome jdk21 = new JavaHomeFinder()
    .jdk()
    .version(21)
    .preferredDistributions(JavaDistribution.ZULU)
    .find();
```

To build a native language model to help build native-specific targets, urls, etc.

```java
final NativeTarget nativeTarget = NativeTarget.detect();
final NativeLanguageModel nlm = new NativeLanguageModel()
    .add("version", "17.0.1")
    .add(OperatingSystem.MACOS, "darwin")
    .add(HardwareArchitecture.X64, "amd64");

final String fileName = nlm.format("restic_{version}_{os}_{arch}.bz2", nativeTarget);
```

## Demo

To run a demo of a "cat" executable

    mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.jne.JneDemo"

With overridden extract dir via system property:

    mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.jne.JneDemo" -Djne.extract.dir="target/jne"

## Including resources as a jar

If using Maven then by default it will include everything in `src/main/resources`
in a jar.  Let's say you wanted to package a "cat" executable for various platforms.
You can easily include these for use with JNE by putting them at

    src/main/resources/jne/windows/x32/cat.exe
    src/main/resources/jne/windows/x64/cat.exe
    src/main/resources/jne/macos/x32/cat
    src/main/resources/jne/macos/x64/cat
    src/main/resources/jne/linux/x32/cat
    src/main/resources/jne/linux/x64/cat
    src/main/resources/jne/linux_musl/x64/cat
    src/main/resources/jne/generic-resource.txt

To find and extract these resources for use in your app

    File exeFile = JNE.findLibrary("cat", options);

You'll probably want to package your executables as statically compiled (it
does not rely on external DLLs / shared objects to be available on the runtime system).
However, since this library does essentially build a "bin" directory by extracting
resources, you could find all dependencies first before trying to execute it.
For example:

    File libFile = JNE.findLibrary("mylib", options);
    File exeFile = JNE.findExecutable("myapp", options);

If this was run on Linux with the extractDir as null (which then uses a temp dir)
you would have the following example result:

    /tmp/1394227238992-0/mylib.so
    /tmp/1394227238992-0/myapp

To extract a generic resource

    File resourceFile = JNE.findFile("generic-resource.txt", options);

## Development

You can use an Ubuntu x86_64 host to test a wide variety of hardware architectures and operating systems. For more
information on how this works, please visit https://github.com/fizzed/blaze-buildx#multiple-architecture-containers

You can test this library on a wide variety of operating systems and architectures

    java -jar blaze.jar test

## License

Copyright (C) 2015+ Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
