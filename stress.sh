#!/bin/bash

while true
do
	curl -X POST -F 'url=http://airezico.tk' -F 'generateQR=true' http://localhost/link
done
