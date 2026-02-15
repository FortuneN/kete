# SignalR Destination

Stream Keycloak events to ASP.NET SignalR hubs.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `signalr` |
| **Protocol** | SignalR (over HTTP/WebSocket) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **ASP.NET Core SignalR** | Primary target, any ASP.NET Core hub |
| **Azure SignalR Service** | Managed, serverless mode supported via access token |
| **Self-hosted SignalR hubs** | Any custom .NET application hosting a SignalR hub |



## Example Configurations

=== "Basic SignalR Hub"

    ```bash
    kete.routes.signalr.destination.kind=signalr
    kete.routes.signalr.destination.url=http://signalr-server:5000/hub
    kete.routes.signalr.destination.hub-method=SendEvent
    ```

=== "Azure SignalR Service"

    ```bash
    kete.routes.azure-signalr.destination.kind=signalr
    kete.routes.azure-signalr.destination.url=https://my-signalr.service.signalr.net/hub
    kete.routes.azure-signalr.destination.hub-method=BroadcastEvent
    kete.routes.azure-signalr.destination.access-token=eyJhbGc...
    ```

=== "With TLS"

    ```bash
    kete.routes.signalr-tls.destination.kind=signalr
    kete.routes.signalr-tls.destination.url=https://signalr-server.example.com/hub
    kete.routes.signalr-tls.destination.hub-method=SendEvent
    kete.routes.signalr-tls.destination.tls.trust-store.loader.kind=pem-file-path
    kete.routes.signalr-tls.destination.tls.trust-store.loader.path=/certs/ca.pem
    ```

=== "With Custom Headers"

    ```bash
    kete.routes.signalr-auth.destination.kind=signalr
    kete.routes.signalr-auth.destination.url=https://signalr.example.com/events-hub
    kete.routes.signalr-auth.destination.hub-method=ReceiveEvent
    kete.routes.signalr-auth.destination.headers.X-API-Key=my-api-key
    kete.routes.signalr-auth.destination.headers.X-Tenant-Id=tenant-123
    ```



## Features

- ✅ Fire-and-forget hub method invocation
- ✅ TLS/SSL support with mutual TLS (mTLS)
- ✅ OAuth 2.0 Client Credentials with token caching (external and internal modes)
- ✅ Access token authentication (Bearer)
- ✅ Custom headers (connection-level, sent during handshake)
- ✅ Automatic reconnection on connection loss
- ✅ Configurable connection and handshake timeout
- ✅ JSON message body passthrough



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `signalr` | `signalr` |
| `destination.url` | Full URL to the SignalR hub endpoint | `http://server:5000/hub` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.hub-method` | `SendEvent` | Hub method name to invoke on the server | `BroadcastEvent` |
| `destination.timeout-seconds` | `10` | Handshake and connection timeout in seconds | `30` |
| `destination.access-token` | _(none)_ | Bearer access token for authentication | `eyJhbGc...` |
| `destination.pool.min-idle` | `1` | Minimum idle connections in pool | `5` |
| `destination.pool.max-idle` | `10` | Maximum idle connections in pool | `20` |
| `destination.pool.max-total` | `20` | Maximum total connections in pool | `50` |

### Custom Headers

Headers are configured under `destination.headers.<NAME>` and sent during the SignalR connection handshake. They are **not sent per-message** — the SignalR hub invocation only includes the message body.

Standard message headers (`eventkind`, `eventtype`, `contenttype`) are not included — the SignalR protocol does not support per-message metadata headers.

```bash
kete.routes.signalr.destination.headers.Authorization=Bearer my-token
kete.routes.signalr.destination.headers.X-Custom-Header=value
```

### Dynamic Hub Method (Templating)

The `hub-method` property supports template variables:

```bash
# Dynamic hub method per event type
kete.routes.signalr.destination.hub-method=Send${eventTypeLowerCase}
# → SendLogin, SendLogout, SendRegister

# Dynamic hub method per realm
kete.routes.signalr.destination.hub-method=On${realmLowerCase}Event
# → OnmasterEvent, OnmyrealmEvent
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

#### Access Token

The simplest authentication method. The token is sent as a Bearer token during the SignalR handshake:

```bash
kete.routes.signalr.destination.access-token=eyJhbGciOiJIUzI1NiIs...
```

#### OAuth 2.0 Client Credentials

The SignalR destination supports OAuth 2.0 Client Credentials flow with automatic token management and caching.

##### External Mode (Default)

Use an external OAuth 2.0 authorization server:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `destination.oauth.enabled` | No | `false` | Enable OAuth 2.0 Client Credentials flow |
| `destination.oauth.mode` | No | `external` | OAuth mode: `external` or `internal` |
| `destination.oauth.token-url` | Yes* | - | OAuth token endpoint URL |
| `destination.oauth.client-id` | Yes* | - | OAuth client ID |
| `destination.oauth.client-secret` | Yes* | - | OAuth client secret |
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes (space-separated) |

*Required when `oauth.enabled=true` and `oauth.mode=external`.

```bash
kete.routes.signalr.destination.oauth.enabled=true
kete.routes.signalr.destination.oauth.token-url=https://auth.example.com/oauth/token
kete.routes.signalr.destination.oauth.client-id=keycloak-client
kete.routes.signalr.destination.oauth.client-secret=secret
kete.routes.signalr.destination.oauth.scope=events:write
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
| `destination.oauth.scope` | No | `""` | Requested OAuth scopes |

```bash
kete.routes.signalr.destination.oauth.enabled=true
kete.routes.signalr.destination.oauth.mode=internal
```

#### Custom Headers

For API key or other header-based authentication:

```bash
kete.routes.signalr.destination.headers.Authorization=Bearer my-token
kete.routes.signalr.destination.headers.X-API-Key=my-api-key
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS (auto-enabled when using `https://` URL) |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Example 1: Local ASP.NET Core Hub

```bash
kete.routes.local.destination.kind=signalr
kete.routes.local.destination.url=http://localhost:5000/events-hub
kete.routes.local.destination.hub-method=OnKeycloakEvent
```

### Example 2: Azure SignalR Service with Token

```bash
kete.routes.azure.destination.kind=signalr
kete.routes.azure.destination.url=https://my-app.service.signalr.net/hub
kete.routes.azure.destination.access-token=eyJhbGciOiJIUzI1NiIs...
kete.routes.azure.destination.hub-method=BroadcastEvent
kete.routes.azure.destination.timeout-seconds=15
```

### Example 3: Secure with mTLS

```bash
kete.routes.secure.destination.kind=signalr
kete.routes.secure.destination.url=https://signalr.internal.example.com/hub
kete.routes.secure.destination.hub-method=SendEvent
kete.routes.secure.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/certs/ca.pem
kete.routes.secure.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.secure.destination.tls.key-store.loader.password=changeit
```



## Quick Starts

| Quickstart | Description |
|------------|-------------|
| [signalr](https://github.com/FortuneN/kete/tree/release/quick-starts/signalr/) | ASP.NET Core SignalR hub echo server |



## See Also

- [Serializers](../serializers/overview.md) — Configure event format (JSON, XML, etc.)
- [Matchers](../matchers/overview.md) — Filter which events are routed
- [Event Types](../event-types.md) — Available Keycloak event types
- [Certificate Loaders](../certificate-loaders/overview.md) — TLS certificate formats
