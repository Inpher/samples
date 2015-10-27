rm -rf ~/.m2/repository/org/inpher/*
cd ../inpher-ux/
mvn package
cd ..
rm -rf inpher-sdk
mkdir inpher-sdk 
cd inpher-sdk
mkdir lib 
mkdir bin
cp ../inpher-ux/target/lib/* lib/
cp ../inpher-ux/target/inpher-ux-0.6.jar lib/
cp ../scripts/setup.sh bin/setup.sh
cp ../scripts/setup.command bin/mac_setup.command
cp ../scripts/setup.cmd bin/win_setup.cmd
cp ../scripts/gui.sh bin/gui.sh
cp ../scripts/gui.command bin/mac_gui.command
cp ../scripts/gui.cmd bin/win_gui.cmd
mkdir inpher-samples
cp ../inpher-samples/.classpath inpher-samples/
cp ../inpher-samples/.project inpher-samples/
cp -Rp ../inpher-samples/.settings inpher-samples/
cp ../inpher-samples/pom.xml inpher-samples/
cp -Rp ../inpher-samples/src inpher-samples/
cd ..
zip -r9 inpher-sdk.zip inpher-sdk/