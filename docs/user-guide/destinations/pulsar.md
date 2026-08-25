# Pulsar Destination

Stream Keycloak events to Apache Pulsar.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `pulsar` |
| **Protocol** | Apache Pulsar Protocol |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Apache Pulsar** | Primary target, all features supported |
| **DataStax Luna Streaming** | Pulsar-compatible managed service |
| **DataStax Astra Streaming** | Pulsar-compatible managed service |
| **StreamNative Cloud** | Pulsar-compatible managed service |



## Example Configurations

=== "Apache Pulsar"

    ```bash
    kete.routes.pulsar.destination.kind=pulsar
    kete.routes.pulsar.destination.service-url=pulsar://localhost:6650
    kete.routes.pulsar.destination.topic=persistent://public/default/keycloak-events
    # Defaults: compression=LZ4, batching off (set a batching-* option to enable it)
    ```

=== "Pulsar with TLS"

    ```bash
    kete.routes.pulsar-tls.destination.kind=pulsar
    kete.routes.pulsar-tls.destination.service-url=pulsar+ssl://pulsar:6651
    kete.routes.pulsar-tls.destination.topic=persistent://public/default/keycloak-events
    kete.routes.pulsar-tls.destination.tls.enabled=true
    kete.routes.pulsar-tls.destination.tls.trust-store.loader.kind=jks-file-path
    kete.routes.pulsar-tls.destination.tls.trust-store.loader.path=/path/to/truststore.jks
    kete.routes.pulsar-tls.destination.tls.trust-store.password=changeit
    ```

=== "Pulsar with Token Authentication"

    ```bash
    kete.routes.pulsar-auth.destination.kind=pulsar
    kete.routes.pulsar-auth.destination.service-url=pulsar://pulsar:6650
    kete.routes.pulsar-auth.destination.topic=persistent://public/default/keycloak-events
    kete.routes.pulsar-auth.destination.authentication-type=token
    kete.routes.pulsar-auth.destination.token=eyJhbGciOiJIUzI1NiJ9...
    ```

=== "Pulsar with Basic Authentication"

    ```bash
    kete.routes.pulsar-basic.destination.kind=pulsar
    kete.routes.pulsar-basic.destination.service-url=pulsar://pulsar:6650
    kete.routes.pulsar-basic.destination.topic=persistent://public/default/keycloak-events
    kete.routes.pulsar-basic.destination.authentication-type=basic
    kete.routes.pulsar-basic.destination.username=admin
    kete.routes.pulsar-basic.destination.password=admin
    ```



## Features

- ✅ Full Pulsar producer configuration support
- ✅ Topic templating with variables
- ✅ Multiple compression types (LZ4, ZSTD, ZLIB, Snappy)
- ✅ Message batching (opt-in)
- ✅ Token, Basic, and OAuth 2.0 authentication
- ✅ TLS/mTLS support
- ✅ Event metadata in message properties (`eventkind`, `eventtype`, `contenttype`) and the event type as message key



## Configuration Properties

### Required Properties

```bash
kete.routes.<NAME>.destination.kind=pulsar
kete.routes.<NAME>.destination.service-url=<PULSAR_URL>
kete.routes.<NAME>.destination.topic=<TOPIC_NAME>
```

### Basic Example

```bash
# Configure Pulsar destination
kete.routes.main-pulsar.destination.kind=pulsar
kete.routes.main-pulsar.realm-matchers.realm=list:master
kete.routes.main-pulsar.event-matchers.filter=glob:*

# Pulsar-specific configuration
kete.routes.main-pulsar.destination.service-url=pulsar://localhost:6650
kete.routes.main-pulsar.destination.topic=persistent://public/default/keycloak-events
```

### All Configuration Properties

#### Core Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `service-url` | Pulsar service URL (required) | - | `pulsar://pulsar:6650` |
| `topic` | Topic name (required, supports templating) | - | `persistent://public/default/events` |
| `compression-type` | Compression algorithm | `LZ4` | `LZ4`, `ZSTD`, `ZLIB`, `SNAPPY`, `NONE` |
| `send-timeout-seconds` | Send timeout | `30` | `60` |
| `operation-timeout-seconds` | Operation timeout | `30` | `60` |
| `connection-timeout-seconds` | Connection timeout | `10` | `30` |
| `keep-alive-interval-seconds` | Keep-alive interval | `30` | `60` |
| `pool.min-idle` | Minimum idle connections in pool | `1` | `5` |
| `pool.max-idle` | Maximum idle connections in pool | `10` | `20` |
| `pool.max-total` | Maximum total connections in pool | `20` | `50` |

#### Batching Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `batching-max-messages` | Maximum messages per batch. Setting this (or `batching-max-publish-delay-seconds`) enables producer batching | `1000` | `2000` |
| `batching-max-publish-delay-seconds` | Maximum batch delay. Setting this (or `batching-max-messages`) enables producer batching; sends are synchronous, so every send then waits for its batch to flush | `1` | `5` |
| `max-pending-messages` | Maximum pending messages | `1000` | `5000` |
| `block-if-queue-full` | Block if queue is full | `true` | `false` |

#### Authentication Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `authentication-type` | Authentication method: `token`, `basic`, or `oauth` | _(none)_ | `token` |
| `token` | JWT token (for `token` auth) | - | `eyJhbGciOiJIUzI1NiJ9...` |
| `username` | Username (for `basic` auth) | - | `admin` |
| `password` | Password (for `basic` auth) | - | `secret` |

When `authentication-type` is `oauth`, the standard `oauth.*` sub-properties apply:

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `oauth.enabled` | Yes | `false` | Must be `true` when `authentication-type=oauth` |
| `oauth.mode` | No | `external` | `external` or `internal` |
| `oauth.token-url` | Yes* | - | OAuth token endpoint URL |
| `oauth.client-id` | Yes* | - | OAuth client ID |
| `oauth.client-secret` | Yes* | - | OAuth client secret |
| `oauth.timeout-seconds` | No | _(none)_ | Connect and read timeout in seconds for the token request; applied only when set |
| `oauth.scope` | No | `""` | Requested OAuth scopes |

*Required when `oauth.mode=external`.

#### Optional Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `producer-name` | Producer name | - | `keycloak-producer` |
| `listener-name` | Listener name (multi-region) | - | `us-west` |

### Custom Headers

Custom headers can be added to Pulsar messages as properties:

```bash
kete.routes.pulsar.destination.headers.X-Source=keycloak
kete.routes.pulsar.destination.headers.X-Environment=production
```

All custom headers are included in the Pulsar message properties, alongside the standard `eventkind`, `eventtype` and `contenttype` properties that are always set. The Pulsar message key is set to the event type.

### Topic Templating

The topic name supports variable substitution:

```bash
# Dynamic topic per realm
kete.routes.pulsar.destination.topic=persistent://public/default/keycloak-events-${realmLowerCase}

# Dynamic topic per event type
kete.routes.pulsar.destination.topic=persistent://public/default/keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

For TLS/mTLS connections:

```bash
kete.routes.pulsar-tls.destination.kind=pulsar
kete.routes.pulsar-tls.destination.service-url=pulsar+ssl://pulsar:6651
kete.routes.pulsar-tls.destination.topic=persistent://public/default/keycloak-events

# TLS configuration
kete.routes.pulsar-tls.destination.tls.enabled=true
kete.routes.pulsar-tls.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.pulsar-tls.destination.tls.trust-store.loader.path=/path/to/truststore.jks
kete.routes.pulsar-tls.destination.tls.trust-store.password=changeit

# Optional: mTLS (client certificate)
kete.routes.pulsar-tls.destination.tls.key-store.loader.kind=jks-file-path
kete.routes.pulsar-tls.destination.tls.key-store.loader.path=/path/to/keystore.jks
kete.routes.pulsar-tls.destination.tls.key-store.password=changeit
kete.routes.pulsar-tls.destination.tls.key-store.key-password=changeit
```



## Configuration Examples

### Example 1: Multiple Tenants

```bash
# Tenant 1 - Dedicated topic
kete.routes.tenant1.destination.kind=pulsar
kete.routes.tenant1.realm-matchers.realm=list:tenant1
kete.routes.tenant1.event-matchers.filter=glob:*
kete.routes.tenant1.destination.service-url=pulsar://pulsar:6650
kete.routes.tenant1.destination.topic=persistent://tenant1/default/keycloak-events

# Tenant 2 - Dedicated topic
kete.routes.tenant2.destination.kind=pulsar
kete.routes.tenant2.realm-matchers.realm=list:tenant2
kete.routes.tenant2.event-matchers.filter=glob:*
kete.routes.tenant2.destination.service-url=pulsar://pulsar:6650
kete.routes.tenant2.destination.topic=persistent://tenant2/default/keycloak-events
```

### Example 2: Multi-Region Setup

```bash
# Primary region
kete.routes.primary.destination.kind=pulsar
kete.routes.primary.realm-matchers.realm=list:master
kete.routes.primary.event-matchers.filter=glob:*
kete.routes.primary.destination.service-url=pulsar://pulsar-us-west:6650
kete.routes.primary.destination.topic=persistent://public/default/events
kete.routes.primary.destination.listener-name=us-west

# Backup region
kete.routes.backup.destination.kind=pulsar
kete.routes.backup.realm-matchers.realm=list:master
kete.routes.backup.event-matchers.filter=glob:*
kete.routes.backup.destination.service-url=pulsar://pulsar-us-east:6650
kete.routes.backup.destination.topic=persistent://public/default/events
kete.routes.backup.destination.listener-name=us-east
```

### Example 3: Per-Event-Type Topics

```bash
# Login events
kete.routes.logins.destination.kind=pulsar
kete.routes.logins.realm-matchers.realm=list:master
kete.routes.logins.event-matchers.login=glob:LOGIN*
kete.routes.logins.destination.service-url=pulsar://pulsar:6650
kete.routes.logins.destination.topic=persistent://public/default/keycloak-logins

# Admin events
kete.routes.admin.destination.kind=pulsar
kete.routes.admin.realm-matchers.realm=list:master
kete.routes.admin.event-matchers.user-ops=glob:USER_*
kete.routes.admin.destination.service-url=pulsar://pulsar:6650
kete.routes.admin.destination.topic=persistent://public/default/keycloak-admin
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [pulsar-apache](https://github.com/FortuneN/kete/tree/release/quick-starts/pulsar-apache) | Apache Pulsar standalone |
| [pulsar-datastax](https://github.com/FortuneN/kete/tree/release/quick-starts/pulsar-datastax) | DataStax Luna Streaming |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
