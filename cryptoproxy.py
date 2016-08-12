#!/usr/bin/python
# found on http://voorloopnul.com/blog/a-python-proxy-in-less-than-100-lines-of-code/
import socket
import select
import time
import sys
import hmac
import hashlib
import base64
import json
from Crypto.Cipher import AES
from Crypto import Random

# Crypto stuff
search_secret_key = 'search-secret-shared-key-goes-here'
path_secret_key = hashlib.md5('path-secret-shared-key-goes-here').digest()

# Generate trapdoors for the index
def generate_trapdoor(data):
    trapdoor_generator = hmac.new(search_secret_key, data, hashlib.sha256)
    # Returning as b64 instead of hex`
    return base64.b64encode(trapdoor_generator.digest()).decode()

# Trapyfy all json fields execpt id which needs to be encrypted
def encrypt_json(json_str):
    data = json.loads(json_str)
    for key in data:
        if key == 'id':
            data[key] = encrypt_path(data[key])
        else:
            data[key] =  " ".join(map(lambda word: generate_trapdoor(word), data[key].split()))
    return json.dumps(data)

# Decrypt the ID field
def decrypt_json(json_str):
    data = json.loads(json_str)
    for key in data:
        if key == 'id':
            data[key] = decrypt_path(data[key])
    return json.dumps(data)

# AES CBC Blocksize and padding
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[:-ord(s[len(s)-1:])]

def encrypt_path(path):
    raw = pad(path)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(path_secret_key, AES.MODE_CBC, iv)
    return base64.b64encode(iv + cipher.encrypt(raw))

def decrypt_path(encPath):
    enc = base64.b64decode(encPath)
    iv = enc[:16]
    cipher = AES.new(path_secret_key, AES.MODE_CBC, iv)
    return unpad(cipher.decrypt(enc[16:]))

# Changing the buffer_size and delay, you can improve the speed and bandwidth.
# But when buffer get to high or delay go too down, you can brake things
buffer_size = 4096
delay = 0.0001
forward_to = ('localhost', 8983)

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
        # here we can parse and/or modify the data before send forward
        out_data = self.data

        # Proxy -> Solr
        if self.channel[self.s].getpeername()[1] == 8983:
            #out_data = generate_trapdoor(out_data)
            #out_data = encrypt_path(out_data)
            out_data = encrypt_json(out_data)
            print "Sending data do Solr:\n" + out_data

        # Proxy -> Client
        else:
            out_data = decrypt_json(out_data)
            print "Sending data do Client:\n" + out_data

        # Forward packet
        self.channel[self.s].send(out_data)

if __name__ == '__main__':
        # Here we define the listen-port
        server = TheServer('', 9090)
        #server = TheServer('', 8983)
        try:
            server.main_loop()
        except KeyboardInterrupt:
            print "Ctrl C - Stopping server"
            sys.exit(1)
