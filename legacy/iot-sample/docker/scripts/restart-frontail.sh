#!/bin/bash
docker stop frontail-iot
docker rm -v frontail-iot
docker run --name frontail-iot -d -p 9331:9001 -P -v /home/ubuntu/log:/log mthenw/frontail /log/iot-container.log
