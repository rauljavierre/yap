#!/bin/bash

printf "\n\n[up.sh] Tearing down old artifacts\n"
sudo bash down.sh

printf "\n[up.sh] Building gradle\n"
gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit  # If build was not successful... Exit with the build code
fi

printf "\n[up.sh] Copying the jar generated with gradle build\n"
cp ./build/libs/YapShortener.jar spring-docker

printf "\n[up.sh] Creating images\n"
sudo docker build -t yap_app --no-cache spring-docker
sudo docker build -t yap_nginx --no-cache nginx-docker

sudo chown -R 472:472 grafana-data  # Fix Grafana permission trouble with volume

printf "\n[up.sh] Tearing up containers\n"
sudo docker stack deploy yap -c docker-compose.yml --prune
sudo docker service logs yap_app --follow
