#!/usr/bin/python
# found on http://voorloopnul.com/blog/a-python-proxy-in-less-than-100-lines-of-code/
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

#### Server stiff
# Changing the buffer_size and delay, you can improve the speed and bandwidth.
# But when buffer get to high or delay go too down, you can brake things
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

def parse(f):
    h = lambda s: ' '.join('%.2X' % ord(x) for x in s) # format as hex
    p = lambda s: sum(ord(x)*256**i for i, x in enumerate(reversed(s))) # parse integer
    magic = f.read(2)
    assert magic == '\xAC\xED', h(magic) # STREAM_MAGIC
    assert p(f.read(2)) == 5 # STREAM_VERSION
    handles = []
    def parse_obj():
        b = f.read(1)
        if not b:
            raise StopIteration # not necessarily the best thing to throw here.
        if b == '\x70': # p TC_NULL
            return None
        elif b == '\x71': # q TC_REFERENCE
            handle = p(f.read(4)) - 0x7E0000 # baseWireHandle
            o = handles[handle]
            return o[1]
        elif b == '\x74': # t TC_STRING
            string = f.read(p(f.read(2))).decode('utf-8')
            handles.append(('TC_STRING', string))
            return string
        elif b == '\x75': # u TC_ARRAY
            data = []
            cls = parse_obj()
            size = p(f.read(4))
            handles.append(('TC_ARRAY', data))
            assert cls['_name'] in ('[B', '[I'), cls['_name']
            for x in range(size):
                data.append(f.read({'[B': 1, '[I': 4}[cls['_name']]))
            return data
        elif b == '\x7E': # ~ TC_ENUM
            enum = {}
            enum['_cls'] = parse_obj()
            handles.append(('TC_ENUM', enum))
            enum['_name'] = parse_obj()
            return enum
        elif b == '\x72': # r TC_CLASSDESC
            cls = {'fields': []}
            full_name = f.read(p(f.read(2)))
            cls['_name'] = full_name.split('.')[-1] # i don't care about full path
            f.read(8) # uid
            cls['flags'] = f.read(1)
            handles.append(('TC_CLASSDESC', cls))
            assert cls['flags'] in ('\2', '\3', '\x0C', '\x12'), h(cls['flags'])
            b = f.read(2)
            for i in range(p(b)):
                typ = f.read(1)
                name = f.read(p(f.read(2)))
                fcls = parse_obj() if typ in 'L[' else ''
                cls['fields'].append((name, typ, fcls.split('/')[-1])) # don't care about full path
            b = f.read(1)
            assert b == '\x78', h(b)
            cls['parent'] = parse_obj()
            return cls
        # TC_OBJECT
        assert b == '\x73', (h(b), h(f.read(4)), repr(f.read(50)))
        obj = {}
        obj['_cls'] = parse_obj()
        obj['_name'] = obj['_cls']['_name']
        handle = len(handles)
        parents = [obj['_cls']]
        while parents[0]['parent']:
            parents.insert(0, parents[0]['parent'])
        handles.append(('TC_OBJECT', obj))
        for cls in parents:
            for name, typ, fcls in cls['fields'] if cls['flags'] in ('\2', '\3') else []:
                if typ == 'I': # Integer
                    obj[name] = p(f.read(4))
                elif typ == 'S': # Short
                    obj[name] = p(f.read(2))
                elif typ == 'J': # Long
                    obj[name] = p(f.read(8))
                elif typ == 'Z': # Bool
                    b = f.read(1)
                    assert p(b) in (0, 1)
                    obj[name] = bool(p(b))
                elif typ == 'F': # Float
                    obj[name] = h(f.read(4))
                elif typ in 'BC': # Byte, Char
                    obj[name] = f.read(1)
                elif typ in 'L[': # Object, Array
                    obj[name] = parse_obj()
                else: # Unknown
                    assert False, (name, typ, fcls)
            if cls['flags'] in ('\3', '\x0C'): # SC_WRITE_METHOD, SC_BLOCKDATA
                b = f.read(1)
                if b == '\x77': # see the readObject / writeObject methods
                    block = f.read(p(f.read(1)))
                    if cls['_name'].endswith('HashMap') or cls['_name'].endswith('Hashtable'):
                        # http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java/util/HashMap.java.html
                        # http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java/util/Hashtable.java.html
                        assert len(block) == 8, h(block)
                        size = p(block[4:])
                        obj['data'] = [] # python doesn't allow dicts as keys
                        for i in range(size):
                            k = parse_obj()
                            v = parse_obj()
                            obj['data'].append((k, v))
                        try:
                            obj['data'] = dict(obj['data'])
                        except TypeError:
                            pass # non hashable keys
                    elif cls['_name'].endswith('HashSet'):
                        # http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java/util/HashSet.java.html
                        assert len(block) == 12, h(block)
                        size = p(block[-4:])
                        obj['data'] = []
                        for i in range(size):
                            obj['data'].append(parse_obj())
                    elif cls['_name'].endswith('ArrayList'):
                        # http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java/util/ArrayList.java.html
                        assert len(block) == 4, h(block)
                        obj['data'] = []
                        for i in range(obj['size']):
                            obj['data'].append(parse_obj())
                    else:
                        assert False, cls['_name']
                    b = f.read(1)
                assert b == '\x78', h(b) + ' ' + repr(f.read(30)) # TC_ENDBLOCKDATA
        handles[handle] = ('py', obj)
        return obj
    objs = []
    while 1:
        try:
            objs.append(parse_obj())
        except StopIteration:
            return objs

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

def decrypt_path(encPath):
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
    mod_query_qparam = ':'.join(split)

    ## Reconstruct
    query['q'][0] = mod_query_qparam.encode('utf-8')
    query['fl'] = 'id,mnemonic,name,address,postcode,table'.encode('utf-8')
    lst = list(url)
    lst[3] = urllib.urlencode(query,doseq=True)

    return reassemble_HTTP_raw(query_str, urlparse.urlunsplit(tuple(lst)))

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
            print "Sending data do Solr:\n" + out_data

        # Proxy -> Client
        else:
            out_data = self.data
            #out_data = decrypt_json(out_data)
            print parse(self.data)
            print "Sending data do Client:\n" + out_data

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
