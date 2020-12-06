#!/bin/bash

number_of_apps=1

while true
do
  res=$(curl -s http://localhost/actuator/metrics/system.load.average.1m | grep value | cut -f2 -d:)
  echo "load: " $res
  echo "apps: "$number_of_apps
  if [ "$(echo " 5 < $res " | bc -l )" == 1 ] && [ "$number_of_apps" -eq "1" ]; then
    echo "SCALE UP"
    number_of_apps=$((number_of_apps + 1))
  elif [ "$(echo " 5 > $res " | bc -l )" == 1 ] && [ "$number_of_apps" -eq "2" ]; then
    echo "SCALE DOWN"
    number_of_apps=$((number_of_apps - 1))
  fi
done
