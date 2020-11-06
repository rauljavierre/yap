#!/bin/bash

sudo bash down.sh

gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi


cp ./build/libs/YapShortener.jar spring-docker
sudo docker-compose up --build --detach

pip3 install pika
pip3 install psutil
python3 src/main/python/get_host_information.py &

sleep 60

sudo gradle sonarqube
