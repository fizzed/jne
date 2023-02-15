#!/bin/bash -lex
# shell w/ login & interactive, exit if any command fails, log each command

BASEDIR=$(dirname "$0")
cd "$BASEDIR/.."
PROJECT_DIR=$PWD

BUILDOS=$1
BUILDARCH=$2

# Setup cross compile environment
if [ -f /opt/setup-cross-build-environment.sh ]; then
  source /opt/setup-cross-build-environment.sh $BUILDOS $BUILDARCH
fi

mkdir -p target
rsync -avrt --delete ./native/ ./target/

cd target/jcat
make

cd ../libhelloj
make

#TARGET_LIB=libjtkrzw.so
#$STRIP ./$TARGET_LIB

cd ..
OUTPUT_DIR="../src/test/resources/jne/${BUILDOS}/${BUILDARCH}"
cp jcat/jcat "$OUTPUT_DIR"
cp libhelloj/libhelloj.so "$OUTPUT_DIR"