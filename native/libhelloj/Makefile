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
EXTRA_CXXFLAGS := -z noexecstack
CXXFLAGS :=
CFLAGS :=
JAVA_INCLUDE := -I${JAVA_HOME}/include
ifeq ($(UNAME_S),Darwin)
    JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/darwin
	SHAREDFILEEXT = dylib
	EXTRA_CXXFLAGS =
endif
ifeq ($(UNAME_S),Linux)
    JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/linux
endif
ifeq ($(UNAME_S),FreeBSD)
    JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/freebsd
    CXX = clang++
endif
ifeq ($(UNAME_S),OpenBSD)
    JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/openbsd
    EXTRA_CXXFLAGS =
    CXX = clang++
endif

all:
	$(CXX) -shared -fPIC -Wall -pedantic -O3 $(CXXFLAGS) $(EXTRA_CXXFLAGS) $(JAVA_INCLUDE) -o libhelloj.$(SHAREDFILEEXT) -lc helloj_HelloLib.cpp

jniheaders:
	$(JAVAC) -h . helloj/HelloLib.java

test:
	$(JAVAC) -d . helloj/HelloLibMain.java
	$(JAVA) -cp . helloj/HelloLibMain

clean:
	rm -f libhelloj.so
	rm -f libhelloj.dylib
	rm -f helloj/*.class
