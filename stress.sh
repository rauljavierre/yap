#!/bin/bash

while true
do
	curl -X POST -F 'url=https://github.com/' -F 'generateQR=true' http://localhost/link
done
