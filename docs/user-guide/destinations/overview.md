# Destinations

Where events are delivered.

## Overview

Every route needs exactly one destination. Destinations connect to message brokers, APIs, and other systems.

!!! tip "Which broker supports which destination?"
    See the [Destination Support Matrix](support-matrix.md) for a comprehensive cross-reference of all supported brokers and destinations.

## Common Features

All destinations support:

- **Destination Pooling** - Configurable pool sizes for performance tuning (see below)
- **TLS Encryption** - Secure connections with optional mutual TLS (mTLS) - see [TLS & mTLS](#tls-mtls) below
- **Dynamic Routing (Templating)** - Use variables in topic names, URLs, routing keys, etc. (see below)
- **Message Headers** - Event metadata sent with each message (see below)
- **Content Encoding** - Compress payload with gzip or deflate - see [Content Encodings](../content-encodings/overview.md)
- **Content Transfer Encoding** - Encode payload with base64 - see [Content Transfer Encodings](../content-transfer-encodings/overview.md)

## Destination Pool

KETE maintains a pool of destination instances for each route. Destination pooling improves performance by reusing instances instead of creating new ones for each event.

### Pool Configuration

KETE uses [Apache Commons Pool 2](https://commons.apache.org/proper/commons-pool/) for destination pooling. All properties are under the `destination.pool.*` prefix.

#### Core Properties

| Property | Default | Description |
|----------|---------|-------------|
| `destination.pool.min-idle` | `1` | Minimum number of idle connections maintained in the pool |
| `destination.pool.max-idle` | `10` | Maximum number of idle connections allowed in the pool |
| `destination.pool.max-total` | `20` | Maximum total connections (active + idle) allowed in the pool |
| `destination.pool.max-wait-seconds` | `-1` | Maximum seconds to wait when borrowing from an exhausted pool (`-1` = wait indefinitely) |
| `destination.pool.block-when-exhausted` | `true` | Whether to block when the pool is exhausted (if false, throws exception) |

#### Advanced Properties

| Property | Default | Description |
|----------|---------|-------------|
| `destination.pool.lifo` | `true` | Last In First Out - uses recently returned connections first (stack behavior) |
| `destination.pool.fairness` | `false` | Whether to ensure fairness when threads wait for connections (adds overhead) |
| `destination.pool.test-on-create` | `false` | Validate connections when created |
| `destination.pool.test-on-borrow` | `false` | Validate connections before use (recommended for production) |
| `destination.pool.test-on-return` | `false` | Validate connections when returned to pool |
| `destination.pool.test-while-idle` | `false` | Validate idle connections proactively (recommended with eviction enabled) |
| `destination.pool.time-between-eviction-runs-seconds` | `-1` | Seconds between eviction runs (-1 disables, recommended: 60 for 1 minute) |
| `destination.pool.min-evictable-idle-time-seconds` | `1800` | Minimum idle time in seconds before connection eligible for eviction (30 minutes) |
| `destination.pool.soft-min-evictable-idle-time-seconds` | `-1` | Soft minimum idle time in seconds (only evicts if min-idle exceeded, -1 disables) |
| `destination.pool.num-tests-per-eviction-run` | `3` | Number of connections to test during each eviction run |

### Example

```bash
# Basic pool configuration for high-volume route
kete.routes.high-volume.destination.kind=kafka
kete.routes.high-volume.destination.bootstrap.servers=kafka:9092
kete.routes.high-volume.destination.topic=keycloak-events
kete.routes.high-volume.destination.pool.min-idle=10
kete.routes.high-volume.destination.pool.max-idle=25
kete.routes.high-volume.destination.pool.max-total=50

# Production-ready configuration with validation and eviction
kete.routes.production.destination.kind=amqp-0.9.1
kete.routes.production.destination.host=rabbitmq.prod
kete.routes.production.destination.exchange=events
kete.routes.production.destination.pool.min-idle=5
kete.routes.production.destination.pool.max-idle=15
kete.routes.production.destination.pool.max-total=30
kete.routes.production.destination.pool.test-on-borrow=true
kete.routes.production.destination.pool.test-while-idle=true
kete.routes.production.destination.pool.time-between-eviction-runs-seconds=60
```

### Tuning Guidelines

| Scenario | Recommendation |
|----------|----------------|
| **Low volume** (< 10 events/sec) | Default values are sufficient (`min-idle=1`, `max-idle=10`, `max-total=20`) |
| **Medium volume** (10-100 events/sec) | `min-idle=10`, `max-idle=20`, `max-total=30` |
| **High volume** (> 100 events/sec) | `min-idle=20`, `max-idle=50`, `max-total=100` |
| **Fixed pool size** | Set `min-idle=max-idle=max-total` (e.g., all to `15`) |
| **Production environments** | Enable `test-on-borrow=true` and `test-while-idle=true` with `time-between-eviction-runs-seconds=60` |
| **Unstable networks** | Enable validation (`test-on-borrow=true`) to detect broken connections before use |

!!! tip "Performance vs. Reliability"
    **Default configuration favors performance** with validation disabled. For production:
    
    - Enable `test-on-borrow=true` (~1-5ms latency per borrow, but prevents broken connections)
    - Enable `test-while-idle=true` + set `time-between-eviction-runs-seconds=60` (proactive health checks)
    - Reduce `max-idle` to match `max-total` more closely (avoid holding excess idle connections)

!!! note "Validation Rules"
    - `pool.min-idle` must be greater than 0
    - `pool.max-idle` must be greater than 0
    - `pool.max-total` must be greater than 0
    - `pool.max-total` must be greater than or equal to `pool.min-idle`
    - `pool.max-wait-seconds` must be -1 or greater (`-1` = wait indefinitely)

## Template Variables

All destinations support template variables for dynamic routing. Use these in topic names, exchange names, routing keys, URLs, etc.

### Available Variables

| Variable | Description | Example Values |
|----------|-------------|----------------|
| `${kindLowerCase}` | Event kind (lowercase) | `event`, `admin_event` |
| `${kindUpperCase}` | Event kind (uppercase) | `EVENT`, `ADMIN_EVENT` |
| `${kindKebabCase}` | Event kind (kebab-case) | `event`, `admin-event` |
| `${kindPascalCase}` | Event kind (PascalCase) | `Event`, `AdminEvent` |
| `${kindCamelCase}` | Event kind (camelCase) | `event`, `adminEvent` |
| `${eventTypeLowerCase}` | Event type (lowercase) | `login`, `login_error` |
| `${eventTypeUpperCase}` | Event type (uppercase) | `LOGIN`, `LOGIN_ERROR` |
| `${eventTypeKebabCase}` | Event type (kebab-case) | `login`, `login-error` |
| `${eventTypePascalCase}` | Event type (PascalCase) | `Login`, `LoginError` |
| `${eventTypeCamelCase}` | Event type (camelCase) | `login`, `loginError` |
| `${realmLowerCase}` | Realm name (lowercase) | `master`, `my_realm` |
| `${realmUpperCase}` | Realm name (uppercase) | `MASTER`, `MY_REALM` |
| `${realmKebabCase}` | Realm name (kebab-case) | `master`, `my-realm` |
| `${realmPascalCase}` | Realm name (PascalCase) | `Master`, `MyRealm` |
| `${realmCamelCase}` | Realm name (camelCase) | `master`, `myRealm` |
| `${resourceTypeLowerCase}` | Admin event resource (lowercase) | `user`, `realm_role` |
| `${resourceTypeUpperCase}` | Admin event resource (uppercase) | `USER`, `REALM_ROLE` |
| `${resourceTypeKebabCase}` | Admin event resource (kebab-case) | `user`, `realm-role` |
| `${resourceTypePascalCase}` | Admin event resource (PascalCase) | `User`, `RealmRole` |
| `${resourceTypeCamelCase}` | Admin event resource (camelCase) | `user`, `realmRole` |
| `${operationTypeLowerCase}` | Admin event operation (lowercase) | `create`, `update`, `delete` |
| `${operationTypeUpperCase}` | Admin event operation (uppercase) | `CREATE`, `UPDATE`, `DELETE` |
| `${operationTypeKebabCase}` | Admin event operation (kebab-case) | `create`, `update`, `delete` |
| `${operationTypePascalCase}` | Admin event operation (PascalCase) | `Create`, `Update`, `Delete` |
| `${operationTypeCamelCase}` | Admin event operation (camelCase) | `create`, `update`, `delete` |
| `${resultLowerCase}` | Event result (lowercase) | `success`, `error` |
| `${resultUpperCase}` | Event result (uppercase) | `SUCCESS`, `ERROR` |
| `${resultKebabCase}` | Event result (kebab-case) | `success`, `error` |
| `${resultPascalCase}` | Event result (PascalCase) | `Success`, `Error` |
| `${resultCamelCase}` | Event result (camelCase) | `success`, `error` |

### Understanding Event Types

**User Events** (`kind=EVENT`): Standard authentication and account events like `LOGIN`, `LOGOUT`, `REGISTER`, `UPDATE_PASSWORD`, etc.

**Admin Events** (`kind=ADMIN_EVENT`): Administrative operations performed via the Admin Console or API. The `eventType` is formed as `{resourceType}_{operationType}`, for example:

- `USER_CREATE` - A user was created
- `CLIENT_UPDATE` - A client was updated  
- `REALM_DELETE` - A realm was deleted

For a complete list of event types, see [Event Types Reference](../event-types.md).

### Usage Examples

**Route events to different Kafka topics by type:**
```bash
kete.routes.events.destination.topic=keycloak-${eventTypeLowerCase}
# → keycloak-login, keycloak-logout, keycloak-user_create
```

**Route to different RabbitMQ exchanges by kind:**
```bash
kete.routes.events.destination.exchange=keycloak-${kindLowerCase}
# → keycloak-event, keycloak-admin_event
```

**Route to different HTTP endpoints by realm:**
```bash
kete.routes.events.destination.url=https://api.example.com/${realmLowerCase}/events
# → https://api.example.com/master/events
```

**Use kebab-case for URL-friendly paths:**
```bash
kete.routes.events.destination.url=https://api.example.com/events/${eventTypeKebabCase}
# → https://api.example.com/events/login-error (instead of login_error)
```

**Use PascalCase for Azure Event Grid event types:**
```bash
kete.routes.events.destination.event-type=Keycloak.${eventTypePascalCase}
# → Keycloak.LoginError
```

## Message Headers

Most destinations send event metadata as headers alongside the message body.

### Standard Headers

Most destinations that support headers send these three headers:

| Header | Description | Example Values |
|--------|-------------|----------------|
| `eventtype` | The Keycloak event type | `LOGIN`, `LOGOUT`, `REGISTER`, `USER_CREATE`, `CLIENT_UPDATE` |
| `eventkind` | Whether it's a user event or admin event | `EVENT`, `ADMIN_EVENT` |
| `contenttype` | The MIME type of the message body | `application/json`, `application/xml`, `application/cbor` |

Header names are **all lowercase with no dashes or underscores** for maximum compatibility across messaging systems. Exceptions: HTTP uses `x-eventkind`, `x-eventtype`, and the standard `Content-Type` header; some destinations use their protocol's native content-type property (see per-destination details below).

### Per-Destination Details

| Destination | Headers Supported | Content-Type Handling | Notes |
|-------------|:-----------------:|----------------------|-------|
| [Kafka](kafka.md) | ✅ | `contenttype` header | All three as Kafka record headers (byte[]) |
| [AMQP 0.9.1](amqp-0.9.1.md) | ✅ | Native `content-type` property | `eventtype` and `eventkind` as AMQP headers |
| [AMQP 1](amqp-1.md) | ✅ | `contenttype` JMS property | All three as JMS String properties |
| [MQTT 5](mqtt-5.md) | ✅ | Native MQTT `contentType` | `eventtype` and `eventkind` as User Properties |
| [MQTT 3](mqtt-3.md) | ❌ | Not supported | Protocol limitation |
| [Redis Stream](redis-stream.md) | ✅ | `contenttype` field | All three as stream entry fields |
| [Redis Pub/Sub](redis-pubsub.md) | ❌ | Not supported | Protocol limitation |
| [HTTP](http.md) | ✅ | Standard `Content-Type` header | `x-eventkind`, `x-eventtype` as custom headers; `Content-Type` as standard header |
| [WebSocket](websocket.md) | ✅ | `contenttype` header | All three + custom as HTTP headers on WebSocket client (sent during handshake) |
| [SignalR](signalr.md) | ❌ | Not supported | Message body only |
| [Socket.IO](socketio.md) | ❌ | Not supported | Message body only |
| [STOMP](stomp.md) | ✅ | Native `content-type` header | `eventtype` and `eventkind` as STOMP headers |
| [ZeroMQ](zeromq.md) | ❌ | Not supported | Raw message bytes only |
| [GCP Pub/Sub](gcp-pubsub.md) | ✅ | `contenttype` attribute | All three as Pub/Sub message attributes |
| [GCP Cloud Tasks](gcp-cloud-tasks.md) | ✅ | `contenttype` header | All three as HTTP headers on the Cloud Task request |
| [gRPC](grpc.md) | ✅ | `contenttype` metadata | All three as gRPC Metadata (ASCII string marshaller) |
| [NATS](nats.md) | ✅ | `contenttype` header | All three as NATS message headers |
| [NATS JetStream](nats-jetstream.md) | ✅ | `contenttype` header | All three as NATS message headers |
| [Pulsar](pulsar.md) | ✅ | `contenttype` property | All three as Pulsar message properties |
| [AWS EventBridge](aws-eventbridge.md) | ❌ | Not supported | No per-event custom attributes in EventBridge |
| [AWS SNS](aws-sns.md) | ✅ | `contenttype` attribute | All three as SNS MessageAttributes |
| [AWS SQS](aws-sqs.md) | ✅ | `contenttype` attribute | All three as SQS MessageAttributes |
| [AWS Kinesis](aws-kinesis.md) | ❌ | Not supported | Only data + partition key |
| [Azure Event Hubs](azure-eventhubs.md) | ✅ | `contenttype` property | All three as Application Properties |
| [Azure Service Bus](azure-servicebus.md) | ✅ | Native `contentType` | All three as Application Properties + native `contentType` property |
| [SOAP](soap.md) | ✅ | Standard `Content-Type` header | `x-eventkind`, `x-eventtype` as custom headers; `Content-Type` as standard header |
| [Azure Event Grid](azure-eventgrid.md) | ❌ | Not supported | EventGridEvent schema has no custom properties |
| [Azure Storage Queue](azure-storage-queue.md) | ❌ | Not supported | Queue messages contain body only |
| [Azure Web PubSub](azure-webpubsub.md) | ❌ | Not supported | Message body only |

## TLS & mTLS

Most destinations support TLS encryption via the standard `tls.*` configuration properties. Exceptions:

- **Azure Event Hubs** and **Azure Service Bus** — TLS is handled internally by the Azure SDK (always enabled, no `tls.*` config needed)
- **ZeroMQ** — uses CurveZMQ for encryption instead of TLS (see [ZeroMQ](zeromq.md))

### TLS Properties Reference

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.version` | `TLS` | TLS protocol version (e.g., `TLSv1.2`, `TLSv1.3`) |
| `destination.tls.verify-hostname` | `false` | Verify the server's hostname against the certificate |
| `destination.tls.trust-store.loader.kind` | — | Certificate loader kind (see below) |
| `destination.tls.trust-store.loader.*` | — | Loader-specific properties |
| `destination.tls.trust-store.password` | _(empty)_ | Trust store password |
| `destination.tls.trust-store.type` | JVM default | Trust store type (e.g., `pkcs12`, `jks`) |
| `destination.tls.trust-store.trust-manager-algorithm` | JVM default | Trust manager algorithm |
| `destination.tls.key-store.loader.kind` | — | Certificate loader kind (see below) |
| `destination.tls.key-store.loader.*` | — | Loader-specific properties |
| `destination.tls.key-store.password` | _(empty)_ | Key store password |
| `destination.tls.key-store.key-password` | _(empty)_ | Private key password (if different from key store password) |
| `destination.tls.key-store.type` | JVM default | Key store type (e.g., `pkcs12`, `jks`) |
| `destination.tls.key-store.key-manager-algorithm` | JVM default | Key manager algorithm |

There are two main scenarios:

### TLS (Server Authentication)

Your application verifies the server's certificate. Use a **trust store** containing the CA certificate(s) that signed the server's certificate.

```bash
kete.routes.myroute.destination.tls.enabled=true

# Load CA certificate from file path
kete.routes.myroute.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.tls.trust-store.loader.path=/path/to/ca-cert.pem
```

### mTLS (Mutual Authentication)

Both parties verify each other. Use a **trust store** for server verification AND a **key store** for your client certificate.

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (verify server)
kete.routes.myroute.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.tls.trust-store.loader.path=/path/to/ca-cert.pem

# Key store (your client certificate)
kete.routes.myroute.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.myroute.destination.tls.key-store.loader.path=/path/to/client.p12
kete.routes.myroute.destination.tls.key-store.password=changeit
```

### Certificate Loaders

KETE supports various certificate formats through **Certificate Loaders**. Each loader is identified by a `kind` value:

| Format | File Path | Base64 Encoded | Text Content |
|--------|:---------:|:--------------:|:------------:|
| **PEM** | `pem-file-path` | `pem-file-base64` | `pem-file-text` |
| **DER** | `der-file-path` | `der-file-base64` | — |
| **PKCS#12** | `pkcs12-file-path` | `pkcs12-file-base64` | — |
| **JKS** | `jks-file-path` | `jks-file-base64` | — |
| **PKCS#7** | `pkcs7-file-path` | `pkcs7-file-base64` | — |

For detailed information about each loader and their properties, see **[Certificate Loaders](../certificate-loaders/overview.md)**.

### Quick Format Guide

| Format | Best For |
|--------|----------|
| **PEM** | Most common, human-readable, supports certificates and private keys |
| **DER** | Binary format, single certificate, trust stores only |
| **PKCS#12** (.p12/.pfx) | Bundled certificate + private key, password protected |
| **JKS** | Java KeyStore format, legacy Java applications |
| **PKCS#7** (.p7b/.p7c) | Certificate chains, no private keys, trust stores only |

## Available Destinations

| `destination.kind` | Protocol | Compatible Systems |
|--------------------|----------|-------------------|
| **[kafka](kafka.md)** | Kafka Protocol | Kafka, Redpanda, Confluent, Azure Event Hubs, Amazon MSK |
| **[amqp-0.9.1](amqp-0.9.1.md)** | AMQP 0-9-1 | RabbitMQ, LavinMQ |
| **[amqp-1](amqp-1.md)** | AMQP 1 | ActiveMQ Artemis, Azure Service Bus, Azure Event Hubs, Qpid, RabbitMQ 4.0+, Solace PubSub+ |
| **[mqtt-3](mqtt-3.md)** | MQTT 3 | Mosquitto, HiveMQ, EMQX, VerneMQ, NanoMQ, RabbitMQ, ActiveMQ Artemis, Solace PubSub+, Apache RocketMQ, AWS IoT, Azure IoT Hub |
| **[mqtt-5](mqtt-5.md)** | MQTT 5 | HiveMQ, EMQX, VerneMQ, NanoMQ, Mosquitto 2.0+, RabbitMQ, ActiveMQ Artemis, Solace PubSub+, Azure Event Grid |
| **[redis-pubsub](redis-pubsub.md)** | Redis RESP | Redis, Valkey, Dragonfly, KeyDB, Garnet, ElastiCache, Azure Cache for Redis, Upstash |
| **[redis-stream](redis-stream.md)** | Redis RESP | Redis 5.0+, Valkey, Dragonfly, KeyDB, ElastiCache, Azure Cache for Redis, Upstash |
| **[nats](nats.md)** | NATS Protocol | NATS Server, Synadia Cloud |
| **[nats-jetstream](nats-jetstream.md)** | NATS JetStream | NATS Server, Synadia Cloud |
| **[pulsar](pulsar.md)** | Pulsar Protocol | Apache Pulsar, StreamNative Cloud, DataStax Astra Streaming, DataStax Luna Streaming |
| **[http](http.md)** | HTTP/HTTPS | Webhooks, REST APIs, any HTTP endpoint, Azure Event Grid |
| **[websocket](websocket.md)** | WebSocket | Real-time servers, custom backends, dashboards |
| **[stomp](stomp.md)** | STOMP 1.2 | ActiveMQ Classic, ActiveMQ Artemis, Amazon MQ, RabbitMQ, EMQX |
| **[zeromq](zeromq.md)** | ZMTP (ZeroMQ) | Any ZeroMQ peer (brokerless, 40+ language bindings) |
| **[gcp-pubsub](gcp-pubsub.md)** | HTTP/REST (Pub/Sub API) | Google Cloud Pub/Sub, GCP Pub/Sub Emulator |
| **[azure-storage-queue](azure-storage-queue.md)** | Azure Storage Queue REST API | Azure Storage Queue, Azurite Emulator |
| **[aws-eventbridge](aws-eventbridge.md)** | AWS EventBridge API (SDK) | Amazon EventBridge, LocalStack |
| **[aws-sqs](aws-sqs.md)** | AWS SQS API (SDK) | Amazon SQS, LocalStack |
| **[aws-sns](aws-sns.md)** | AWS SNS API (SDK) | Amazon SNS, LocalStack |
| **[aws-kinesis](aws-kinesis.md)** | AWS Kinesis API (SDK) | Amazon Kinesis Data Streams, LocalStack |
| **[azure-eventhubs](azure-eventhubs.md)** | Azure Event Hubs SDK (AMQP) | Azure Event Hubs, Azure Event Hubs Emulator |
| **[azure-servicebus](azure-servicebus.md)** | Azure Service Bus SDK (AMQP) | Azure Service Bus, Azure Service Bus Emulator |
| **[azure-eventgrid](azure-eventgrid.md)** | Azure Event Grid REST API (SDK) | Azure Event Grid |
| **[azure-webpubsub](azure-webpubsub.md)** | Azure Web PubSub REST API (SDK) | Azure Web PubSub |
| **[gcp-cloud-tasks](gcp-cloud-tasks.md)** | Cloud Tasks REST API (SDK) | Google Cloud Tasks, Cloud Tasks Emulator |
| **[grpc](grpc.md)** | gRPC (HTTP/2) | Any gRPC server |
| **[signalr](signalr.md)** | SignalR (HTTP/WebSocket) | ASP.NET Core SignalR, Azure SignalR Service |
| **[soap](soap.md)** | SOAP (HTTP/HTTPS) | Any SOAP endpoint |
| **[socketio](socketio.md)** | Socket.IO (Engine.IO) | Socket.IO v3/v4 servers (Node.js, Python, Java) |

## Cloud Services Compatibility

KETE works with major cloud messaging services through protocol compatibility:

| Cloud Service | Use Destination | Documentation |
|---------------|-----------------|---------------|
| **Azure Event Hubs** | `azure-eventhubs`, `kafka`, or `amqp-1` | [Azure Event Hubs](azure-eventhubs.md) / [Kafka](kafka.md) / [AMQP 1](amqp-1.md) |
| **Azure Service Bus** | `azure-servicebus` or `amqp-1` | [Azure Service Bus](azure-servicebus.md) / [AMQP 1](amqp-1.md) |
| **Azure Event Grid** | `azure-eventgrid`, `http`, or `mqtt-5` | [Azure Event Grid](azure-eventgrid.md) / [HTTP](http.md) / [MQTT 5](mqtt-5.md) |
| **Azure Cache for Redis** | `redis-pubsub` or `redis-stream` | [Redis Pub/Sub](redis-pubsub.md) / [Redis Stream](redis-stream.md) |
| **Azure Storage Queue** | `azure-storage-queue` | [Azure Storage Queue](azure-storage-queue.md) |
| **Amazon ElastiCache** | `redis-pubsub` or `redis-stream` | [Redis Pub/Sub](redis-pubsub.md) / [Redis Stream](redis-stream.md) |
| **Amazon MSK** | `kafka` | [Kafka](kafka.md) |
| **Amazon MQ (Artemis)** | `amqp-1` or `stomp` | [AMQP 1](amqp-1.md) / [STOMP](stomp.md) |
| **Amazon MQ (ActiveMQ)** | `amqp-1` or `stomp` | [AMQP 1](amqp-1.md) / [STOMP](stomp.md) |
| **Amazon MQ (RabbitMQ)** | `amqp-0.9.1` or `amqp-1` | [AMQP 0.9.1](amqp-0.9.1.md) / [AMQP 1](amqp-1.md) |
| **Confluent Cloud** | `kafka` | [Kafka](kafka.md) |
| **AWS IoT Core** | `mqtt-3` | [MQTT 3](mqtt-3.md) |
| **Azure IoT Hub** | `mqtt-3` | [MQTT 3](mqtt-3.md) |
| **Google Cloud Memorystore** | `redis-pubsub` or `redis-stream` | [Redis Pub/Sub](redis-pubsub.md) / [Redis Stream](redis-stream.md) |
| **Google Cloud Pub/Sub** | `gcp-pubsub` | [GCP Pub/Sub](gcp-pubsub.md) |
| **Google Cloud Tasks** | `gcp-cloud-tasks` | [GCP Cloud Tasks](gcp-cloud-tasks.md) |
| **Amazon EventBridge** | `aws-eventbridge` | [AWS EventBridge](aws-eventbridge.md) |
| **Amazon SQS** | `aws-sqs` | [AWS SQS](aws-sqs.md) |
| **Amazon SNS** | `aws-sns` | [AWS SNS](aws-sns.md) |
| **Amazon Kinesis** | `aws-kinesis` | [AWS Kinesis](aws-kinesis.md) |
| **Azure Web PubSub** | `azure-webpubsub` | [Azure Web PubSub](azure-webpubsub.md) |
| **Azure SignalR Service** | `signalr` | [SignalR](signalr.md) |
| **Upstash** | `redis-pubsub` or `redis-stream` | [Redis Pub/Sub](redis-pubsub.md) / [Redis Stream](redis-stream.md) |
| **Aiven for Kafka** | `kafka` | [Kafka](kafka.md) |
| **StreamNative Cloud** | `pulsar` | [Pulsar](pulsar.md) |
| **DataStax Astra Streaming** | `pulsar` | [Pulsar](pulsar.md) |
| **DataStax Luna Streaming** | `pulsar` | [Pulsar](pulsar.md) |

!!! tip "Multiple Protocol Options"
    Azure Event Hubs, Azure Service Bus, and Azure Event Grid can each be accessed via their native SDK destination or through standard protocols (Kafka, AMQP 1.0, HTTP, MQTT). Use the native SDK destination for features like Managed Identity authentication, or use protocol-based destinations for portability.

## Quick Examples

**Kafka:**
```bash
kete.routes.events.destination.kind=kafka
kete.routes.events.destination.bootstrap.servers=kafka:9092
kete.routes.events.destination.topic=keycloak-events
```

**RabbitMQ:**
```bash
kete.routes.events.destination.kind=amqp-0.9.1
kete.routes.events.destination.host=rabbitmq
kete.routes.events.destination.exchange=keycloak-events
kete.routes.events.destination.routing-key=events
```

**HTTP Webhook:**
```bash
kete.routes.events.destination.kind=http
kete.routes.events.destination.url=https://api.example.com/events
```

**ZeroMQ (PUB/SUB):**
```bash
kete.routes.events.destination.kind=zeromq
kete.routes.events.destination.endpoint=tcp://subscriber:5556
# Defaults: socket-type=PUBLISH, connection-mode=CONNECT
```

**GCP Pub/Sub:**
```bash
kete.routes.events.destination.kind=gcp-pubsub
kete.routes.events.destination.project=my-gcp-project
kete.routes.events.destination.topic=keycloak-events
kete.routes.events.destination.credentials-file-path=/secrets/service-account.json
```

**Azure Storage Queue:**
```bash
kete.routes.events.destination.kind=azure-storage-queue
kete.routes.events.destination.connection-string=DefaultEndpointsProtocol=https;AccountName=mystorageaccount;AccountKey=your-key;EndpointSuffix=core.windows.net
kete.routes.events.destination.queue=keycloak-events
```

**AWS EventBridge:**
```bash
kete.routes.events.destination.kind=aws-eventbridge
kete.routes.events.destination.event-bus=my-event-bus
kete.routes.events.destination.source=keycloak
kete.routes.events.destination.detail-type=KeycloakEvent
kete.routes.events.destination.region=us-east-1
```

**Azure Web PubSub:**
```bash
kete.routes.events.destination.kind=azure-webpubsub
kete.routes.events.destination.connection-string=Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=your-key;Version=1.0;
kete.routes.events.destination.hub=keycloak-events
```

**GCP Cloud Tasks:**
```bash
kete.routes.events.destination.kind=gcp-cloud-tasks
kete.routes.events.destination.project=my-gcp-project
kete.routes.events.destination.location=us-central1
kete.routes.events.destination.queue=keycloak-events
kete.routes.events.destination.target-url=https://my-service.run.app/events
```

**SignalR:**
```bash
kete.routes.events.destination.kind=signalr
kete.routes.events.destination.url=http://signalr-server:5000/hub
kete.routes.events.destination.hub-method=SendEvent
```

**Socket.IO:**
```bash
kete.routes.events.destination.kind=socketio
kete.routes.events.destination.url=http://socketio-server:3000
kete.routes.events.destination.event-name=keycloak-event
```

**gRPC:**
```bash
kete.routes.events.destination.kind=grpc
kete.routes.events.destination.host=grpc-server
kete.routes.events.destination.port=50051
kete.routes.events.destination.service=EventService
kete.routes.events.destination.method=SendEvent
kete.routes.events.destination.use-plaintext=true
```

**SOAP:**
```bash
kete.routes.events.destination.kind=soap
kete.routes.events.destination.url=https://api.example.com/soap/events
```

**Azure Event Grid:**
```bash
kete.routes.events.destination.kind=azure-eventgrid
kete.routes.events.destination.endpoint=https://my-topic.eastus-1.eventgrid.azure.net/api/events
kete.routes.events.destination.access-key=your-access-key
```

**Azure Event Hubs:**
```bash
kete.routes.events.destination.kind=azure-eventhubs
kete.routes.events.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=send;SharedAccessKey=your-key;EntityPath=my-hub
```

**Azure Service Bus:**
```bash
kete.routes.events.destination.kind=azure-servicebus
kete.routes.events.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=send;SharedAccessKey=your-key
kete.routes.events.destination.queue=keycloak-events
```
