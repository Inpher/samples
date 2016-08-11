# crypto-proxy

### How to test 
(keep a terminal open for each of the commands):

#### Simulate Solr: 
Create a listener on port 8983 (Solr)
```
nc -l 8983
```

#### Run the proxy:
```
./cryptoproxy.py
```

#### Connect to the proxy:
```
telnet localhost 9090
```
then type random stuff

