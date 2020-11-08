#!/bin/bash

sudo bash down.sh

gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi


cp ./build/libs/YapShortener.jar spring-docker
sudo docker-compose up --build --detach

sleep 40

pip3 install pika
pip3 install psutil
pip3 install hurry.filesize
python3 src/main/python/get_host_information.py &

# sudo gradle sonarqube
