#!/bin/sh
mvn com.spotify.fmt:fmt-maven-plugin:format && mvn clean install

echo 'Launching local instance'
./run.sh
