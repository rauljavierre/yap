#!/bin/bash

sudo docker stack rm yap

sudo docker stop $(sudo docker ps -a -q)
sudo docker rm $(sudo docker ps -a -q)
sudo docker volume rm $(sudo docker volume ls -q)

sudo rm ./ServiceURLsQRs/spring-docker/ServiceURLsQRs.jar
sudo rm ./ServiceCSVs/spring-docker/ServiceCSVs.jar
sudo rm ./ServiceWorker/spring-docker/ServiceWorker.jar

