#!/bin/bash

printf "\n\n[up.sh] Tearing down old artifacts\n"
sudo bash down.sh

printf "\n[up.sh] Building gradle\n"
gradle build --stacktrace # sudo snap install gradle --classic

# If build was not successful... Exit with the build code
if [ $? -ne 0 ]; then
  exit
fi

printf "\n[up.sh] Copying the jar generated with gradle build\n"
cp ./build/libs/YapShortener.jar spring-docker

printf "\n[up.sh] Creating images\n"
sudo docker build -t yap_app --no-cache spring-docker
sudo docker build -t yap_nginx --no-cache nginx-docker

printf "\n[up.sh] Tearing up containers\n"
sudo docker stack deploy yap -c docker-compose.yml --prune
sudo docker service logs yap_nginx --follow
