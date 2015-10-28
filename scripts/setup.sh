#!/bin/sh

BASEDIR=$(dirname "$0")

if [ ! -f "$BASEDIR/../lib/clientapi-0.6-beta.jar" ]
then
    . "$BASEDIR/download-dependencies.sh"
fi

java -cp "$BASEDIR/../lib/*" application.SetupApp
