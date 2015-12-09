#!/bin/sh
BASEDIR=$(dirname "$0")

cd "$BASEDIR/.." || exit 1 
rm -rf ~/.m2/repository/org/inpher/*
#(cd inpher-ux; mvn package) || exit 1
rm -rf inpher-sdk
mkdir inpher-sdk 
cp -r base/* inpher-sdk/
#mkdir inpher-sdk/bin
#cp scripts/setup.sh inpher-sdk/bin/
#cp scripts/setup.command inpher-sdk/bin/
#cp scripts/setup.cmd inpher-sdk/bin/
#cp scripts/gui.sh inpher-sdk/bin/
#cp scripts/gui.command inpher-sdk/bin/
#cp scripts/gui.cmd inpher-sdk/bin/
#cp scripts/download-dependencies.sh inpher-sdk/bin/
mkdir inpher-sdk/inpher-samples
cp inpher-samples/.classpath inpher-sdk/inpher-samples/
cp inpher-samples/.project inpher-sdk/inpher-samples/
cp -r inpher-samples/.settings inpher-sdk/inpher-samples/
cp inpher-samples/pom.xml inpher-sdk/inpher-samples/
cp -r inpher-samples/src inpher-sdk/inpher-samples/
#copy dependencies
#cp inpher-ux/target/inpher-ux-0.6.jar inpher-sdk/lib/
#if [ "x$1" = "x" ]; then
#    rm inpher-sdk-with-dependencies.zip
#    (cd inpher-sdk/lib; mvn dependency:copy-dependencies -DoutputDirectory=.) || exit 1
#    zip -r9 inpher-sdk-with-dependencies.zip inpher-sdk/
#else
    rm -rf inpher-sdk/lib #remove the lib dir
    rm inpher-sdk.zip
    zip -r9 inpher-sdk.zip inpher-sdk/
#fi
