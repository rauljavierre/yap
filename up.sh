#!/bin/bash

printf "[up.sh] Tearing down old artifacts\n"
sudo bash down.sh

printf "\n[up.sh] Building gradle\n"
cd ServiceURLsQRs
gradle build --stacktrace # sudo snap install gradle --classic
cd -

cd ServiceCSVs
gradle build --stacktrace # sudo snap install gradle --classic
cd -

cd ServiceWorker
gradle build --stacktrace # sudo snap install gradle --classic
cd -

if [ $? -ne 0 ]; then
  exit  # If build was not successful... Exit with the build code
fi

printf "\n[up.sh] Copying the jar generated with gradle build\n"
cp ./ServiceURLsQRs/build/libs/ServiceURLsQRs.jar ./ServiceURLsQRs/spring-docker
cp ./ServiceCSVs/build/libs/ServiceCSVs.jar ./ServiceCSVs/spring-docker
cp ./ServiceWorker/build/libs/ServiceWorker.jar ./ServiceWorker/spring-docker


printf "\n[up.sh] Creating images\n"
sudo docker build -t yap_python --no-cache python-docker
sudo docker build -t yap_urlsqrs --no-cache ServiceURLsQRs/spring-docker
sudo docker build -t yap_csvsmaster --no-cache ServiceCSVs/spring-docker
sudo docker build -t yap_csvsworker --no-cache ServiceWorker/spring-docker
sudo docker build -t yap_nginx --no-cache nginx-docker

sudo chown -R 472:472 grafana-data  # Fix Grafana permission trouble with volume

printf "\n[up.sh] Tearing up containers\n"
sudo docker stack deploy yap -c docker-compose.yml --prune

# Start monitoring -> reescale CSV workers if necessary
# sudo bash monitor.sh