#
# To run this in your own shell
#   CC=gcc CXX=g++ make
#

CC ?= gcc
CPP ?= g++
JAVAC ?= javac
JAVA ?= java
SHAREDFILEEXT ?= so
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	SHAREDFILEEXT = dylib
endif

all:
	$(CXX) -shared -fPIC -Wall -pedantic -O3 $(CXXFLAGS) -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/freebsd -I${JAVA_HOME}/include/openbsd -o libhelloj.$(SHAREDFILEEXT) -lc helloj_HelloLib.cpp

jniheaders:
	$(JAVAC) -h . helloj/HelloLib.java

test:
	$(JAVAC) -d . helloj/HelloLibMain.java
	$(JAVA) -cp . helloj/HelloLibMain

clean:
	rm -f libhelloj.so
	rm -f helloj/*.class