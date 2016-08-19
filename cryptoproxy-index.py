#!/usr/bin/python
import socket
import select
import time
import sys
import hmac
import hashlib
import base64
from Crypto.Cipher import AES
from Crypto import Random
import StringIO
import requests
import xml.etree.ElementTree as ET

###### Crypto stuff
# Keys
search_secret_key = 'search-secret-shared-key-goes-here'
path_secret_key = hashlib.md5('path-secret-shared-key-goes-here').digest()

# AES CBC Blocksize and padding
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[:-ord(s[len(s)-1:])]

###### Server stuff
# Changing the buffer_size and delay, you can improve the speed and bandwidth.
# But when buffer get to high or delay go too down, you can brake things
listen_port = 8984
buffer_size = 4096*2
delay = 0.0001

# Solr Server
solr_host = 'marvel.inpher.io'
solr_port = '8080'
solr_timeout = 2 # seconds
solr_collection = 'GB0010001_Customer'
solr_url = 'http://' + solr_host + ':' + solr_port + '/solr/' + solr_collection + '/update'

# Generate trapdoors for the index
def generate_trapdoor(data):
    trapdoor_generator = hmac.new(search_secret_key, data, hashlib.sha256)
    # Returning as b64 instead of hex`
    return base64.b64encode(trapdoor_generator.digest()).decode()

def encrypt_ID(path):
    raw = pad(path)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(path_secret_key, AES.MODE_CBC, iv)
    return base64.b64encode(iv + cipher.encrypt(raw))

# Trapyfy all XML leaf fields execpt id which needs to be encrypted
def preprocess_XML(data):
    # Creating blank Solr XML
    solr_root = ET.Element('add')
    solr_root.attrib = {'commitWithin' : '1000', 'overwrite' : 'true'}
    doc_root = ET.SubElement(solr_root,'doc')

    # Reading data from Temenos (XML)
    sio = StringIO.StringIO(data)

    # Skip first garbled bytes (no clue why this is needed for java...)
    if data[0] != '<':
        sio.read(2);

    # Read the rest
    out_data = sio.read()
    # Construct XML tree
    root = ET.fromstring(out_data);
    # Iterate through all leafs
    for elem in root.iter():
        # leafs
        if len(elem.getchildren()) == 0 and elem.text != None:
            field = ET.Element('field')
            field.attrib = { 'name' : elem.tag}
            if elem.tag == 'id':
                field.text = encrypt_ID(elem.text)
            else:
                field.text = generate_trapdoor(elem.text)

            # Append field to solr_doc
            doc_root.insert(0,field)

    return ET.tostring(solr_root)

# Send encrypted XML document to Solr
def commit_solr(out_data):
     headers={'Content-Type' : 'text/xml'}
     try:
         r = requests.post(solr_url, data=out_data, headers=headers, timeout=solr_timeout)
         print str(r)
     except requests.exceptions.RequestException as e:
         print e

# Server Class
class TheServer:
    input_list = []
    channel = {}

    def __init__(self, host, port):
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server.bind((host, port))
        self.server.listen(200)

    def main_loop(self):
        self.input_list.append(self.server)
        while 1:
            time.sleep(delay)
            ss = select.select
            inputready, outputready, exceptready = ss(self.input_list, [], [])
            for self.s in inputready:
                if self.s == self.server:
                    self.on_accept()
                    break

                self.data = self.s.recv(buffer_size)
                if len(self.data) == 0:
                    self.on_close()
                    break
                else:
                    self.on_recv()

    def on_accept(self):
        clientsock, clientaddr = self.server.accept()
        print clientaddr, 'has connected'
        self.input_list.append(clientsock)

    def on_close(self):
        print self.s.getpeername(), 'has disconnected'
        #remove objects from input_list
        self.input_list.remove(self.s)

        # close the connection with client
        self.s.close()

    def on_recv(self):
        # Proxy -> Solr
        out_data = preprocess_XML(self.data)
        commit_solr(out_data)
        print 'Data sent to Solr'

# Main method
if __name__ == '__main__':
        # Here we define the listen-port
        server = TheServer('', listen_port)
        try:
            server.main_loop()
        except KeyboardInterrupt:
            print 'Ctrl C - Stopping server'
            sys.exit(1)
