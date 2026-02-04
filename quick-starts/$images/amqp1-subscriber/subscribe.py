#!/usr/bin/env python3
"""AMQP 1.0 subscriber for event reception verification."""
import os
import sys
import time
from proton.handlers import MessagingHandler
from proton.reactor import Container

class Subscriber(MessagingHandler):
    def __init__(self, url, address):
        super(Subscriber, self).__init__()
        self.url = url
        self.address = address

    def on_start(self, event):
        print(f"Connecting to {self.url}, receiving from {self.address}", flush=True)
        conn = event.container.connect(self.url)
        event.container.create_receiver(conn, self.address)

    def on_message(self, event):
        print("EVENT-RECEIVED", flush=True)
        event.delivery.update(event.delivery.ACCEPTED)
        event.delivery.settle()

    def on_transport_error(self, event):
        print(f"Transport error: {event.transport.condition}", file=sys.stderr, flush=True)

    def on_connection_error(self, event):
        print(f"Connection error: {event.connection.remote_condition}", file=sys.stderr, flush=True)

def main():
    host = os.getenv('AMQP_HOST', 'localhost')
    port = os.getenv('AMQP_PORT', '5672')
    username = os.getenv('AMQP_USERNAME', '')
    password = os.getenv('AMQP_PASSWORD', '')
    address = os.getenv('AMQP_ADDRESS', 'keycloak-events')

    # Build connection URL
    if username and password:
        url = f"amqp://{username}:{password}@{host}:{port}"
    else:
        url = f"amqp://{host}:{port}"

    print(f"Starting AMQP 1.0 subscriber for {address}", flush=True)

    while True:
        try:
            Container(Subscriber(url, address)).run()
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"Error: {e}", file=sys.stderr, flush=True)
            time.sleep(5)

if __name__ == '__main__':
    main()
