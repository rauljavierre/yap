#!/bin/bash

sudo docker stop $(docker ps -a -q)
sudo docker rm $(docker ps -a -q)
sudo docker volume rm $(docker volume ls -q)

sudo rm ./spring-docker/YapShortener.jar

