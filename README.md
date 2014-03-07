Java Native Executable Library
=================================

### Overview

[Mfizz, Inc.](http://mfizz.com)
Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

Utility library for finding, extracting, and using native executables in Java 
that are packaged as resources within jar files. Allows them to be easily
included as part of a Java application and extracted/used at runtime. Basically,
this library makes it easy to build your own custom "bin" directory based on
the runtime operating system and architecture.  You can package .exe and .dll/.so
resources within jars and then use something like maven for dependency management.

The resource path searched at runtime:

        /jne/<os>/<arch>/<exe>

os: windows, mac, or linux
arch: x86 or x64

### Features

 - Support for multiple operating systems and architectures so that a single
   JAR dependency can support them all.
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
 - Support for .dll/.so resources by simply disabling "appendExeOnWindows" option
   when searching for an executable.

### Demo

Since this project requires a compiled jar to work, a simple method of testing
is to use the "mfz-jne-cat" sample project (in a subdir of this project).

Simply do:
        cd mfz-jne-cat
        mvn install

Then you can go back to the main project:
        mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.mfizz.jne.demo.JneDemo" -Dexec.args=""

### Packaging as a JAR

Please see "mfz-jne-cat" as a reference project of packaging the "cat"
executable across various platforms.

Since only a single executable is extracted by this library -- you'll probably
want to package your executables as statically compiled (e.g. it doesn't rely
on external DLLs / shared objects to be available on the runtime system).

### License

Copyright (C) 2014 Joe Lauer / Mfizz, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.