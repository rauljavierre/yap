#!/bin/bash

sudo bash down.sh

cd ..
gradle build # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi

cd docker
cp ../build/libs/urlshortener.jar spring-docker
sudo docker-compose up --build
sudo bash down.sh
