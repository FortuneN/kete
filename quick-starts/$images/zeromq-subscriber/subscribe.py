#!/usr/bin/env python3
"""ZeroMQ subscriber for event reception verification."""
import os
import sys
import zmq

def main():
    endpoint = os.getenv('ZEROMQ_ENDPOINT', 'tcp://*:5555')
    socket_type = os.getenv('ZEROMQ_SOCKET_TYPE', 'PULL').upper()

    print(f"==================================================", flush=True)
    print(f"ZeroMQ Subscriber Starting", flush=True)
    print(f"  Endpoint: {endpoint}", flush=True)
    print(f"  Socket Type: {socket_type}", flush=True)
    print(f"==================================================", flush=True)

    context = zmq.Context()

    if socket_type == 'SUB':
        socket = context.socket(zmq.SUB)
        socket.bind(endpoint)
        socket.setsockopt_string(zmq.SUBSCRIBE, '')
    else:
        socket = context.socket(zmq.PULL)
        socket.bind(endpoint)

    print(f"Bound to {endpoint}, waiting for messages...", flush=True)

    try:
        while True:
            message = socket.recv()
            print("==================================================", flush=True)
            print("MESSAGE RECEIVED:", flush=True)
            print(message.decode('utf-8', errors='replace'), flush=True)
            print("==================================================", flush=True)
            print("EVENT-RECEIVED", flush=True)
    except KeyboardInterrupt:
        pass
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr, flush=True)
    finally:
        socket.close()
        context.term()

if __name__ == '__main__':
    main()
