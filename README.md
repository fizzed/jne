Java Native Extractor by Fizzed
===============================

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Overview

Utility Java library for finding, extracting, and using os and architecture
dependent files (executables, libraries, and/or other files) that are packaged
as resources within jar files. Allows them to be easily included as part of a
Java application and intelligently extracted for use at runtime. This library makes
it easy to build your own custom "bin" directory based on the runtime operating
system and architecture.  You can package .exe and .dll/.so resources within
jars and then use something like maven for dependency management.

Here is how it works. At runtime, Java let's you find resources in directories
and/or jars (if they are included on the classpath). Let's say you wanted to call
an external "cat" executable. With this library, you'd do the following:

    File catExeFile = JNE.findExecutable("cat", options);

The library would then search for the following resource:

    /jne/<os>/<arch>/<exe|lib|file>

Where "os" would be either "windows", "osx", or "linux" and "arch" would either
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
 - Support for finding exact files (e.g. movie.swf)
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

## Demo

To run a demo of a "cat" executable

    mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.jne.demo.JneDemo"

With overridden extract dir via system property:

    mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.fizzed.jne.demo.JneDemo" -Djne.extract.dir="target/jne"

## Including resources as a jar

If using Maven then by default it will include everything in `src/main/resources`
in a jar.  Let's say you wanted to package a "cat" executable for various platforms.
You can easily include these for use with JNE by putting them at

    src/main/resources/jne/windows/x32/cat.exe
    src/main/resources/jne/windows/x64/cat.exe
    src/main/resources/jne/osx/x32/cat
    src/main/resources/jne/osx/x64/cat
    src/main/resources/jne/linux/x32/cat
    src/main/resources/jne/linux/x64/cat

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

## License

Copyright (C) 2015 Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.