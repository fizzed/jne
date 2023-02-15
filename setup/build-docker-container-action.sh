#!/bin/sh -lex

BASEDIR=$(dirname "$0")
cd "$BASEDIR/.." || exit 1
PROJECT_DIR=$PWD

USERID=$(id -u ${USER})
USERNAME=${USER}

DOCKER_IMAGE="$1"
CONTAINER_NAME="$2"
BUILDOS=$3
BUILDARCH=$4

DOCKERFILE="setup/Dockerfile.linux-cross-build"
if [ ! -z "$(echo $BUILDARCH | grep "\-test")" ]; then
  DOCKERFILE="setup/Dockerfile.linux"
  if [ "$BUILDOS" = "linux_musl" ]; then
    DOCKERFILE="setup/Dockerfile.linux_musl"
  fi
fi

docker build -f "$DOCKERFILE" --progress=plain \
  --build-arg "FROM_IMAGE=${DOCKER_IMAGE}" \
  --build-arg USERID=${USERID} \
  --build-arg USERNAME=${USERNAME} \
  -t ${CONTAINER_NAME} \
  "$PROJECT_DIR/setup"