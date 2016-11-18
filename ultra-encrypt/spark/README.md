# Spark word count sample application
## Overview
This [inpher encrypt module](https://dev.inpher.io/encrypt-module) sample is inspired by the official [word count sample](http://spark.apache.org/examples.html) provided by spark. The following ingestion, transformation and encryption flow is performed in this sample:

1. Read a text file
2. Preprocess text file (remove special characters and transform to lower case)
3. Create a flat map from words
4. Create a spark pair RDD containing word / count tuples
5. Make the union with another word count RDD
6. Encrypt the map using the Inpher encrypt module across the cluster
7. Store the encrypted map
8. Load the encrypted map again
9. Decrypt the encrypted map using the Inpher encrypt module across the cluster
10. Collect and print the top 100 words of the map

## How to run the app:

This sample will create a local spark test instance:

```bash
gradle run
```

The output will show the top 100 words in descending order including their number of occurrences.