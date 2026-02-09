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
- Configurable connection and command timeouts
- Automatic reconnection
- Dynamic channel names (templating)

!!! warning "No Message Headers"
    Redis Pub/Sub does not support message headers (this is a protocol limitation). For header support, use [Redis Stream](redis-stream.md).



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `host` | Redis server hostname | `redis.example.com` |
| `channel` | Redis channel to publish to (supports templating) | `keycloak-events` |

### Dynamic Channels (Templating)

The `channel` property supports template variables:

```bash
# Dynamic channel per realm
kete.routes.redis.destination.channel=keycloak-${realmLowerCase}-events

# Dynamic channel per event type
kete.routes.redis.destination.channel=keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `port` | `6379` (TCP) / `6380` (TLS) | Redis server port | `6380` |
| `database` | `0` | Redis database number | `1` |
| `username` | `""` | Redis username (Redis 6+) | `default` |
| `password` | `""` | Redis password | `secret123` |
| `client-name` | `kete` | Client name for connection | `keycloak-events` |
| `connection-timeout-seconds` | `10` | Connection timeout in seconds | `30` |
| `command-timeout-seconds` | `60` | Command timeout in seconds | `120` |

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS (auto-enabled for port 6380) |
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
| Redis | [redis-pubsub-redis](../../quick-starts/redis-pubsub-redis/) |
| Valkey | [redis-pubsub-valkey](../../quick-starts/redis-pubsub-valkey/) |
| Dragonfly | [redis-pubsub-dragonfly](../../quick-starts/redis-pubsub-dragonfly/) |
| KeyDB | [redis-pubsub-keydb](../../quick-starts/redis-pubsub-keydb/) |
| Microsoft Garnet | [redis-pubsub-garnet](../../quick-starts/redis-pubsub-garnet/) |
| Azure Cache for Redis | [redis-pubsub-azure-cache-for-redis](../../quick-starts/redis-pubsub-azure-cache-for-redis/) |
| Upstash | [redis-pubsub-upstash](../../quick-starts/redis-pubsub-upstash/) |



## See Also

- [Redis Stream Destination](redis-stream.md) — Persistent alternative with consumer groups
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
