#!/bin/bash

SOLR_NAME=insolr

HDFS_NAME=hdfs
HDFS_PORT=9000

function cleanup()
{
    DOCKER_CLEAN="docker kill $SOLR_NAME;docker rm $SOLR_NAME"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_CLEAN
    else
        $(echo $DOCKER_CLEAN) $> /dev/null
    fi
}

function rminsolr()
{
    RM_INSOLR="docker rm insolr"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine ssh $(docker-machine ls -q) -C $RM_INSOLR
    else
        $(echo $RM_INSOLR)
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

function rmHdfs()
{
    RM_HDFS="docker rm hdfs"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine ssh $(docker-machine ls -q) -C $RM_HDFS
    else
        $(echo $RM_HDFS)
    fi
}

function startHdfs()
{
    DOCKER_RUN_HDFS="docker run -td --name $HDFS_NAME -p $HDFS_PORT:$HDFS_PORT sequenceiq/hadoop-docker &> /dev/null"
    DOCKER_CP_SCRIPT="docker cp scripts/create_hdfs_user.sh $HDFS_NAME:/"
    DOCKER_EXEC_SCRIPT="docker exec $HDFS_NAME /create_hdfs_user.sh $(whoami) &> /dev/null"
    if [ "$(uname)" == "Darwin" ]; then
        docker-machine scp -r scripts $(docker-machine ls -q):scripts
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_RUN_HDFS
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_CP_SCRIPT
        docker-machine ssh $(docker-machine ls -q) -C $DOCKER_EXEC_SCRIPT
        docker-machine ssh $(docker-machine ls -q) -C "rm -rf scripts"
    else
        $(echo $DOCKER_RUN_HDFS)
        $(echo $DOCKER_CP_SCRIPT)
        $(echo $DOCKER_EXEC_SCRIPT)
    fi
}

#trap cleanup EXIT

set -e

# Setup config.properties
cat > config.properties << EOM
cloudStorageType=HDFS_STORAGE
hdfsURI=hdfs://localhost:9000
searchServerType=REMOTE_SOLR
solrURL=http://localhost:8983/solr/inpher-frequency
EOM

PS="docker ps -fq name=$SOLR_NAME"
if [ "$(uname)" == "Darwin" ]; then
    IS="$(docker-machine ssh $(docker-machine ls -q) -C $PS)"
else
    IS="$(echo $PS)"
fi
if [ "$IS" == "" ]; then
    rminsolr || true
    startSolr

    until [ "$(curl -s http://localhost:8983/solr/)" != "" &>/dev/null ]; do :; done
fi

PS_HDFS="docker ps -fq name=$HDFS_NAME"
if [ "$(uname)" == "Darwin" ]; then
    IS="$(docker-machine ssh $(docker-machine ls -q) -C $PS_HDFS)"
else
    IS="$(echo $PS)"
fi
if [ "$IS" == "" ]; then
    rmHdfs || true
    startHdfs
fi

echo "[START THE GUI APP]"
./build/install/sampleapp/bin/sampleapp
