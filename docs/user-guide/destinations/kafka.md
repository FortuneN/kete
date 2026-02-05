# Kafka Destination

Stream Keycloak events to Kafka-compatible systems.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `kafka` |
| **Protocol** | Apache Kafka Protocol |




## Compatible Systems

| System | Notes |
|--------|-------|
| **Apache Kafka** | Primary target, all features supported |
| **Redpanda** | Kafka-compatible, zero-JVM |
| **Confluent Cloud** | Managed Kafka (supports SASL/TLS) |
| **Azure Event Hubs** | Kafka protocol endpoint (requires SASL_SSL) |
| **Amazon MSK** | Managed Kafka (supports IAM/SASL) |
| **Amazon MSK Serverless** | Serverless managed Kafka |
| **Aiven for Kafka** | Multi-cloud managed Kafka |
| **Strimzi** | Kubernetes Kafka operator |
| **WarpStream** | Confluent's zero-disk Kafka |
| **Instaclustr** | Multi-cloud managed Kafka |



## Example Configurations

=== "Apache Kafka"

    ```bash
    kete.routes.kafka.destination.kind=kafka
    kete.routes.kafka.destination.bootstrap.servers=kafka1:9092,kafka2:9092,kafka3:9092
    kete.routes.kafka.destination.topic=keycloak-events
    # Defaults: acks=all, compression=lz4, idempotence=true
    ```

=== "Redpanda"

    ```bash
    kete.routes.redpanda.destination.kind=kafka
    kete.routes.redpanda.destination.bootstrap.servers=redpanda:9092
    kete.routes.redpanda.destination.topic=keycloak-events
    # Same configuration as Kafka - fully compatible
    ```

=== "Confluent Cloud"

    ```bash
    kete.routes.confluent.destination.kind=kafka
    kete.routes.confluent.destination.bootstrap.servers=pkc-xxxxx.region.confluent.cloud:9092
    kete.routes.confluent.destination.topic=keycloak-events
    kete.routes.confluent.destination.security.protocol=SASL_SSL
    kete.routes.confluent.destination.sasl.mechanism=PLAIN
    kete.routes.confluent.destination.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="API_KEY" password="API_SECRET";
    ```

=== "Azure Event Hubs"

    ```bash
    kete.routes.eventhubs.destination.kind=kafka
    kete.routes.eventhubs.destination.bootstrap.servers=your-namespace.servicebus.windows.net:9093
    kete.routes.eventhubs.destination.topic=keycloak-events
    kete.routes.eventhubs.destination.security.protocol=SASL_SSL
    kete.routes.eventhubs.destination.sasl.mechanism=PLAIN
    kete.routes.eventhubs.destination.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="Endpoint=sb://...";
    ```

=== "Amazon MSK"

    ```bash
    kete.routes.msk.destination.kind=kafka
    kete.routes.msk.destination.bootstrap.servers=broker1.msk.region.amazonaws.com:9094
    kete.routes.msk.destination.topic=keycloak-events
    kete.routes.msk.destination.security.protocol=SASL_SSL
    kete.routes.msk.destination.sasl.mechanism=AWS_MSK_IAM
    kete.routes.msk.destination.sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
    kete.routes.msk.destination.sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler
    ```



## Features

- ✅ Full Kafka producer configuration support
- ✅ Idempotent producer (exactly-once semantics)
- ✅ Topic templating with variables
- ✅ Event metadata in message headers
- ✅ SASL and TLS authentication

!!! warning "Transactional Producers Not Supported"
    Transactional producers (`transactional.id`) are not supported due to destination pooling. Use idempotent producers instead, which are enabled by default.



## Configuration Properties

### Required Properties

```bash
kete.routes.<NAME>.destination.kind=kafka
kete.routes.<NAME>.destination.bootstrap.servers=<KAFKA_BROKERS>
kete.routes.<NAME>.destination.topic=<TOPIC_NAME>
```

### Basic Example

```bash
# Configure Kafka destination
kete.routes.main-kafka.destination.kind=kafka
kete.routes.main-kafka.realm-matchers.realm=list:master
kete.routes.main-kafka.event-matchers.filter=glob:*

# Kafka-specific configuration
kete.routes.main-kafka.destination.bootstrap.servers=localhost:9092
kete.routes.main-kafka.destination.topic=keycloak-events
```

### All Kafka Properties

Any property under `kete.routes.<NAME>.destination.*` is passed directly to the Kafka producer, except for `topic` which is used internally.

#### Built-in Defaults

| Property | Default |
|----------|---------|
| `acks` | `all` |
| `linger.ms` | `5` |
| `batch.size` | `32768` |
| `compression.type` | `lz4` |
| `enable.idempotence` | `true` |
| `max.in.flight.requests.per.connection` | `5` |
| `key.serializer` | StringSerializer |
| `value.serializer` | ByteArraySerializer |
| `pool.min-idle` | `1` |
| `pool.max-idle` | `10` |
| `pool.max-total` | `20` |

#### Common Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `bootstrap.servers` | Kafka broker addresses (required) | - | `kafka1:9092,kafka2:9092` |
| `topic` | Topic name (required, supports templating) | - | `keycloak-events-${realmLowerCase}` |
| `acks` | Acknowledgment mode | `all` | `all`, `0`, `1` |
| `compression.type` | Compression algorithm | `lz4` | `gzip`, `snappy`, `lz4`, `zstd` |
| `batch.size` | Batch size in bytes | `32768` | `65536` |
| `linger.ms` | Batch linger time | `5` | `10` |
| `buffer.memory` | Producer buffer memory | `33554432` | `67108864` |
| `retries` | Retry attempts | `2147483647` | `3` |
| `max.in.flight.requests.per.connection` | Max unacked requests | `5` | `1` |
| `enable.idempotence` | Idempotent producer | `true` | `true`, `false` |
| `pool.min-idle` | Minimum idle connections in pool | `1` | `5` |
| `pool.max-idle` | Maximum idle connections in pool | `10` | `20` |
| `pool.max-total` | Maximum total connections in pool | `20` | `50` |

### Custom Headers

Custom headers can be added to Kafka messages:

```bash
kete.routes.kafka.destination.headers.X-Source=keycloak
kete.routes.kafka.destination.headers.X-Environment=production
```

All custom headers are included in the Kafka message headers.

### Topic Templating

The topic name supports variable substitution:

```bash
# Dynamic topic per realm
kete.routes.kafka.destination.topic=keycloak-events-${realmLowerCase}

# Dynamic topic per event type
kete.routes.kafka.destination.topic=keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Configuration Examples

#### Production Configuration (High Reliability)

```bash
kete.routes.prod.destination.bootstrap.servers=kafka1:9092,kafka2:9092,kafka3:9092
kete.routes.prod.destination.topic=keycloak-events
kete.routes.prod.destination.acks=all
kete.routes.prod.destination.compression.type=snappy
kete.routes.prod.destination.enable.idempotence=true
kete.routes.prod.destination.max.in.flight.requests.per.connection=5
kete.routes.prod.destination.retries=10
```

#### High Throughput Configuration

```bash
kete.routes.throughput.destination.bootstrap.servers=kafka:9092
kete.routes.throughput.destination.topic=keycloak-events
kete.routes.throughput.destination.acks=1
kete.routes.throughput.destination.compression.type=lz4
kete.routes.throughput.destination.batch.size=65536
kete.routes.throughput.destination.linger.ms=10
kete.routes.throughput.destination.buffer.memory=67108864
```

#### Low Latency Configuration

```bash
kete.routes.lowlatency.destination.bootstrap.servers=kafka:9092
kete.routes.lowlatency.destination.topic=keycloak-events
kete.routes.lowlatency.destination.acks=1
kete.routes.lowlatency.destination.linger.ms=0
kete.routes.lowlatency.destination.compression.type=none
```

#### Security Configuration (TLS/SASL)

```bash
kete.routes.secure.destination.bootstrap.servers=kafka-secure:9093
kete.routes.secure.destination.topic=keycloak-events
kete.routes.secure.destination.security.protocol=SASL_SSL
kete.routes.secure.destination.sasl.mechanism=SCRAM-SHA-512
kete.routes.secure.destination.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="keycloak" password="secret";
kete.routes.secure.destination.ssl.truststore.location=/path/to/truststore.jks
kete.routes.secure.destination.ssl.truststore.password=truststore-password
```



## Configuration Examples

### Example 1: Multiple Topics by Event Type

```bash
# Login events to one topic (glob)
kete.routes.logins.destination.kind=kafka
kete.routes.logins.realm-matchers.realm=list:master
kete.routes.logins.event-matchers.login=glob:LOGIN*
kete.routes.logins.destination.bootstrap.servers=kafka:9092
kete.routes.logins.destination.topic=keycloak-logins

# Admin events to another topic (admin event format: RESOURCETYPE_OPERATIONTYPE)
kete.routes.admin.destination.kind=kafka
kete.routes.admin.realm-matchers.realm=list:master
kete.routes.admin.event-matchers.user-ops=glob:USER_*
kete.routes.admin.destination.bootstrap.servers=kafka:9092
kete.routes.admin.destination.topic=keycloak-admin
```

### Example 2: Multi-Cluster Setup

```bash
# Primary cluster
kete.routes.primary.destination.kind=kafka
kete.routes.primary.realm-matchers.realm=list:master
kete.routes.primary.event-matchers.filter=glob:*
kete.routes.primary.destination.bootstrap.servers=kafka-primary:9092
kete.routes.primary.destination.topic=events

# Backup cluster
kete.routes.backup.destination.kind=kafka
kete.routes.backup.realm-matchers.realm=list:master
kete.routes.backup.event-matchers.filter=glob:*
kete.routes.backup.destination.bootstrap.servers=kafka-backup:9092
kete.routes.backup.destination.topic=events
```

### Example 3: Per-Realm Topics

```bash
# Production realm
kete.routes.prod-example.destination.kind=kafka
kete.routes.prod-example.realm-matchers.realm=list:production
kete.routes.prod-example.event-matchers.filter=glob:*
kete.routes.prod-example.destination.bootstrap.servers=kafka:9092
kete.routes.prod-example.destination.topic=prod-events

# Development realm
kete.routes.dev-example.destination.kind=kafka
kete.routes.dev-example.realm-matchers.realm=list:development
kete.routes.dev-example.event-matchers.filter=glob:*
kete.routes.dev-example.destination.bootstrap.servers=kafka:9092
kete.routes.dev-example.destination.topic=dev-events
```
