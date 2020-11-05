#!/bin/bash

sudo bash down.sh

gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi

cp ./build/libs/urlshortener.jar spring-docker
sudo docker-compose up --build
sudo bash down.sh
