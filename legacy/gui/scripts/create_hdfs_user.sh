#!/bin/bash

if [[ "$1" = "" ]]; then
    echo "$0 <username>"
    exit 1
fi

cd $HADOOP_PREFIX

until bin/hdfs dfs -mkdir "/user/$1"; do :; done
bin/hdfs dfs -chown "$1:$1" "/user/$1"
