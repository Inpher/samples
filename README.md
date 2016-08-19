# crypto-proxy

## crypoproxy-index.py

```
python cryptoproxy-index.py
```

Starts a server listening on port *8984*

```
# Send the customers.xml file to the server for indexing using netcat
nc -w 3 localhost 8984 < customer.xml
```

The server will then encrypt and forward the sample document customer.xml to Solr running on *marvel.inpher.io*


```
Ctrl+C to terminate server
```

## crypoproxy-search.py
not yet implemented
