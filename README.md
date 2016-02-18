# gui-sample-app

This is a GUI sample application to show developers how to use the Inpher SDK.
The application is a simple dropbox-like client which allows the user to
register/login, create directories, upload/download files and search.

## Setup

### Requirements

- Docker
- OS X or Linux
- JAVA: JDK 8

### Build

To build the App before running it, run:
```
    ./gradlew installApp
```

### How to start

To start the GUI and all the backends:
```
    chmod u+x run.sh
    ./run.sh
```

## How it works

The `run` script starts sets the backend in docker containers to easily use the
client. For the client to work, there needs to be a search backend and a storage
backend. Both backend are started by the script in docker containers.

### Search Backend

The search backend used by default with the sample app is **Solr**. The `run`
script starts **Solr** in a docker container and sets the inpher config
properties file accordingly.

Supported search backends:
- **elastic**: https://www.elastic.co/
- **Solr**: http://lucene.apache.org/solr/

### Storage Backend

The storage backend used currently is HDFS. The script will start it in a docker
container and set the proper user permissions.

Supported storage backeds:
- **Amazon S3**: https://aws.amazon.com/s3/
- **Local Storage**: this is just a local folder mounted on the client file
  system.
- **HDFS**: https://hadoop.apache.org/docs/r1.2.1/hdfs_design.html

### The Client

The client uses the Inpher SDK to easily encrypt data before sending it on the
cloud. This ensures the security and the privacy of sensitive user data.

The client allows a user to register or login through the inpher SDK which sets
up all the required cryptography keys. Once registered and logged in, the user
can add directories, upload new files. A tree view of the remote file system on
the cloud allows the user the see what files he uploaded and gets a preview of
the file metadata. The files can be downloaded and opened.

One of the most interesting features is to be able to search on the encrypted
uploaded documents. A search box is available in the sample client to
demonstrate how to use the Inpher SDK search feature.

