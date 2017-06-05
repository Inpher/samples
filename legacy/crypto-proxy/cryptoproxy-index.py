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

##### debug
debug = True

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
delay = 1 # server mode
#delay = 0.01 #testing

# Solr Server
#solr_host = 'marvel.inpher.io'
solr_host = 'marvel.inpher.io'
solr_port = '9090'
solr_timeout = 2 # seconds
solr_collection = 'GB0010001_Customer'
solr_url = 'http://' + solr_host + ':' + solr_port + '/solr/' + solr_collection + '/update'

# Generate trapdoors for the index
def generate_trapdoor(data):
    # get rid of trailing/leading spaces
    data = data.strip()
    # split whitespaces
    values = data.split(' ')

    # hmac each keyword seperately
    enc_values = []
    for v in values:
        trapdoor_generator = hmac.new(search_secret_key, v, hashlib.sha256)
        enc_values.append(base64.b64encode(trapdoor_generator.digest()).decode())

    # Returning as b64 instead of hex`
    return " ".join(enc_values)

def encrypt(path):
    raw = pad(path)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(path_secret_key, AES.MODE_CBC, iv)
    return base64.b64encode(iv + cipher.encrypt(raw))

def light_Solr_doc(data):
    # Creating blank Solr XML
    solr_root = ET.Element('add')
    solr_root.attrib = {'commitWithin' : '1000', 'overwrite' : 'true'}
    doc_root = ET.SubElement(solr_root,'doc')

    # Reading data from Temenos (XML)
    ns = '{http://www.temenos.com/T24/event/IFSampleEventFlow/CUSTOMER}'
    namespaces = {'tns': 'http://www.temenos.com/T24/event/IFSampleEventFlow/CUSTOMER'}
    sio = StringIO.StringIO(data)

    # Skip first garbled bytes (no clue why this is needed for java...)
    if data[0] != '<':
        sio.read(2)

    # Read the rest
    out_data = sio.read()
    # Construct XML tree
    root = ET.fromstring(out_data);

    ### ID
    tags = ['name1List','nameAddress','postCodeList','mnemonic','id','familyName']
    for tag in tags:
        t_tag = "tns:"+tag
        elem = root.find(t_tag,namespaces)

        if elem != None:
            # Solr field table
            if elem.tag == ns+'name1List':
                sub_elem = elem.find('tns:name1',namespaces)
                if sub_elem != None:
                    fields = create_field('table',sub_elem.text.strip())

            # Solr field address
            elif elem.tag == ns+'nameAddress':
                fields = create_field('address',elem.text.strip())

            # Solr field postcode
            elif elem.tag == ns+'postCodeList':
                sub_elem = elem.find('tns:postCode',namespaces)
                if sub_elem != None:
                    fields = create_field('postcode',sub_elem.text.strip())

            # Solr field name
            elif elem.tag == ns+'familyName':
                fields = create_field('name',elem.text.strip())

            # Solr field == xml field (id,mnemonic)
            else:
                fields = create_field(tag,elem.text.strip())

            doc_root.insert(0,fields[0])
            doc_root.insert(0,fields[1])

    # Check if all required fields are present (needed for search)
    sanity_check(doc_root)

    ## Index all other non empty fields
    already_visited = [ns + s for s in tags]
    i = 0
    for elem in root.iter():
        # leafs
        if len(elem.getchildren()) == 0 and elem.text != None and elem.tag not in already_visited:
            field = ET.Element('field')
            field.attrib = { 'name' : 'ENCfield'+str(i)}
            elem.text = elem.text.strip()
            field.text = generate_trapdoor(elem.text)
            doc_root.insert(0,field)
            i+=1

    if debug:
        print "DEBUG: Sending Encrypted Document to SOLR"
        print "========================================="
        print ET.tostring(solr_root)
        print "========================================="

    return ET.tostring(solr_root)

def sanity_check(xml_root):
    required_attributes = ['name','address','postcode','mnemonic','id','table']
    attributes_present = [child.attrib['name'] for child in xml_root]

    for attr in required_attributes:
        if not attr in attributes_present:
            field = ET.Element('field')
            field.attrib = { 'name' : attr}
            field.text =  ' - '
            xml_root.insert(0,field)

def create_field(solr_tag,value):
    field_e = ET.Element('field')
    field_t = ET.Element('field')
    field_e.attrib = { 'name' : solr_tag}
    field_t.attrib = { 'name' : 'ENC'+solr_tag}
    field_e.text =  encrypt(value)
    #field_e.text =  value
    field_t.text =  generate_trapdoor(value)
    return [field_e , field_t]

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
        out_data = light_Solr_doc(self.data)
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
