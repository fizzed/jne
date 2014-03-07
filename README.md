Java Native Executable Library
=================================

### Overview

 - [Mfizz, Inc.](http://mfizz.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

Utility Java library for finding, extracting, and using native executables 
that are packaged as resources within jar files. Allows them to be easily
included as part of a Java application and extracted/used at runtime. Basically,
this library makes it easy to build your own custom "bin" directory based on
the runtime operating system and architecture.  You can package .exe and .dll/.so
resources within jars and then use something like maven for dependency management.

Here is how it works. At runtime, Java let's you find resources in directories
and/or JARS (if they are included on the classpath). Let's say you wanted to call
an external "cat" executable. With this library, you'd do the following:

    File catExeFile = JNE.find("cat", options);

The library would then search for the following resource:

    /jne/<os>/<arch>/<exe>

Where "os" would be either "windows", "mac", or "linux" and "arch" would either
be "x86" or "x64". If we were running on Linux with a 64-bit operating system
then the library would search for "/jne/linux/x64/cat". If found and contained
within a jar file then this executable would be extracted to either a specific
or temporary directory and returned as a File object. This File object can then
be included as the first argument to a Process or ProcessBuilder object. There
are other options as well (e.g. fallback to x86 resources on x64 platforms) so
please see features below.

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

Since this project primarily requires a compiled jar for testing, a simple method
of testing is to use the "mfz-jne-cat" sample project (in a subdir of this project).

    cd mfz-jne-cat
    mvn install

Then you can go back to the main project:

    mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.mfizz.jne.demo.JneDemo" -Dexec.args=""

### Packaging as a JAR

Please see "mfz-jne-cat" as a reference project of packaging the "cat"
executable across various platforms.

You'll probably want to package your executables as statically compiled (e.g. it
doesn't rely on external DLLs / shared objects to be available on the runtime system).
However, since this library does essentially build a "bin" directory by extracting
resources, you could find all dependencies first before trying to execute it.
For example, 

    File dllFile = JNE.find("cat-dependency.dll", options);
    File dllFile = JNE.find("cat-dependency.so", options);
    File exeFile = JNE.find("cat", options);

### License

Copyright (C) 2014 Joe Lauer / Mfizz, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.