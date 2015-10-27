rm -rf samples
git archive -o samples.zip HEAD
mkdir samples
unzip samples.zip -d samples/
rm samples/.gitignore
rm samples.zip
cd samples
mkdir lib
mkdir bin
cp ../scripts/*.sh bin/
cp ../scripts/*.command bin/
cp ../lib/* lib/
cd ..
zip -r9 samples.zip samples/