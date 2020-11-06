#!/bin/bash

pythonpid=$(ps aux | grep get_host_information.py | sed -e "s/[[:space:]]\+/ /g" | cut -d' ' -f2 | head -n1)
kill -9 $pythonpid

sudo docker stop $(docker ps -a -q)
sudo docker rm $(docker ps -a -q)
sudo docker volume rm $(docker volume ls -q)

sudo rm ./spring-docker/urlshortener.jar

