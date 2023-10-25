#!/bin/sh

BASEDIR=$(dirname "$0")
cd "$BASEDIR/.."
PROJECT_DIR=$PWD

BUILDOS=$1
BUILDARCH=$2

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