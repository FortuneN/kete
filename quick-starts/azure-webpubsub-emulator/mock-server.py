from http.server import HTTPServer, BaseHTTPRequestHandler
import sys


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"ok")

    def do_POST(self):
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length).decode()
        print(f"POST {self.path} body: {body}", flush=True)
        self.send_response(202)
        self.end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()


if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 8090), Handler)
    print("Mock server listening on port 8090", flush=True)
    server.serve_forever()
