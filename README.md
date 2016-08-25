# crypto-proxy

## crypoproxy-index.py

```
python cryptoproxy-index.py
```

Starts a server listening on port *8984*

```
# Send the customers.xml file to the server for indexing using netcat
nc -w 3 localhost 8984 < sample_customer.xml
```

The server will then encrypt and forward the sample document customer.xml to Solr running on *marvel.inpher.io*


```
Ctrl+C to terminate server
```

## crypoproxy-search.py
```
python cryptoproxy-search.py
```

Starts a server listening on port *8983*

```
# Run a solr query using curl
curl 'http://localhost:8983/solr/GB0010001_Customer/select?rows=50&q=text%3A*Wiseau*'
```

The server will then encrypt and forward the query to Solr running on *marvel.inpher.io*


```
Ctrl+C to terminate server
```

## TODO

 * Multi Collection Support (switch between companies)

### References
https://cwiki.apache.org/confluence/display/solr/Uploading+Data+with+Index+Handlers
https://docs.python.org/2/library/xml.etree.elementtree.html
