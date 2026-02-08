# ZeroMQ Destination

Stream Keycloak events to ZeroMQ peers using JeroMQ (pure Java implementation).

| Property | Value |
|----------|-------|
| **`destination.kind`** | `zeromq` |
| **Protocol** | ZMTP (ZeroMQ Message Transport Protocol) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Any ZeroMQ application** | Language-agnostic, 40+ language bindings |
| **PyZMQ** | Python ZeroMQ binding |
| **CZMQ** | High-level C binding |
| **NetMQ** | .NET ZeroMQ binding |
| **JeroMQ** | Pure Java ZeroMQ implementation |
| **ZMQ.js** | Node.js ZeroMQ binding |

!!! note "Brokerless Architecture"
    ZeroMQ is a **brokerless** messaging library — there is no broker to deploy or manage. KETE connects directly to ZeroMQ peers (subscribers or workers) over TCP, IPC, or other ZeroMQ transports. This makes it ideal for low-latency, high-throughput scenarios where a broker is unnecessary overhead.

!!! note "JeroMQ — No Native Libraries"
    KETE uses [JeroMQ](https://github.com/zeromq/jeromq), a pure Java implementation of ZeroMQ. No native libraries (libzmq) are required, making deployment simple with no platform-specific dependencies.



## Example Configurations

=== "Publish/Subscribe (PUB/SUB)"

    ```bash
    kete.routes.zmq-pub.destination.kind=zeromq
    kete.routes.zmq-pub.destination.endpoint=tcp://*:5556
    kete.routes.zmq-pub.destination.socket-type=PUBLISH
    kete.routes.zmq-pub.destination.connection-mode=BIND
    ```

=== "Push/Pull (PUSH/PULL)"

    ```bash
    kete.routes.zmq-push.destination.kind=zeromq
    kete.routes.zmq-push.destination.endpoint=tcp://*:5557
    kete.routes.zmq-push.destination.socket-type=PUSH
    kete.routes.zmq-push.destination.connection-mode=BIND
    ```

=== "Connect to Remote Subscriber"

    ```bash
    kete.routes.zmq-remote.destination.kind=zeromq
    kete.routes.zmq-remote.destination.endpoint=tcp://subscriber.example.com:5556
    kete.routes.zmq-remote.destination.socket-type=PUBLISH
    # connection-mode defaults to CONNECT
    ```

=== "Custom Linger"

    ```bash
    kete.routes.zmq-linger.destination.kind=zeromq
    kete.routes.zmq-linger.destination.endpoint=tcp://*:5556
    kete.routes.zmq-linger.destination.socket-type=PUBLISH
    kete.routes.zmq-linger.destination.connection-mode=BIND
    kete.routes.zmq-linger.destination.linger=5000
    ```



## Features

- Brokerless peer-to-peer messaging (no broker required)
- PUB/SUB and PUSH/PULL messaging patterns
- BIND or CONNECT connection modes
- Pure Java implementation (JeroMQ) — no native libraries
- Ultra-low latency, high throughput
- Configurable linger time for pending messages on close
- Supports TCP, IPC, and other ZeroMQ transports



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `endpoint` | ZeroMQ endpoint URI | `tcp://*:5556` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `socket-type` | `PUBLISH` | Socket pattern: `PUBLISH` (PUB/SUB) or `PUSH` (PUSH/PULL) | `PUSH` |
| `connection-mode` | `CONNECT` | Connection mode: `BIND` (listen) or `CONNECT` (dial) | `BIND` |
| `linger` | `1000` | Milliseconds to wait for pending messages on close. `-1` = wait forever, `0` = discard immediately | `5000` |

### Socket Types

| Socket Type | ZeroMQ Socket | Pattern | Description |
|-------------|---------------|---------|-------------|
| `PUBLISH` | `PUB` | PUB/SUB | Fan-out to all connected subscribers. Messages are dropped if no subscribers are connected. |
| `PUSH` | `PUSH` | PUSH/PULL | Round-robin distribution to connected workers. Messages are queued if no workers are connected. |

### Connection Modes

| Mode | Description | Use When |
|------|-------------|----------|
| `BIND` | Listen on the endpoint for incoming connections | KETE is the stable node (server-side), subscribers/workers connect to it |
| `CONNECT` | Connect to a remote endpoint | Subscribers/workers are the stable nodes, KETE connects to them |

!!! tip "BIND vs CONNECT"
    The general rule: the **stable** node (the one that stays up longer) should `BIND`, and the **transient** node should `CONNECT`. In most KETE deployments, Keycloak is the stable node, so use `BIND`. If your subscribers are long-running services and Keycloak may restart, use `CONNECT`.

### Endpoint Formats

ZeroMQ supports several transport protocols:

| Transport | Format | Example |
|-----------|--------|---------|
| **TCP** | `tcp://<host>:<port>` | `tcp://*:5556`, `tcp://192.168.1.100:5556` |
| **IPC** | `ipc://<path>` | `ipc:///tmp/keycloak-events` |
| **In-process** | `inproc://<name>` | `inproc://events` |

!!! note "TCP Bind Address"
    When using `BIND` mode with TCP, use `tcp://*:<port>` to listen on all interfaces, or `tcp://0.0.0.0:<port>` for the same effect. Use a specific IP address to bind to a particular interface.



## Limitations

- **No TLS** — ZeroMQ uses CurveZMQ for encryption (not implemented in KETE). Use network-level security (VPN, mTLS proxy) for encrypted transport.
- **No authentication** — ZeroMQ's PLAIN and CURVE authentication mechanisms are not supported. Use network-level security.
- **No message headers** — ZeroMQ sends raw message bytes. Event metadata (type, kind, content type) is not sent as separate headers.
- **No dynamic endpoint templating** — The endpoint is static (not variable-substituted per event).
- **Fire-and-forget** — PUB/SUB drops messages when no subscribers are connected. PUSH/PULL queues messages but provides no delivery acknowledgment.



## Configuration Examples

### PUB/SUB — Broadcast to All Subscribers

```bash
kete.routes.broadcast.destination.kind=zeromq
kete.routes.broadcast.realm-matchers.realm=list:master
kete.routes.broadcast.destination.endpoint=tcp://*:5556
kete.routes.broadcast.destination.socket-type=PUBLISH
kete.routes.broadcast.destination.connection-mode=BIND
```

Subscribers connect with a `SUB` socket to `tcp://<keycloak-host>:5556`.

### PUSH/PULL — Distribute to Workers

```bash
kete.routes.workers.destination.kind=zeromq
kete.routes.workers.realm-matchers.realm=list:master
kete.routes.workers.destination.endpoint=tcp://*:5557
kete.routes.workers.destination.socket-type=PUSH
kete.routes.workers.destination.connection-mode=BIND
```

Workers connect with a `PULL` socket to `tcp://<keycloak-host>:5557`. Events are distributed round-robin across workers.

### Connect to External Subscriber

```bash
kete.routes.external.destination.kind=zeromq
kete.routes.external.destination.endpoint=tcp://monitoring.internal:5556
kete.routes.external.destination.socket-type=PUBLISH
# connection-mode defaults to CONNECT
```

The external subscriber must `BIND` on `tcp://*:5556` and wait for KETE to connect.

### Minimal Configuration

```bash
kete.routes.zmq.destination.kind=zeromq
kete.routes.zmq.destination.endpoint=tcp://subscriber.example.com:5556
# Defaults: socket-type=PUBLISH, connection-mode=CONNECT, linger=1000ms
```
