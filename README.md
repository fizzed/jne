### Overview

Utility library for finding, extracting, and using native executables in Java 
that are packaged as resources within jar files. Allows them to be easily
included as part of a Java application and extracted/used at runtime.

The resource path searched at runtime:

        /jne/<os>/<arch>/<exe>

os: windows, mac, or linux
arch: x86 or x64

Since x86 executables can run on x64 systems, on x64 platforms the library will
search for x64 first and fallback to x86. The library is somewhat simple in that
it will only extract a single executable -- so you'll need to remember to package
your executable as a statically built one.

### Demo

Since this project requires a compiled jar to work, a simple method of testing
is to use the "mfz-jne-cat" sample project (in a subdir of this project).

Simply do:
        cd mfz-jne-cat
        mvn install

Then you can go back to the main project:
        mvn -e test-compile exec:java -Dexec.classpathScope="test" -Dexec.mainClass="com.mfizz.jne.demo.JneDemo" -Dexec.args=""

### Packaging as jar

Please see "mfz-jne-cat" as a reference implementation of packaging the "cat"
executable across various platforms.
