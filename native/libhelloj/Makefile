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
# we also need an architecture/os dependant path as well that mirrors the uname
UNAME_S_LOWER := $(shell echo $(UNAME_S) | tr '[:upper:]' '[:lower:]')
JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/${UNAME_S_LOWER}
ifeq ($(UNAME_S),Darwin)
	SHAREDFILEEXT = dylib
	EXTRA_CXXFLAGS =
endif
ifeq ($(UNAME_S),FreeBSD)
    CXX = clang++
endif
ifeq ($(UNAME_S),OpenBSD)
    EXTRA_CXXFLAGS =
    CXX = clang++
endif
ifeq ($(UNAME_S),NetBSD)
    EXTRA_CXXFLAGS =
    CXX = cc
endif
ifeq ($(UNAME_S),SunOS)
    JAVA_INCLUDE := ${JAVA_INCLUDE} -I${JAVA_HOME}/include/solaris
    #CXX = clang++
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
