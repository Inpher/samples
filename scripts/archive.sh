#!/bin/sh
BASEDIR=$(dirname "$0")

cd "$BASEDIR/.." 

(cd inpher-ux; mvn package) || exit 1
rm -rf inpher-sdk
mkdir inpher-sdk 
mkdir inpher-sdk/lib 
cp inpher-ux/target/lib/* inpher-sdk/lib/
cp inpher-ux/target/inpher-ux-0.6.jar inpher-sdk/lib/
mkdir inpher-sdk/bin
cp scripts/setup.sh inpher-sdk/bin/setup.sh
cp scripts/setup.command inpher-sdk/bin/mac_setup.command
cp scripts/setup.cmd inpher-sdk/bin/win_setup.cmd
cp scripts/gui.sh inpher-sdk/bin/gui.sh
cp scripts/gui.command inpher-sdk/bin/mac_gui.command
cp scripts/gui.cmd inpher-sdk/bin/win_gui.cmd
mkdir inpher-sdk/inpher-samples
cp inpher-samples/.classpath inpher-sdk/inpher-samples/
cp inpher-samples/.project inpher-sdk/inpher-samples/
cp inpher-samples/.settings inpher-sdk/inpher-samples/
cp inpher-samples/pom.xml inpher-sdk/inpher-samples/
cp -r inpher-samples/src inpher-sdk/inpher-samples/
zip -r9 inpher-sdk.zip inpher-sdk/
