#!/bin/bash

printf "\n[up.sh] REMEMBER THAT IF YOU WANT TO GET SONARQUBE REPORTS ON PORT 9000 YOU MUST EXECUTE 'SUDO GRADLE SONARQUE' AFTER THIS SCRIPT"
printf "\n\n[up.sh] Tearing down old artifacts"
sudo bash down.sh

printf "\n[up.sh] Building gradle"
gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi

cp ./build/libs/YapShortener.jar spring-docker

printf "\n[up.sh] Tearing up containers"
sudo docker-compose up --build

