# WebSocket Destination

Stream Keycloak events to WebSocket servers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `websocket` |
| **Protocol** | WebSocket (RFC 6455) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Web PubSub** | Managed WebSocket service |
| **Any WebSocket server** | Standard RFC 6455 compatible |
| **Custom backends** | Node.js, Python, Go, Java WebSocket servers |
| **Real-time dashboards** | Live event monitoring applications |
| **Browser applications** | Via WebSocket-to-browser bridges |
| **API Gateways** | Kong, AWS API Gateway WebSocket APIs |
| **Serverless** | AWS API Gateway WebSocket, Azure Web PubSub |

!!! note "WebSocket vs Native Protocol"
    Many message brokers support WebSocket as a **transport layer** for their native protocol (e.g., MQTT over WebSocket, STOMP over WebSocket). KETE's WebSocket destination sends raw messages directly to WebSocket endpoints, which is ideal for custom backends and dashboards. For broker integrations, consider using the native protocol destination (e.g., `mqtt-3`, `stomp`) with the broker's WebSocket port.



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

=== "With OAuth 2.0 (External)"

    ```bash
    kete.routes.oauth-ws.realm-matchers.realm=list:master
    kete.routes.oauth-ws.destination.kind=websocket
    kete.routes.oauth-ws.destination.url=wss://api.example.com/events
    kete.routes.oauth-ws.destination.oauth.enabled=true
    kete.routes.oauth-ws.destination.oauth.token-url=https://auth.example.com/oauth/token
    kete.routes.oauth-ws.destination.oauth.client-id=keycloak-client
    kete.routes.oauth-ws.destination.oauth.client-secret=secret
    kete.routes.oauth-ws.destination.oauth.scope=events:write
    ```

=== "With OAuth 2.0 (Internal)"

    ```bash
    # Uses Keycloak itself as OAuth server - auto-creates client!
    kete.routes.internal-oauth-ws.realm-matchers.realm=list:master
    kete.routes.internal-oauth-ws.destination.kind=websocket
    kete.routes.internal-oauth-ws.destination.url=wss://api.example.com/events
    kete.routes.internal-oauth-ws.destination.oauth.enabled=true
    kete.routes.internal-oauth-ws.destination.oauth.mode=internal
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
- ✅ OAuth 2.0 Client Credentials (bearer token sent as an `Authorization` handshake header)
- ✅ Custom headers for authentication (sent during the WebSocket handshake)
- ✅ Dead connections are detected by the pool health check and replaced
- ✅ Configurable connection timeout
- ✅ Ping/pong heartbeat for connection health detection



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `websocket` | `websocket` |
| `destination.url` | Full WebSocket URL (supports templating) | `ws://server:8080/events/${realmLowerCase}` |

### Dynamic URLs (Templating)

The `url` property supports template variables:

```bash
# Dynamic URL per realm
kete.routes.ws.destination.url=ws://websocket-server.example.com:8080/events/${realmLowerCase}

# Dynamic URL with event type
kete.routes.ws.destination.url=ws://websocket-server.example.com/${kindLowerCase}/${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

When using `destination.url`:

- The **scheme** must be `ws://` or `wss://`
- **TLS is auto-enabled** when the scheme is `wss://`
- The **host**, **port**, and **path** are extracted from the URL
- The **path and query string** can include template variables for dynamic routing

If both `url` and individual properties are specified, `url` takes precedence.

### Alternative: Individual Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.host` | WebSocket server hostname (if no `url`) | `websocket.example.com` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `80` (WS) / `443` (WSS) | WebSocket server port | `8080` |
| `destination.path` | `/` | URL path | `/events` |
| `destination.binary-mode` | `false` | Send as binary frames (not text) | `true` |
| `destination.subprotocol` | _(empty)_ | Comma-separated WebSocket subprotocols (RFC 6455 Sec-WebSocket-Protocol) | `graphql-ws,mqtt` |
| `destination.connection-timeout-seconds` | `10` | Connection timeout in seconds | `30` |
| `destination.connection-lost-timeout-seconds` | `60` | Heartbeat timeout in seconds; uses WebSocket ping/pong to detect dead connections. Set to `0` to disable. | `120` |
| `destination.pool.min-idle` | `1` | Minimum idle connections in pool | `5` |
| `destination.pool.max-idle` | `10` | Maximum idle connections in pool | `20` |
| `destination.pool.max-total` | `20` | Maximum total connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.ws.destination.headers.Authorization=Bearer my-token
kete.routes.ws.destination.headers.X-Custom-Header=value
```

Headers are HTTP-level headers sent once, during the WebSocket handshake. Because the connection is long-lived, the per-event `eventkind`, `eventtype` and `contenttype` headers used by other destinations cannot be delivered over WebSocket — consumers must derive that information from the message body (or from a templated URL).

### OAuth 2.0 Properties

The WebSocket destination supports two OAuth modes:

#### External Mode (Default)

Use an external OAuth 2.0 authorization server:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | No | `false` | Enable OAuth 2.0 Client Credentials flow |
| `destination.oauth.mode` | No | `external` | OAuth mode: `external` or `internal` |
| `destination.oauth.token-url` | Yes* | - | OAuth token endpoint URL |
| `destination.oauth.client-id` | Yes* | - | OAuth client ID |
| `destination.oauth.client-secret` | Yes* | - | OAuth client secret |
| `destination.oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes (space-separated) |

*Required when `oauth.enabled=true` and `oauth.mode=external`.

#### Internal Mode

Use the current Keycloak instance as the OAuth server. This mode **automatically registers a service account client** in Keycloak during initialization - the simplest setup possible:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | Yes | `false` | Enable OAuth 2.0 |
| `destination.oauth.mode` | Yes | - | Must be `internal` |
| `destination.oauth.realm` | No | Route realm | Override realm for token URL |
| `destination.oauth.client-id` | No | `kete-oauth-client` | Override auto-generated client ID |
| `destination.oauth.client-secret` | No | Auto-generated | Override auto-generated secret |
| `destination.oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes |

**Internal Mode Example:**

```bash
# Simplest OAuth setup - just 2 properties!
kete.routes.ws.destination.oauth.enabled=true
kete.routes.ws.destination.oauth.mode=internal
```

This automatically:

1. Creates a confidential client `kete-oauth-client` in the route's realm
2. Enables service account (client credentials grant)
3. Generates a secure client secret
4. Configures the token URL for the realm

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable WSS (auto-enabled when using `wss://` URL) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



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
kete.routes.secure-ws.destination.tls.enabled=true
kete.routes.secure-ws.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure-ws.destination.tls.trust-store.loader.path=/certs/ca.pem
```

### With OAuth 2.0

```bash
kete.routes.oauth-ws.destination.kind=websocket
kete.routes.oauth-ws.realm-matchers.realm=list:master
kete.routes.oauth-ws.destination.url=wss://api.example.com/events
kete.routes.oauth-ws.destination.oauth.enabled=true
kete.routes.oauth-ws.destination.oauth.token-url=https://auth.example.com/oauth/token
kete.routes.oauth-ws.destination.oauth.client-id=keycloak-client
kete.routes.oauth-ws.destination.oauth.client-secret=secret
```

### Using Individual Properties

```bash
kete.routes.ws.destination.kind=websocket
kete.routes.ws.destination.host=websocket.example.com
kete.routes.ws.destination.port=8080
kete.routes.ws.destination.path=/keycloak/events
kete.routes.ws.destination.tls.enabled=true
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [websocket-echo](https://github.com/FortuneN/kete/tree/release/quick-starts/websocket-echo/) | WebSocket echo server |



## See Also

- [Socket.IO Destination](socketio.md) — Socket.IO protocol with namespaces and rooms
- [SignalR Destination](signalr.md) — ASP.NET SignalR protocol
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
