#!/bin/bash

number_of_apps=1

while true
do
  res=$(curl -s http://localhost/actuator/metrics/system.load.average.1m | grep value | cut -f2 -d:)
  echo "load: " $res
  echo "apps: "$number_of_apps
  if [ "$(echo " 5 < $res " | bc -l )" == 1 ] && [ "$number_of_apps" -eq "1" ]; then
    number_of_apps=$((number_of_apps + 1))
    sudo docker service scale yap_app=2
  elif [ "$(echo " 5 > $res " | bc -l )" == 1 ] && [ "$number_of_apps" -eq "2" ]; then
    number_of_apps=$((number_of_apps - 1))
    sudo docker service scale yap_app=1
  fi
  sleep 1
done