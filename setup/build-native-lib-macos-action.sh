#!/bin/sh -liex
# shell w/ login & interactive, exit if any command fails, log each command

BASEDIR=$(dirname "$0")
cd "$BASEDIR/.."
PROJECT_DIR=$PWD

BUILDOS=$1
BUILDARCH=$2

mkdir -p target
rsync -avrt --delete ./native/ ./target/

cd target/jcat
make

export SHAREDFILEEXT="dylib"

cd ../libhelloj
make

cd ..
OUTPUT_DIR="../src/test/resources/jne/${BUILDOS}/${BUILDARCH}"
cp jcat/jcat "$OUTPUT_DIR"
cp libhelloj/libhelloj.dylib "$OUTPUT_DIR"