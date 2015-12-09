Inpher SDK for Java
=====================

The Inpher SDK for Java enables application developers to index and
secure sensitive data at the point of creation while reducing cloud
computing cost by searching encrypted data without decryption. The SDK is
configured for rapid deployment on AWS with Inpher's client-side
libraries and search server built on top of Apache Solr. Cloud operations
teams can scale the infrastructure without security compromises because
data and search indexes are encrypted at all times.

Features
=========

The Inpher SDK for Java includes:

 * Inpher Java Libraries — 
    the libraries makes coding easy by providing Java APIs for all
    client-side functions such as encryption, decryption, keyword extraction,
    indexing, and more. Encrypted File System — pre-configured for rapid
    deployment on Amazon Web Services Simple Storage Service (AWS S3) yet
    adaptable for any private or public cloud environment.
 * Solr for Encrypted Data — 
    ready to deploy pre-configured Solr server based on the open source Solr
    platform from the Apache Lucene project. The server is available as
    Amazon Machine Image (AMI) to run on Amazon EC2 or standalone for private
    cloud deployments. See http://inpher.io/beta for more information.

This product requires additional third party libraries, licensed
independently from this product. Please refer to these libraries directly
for their respective licensing information.

Running the samples
====================

Setup your environment:
-----------------------
1. mvn dependency:copy-dependencies
2. create a config.properties configuration file:
    java -cp 'target/dependency/*' org.inpher.clientapi.GenerateConfigProperties
3. manually edit the config.properties file to point it to your backend

Compile and run your project
----------------------------
4. mvn compile
5. execute your project
    java -cp 'target/classes:target/dependency/*' your.main.Class

