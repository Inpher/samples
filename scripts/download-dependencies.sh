#!/bin/sh

DIR=$(dirname "$0")
(cd "$DIR/../lib" && mvn dependency:copy-dependencies -DoutputDirectory=.) || exit 1
