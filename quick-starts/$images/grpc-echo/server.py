import grpc
import json
import threading
from concurrent import futures
from http.server import HTTPServer, BaseHTTPRequestHandler

events = []


class EchoHandler(grpc.GenericRpcHandler):
    def service(self, handler_call_details):
        return grpc.unary_unary_rpc_method_handler(
            lambda request, context: self._handle(request, handler_call_details.method),
        )

    def _handle(self, request, method):
        payload = request.decode("utf-8", errors="replace")
        print(f"EVENT-RECEIVED: {payload}", flush=True)
        events.append(payload)
        return b"OK"


class EventsHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/events":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps(events).encode())
        elif self.path == "/health":
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass


def start_http_server(port):
    server = HTTPServer(("0.0.0.0", port), EventsHandler)
    server.serve_forever()


def main():
    grpc_port = 50051
    http_port = 8080

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    server.add_generic_rpc_handlers([EchoHandler()])
    server.add_insecure_port(f"0.0.0.0:{grpc_port}")

    http_thread = threading.Thread(target=start_http_server, args=(http_port,), daemon=True)
    http_thread.start()

    server.start()
    print(f"gRPC echo server started on port {grpc_port}", flush=True)
    print(f"HTTP events endpoint on port {http_port}", flush=True)
    server.wait_for_termination()


if __name__ == "__main__":
    main()
