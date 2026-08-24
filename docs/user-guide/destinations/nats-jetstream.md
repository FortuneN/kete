# NATS JetStream Destination

Stream Keycloak events to NATS JetStream for persistent, at-least-once delivery.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `nats-jetstream` |
| **Protocol** | NATS JetStream |



## Compatible Systems

| System | Notes |
|--------|-------|
| **NATS Server** | Open-source messaging system with JetStream enabled |
| **Synadia Cloud** | Managed NATS service with JetStream |
| **NATS Kubernetes** | Self-hosted NATS on Kubernetes with JetStream |

!!! info "JetStream vs Core NATS"
    JetStream provides persistence, at-least-once delivery, and publish acknowledgments. For fire-and-forget pub/sub, use [NATS Core](nats.md) instead.



## Example Configurations

=== "Basic JetStream"

    ```bash
    kete.routes.jetstream.destination.kind=nats-jetstream
    kete.routes.jetstream.destination.servers=nats://localhost:4222
    kete.routes.jetstream.destination.subject=keycloak.events
    kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
    kete.routes.jetstream.destination.authentication-method=none
    ```

=== "Username/Password"

    ```bash
    kete.routes.jetstream.destination.kind=nats-jetstream
    kete.routes.jetstream.destination.servers=nats://localhost:4222
    kete.routes.jetstream.destination.subject=keycloak.events
    kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
    kete.routes.jetstream.destination.authentication-method=username-and-password
    kete.routes.jetstream.destination.username=keycloak
    kete.routes.jetstream.destination.password=secret
    ```

=== "Credentials File"

    ```bash
    kete.routes.jetstream.destination.kind=nats-jetstream
    kete.routes.jetstream.destination.servers=nats://localhost:4222
    kete.routes.jetstream.destination.subject=keycloak.events
    kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
    kete.routes.jetstream.destination.authentication-method=credentials-file-path
    kete.routes.jetstream.destination.credentials-file-path=/secrets/nats.creds
    ```

=== "Custom Timeout"

    ```bash
    kete.routes.jetstream.destination.kind=nats-jetstream
    kete.routes.jetstream.destination.servers=nats://localhost:4222
    kete.routes.jetstream.destination.subject=keycloak.events
    kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
    kete.routes.jetstream.destination.authentication-method=none
    kete.routes.jetstream.destination.publish-timeout-seconds=30
    ```



## Features

- Persistent message storage
- At-least-once delivery with acknowledgments
- Publish confirmation with timeout
- Stream verification on startup
- Subject-based routing with wildcards
- TLS/SSL support with mutual TLS (mTLS)
- Multiple authentication methods
- Automatic reconnection and failover (publishes attempted while a reconnect is in progress fail fast instead of being buffered — configure route `retry.*` to cover reconnect windows)
- Message headers support
- Dynamic subject names (templating)



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `servers` | Comma-separated NATS server URLs | `nats://localhost:4222` |
| `subject` | NATS subject to publish to (supports templating) | `keycloak.events` |
| `stream` | JetStream stream name | `KEYCLOAK_EVENTS` |
| `authentication-method` | Authentication method (see [NATS Auth](nats.md#authentication-methods)) | `none` |

!!! warning "Stream Must Exist"
    The specified stream must exist in NATS JetStream before KETE starts. KETE verifies stream existence on initialization and fails if the stream is not found.

### Dynamic Subjects (Templating)

The `subject` property supports template variables:

```bash
# Dynamic subject per realm
kete.routes.jetstream.destination.subject=keycloak.${realmLowerCase}.events

# Dynamic subject per event type
kete.routes.jetstream.destination.subject=keycloak.events.${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `connection-timeout-seconds` | `10` | Connection timeout in seconds | `30` |
| `ping-interval-seconds` | `60` | Ping interval for health checks | `120` |
| `connection-name` | `kete` | Client connection name | `keycloak-events` |
| `publish-timeout-seconds` | `10` | Timeout for publish acknowledgment | `30` |



### Custom Headers

Custom headers can be added to NATS JetStream messages:

```bash
kete.routes.jetstream.destination.headers.X-Source=keycloak
kete.routes.jetstream.destination.headers.X-Environment=production
```

Headers are included in the NATS message headers.

### Authentication

JetStream uses the same authentication methods as [NATS Core](nats.md#authentication-methods):

| Method | Description | Required Properties |
|--------|-------------|---------------------|
| `none` | No authentication | - |
| `username-and-password` | Username/password authentication | `username`, `password` |
| `token` | Token-based authentication | `token` |
| `nkey` | NKey seed authentication | `nkey-seed` |
| `credentials-file-path` | Credentials file from filesystem | `credentials-file-path` |
| `credentials-file-text` | Credentials file content inline | `credentials-file-text` |
| `credentials-file-base64` | Base64-encoded credentials file | `credentials-file-base64` |



### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS |
| `tls.key-store.*` | - | Client certificate for mTLS |
| `tls.trust-store.*` | - | CA certificates |



## Stream Configuration

Before using this destination, create a JetStream stream that matches your subject pattern:

```bash
nats stream add KEYCLOAK_EVENTS \
  --subjects "keycloak.>" \
  --storage file \
  --replicas 3 \
  --retention limits \
  --max-age 7d \
  --max-bytes 10GB \
  --discard old
```

!!! tip "Subject Pattern"
    Use wildcards in the stream subject filter (e.g., `keycloak.>`) if you use dynamic subjects.



## Configuration Examples

### Basic JetStream

```bash
kete.routes.jetstream.destination.kind=nats-jetstream
kete.routes.jetstream.realm-matchers.realm=list:master
kete.routes.jetstream.destination.servers=nats://localhost:4222
kete.routes.jetstream.destination.subject=keycloak.events
kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
kete.routes.jetstream.destination.authentication-method=none
```

### JetStream with TLS

```bash
kete.routes.secure-jetstream.destination.kind=nats-jetstream
kete.routes.secure-jetstream.destination.servers=tls://nats.example.com:4222
kete.routes.secure-jetstream.destination.subject=keycloak.events
kete.routes.secure-jetstream.destination.stream=KEYCLOAK_EVENTS
kete.routes.secure-jetstream.destination.authentication-method=none
kete.routes.secure-jetstream.destination.tls.enabled=true
```

### JetStream with mTLS

```bash
kete.routes.mtls-jetstream.destination.kind=nats-jetstream
kete.routes.mtls-jetstream.destination.servers=tls://nats.example.com:4222
kete.routes.mtls-jetstream.destination.subject=keycloak.events
kete.routes.mtls-jetstream.destination.stream=KEYCLOAK_EVENTS
kete.routes.mtls-jetstream.destination.authentication-method=none
kete.routes.mtls-jetstream.destination.tls.enabled=true
kete.routes.mtls-jetstream.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-jetstream.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-jetstream.destination.tls.key-store.password=keystorepass
kete.routes.mtls-jetstream.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-jetstream.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-jetstream.destination.tls.trust-store.password=truststorepass
```

### Synadia Cloud JetStream

```bash
kete.routes.synadia.destination.kind=nats-jetstream
kete.routes.synadia.destination.servers=tls://connect.ngs.global:4222
kete.routes.synadia.destination.subject=keycloak.events
kete.routes.synadia.destination.stream=KEYCLOAK_EVENTS
kete.routes.synadia.destination.authentication-method=credentials-file-path
kete.routes.synadia.destination.credentials-file-path=/secrets/synadia.creds
kete.routes.synadia.destination.tls.enabled=true
```

### JetStream with Extended Timeout

```bash
kete.routes.jetstream.destination.kind=nats-jetstream
kete.routes.jetstream.destination.servers=nats://localhost:4222
kete.routes.jetstream.destination.subject=keycloak.events
kete.routes.jetstream.destination.stream=KEYCLOAK_EVENTS
kete.routes.jetstream.destination.authentication-method=none
kete.routes.jetstream.destination.publish-timeout-seconds=30
kete.routes.jetstream.destination.connection-timeout-seconds=20
```

### Dynamic Subject per Realm

```bash
kete.routes.dynamic.destination.kind=nats-jetstream
kete.routes.dynamic.destination.servers=nats://localhost:4222
kete.routes.dynamic.destination.subject=keycloak.${realmLowerCase}.events
kete.routes.dynamic.destination.stream=KEYCLOAK_EVENTS
kete.routes.dynamic.destination.authentication-method=none
```



## Comparison: NATS Core vs JetStream

| Feature | NATS Core | JetStream |
|---------|-----------|-----------|
| Delivery | At-most-once | At-least-once |
| Persistence | No | Yes |
| Acknowledgment | No | Yes |
| Replay | No | Yes |
| Use Case | Fire-and-forget | Reliable delivery |
| Latency | Lower | Slightly higher |



## Quick Starts

| System | Quick Start |
|--------|-------------|
| NATS Server | [nats-jetstream-nats-server](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-jetstream-nats-server/) |
| Synadia Cloud | [nats-jetstream-synadia-cloud](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-jetstream-synadia-cloud/) |



## See Also

- [NATS Destination](nats.md) — Fire-and-forget alternative
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
