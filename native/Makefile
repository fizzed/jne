CC = gcc
CPP = g++
JAVAC = javac
JAVA = java

all:
	$(CC) -Wall -pedantic -O2 -o jcat jcat.c

jnilib:
	$(JAVAC) -h . helloj/HelloLib.java
	$(CPP) -shared -fPIC -Wall -pedantic -O3 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -o libhelloj.so -lc helloj_HelloLib.cpp
	$(JAVAC) -d . helloj/HelloLibMain.java

jnitest:
	$(JAVA) -cp . helloj/HelloLibMain

clean:
	rm -f jcat
	rm -f helloj_HelloLib.h
	rm -f libhelloj.so
	rm -f libhelloj.so
	rm -f helloj/*.class