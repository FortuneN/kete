# Redis Pub/Sub Destination

Stream Keycloak events to Redis using Pub/Sub messaging.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `redis-pubsub` |
| **Protocol** | Redis RESP2/RESP3 |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Redis** | Open-source, in-memory data store |
| **Valkey** | Linux Foundation Redis fork, 100% compatible |
| **Redis Stack** | Redis with additional modules |
| **Amazon ElastiCache** | AWS-managed Redis/Valkey |
| **Azure Cache for Redis** | Azure-managed Redis |
| **Google Cloud Memorystore** | GCP-managed Redis/Valkey |
| **Upstash** | Serverless Redis |
| **Dragonfly** | Redis-compatible, multi-threaded |
| **KeyDB** | Redis-compatible, multi-threaded |
| **Microsoft Garnet** | High-performance Redis-compatible cache |

!!! note "Pub/Sub Limitations"
    Redis Pub/Sub is fire-and-forget. Messages are not persisted and will be lost if no subscribers are connected. For persistent messaging, use [Redis Stream](redis-stream.md) instead.



## Example Configurations

=== "Redis"

    ```bash
    kete.routes.redis.destination.kind=redis-pubsub
    kete.routes.redis.destination.host=redis.example.com
    kete.routes.redis.destination.port=6379
    kete.routes.redis.destination.channel=keycloak-events
    ```

=== "Redis with Auth"

    ```bash
    kete.routes.redis.destination.kind=redis-pubsub
    kete.routes.redis.destination.host=redis.example.com
    kete.routes.redis.destination.port=6379
    kete.routes.redis.destination.channel=keycloak-events
    kete.routes.redis.destination.username=default
    kete.routes.redis.destination.password=secret
    ```

=== "Amazon ElastiCache"

    ```bash
    kete.routes.elasticache.destination.kind=redis-pubsub
    kete.routes.elasticache.destination.host=my-cluster.abc123.cache.amazonaws.com
    kete.routes.elasticache.destination.port=6379
    kete.routes.elasticache.destination.channel=keycloak-events
    ```

=== "Azure Cache for Redis"

    ```bash
    kete.routes.azure.destination.kind=redis-pubsub
    kete.routes.azure.destination.host=myredis.redis.cache.windows.net
    kete.routes.azure.destination.port=6380
    kete.routes.azure.destination.channel=keycloak-events
    kete.routes.azure.destination.tls.enabled=true
    kete.routes.azure.destination.password=your-access-key
    ```



## Features

- Simple publish/subscribe messaging pattern
- Low latency message delivery
- TLS/SSL support with mutual TLS (mTLS)
- Username/password authentication (Redis 6+)
- Standalone, Sentinel, and Cluster mode support
- Configurable connection and command timeouts
- Automatic reconnection
- Dynamic channel names (templating)

!!! warning "No Message Headers"
    Redis Pub/Sub does not support message headers (this is a protocol limitation). For header support, use [Redis Stream](redis-stream.md).



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `channel` | Redis channel to publish to (supports templating) | `keycloak-events` |
| `host` | Redis server hostname (required for `standalone` mode) | `redis.example.com` |

### Dynamic Channels (Templating)

The `channel` property supports template variables:

```bash
# Dynamic channel per realm
kete.routes.redis.destination.channel=keycloak-${realmLowerCase}-events

# Dynamic channel per event type
kete.routes.redis.destination.channel=keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `port` | `6379` (TCP) / `6380` (TLS) | Redis server port | `6380` |
| `database` | `0` | Redis database number | `1` |
| `username` | `""` | Redis username (Redis 6+) | `default` |
| `password` | `""` | Redis password | `secret123` |
| `mode` | `standalone` | Connection mode: `standalone`, `sentinel`, or `cluster` | `sentinel` |
| `sentinel-nodes` | _(empty)_ | Comma-separated `host:port` pairs (required for `sentinel` mode) | `sentinel1:26379,sentinel2:26379` |
| `sentinel-master-id` | _(empty)_ | Sentinel master name (required for `sentinel` mode) | `mymaster` |
| `cluster-nodes` | _(empty)_ | Comma-separated `host:port` pairs (required for `cluster` mode) | `node1:6379,node2:6379,node3:6379` |
| `client-name` | `kete` | Client name for connection | `keycloak-events` |
| `connection-timeout-seconds` | `10` | TCP connect timeout in seconds (`0` keeps the Lettuce default of 10 seconds) | `30` |
| `command-timeout-seconds` | `10` | Command timeout in seconds (always applied; bounds each send when a connection dies mid-command) | `30` |

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS (the default port becomes `6380` when enabled) |
| `tls.key-store.*` | - | Client certificate for mTLS |
| `tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Basic Redis Pub/Sub

```bash
kete.routes.redis.destination.kind=redis-pubsub
kete.routes.redis.realm-matchers.realm=list:master
kete.routes.redis.destination.host=redis.example.com
kete.routes.redis.destination.port=6379
kete.routes.redis.destination.channel=keycloak-events
```

### Redis with TLS

```bash
kete.routes.secure-redis.destination.kind=redis-pubsub
kete.routes.secure-redis.destination.host=redis.example.com
kete.routes.secure-redis.destination.port=6380
kete.routes.secure-redis.destination.channel=keycloak-events
kete.routes.secure-redis.destination.tls.enabled=true
kete.routes.secure-redis.destination.password=secret
```

### Redis with mTLS

```bash
kete.routes.mtls-redis.destination.kind=redis-pubsub
kete.routes.mtls-redis.destination.host=secure-redis.example.com
kete.routes.mtls-redis.destination.port=6380
kete.routes.mtls-redis.destination.channel=keycloak-events
kete.routes.mtls-redis.destination.tls.enabled=true
kete.routes.mtls-redis.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-redis.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-redis.destination.tls.key-store.password=keystorepass
kete.routes.mtls-redis.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-redis.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-redis.destination.tls.trust-store.password=truststorepass
```

### Redis with Authentication and Database

```bash
kete.routes.auth-redis.destination.kind=redis-pubsub
kete.routes.auth-redis.destination.host=redis.example.com
kete.routes.auth-redis.destination.port=6379
kete.routes.auth-redis.destination.channel=keycloak-events
kete.routes.auth-redis.destination.database=2
kete.routes.auth-redis.destination.username=keycloak
kete.routes.auth-redis.destination.password=secret
kete.routes.auth-redis.destination.client-name=keycloak-events
```



## Quick Starts

| Broker | Quickstart |
|--------|------------|
| Redis | [redis-pubsub-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-redis/) |
| Valkey | [redis-pubsub-valkey](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-valkey/) |
| Dragonfly | [redis-pubsub-dragonfly](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-dragonfly/) |
| KeyDB | [redis-pubsub-keydb](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-keydb/) |
| Microsoft Garnet | [redis-pubsub-garnet](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-garnet/) |
| Azure Cache for Redis | [redis-pubsub-azure-cache-for-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-azure-cache-for-redis/) |
| Upstash | [redis-pubsub-upstash](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-upstash/) |
| Amazon ElastiCache | [redis-pubsub-amazon-elasticache](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-amazon-elasticache/) |
| Google Memorystore | [redis-pubsub-google-memorystore](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-google-memorystore/) |



## See Also

- [Redis Stream Destination](redis-stream.md) — Persistent alternative with consumer groups
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
