#!/bin/sh

BASEDIR=$(dirname "$0")
java -cp "$BASEDIR/../lib/*" application.SetupApp
