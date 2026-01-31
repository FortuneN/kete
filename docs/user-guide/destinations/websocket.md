# WebSocket Destination

Stream Keycloak events to WebSocket servers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `websocket` |
| **Protocol** | WebSocket (RFC 6455) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Any WebSocket server** | Standard RFC 6455 compatible |
| **Custom backends** | Node.js, Python, Go, Java WebSocket servers |
| **Real-time dashboards** | Live event monitoring applications |
| **Browser applications** | Via WebSocket-to-browser bridges |
| **API Gateways** | Kong, AWS API Gateway WebSocket APIs |
| **Serverless** | AWS API Gateway WebSocket, Azure Web PubSub |



## Example Configurations

=== "Basic WebSocket"

    ```bash
    kete.routes.ws.destination.kind=websocket
    kete.routes.ws.destination.url=ws://websocket-server.example.com:8080/events
    ```

=== "Secure WebSocket (WSS)"

    ```bash
    kete.routes.wss.destination.kind=websocket
    kete.routes.wss.destination.url=wss://websocket-server.example.com/events
    ```

=== "With Custom Headers"

    ```bash
    kete.routes.ws-auth.destination.kind=websocket
    kete.routes.ws-auth.destination.url=wss://api.example.com/events
    kete.routes.ws-auth.destination.headers.Authorization=Bearer token123
    kete.routes.ws-auth.destination.headers.X-API-Key=my-api-key
    ```

=== "Binary Mode"

    ```bash
    kete.routes.ws-binary.destination.kind=websocket
    kete.routes.ws-binary.destination.url=ws://events.example.com/binary
    kete.routes.ws-binary.destination.binary-mode=true
    ```



## Features

- ✅ Text and binary message modes
- ✅ TLS/SSL support with mutual TLS (mTLS)
- ✅ Custom headers for authentication
- ✅ Automatic reconnection on connection loss
- ✅ Configurable connection timeout
- ✅ Ping/pong heartbeat for connection health detection



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `websocket` | `websocket` |
| `destination.url` | Full WebSocket URL | `ws://server:8080/path` |

Alternatively, use individual properties:

| Property | Description | Example |
|----------|-------------|---------|
| `destination.host` | WebSocket server hostname (if no `url`) | `websocket.example.com` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `80` (WS) / `443` (WSS) | WebSocket server port | `8080` |
| `destination.path` | `/` | URL path | `/events` |
| `destination.binary-mode` | `false` | Send as binary frames (not text) | `true` |
| `destination.connection-timeout` | `10` | Connection timeout in seconds | `30` |
| `destination.connection-lost-timeout` | `60` | Heartbeat timeout in seconds (0 = disabled). Uses WebSocket ping/pong to detect dead connections. | `30` |
| `destination.min-pool-size` | `5` | Minimum connections in pool | `10` |
| `destination.max-pool-size` | `20` | Maximum connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.ws.destination.headers.Authorization=Bearer my-token
kete.routes.ws.destination.headers.X-Custom-Header=value
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable WSS (auto-enabled when using `wss://` URL) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## URL Configuration

When using `destination.url`:

- The **scheme** must be `ws://` or `wss://`
- **TLS is auto-enabled** when the scheme is `wss://`
- The **host**, **port**, and **path** are extracted from the URL

If both `url` and individual properties are specified, `url` takes precedence.



## Message Format

Messages are sent as either:

- **Text frames** (default): UTF-8 encoded JSON from the serializer
- **Binary frames** (when `binary-mode=true`): Raw bytes from the serializer

The message content is the serialized event (JSON, XML, etc. based on your serializer configuration).



## Configuration Examples

### Basic WebSocket

```bash
kete.routes.ws.destination.kind=websocket
kete.routes.ws.realm-matchers.realm=list:master
kete.routes.ws.destination.url=ws://websocket-server:8080/events
```

### Secure WebSocket with Authentication

```bash
kete.routes.secure-ws.destination.kind=websocket
kete.routes.secure-ws.realm-matchers.realm=list:master
kete.routes.secure-ws.destination.url=wss://api.example.com/events
kete.routes.secure-ws.destination.headers.Authorization=Bearer eyJhbGc...
kete.routes.secure-ws.destination.tls.trust-store.path=/certs/ca.pem
```

### Using Individual Properties

```bash
kete.routes.ws.destination.kind=websocket
kete.routes.ws.destination.host=websocket.example.com
kete.routes.ws.destination.port=8080
kete.routes.ws.destination.path=/keycloak/events
kete.routes.ws.destination.tls.enabled=true
```
