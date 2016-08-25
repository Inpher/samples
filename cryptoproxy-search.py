#!/usr/bin/python
import socket
import select
import time
import sys
import hmac
import hashlib
import base64
import re
from Crypto.Cipher import AES
from Crypto import Random
import urllib
import urlparse
import xml.etree.ElementTree as ET

#### Server stiff
# Changing the buffer_size and delay, you can improve the speed and bandwidth.
# But when buffer get to high or delay go too down, you can brake things
debug = True
buffer_size = 4096
delay = 0.1
solr_port = 9090
solr_host = 'marvel.inpher.io'
forward_to = (solr_host, solr_port)

#### Crypto stuff
search_secret_key = 'search-secret-shared-key-goes-here'
path_secret_key = hashlib.md5('path-secret-shared-key-goes-here').digest()
# AES CBC Blocksize and padding
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[:-ord(s[len(s)-1:])]

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

def decrypt(encPath):
    enc = base64.b64decode(encPath)
    iv = enc[:16]
    cipher = AES.new(path_secret_key, AES.MODE_CBC, iv)
    return unpad(cipher.decrypt(enc[16:]))

def get_HTTP_path(http_string):
    ind1 = http_string.find('\r\n')
    http_l1 = http_string[0:ind1].split(' ')
    return http_l1[1]

def reassemble_HTTP_raw(http_orig, new_path):
    ind1 = http_orig.find('\r\n')
    http_l1 = http_orig[0:ind1].split(' ')
    http_rest = http_orig[ind1+1:]
    http_l1[1] = new_path
    http_l1 = ' '.join(http_l1)
    return http_l1 + http_rest

def get_HTTP_xml(http_string):
    ind1 = http_string.find('<?xml')
    return http_string[ind1:]

def reassemble_HTTP_xml(http_orig, new_xml):
    new_xml = '<?xml version="1.0" encoding="UTF-8"?>\n'+new_xml+'\n'
    ind_xml = http_orig.find('<?xml')
    ind_len = http_orig.find('Content-Length:')
    ind_nwl = [m.start() for m in re.finditer('\r\n', http_orig)]
    http_begin = http_orig[0:ind_xml]
    old_len = http_begin[ind_len+16:ind_nwl[2]]
    new_len = str(len(new_xml))
    http_begin = http_begin.replace('Content-Length: '+old_len, 'Content-Length: '+new_len)
    return http_begin + new_xml

def modify_query(query_str):
    # query = "text:*queryword*"
    # enc_query = "_text_:enc(queryword)"

    ## Deconstruct
    url = urlparse.urlsplit(urllib.unquote(get_HTTP_path(query_str)))
    query = urlparse.parse_qs(url[3])
    query_qparam = query['q'][0]

    ## Modify
    split = query_qparam.split(':')
    split[1] = generate_trapdoor(re.sub('[*]','',split[1]))
    split[0] = '_text_'
    mod_query_qparam = (':'.join(split)).encode('utf-8')

    ## Reconstruct
    query['q'][0] = mod_query_qparam
    query['fl'] = 'id,mnemonic,name,address,postcode,table'.encode('utf-8')
    lst = list(url)
    lst[3] = urllib.urlencode(query,doseq=True)

    if debug:
        print "\nDEBUG: Receiving Query..."
        print "  Plain query            :    " + query_qparam
        print "  Encrypting query       :    " + mod_query_qparam
        print ""

    return reassemble_HTTP_raw(query_str, urlparse.urlunsplit(tuple(lst)))

def modify_response(response_str):
    response_xml = get_HTTP_xml(response_str)
    root = ET.fromstring(response_xml);
    result = root.find('result')
    if result != None:
        for doc in result:
            for elem in doc:
                if elem.attrib['name'] == 'id':
                    elem.text = decrypt(elem.text)
                else:
                    child = elem.find('str')
                    child.text = decrypt(child.text)

    response_xml_decrypted = ET.tostring(root)

    if debug:
        print "\nDEBUG: Receiving Solr response..."
        print "Encrypted response     :    \n" + response_xml
        print "Decrypted response     :    \n" + response_xml_decrypted
        print ""

    return reassemble_HTTP_xml(response_str,response_xml_decrypted)


class Forward:
    def __init__(self):
        self.forward = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def start(self, host, port):
        try:
            self.forward.connect((host, port))
            return self.forward
        except Exception, e:
            print e
            return False

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
        forward = Forward().start(forward_to[0], forward_to[1])
        clientsock, clientaddr = self.server.accept()
        if forward:
            print clientaddr, "has connected"
            self.input_list.append(clientsock)
            self.input_list.append(forward)
            self.channel[clientsock] = forward
            self.channel[forward] = clientsock
        else:
            print "Can't establish connection with remote server.",
            print "Closing connection with client side", clientaddr
            clientsock.close()

    def on_close(self):
        print self.s.getpeername(), "has disconnected"
        #remove objects from input_list
        self.input_list.remove(self.s)
        self.input_list.remove(self.channel[self.s])
        out = self.channel[self.s]
        # close the connection with client
        self.channel[out].close()  # equivalent to do self.s.close()
        # close the connection with remote server
        self.channel[self.s].close()
        # delete both objects from channel dict
        del self.channel[out]
        del self.channel[self.s]

    def on_recv(self):
        # Proxy -> Solr
        if self.channel[self.s].getpeername()[1] == 9090:
            out_data = modify_query(self.data)
            print "Sending data do Solr..."

        # Proxy -> Client
        else:
            out_data = modify_response(self.data)
            print "Sending data do Client:..."


        # Forward packet
        self.channel[self.s].send(out_data)

if __name__ == '__main__':
        # Here we define the listen-port
        server = TheServer('', 8983)
        try:
            server.main_loop()
        except KeyboardInterrupt:
            print "Ctrl C - Stopping server"
            sys.exit(1)
