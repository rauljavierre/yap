#!/usr/bin/env python3

import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
import base64

if len(sys.argv)-1 != 2:
    print("""Usage: {} <port_number> <url>""".format(sys.argv[0]))
    sys.exit()

class Redirect(BaseHTTPRequestHandler):
    def do_GET(self):
        isAFrontendRoute = True if "/" == self.path or self.path.endswith(".html") or self.path.endswith(".js") or self.path.endswith(".png") else False

        if isAFrontendRoute:
            if self.path == "/":
                self.path = "/index.html"

            if self.path.endswith(".html"):
                f = open("." + self.path)
                self.send_response(200)
                self.send_header('Content-type', 'text/html')
                self.end_headers()
                self.wfile.write(bytes(f.read(), encoding="UTF-8"))
                f.close()

            elif self.path.endswith(".js"):
                f = open("." + self.path)
                self.send_response(200)
                self.send_header('Content-type', 'text/javascript')
                self.end_headers()
                self.wfile.write(bytes(f.read(), encoding="UTF-8"))
                f.close()

            elif self.path.endswith(".png"):
                f = open("." + self.path, 'rb')
                self.send_response(200)
                self.end_headers()
                self.wfile.write(f.read())
                f.close()

        else:
            self.send_response(302)
            self.send_header('Location', sys.argv[2] + self.path)
            self.end_headers()

HTTPServer(("", int(sys.argv[1])), Redirect).serve_forever()
