#!/bin/bash

echo "[up.sh] Tearing down old artifacts"
sudo bash down.sh

echo "[up.sh] Building gradle"
gradle build --stacktrace # sudo snap install gradle --classic

if [ $? -ne 0 ]; then
  exit
fi


cp ./build/libs/YapShortener.jar spring-docker

echo "[up.sh] Tearing up containers"
sudo docker-compose up --build --detach

sleep 60

echo "[up.sh] Tearing up Python script (get_host_information)"
pip3 install pika
pip3 install psutil
pip3 install hurry.filesize
python3 src/main/python/get_host_information.py &

sleep 30

echo "[up.sh] Building SonarQube"
sudo gradle sonarqube
