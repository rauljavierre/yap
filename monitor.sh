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
        sudo docker inspect $task --format '{{.Status}}'
        if sudo docker inspect $task --format '{{.Status}}' | grep 'insufficient resources' 1>/dev/null; then
          echo "TODO: CREATE MORE REPLICAS"
        fi
  		done
	done
done
