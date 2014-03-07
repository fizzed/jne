### Overview

Utility library for finding, extracting, and using native executables in Java 
that are packaged as resources within jar files. This allows them to be easily
included as part of a Java application and extracted at runtime.

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
