CC = gcc
CPP = g++
JAVAC = javac
JAVA = java

all:
	$(CPP) -shared -fPIC -Wall -pedantic -O3 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -o libhelloj.so -lc helloj_HelloLib.cpp

jniheaders:
	$(JAVAC) -h . helloj/HelloLib.java

test:
	$(JAVAC) -d . helloj/HelloLibMain.java
	$(JAVA) -cp . helloj/HelloLibMain

clean:
	rm -f libhelloj.so
	rm -f helloj/*.class