# Redis Stream Destination

Stream Keycloak events to Redis Stream for persistent, ordered message storage.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `redis-stream` |
| **Protocol** | Redis RESP2/RESP3 |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Redis 5.0+** | Native Streams support |
| **Valkey** | Linux Foundation Redis fork, 100% compatible |
| **Redis Stack** | Redis with additional modules |
| **Amazon ElastiCache** | AWS-managed Redis/Valkey (6.2+) |
| **Azure Cache for Redis** | Azure-managed Redis |
| **Google Cloud Memorystore** | GCP-managed Redis/Valkey |
| **Upstash** | Serverless Redis with Streams |
| **Dragonfly** | Redis-compatible, multi-threaded |
| **KeyDB** | Redis-compatible, multi-threaded |

!!! warning "Microsoft Garnet"
    Garnet does **not** support Redis Streams (`XADD` command). Use [Redis Pub/Sub](redis-pubsub.md) with Garnet instead. See [github.com/microsoft/garnet/issues/64](https://github.com/microsoft/garnet/issues/64).



## Example Configurations

=== "Redis"

    ```bash
    kete.routes.redis.destination.kind=redis-stream
    kete.routes.redis.destination.host=redis.example.com
    kete.routes.redis.destination.port=6379
    kete.routes.redis.destination.stream=keycloak-events
    ```

=== "Redis with Trimming"

    ```bash
    kete.routes.redis.destination.kind=redis-stream
    kete.routes.redis.destination.host=redis.example.com
    kete.routes.redis.destination.port=6379
    kete.routes.redis.destination.stream=keycloak-events
    kete.routes.redis.destination.max-len=10000
    kete.routes.redis.destination.approximate-trimming=true
    ```

=== "Amazon ElastiCache"

    ```bash
    kete.routes.elasticache.destination.kind=redis-stream
    kete.routes.elasticache.destination.host=my-cluster.abc123.cache.amazonaws.com
    kete.routes.elasticache.destination.port=6379
    kete.routes.elasticache.destination.stream=keycloak-events
    kete.routes.elasticache.destination.max-len=50000
    ```

=== "Azure Cache for Redis"

    ```bash
    kete.routes.azure.destination.kind=redis-stream
    kete.routes.azure.destination.host=myredis.redis.cache.windows.net
    kete.routes.azure.destination.port=6380
    kete.routes.azure.destination.stream=keycloak-events
    kete.routes.azure.destination.tls.enabled=true
    kete.routes.azure.destination.password=your-access-key
    ```



## Features

- Persistent message storage with automatic trimming
- Event metadata as stream fields (headers)
- TLS/SSL support with mutual TLS (mTLS)
- Username/password authentication (Redis 6+)
- Standalone, Sentinel, and Cluster mode support
- Configurable connection and command timeouts
- Automatic reconnection
- Dynamic stream names (templating)
- Consumer group support (via Redis native features)

!!! tip "When to use Redis Stream vs Pub/Sub"
    Use **Redis Stream** when you need message persistence, consumer groups, or message headers.
    Use **Redis Pub/Sub** for simple, low-latency fire-and-forget messaging.



## Redis Stream Capabilities

Redis Stream provides features not available in Pub/Sub:

| Feature | Description |
|---------|-------------|
| **Persistence** | Messages are stored until explicitly deleted |
| **Ordering** | Messages are strictly ordered by ID |
| **Consumer Groups** | Load balancing across multiple consumers |
| **Message Acknowledgment** | At-least-once delivery semantics |
| **Message Headers** | Event metadata stored as stream fields |
| **Stream Trimming** | Automatic size/age management |
| **Replay** | Read messages from any point in the stream |



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `stream` | Redis stream name (supports templating) | `keycloak-events` |
| `host` | Redis server hostname (required for `standalone` mode) | `redis.example.com` |

### Dynamic Streams (Templating)

The `stream` property supports template variables:

```bash
# Dynamic stream per realm
kete.routes.redis.destination.stream=keycloak-${realmLowerCase}-events

# Dynamic stream per event type
kete.routes.redis.destination.stream=keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Headers

Custom headers can be added to Redis stream entries as additional fields:

```bash
kete.routes.redis.destination.headers.X-Source=keycloak
kete.routes.redis.destination.headers.X-Environment=production
```

Headers are included as fields in the Redis stream entry.

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
| `max-len` | `0` | Max stream length (0 = no limit) | `10000` |
| `approximate-trimming` | `true` | Use `~` for efficient trimming | `false` |

### Stream Trimming

Control stream size with `max-len` and `approximate-trimming`:

| Setting | Behavior |
|---------|----------|
| `max-len=0` | No trimming, stream grows indefinitely |
| `max-len=10000` + `approximate-trimming=true` | Trim to ~10000 entries (efficient) |
| `max-len=10000` + `approximate-trimming=false` | Trim to exactly 10000 entries (slower) |

!!! tip "Approximate Trimming"
    Redis uses `MAXLEN ~` for approximate trimming, which is more efficient as it trims entries in whole nodes rather than one at a time. Recommended for most use cases.

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS (the default port becomes `6380` when enabled) |
| `tls.key-store.*` | - | Client certificate for mTLS |
| `tls.trust-store.*` | - | CA certificates |



## Message Format

Messages are stored as stream entries with the following fields:

| Field | Description | Example |
|-------|-------------|---------|
| `eventkind` | Event kind (`EVENT` or `ADMIN_EVENT`) | `EVENT` |
| `eventtype` | Event type | `LOGIN` |
| `contenttype` | Content type of body | `application/json` |
| `body` | Serialized event payload | `{"id":"...","type":"LOGIN",...}` |



## Configuration Examples

### Basic Redis Stream

```bash
kete.routes.redis.destination.kind=redis-stream
kete.routes.redis.realm-matchers.realm=list:master
kete.routes.redis.destination.host=redis.example.com
kete.routes.redis.destination.port=6379
kete.routes.redis.destination.stream=keycloak-events
```

### Redis Stream with TLS

```bash
kete.routes.secure-redis.destination.kind=redis-stream
kete.routes.secure-redis.destination.host=redis.example.com
kete.routes.secure-redis.destination.port=6380
kete.routes.secure-redis.destination.stream=keycloak-events
kete.routes.secure-redis.destination.tls.enabled=true
kete.routes.secure-redis.destination.password=secret
```

### Redis Stream with mTLS

```bash
kete.routes.mtls-redis.destination.kind=redis-stream
kete.routes.mtls-redis.destination.host=secure-redis.example.com
kete.routes.mtls-redis.destination.port=6380
kete.routes.mtls-redis.destination.stream=keycloak-events
kete.routes.mtls-redis.destination.tls.enabled=true
kete.routes.mtls-redis.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-redis.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-redis.destination.tls.key-store.password=keystorepass
kete.routes.mtls-redis.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-redis.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-redis.destination.tls.trust-store.password=truststorepass
```

### Redis Stream with Trimming

```bash
kete.routes.trimmed-redis.destination.kind=redis-stream
kete.routes.trimmed-redis.destination.host=redis.example.com
kete.routes.trimmed-redis.destination.port=6379
kete.routes.trimmed-redis.destination.stream=keycloak-events
kete.routes.trimmed-redis.destination.max-len=50000
kete.routes.trimmed-redis.destination.approximate-trimming=true
```



## Quick Starts

| Broker | Quickstart |
|--------|------------|
| Redis | [redis-stream-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-redis/) |
| Valkey | [redis-stream-valkey](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-valkey/) |
| Dragonfly | [redis-stream-dragonfly](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-dragonfly/) |
| KeyDB | [redis-stream-keydb](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-keydb/) |
| Azure Cache for Redis | [redis-stream-azure-cache-for-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-azure-cache-for-redis/) |
| Upstash | [redis-stream-upstash](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-upstash/) |
| Amazon ElastiCache | [redis-stream-amazon-elasticache](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-amazon-elasticache/) |
| Google Memorystore | [redis-stream-google-memorystore](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-google-memorystore/) |



## See Also

- [Redis Pub/Sub Destination](redis-pubsub.md) — Fire-and-forget alternative
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
