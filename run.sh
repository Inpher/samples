#!/bin/bash

SOLR_NAME=insolr

function cleanup()
{
    DOCKER_CLEAN="docker kill $SOLR_NAME;docker rm $SOLR_NAME"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_CLEAN
    else
        $(echo $DOCKER_CLEAN) $> /dev/null
    fi
}

function startSolr()
{
    DOCKER_SOLR="docker run -td --name $SOLR_NAME -p 8983:8983 inpher/solr-frequency"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_SOLR
    else
        $(echo $DOCKER_SOLR)
    fi
}

#trap cleanup EXIT

LOCAL_STOR=$(pwd)/local_storage
mkdir $LOCAL_STOR

set -e

# Setup config.properties
cat > config.properties << EOM
cloudStorageType=LOCAL_STORAGE
localStorageRootFolder=$LOCAL_STOR
searchServerType=REMOTE_SOLR
solrURL=http://localhost:8983/solr/inpher-frequency
EOM

echo "[COMPILING WITH GRADLE]"
./gradlew installApp

PS="docker ps -fq name=$SOLR_NAME"
if [ "$(uname)" == "Darwin" ]; then
    IS="$(docker-machine ssh $(docker-machine ls -q) -C $PS)"
else
    IS="$(echo $PS)"
fi
if [ "$IS" == "" ]; then
    startSolr

    until [ "$(curl -s http://localhost:8983/solr/)" != "" &>/dev/null ]; do :; done
fi

# TODO start HDFS

echo "[START THE GUI APP]"
./build/install/sampleapp/bin/sampleapp
