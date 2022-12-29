USERID=$(id -u ${USER})
USERNAME=${USER}

echo "Building docker containers with:"
echo " userId: ${USERID}"
echo " userName: ${USERNAME}"

docker build -f setup/Dockerfile.linux-generic --progress=plain --build-arg FROM_IMAGE=amd64/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-x64 setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=i386/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-x32 setup
# musl builds
docker build -f setup/Dockerfile.alpine-generic --build-arg FROM_IMAGE=amd64/alpine --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-alpine-x64 setup
docker build -f setup/Dockerfile.alpine-generic --build-arg FROM_IMAGE=arm64v8/alpine --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-alpine-arm64 setup
# other linux arms, etc.
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=arm64v8/ubuntu --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-arm64 setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=arm32v7/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-armhf setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=arm32v5/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-armel setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=riscv64/ubuntu --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-riscv64 setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=mips64le/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-mips64le setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=s390x/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-s390x setup
docker build -f setup/Dockerfile.linux-generic --build-arg FROM_IMAGE=ppc64le/debian --build-arg USERID=${USERID} --build-arg USERNAME=${USERNAME} -t jne-linux-ppc64le setup