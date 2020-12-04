#!/bin/bash

while true
do
	services=$(sudo docker service ls --format '{{.ID}}')
	for service in $services; do
  		tasks=$(sudo docker service ps $service --format '{{.ID}}')
  		echo
  		echo
  		for task in $tasks; do
        sudo docker inspect $task --format '{{.Spec.ContainerSpec.Image}}'
        sudo docker inspect $task --format '{{.Status.Message}}'
        if sudo docker inspect $task --format '{{.Status.Message}}' | grep 'insufficient resources' 1>/dev/null; then
          echo "TODO: CREATE MORE REPLICAS"
          # sudo docker service --scale yap_app=2
        fi
  		done
	done
done
