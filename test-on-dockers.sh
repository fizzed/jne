# from fastest to slowest
docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-x64 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-x32 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-alpine-x64 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-arm64 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-alpine-arm64 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-armhf sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

# these emulate a little slower usually
docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-riscv64 sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-armel sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-mips64le sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-s390x sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi

docker run --rm -it -v $PWD:/work -v $HOME/.m2:/home/$USER/.m2 jne-linux-ppc64le sh -c "cd /work && mvn test"
if [ ! $? -eq 0 ]; then
  exit 1
fi