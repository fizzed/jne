# Java Native Extractor by Fizzed

[![Maven Central](https://img.shields.io/maven-central/v/com.fizzed/jne?style=flat-square)](https://mvnrepository.com/artifact/com.fizzed/jne)

[![Java 8](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java8.yaml?branch=master&label=Java%208&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java8.yaml)
[![Java 11](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java11.yaml?branch=master&label=Java%2011&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java11.yaml)
[![Java 17](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java17.yaml?branch=master&label=Java%2017&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java17.yaml)

[![Linux x64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/java8.yaml?branch=master&label=Linux%20x64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/java8.yaml)
[![Linux arm64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/linux-arm64.yaml?branch=master&label=Linux%20arm64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/linux-arm64.yaml)
[![Linux riscv64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/linux-riscv64.yaml?branch=master&label=Linux%20riscv64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/linux-riscv64.yaml)
[![MacOS x64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/macos-x64.yaml?branch=master&label=MacOS%20x64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/macos-x64.yaml)
[![Windows x64](https://img.shields.io/github/actions/workflow/status/fizzed/jne/windows-x64.yaml?branch=master&label=Windows%20x64&style=flat-square)](https://github.com/fizzed/jne/actions/workflows/windows-x64.yaml)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Overview

Java library (targeting 6+, plus 8, 11, and 17 etc) for finding, extracting, and using os and architecture
dependent files (executables, libraries, and/or other files) that are packaged
as resources within jar files. Allows them to be easily included as part of a
Java application and intelligently extracted for use at runtime. This library makes
it easy to build your own custom "bin" directory based on the runtime operating
system and architecture.  You can package .exe and .dll/.so resources within
jars and then use something like maven for dependency management.

Here is how it works. At runtime, Java let's you find resources in directories
and/or jars (if they are included on the classpath). Let's say you wanted to call
an external "cat" executable.  With a properly packaged resource on the classpath
this executable can found with

    File catExeFile = JNE.findExecutable("cat");

The library would search for the resource using the following path

    /jne/<os>/<arch>/<exe>

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

## Features

 - Support for multiple operating systems and architectures so that a single
   jar dependency can support them all.
 - Support for finding executables (e.g. cat or cat.exe)
 - Support for finding libraries (e.g. sample.dll/libsample.dylib/libsample.so)
 - Support for finding generic files (e.g. movie.swf)
 - Use a one-time temporary directory for extracted executables (thus same apps
   running multiple instances get their own executable copy)
 - Specify a directory to extract executables to (useful for single instance
   daemons).
 - Specify if executables should be deleted on VM exit. If disabled and an
   extracted directory is specified, then a "hash" is calculated for an extracted
   executable so that if the next run of the app has a dependency change then
   the latest executable will be used.
 - Optional fallback to x86 executables on x64 platforms where an x64-specific
   executable is not found/included.  Useful in the case where an x86 executable
   is good for either architecture and you want to save space by not including both
   versions in your JAR.
 - Utility classes for double-locking, safe loading of libraries.

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
    <version>VERSION</version>
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

You can use an Ubuntu x86_64 host to test a wide variety of hardware architectures and operating systems.
Install QEMU and various emulators: https://www.stereolabs.com/docs/docker/building-arm-container-on-x86/

    sudo apt-get install qemu binfmt-support qemu-user-static
    docker run --rm --privileged multiarch/qemu-user-static --reset -p yes

This will now register docker to be able to detect and run various architectures automatically. You can now try it out:

    docker run --rm -t arm64v8/ubuntu dpkg --print-architecture       #arm64
    docker run --rm -t arm32v7/debian dpkg --print-architecture       #armhf
    docker run --rm -t arm32v5/debian dpkg --print-architecture       #armel
    docker run --rm -t riscv64/ubuntu dpkg --print-architecture       #riscv64
    docker run --rm -t i386/ubuntu dpkg --print-architecture          #i386

If you'd like to try various Java system properties to see what they'd look like:

    docker run --rm -it riscv64/ubuntu
    apt update
    apt install openjdk-11-jdk-headless
    jshell
    System.getProperties().forEach((k, v) -> { System.out.printf("%s: %s\n", k, v); })

You can test this library on a wide variety of operating systems and architectures

    ./build-dockers.sh
    ./test-on-dockers.sh

## License

Copyright (C) 2015+ Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
