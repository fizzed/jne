#!/bin/sh -l
set -e

BASEDIR=$(dirname "$0")
cd "$BASEDIR/.."
PROJECT_DIR=$PWD

BUILDOS=$1
BUILDARCH=$2

# Setup cross compile environment
#if [ -f /opt/setup-cross-build-environment.sh ]; then
#  . /opt/setup-cross-build-environment.sh $BUILDOS $BUILDARCH
#fi

#. setup/setup-environment.sh

#env

mkdir -p target
rsync -avrt --delete ./native/ ./target/

cd target/jcat
make

export CXXFLAGS="-z noexecstack"

cd ../libhelloj
make

cd ..
OUTPUT_DIR="../src/test/resources/jne/${BUILDOS}/${BUILDARCH}"
mkdir -p "$OUTPUT_DIR"
cp jcat/jcat "$OUTPUT_DIR"
cp libhelloj/libhelloj.so "$OUTPUT_DIR"