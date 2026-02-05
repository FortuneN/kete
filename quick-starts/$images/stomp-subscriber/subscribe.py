#!/usr/bin/env python3
import os
import sys
import time
import stomp

class Subscriber(stomp.ConnectionListener):
    def on_message(self, frame):
        print("EVENT-RECEIVED", flush=True)

    def on_error(self, frame):
        print(f"Error: {frame.body}", file=sys.stderr, flush=True)

def main():
    host = os.getenv('STOMP_HOST', 'localhost')
    port = int(os.getenv('STOMP_PORT', '61613'))
    destination = os.getenv('STOMP_DESTINATION', '/topic/keycloak-events')

    print(f"Connecting to {host}:{port}, subscribing to {destination}", flush=True)

    while True:
        try:
            conn = stomp.Connection([(host, port)])
            conn.set_listener('', Subscriber())
            conn.connect(wait=True)
            conn.subscribe(destination=destination, id=1, ack='auto')

            print(f"Subscribed to {destination}", flush=True)

            # Keep connection alive
            while conn.is_connected():
                time.sleep(1)

        except Exception as e:
            print(f"Connection error: {e}", file=sys.stderr, flush=True)
            time.sleep(5)

if __name__ == '__main__':
    main()
