# NATS Destination

Stream Keycloak events to NATS messaging system.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `nats` |
| **Protocol** | NATS Protocol |



## Compatible Systems

| System | Notes |
|--------|-------|
| **NATS Server** | Open-source messaging system |
| **Synadia Cloud** | Managed NATS service |
| **NATS Kubernetes** | Self-hosted NATS on Kubernetes |

!!! note "Core NATS Semantics"
    Core NATS is a fire-and-forget pub/sub system. Messages are delivered to connected subscribers only. For persistent messaging with acknowledgments, use [NATS JetStream](nats-jetstream.md) instead.



## Example Configurations

=== "Basic NATS"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://localhost:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=none
    ```

=== "Username/Password"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://localhost:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=username-and-password
    kete.routes.nats.destination.username=keycloak
    kete.routes.nats.destination.password=secret
    ```

=== "Token Auth"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://localhost:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=token
    kete.routes.nats.destination.token=myAuthToken
    ```

=== "NKey Auth"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://localhost:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=nkey
    kete.routes.nats.destination.nkey-seed=SUAM...
    ```

=== "Credentials File"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://localhost:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=credentials-file-path
    kete.routes.nats.destination.credentials-file-path=/secrets/nats.creds
    ```

=== "Multiple Servers"

    ```bash
    kete.routes.nats.destination.kind=nats
    kete.routes.nats.destination.servers=nats://server1:4222,nats://server2:4222,nats://server3:4222
    kete.routes.nats.destination.subject=keycloak.events
    kete.routes.nats.destination.authentication-method=none
    ```



## Features

- Lightweight, high-performance messaging
- At-most-once delivery semantics
- Subject-based routing with wildcards
- TLS/SSL support with mutual TLS (mTLS)
- Multiple authentication methods
- Automatic reconnection and failover
- Message headers support (NATS 2.2+)
- Dynamic subject names (templating)



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `servers` | Comma-separated NATS server URLs | `nats://localhost:4222` |
| `subject` | NATS subject to publish to (supports templating) | `keycloak.events` |
| `authentication-method` | Authentication method (see below) | `none` |

### Dynamic Subjects (Templating)

The `subject` property supports template variables:

```bash
# Dynamic subject per realm
kete.routes.nats.destination.subject=keycloak.${realmLowerCase}.events

# Dynamic subject per event type
kete.routes.nats.destination.subject=keycloak.events.${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Headers

Custom headers can be added to NATS messages:

```bash
kete.routes.nats.destination.headers.X-Source=keycloak
kete.routes.nats.destination.headers.X-Environment=production
```

Headers are included in the NATS message headers.

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `connection-timeout-seconds` | `10` | Connection timeout in seconds | `30` |
| `ping-interval-seconds` | `60` | Ping interval for health checks | `120` |
| `connection-name` | `kete` | Client connection name | `keycloak-events` |



## Authentication Methods

NATS supports multiple authentication methods via the `authentication-method` property:

| Method | Description | Required Properties |
|--------|-------------|---------------------|
| `none` | No authentication | - |
| `username-and-password` | Username/password authentication | `username`, `password` |
| `token` | Token-based authentication | `token` |
| `nkey` | NKey seed authentication | `nkey-seed` |
| `credentials-file-path` | Credentials file from filesystem | `credentials-file-path` |
| `credentials-file-text` | Credentials file content inline | `credentials-file-text` |
| `credentials-file-base64` | Base64-encoded credentials file | `credentials-file-base64` |

### Username/Password Authentication

```bash
kete.routes.nats.destination.authentication-method=username-and-password
kete.routes.nats.destination.username=keycloak
kete.routes.nats.destination.password=secret
```

### Token Authentication

```bash
kete.routes.nats.destination.authentication-method=token
kete.routes.nats.destination.token=myAuthToken
```

### NKey Authentication

```bash
kete.routes.nats.destination.authentication-method=nkey
kete.routes.nats.destination.nkey-seed=SUAM...
```

!!! note "NKey Seed Format"
    NKey seeds start with `S` followed by the key type (e.g., `SU` for user keys).

### Credentials File Authentication

=== "File Path"

    ```bash
    kete.routes.nats.destination.authentication-method=credentials-file-path
    kete.routes.nats.destination.credentials-file-path=/secrets/nats.creds
    ```

=== "Inline Text"

    ```bash
    kete.routes.nats.destination.authentication-method=credentials-file-text
    kete.routes.nats.destination.credentials-file-text=-----BEGIN NATS USER JWT-----\n...\n-----END NATS USER JWT-----\n-----BEGIN USER NKEY SEED-----\n...\n-----END USER NKEY SEED-----
    ```

=== "Base64 Encoded"

    ```bash
    kete.routes.nats.destination.authentication-method=credentials-file-base64
    kete.routes.nats.destination.credentials-file-base64=LS0tLS1CRUdJTi...
    ```

!!! tip "JWT Expiry Warning"
    KETE automatically checks JWT expiry in credentials files and logs a warning if the JWT expires within 30 days.



## TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS |
| `tls.key-store.*` | - | Client certificate for mTLS |
| `tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Basic NATS

```bash
kete.routes.nats.destination.kind=nats
kete.routes.nats.realm-matchers.realm=list:master
kete.routes.nats.destination.servers=nats://localhost:4222
kete.routes.nats.destination.subject=keycloak.events
kete.routes.nats.destination.authentication-method=none
```

### NATS with TLS

```bash
kete.routes.secure-nats.destination.kind=nats
kete.routes.secure-nats.destination.servers=tls://nats.example.com:4222
kete.routes.secure-nats.destination.subject=keycloak.events
kete.routes.secure-nats.destination.authentication-method=none
kete.routes.secure-nats.destination.tls.enabled=true
```

### NATS with mTLS

```bash
kete.routes.mtls-nats.destination.kind=nats
kete.routes.mtls-nats.destination.servers=tls://nats.example.com:4222
kete.routes.mtls-nats.destination.subject=keycloak.events
kete.routes.mtls-nats.destination.authentication-method=none
kete.routes.mtls-nats.destination.tls.enabled=true
kete.routes.mtls-nats.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-nats.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-nats.destination.tls.key-store.password=keystorepass
kete.routes.mtls-nats.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-nats.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-nats.destination.tls.trust-store.password=truststorepass
```

### Synadia Cloud

```bash
kete.routes.synadia.destination.kind=nats
kete.routes.synadia.destination.servers=tls://connect.ngs.global:4222
kete.routes.synadia.destination.subject=keycloak.events
kete.routes.synadia.destination.authentication-method=credentials-file-path
kete.routes.synadia.destination.credentials-file-path=/secrets/synadia.creds
kete.routes.synadia.destination.tls.enabled=true
```

### NATS Cluster with Failover

```bash
kete.routes.cluster.destination.kind=nats
kete.routes.cluster.destination.servers=nats://node1:4222,nats://node2:4222,nats://node3:4222
kete.routes.cluster.destination.subject=keycloak.events
kete.routes.cluster.destination.authentication-method=username-and-password
kete.routes.cluster.destination.username=keycloak
kete.routes.cluster.destination.password=secret
kete.routes.cluster.destination.connection-name=keycloak-events
```

### Dynamic Subject per Realm

```bash
kete.routes.dynamic.destination.kind=nats
kete.routes.dynamic.destination.servers=nats://localhost:4222
kete.routes.dynamic.destination.subject=keycloak.${realmLowerCase}.events
kete.routes.dynamic.destination.authentication-method=none
```



## Quick Starts

| System | Quick Start |
|--------|-------------|
| NATS Server | [nats-nats-server](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-nats-server/) |
| Synadia Cloud | [nats-synadia-cloud](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-synadia-cloud/) |



## See Also

- [NATS JetStream Destination](nats-jetstream.md) — Persistent alternative with acknowledgments
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
