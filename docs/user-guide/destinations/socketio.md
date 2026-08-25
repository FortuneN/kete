# Socket.IO Destination

Stream Keycloak events to Socket.IO servers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `socketio` |
| **Protocol** | Socket.IO (Engine.IO over HTTP/WebSocket) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Socket.IO v3/v4 servers** | Primary target (Node.js `socket.io` package) |
| **Python Socket.IO** | `python-socketio` servers |
| **Java Socket.IO** | Netty Socket.IO servers |
| **Any Engine.IO-compatible server** | EIO protocol v3 and v4 |
| **Custom real-time backends** | Any framework using Socket.IO protocol |

!!! note "Socket.IO vs WebSocket"
    Socket.IO is **not** a plain WebSocket. It uses the Engine.IO protocol which adds automatic reconnection, multiplexing via namespaces, and event-based messaging on top of WebSocket/HTTP long-polling transports. Use the `socketio` destination for Socket.IO servers and the `websocket` destination for plain WebSocket servers.



## Example Configurations

=== "Basic Socket.IO"

    ```bash
    kete.routes.socketio.destination.kind=socketio
    kete.routes.socketio.destination.url=http://socketio-server:3000
    kete.routes.socketio.destination.event-name=keycloak-event
    ```

=== "With Namespace"

    ```bash
    kete.routes.socketio-ns.destination.kind=socketio
    kete.routes.socketio-ns.destination.url=http://socketio-server:3000
    kete.routes.socketio-ns.destination.event-name=keycloak-event
    kete.routes.socketio-ns.destination.namespace=/events
    ```

=== "With TLS"

    ```bash
    kete.routes.socketio-tls.destination.kind=socketio
    kete.routes.socketio-tls.destination.url=https://socketio-server.example.com
    kete.routes.socketio-tls.destination.event-name=keycloak-event
    kete.routes.socketio-tls.destination.tls.enabled=true
    kete.routes.socketio-tls.destination.tls.trust-store.loader.kind=pem-file-path
    kete.routes.socketio-tls.destination.tls.trust-store.loader.path=/certs/ca.pem
    ```

=== "With Custom Headers"

    ```bash
    kete.routes.socketio-auth.destination.kind=socketio
    kete.routes.socketio-auth.destination.url=https://socketio.example.com
    kete.routes.socketio-auth.destination.event-name=event
    kete.routes.socketio-auth.destination.headers.Authorization=Bearer token123
    kete.routes.socketio-auth.destination.headers.X-API-Key=my-api-key
    ```



## Features

- ✅ Event-based messaging with named events
- ✅ Namespace support for multiplexing
- ✅ TLS/SSL support with mutual TLS (mTLS)
- ✅ OAuth 2.0 Client Credentials with token caching (external and internal modes)
- ✅ Custom headers (sent as extra headers during handshake)
- ✅ Configurable Socket.IO path
- ✅ Configurable connection timeout
- ✅ JSON-aware emission (emits `JSONObject` for JSON content types)
- ✅ Supports WebSocket and HTTP long-polling transports



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `socketio` | `socketio` |
| `destination.url` | Base URL of the Socket.IO server | `http://server:3000` |
| `destination.event-name` | Socket.IO event name to emit | `keycloak-event` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.namespace` | _(none)_ | Socket.IO namespace to connect to | `/events` |
| `destination.path` | `/socket.io/` | Socket.IO server path | `/custom-path/` |
| `destination.timeout-seconds` | `20` | Connection timeout in seconds | `30` |
| `destination.pool.min-idle` | `1` | Minimum idle connections in pool | `5` |
| `destination.pool.max-idle` | `10` | Maximum idle connections in pool | `20` |
| `destination.pool.max-total` | `20` | Maximum total connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>` and sent as extra headers during the Engine.IO handshake:

```bash
kete.routes.socketio.destination.headers.Authorization=Bearer my-token
kete.routes.socketio.destination.headers.X-Custom-Header=value
```

### Dynamic Event Name (Templating)

The `event-name` property supports template variables:

```bash
# Dynamic event name per event type
kete.routes.socketio.destination.event-name=keycloak-${eventTypeLowerCase}
# → keycloak-login, keycloak-logout, keycloak-register

# Dynamic event name per realm
kete.routes.socketio.destination.event-name=${realmLowerCase}-event
# → master-event, myrealm-event
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

#### OAuth 2.0 Client Credentials

The Socket.IO destination supports OAuth 2.0 Client Credentials flow with automatic token management. The OAuth access token is injected as an `Authorization` header during the Engine.IO handshake.

##### External Mode (Default)

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

```bash
kete.routes.socketio.destination.oauth.enabled=true
kete.routes.socketio.destination.oauth.token-url=https://auth.example.com/oauth/token
kete.routes.socketio.destination.oauth.client-id=keycloak-client
kete.routes.socketio.destination.oauth.client-secret=secret
kete.routes.socketio.destination.oauth.scope=events:write
```

##### Internal Mode

Use the current Keycloak instance as the OAuth server. Automatically registers a service account client:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | Yes | `false` | Enable OAuth 2.0 |
| `destination.oauth.mode` | Yes | - | Must be `internal` |
| `destination.oauth.realm` | No | Route realm | Override realm for token URL |
| `destination.oauth.client-id` | No | `kete-oauth-client` | Override auto-generated client ID |
| `destination.oauth.client-secret` | No | Auto-generated | Override auto-generated secret |
| `destination.oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes |

```bash
kete.routes.socketio.destination.oauth.enabled=true
kete.routes.socketio.destination.oauth.mode=internal
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS — required for `tls.*` stores to be applied (otherwise the JVM default trust store is used) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Example 1: Simple Event Emission

```bash
kete.routes.events.destination.kind=socketio
kete.routes.events.destination.url=http://dashboard:3000
kete.routes.events.destination.event-name=keycloak-event
```

### Example 2: Namespace with Custom Path

```bash
kete.routes.ns-events.destination.kind=socketio
kete.routes.ns-events.destination.url=http://socketio-server:3000
kete.routes.ns-events.destination.event-name=user-event
kete.routes.ns-events.destination.namespace=/keycloak
kete.routes.ns-events.destination.path=/ws/
```

### Example 3: Secure with mTLS

```bash
kete.routes.secure.destination.kind=socketio
kete.routes.secure.destination.url=https://socketio.internal.example.com
kete.routes.secure.destination.event-name=keycloak-event
kete.routes.secure.destination.tls.enabled=true
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/certs/ca.pem
kete.routes.secure.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.secure.destination.tls.key-store.password=changeit
```

### Example 4: High-Throughput Dashboard

```bash
kete.routes.dashboard.destination.kind=socketio
kete.routes.dashboard.destination.url=http://realtime-dashboard:3000
kete.routes.dashboard.destination.event-name=event
kete.routes.dashboard.destination.namespace=/admin
kete.routes.dashboard.destination.timeout-seconds=30
kete.routes.dashboard.destination.pool.min-idle=5
kete.routes.dashboard.destination.pool.max-idle=15
kete.routes.dashboard.destination.pool.max-total=30
```



## Quick Starts

| Quickstart | Description |
|------------|-------------|
| [socketio](https://github.com/FortuneN/kete/tree/release/quick-starts/socketio/) | Node.js Socket.IO echo server |



## See Also

- [Serializers](../serializers/overview.md) — Configure event format (JSON, XML, etc.)
- [Matchers](../matchers/overview.md) — Filter which events are routed
- [Event Types](../event-types.md) — Available Keycloak event types
- [Certificate Loaders](../certificate-loaders/overview.md) — TLS certificate formats
