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
then send a JSON

```
{"keywords": "bla bli blu", "id": "moep"}
```

you will see the following on the server

```
{"keywords": "IBnvMiCEBNVS2KeMvc5T93ZQV2VSvxh4eTuUK08ObXE= CjWPguwg/lKDmiSVbg7sBhRYEMSMHoVudDOi1qLQKBY= 61kdTgPSfCk/4uYG66ZOu6kx1wDVdSvgcC9UWMnfHfU=", "id": "Vp/4/VvUpJMaKTGLkSMiqe80rWgykhXXPmBwNJZYv0g="}
```

in the server terminal, enter the encrypted JSON from above

```
{"keywords": "IBnvMiCEBNVS2KeMvc5T93ZQV2VSvxh4eTuUK08ObXE= CjWPguwg/lKDmiSVbg7sBhRYEMSMHoVudDOi1qLQKBY= 61kdTgPSfCk/4uYG66ZOu6kx1wDVdSvgcC9UWMnfHfU=", "id": "moep"}
```

