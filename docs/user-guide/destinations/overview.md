# Destinations

Where events are delivered.

## Overview

Every route needs exactly one destination. Destinations connect to message brokers, APIs, and other systems.

## Common Features

All destinations support:

- **Destination Pooling** - Configurable pool sizes for performance tuning (see below)
- **TLS Encryption** - Secure connections with optional mutual TLS (mTLS) - see [TLS & mTLS](#tls-mtls) below
- **Dynamic Routing (Templating)** - Use variables in topic names, URLs, routing keys, etc. (see below)
- **Message Headers** - Event metadata sent with each message (see below)

## Destination Pool

KETE maintains a pool of destination instances for each route. Destination pooling improves performance by reusing instances instead of creating new ones for each event.

### Pool Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `destination.min-pool-size` | `5` | Minimum number of idle connections in the pool |
| `destination.max-pool-size` | `20` | Maximum number of connections in the pool |

### Example

```bash
# Configure pool sizes for high-volume route
kete.routes.high-volume.destination.kind=kafka
kete.routes.high-volume.destination.bootstrap.servers=kafka:9092
kete.routes.high-volume.destination.topic=keycloak-events
kete.routes.high-volume.destination.min-pool-size=10
kete.routes.high-volume.destination.max-pool-size=50
```

### Tuning Guidelines

| Scenario | Recommendation |
|----------|----------------|
| **Low volume** (< 10 events/sec) | Default values (`5` / `20`) are sufficient |
| **Medium volume** (10-100 events/sec) | Consider `min-pool-size=10`, `max-pool-size=30` |
| **High volume** (> 100 events/sec) | Consider `min-pool-size=20`, `max-pool-size=100` |
| **Fixed pool size** | Set both to the same value (e.g., `min-pool-size=15`, `max-pool-size=15`) |

!!! note "Validation Rules"
    - `min-pool-size` must be greater than 0
    - `max-pool-size` must be greater than 0
    - `max-pool-size` must be greater than or equal to `min-pool-size`

## Template Variables

All destinations support template variables for dynamic routing. Use these in topic names, exchange names, routing keys, URLs, etc.

### Available Variables

| Variable | Description | Example Values |
|----------|-------------|----------------|
| `${kindLowerCase}` | Event kind (lowercase) | `event`, `admin-event` |
| `${kindUpperCase}` | Event kind (uppercase) | `EVENT`, `ADMIN-EVENT` |
| `${eventTypeLowerCase}` | Event type (lowercase) | `login`, `logout`, `user_create` |
| `${eventTypeUpperCase}` | Event type (uppercase) | `LOGIN`, `LOGOUT`, `USER_CREATE` |
| `${realmLowerCase}` | Realm name (lowercase) | `master`, `myrealm` |
| `${realmUpperCase}` | Realm name (uppercase) | `MASTER`, `MYREALM` |
| `${resourceTypeLowerCase}` | Admin event resource (lowercase) | `user`, `client`, `realm` |
| `${resourceTypeUpperCase}` | Admin event resource (uppercase) | `USER`, `CLIENT`, `REALM` |
| `${operationTypeLowerCase}` | Admin event operation (lowercase) | `create`, `update`, `delete` |
| `${operationTypeUpperCase}` | Admin event operation (uppercase) | `CREATE`, `UPDATE`, `DELETE` |
| `${resultLowerCase}` | Event result (lowercase) | `success`, `error` |
| `${resultUpperCase}` | Event result (uppercase) | `SUCCESS`, `ERROR` |

### Understanding Event Types

**User Events** (`kind=EVENT`): Standard authentication and account events like `LOGIN`, `LOGOUT`, `REGISTER`, `UPDATE_PASSWORD`, etc.

**Admin Events** (`kind=ADMIN-EVENT`): Administrative operations performed via the Admin Console or API. The `eventType` is formed as `{resourceType}_{operationType}`, for example:

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
# → keycloak-event, keycloak-admin-event
```

**Route to different HTTP endpoints by realm:**
```bash
kete.routes.events.destination.url=https://api.example.com/${realmLowerCase}/events
# → https://api.example.com/master/events
```

## Message Headers

Most destinations send event metadata as headers alongside the message body. This is controlled by the `message-headers-enabled` property (default: `true`).

### Standard Headers

All destinations that support headers send these three headers:

| Header | Description | Example Values |
|--------|-------------|----------------|
| `eventtype` | The Keycloak event type | `LOGIN`, `LOGOUT`, `REGISTER`, `USER_CREATE`, `CLIENT_UPDATE` |
| `eventkind` | Whether it's a user event or admin event | `EVENT`, `ADMIN_EVENT` |
| `contenttype` | The MIME type of the message body | `application/json`, `application/xml`, `application/cbor` |

Header names are **all lowercase with no dashes or underscores** for maximum compatibility across messaging systems.

### Per-Destination Details

| Destination | Headers Supported | Content-Type Handling | Notes |
|-------------|:-----------------:|----------------------|-------|
| [Kafka](kafka.md) | ✅ | `contenttype` header | All three as Kafka record headers (byte[]) |
| [AMQP 0.9.1](amqp-0.9.1.md) | ✅ | Native `content-type` property | `eventtype` and `eventkind` as AMQP headers |
| [AMQP 1](amqp-1.md) | ✅ | `contenttype` JMS property | All three as JMS String properties |
| [MQTT 5](mqtt-5.md) | ✅ | Native MQTT `contentType` | `eventtype` and `eventkind` as User Properties |
| [MQTT 3](mqtt-3.md) | ❌ | Not supported | Protocol limitation |
| [HTTP](http.md) | ✅ | `contenttype` header | All three as HTTP headers |
| [WebSocket](websocket.md) | ❌ | Not supported | Headers sent via handshake only |
| [STOMP](stomp.md) | ✅ | Native `content-type` header | `eventtype` and `eventkind` as STOMP headers |

### Disabling Headers

To disable headers for a destination:

```bash
kete.routes.myroute.destination.message-headers-enabled=false
```

When disabled, only the message body is sent - no metadata headers are included.

## TLS & mTLS

All destinations support TLS encryption for secure communication. There are two main scenarios:

### TLS (Server Authentication)

Your application verifies the server's certificate. Use a **trust store** containing the CA certificate(s) that signed the server's certificate.

```bash
# Load CA certificate from file path
kete.routes.myroute.destination.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.trust-store.loader.path=/path/to/ca-cert.pem
```

### mTLS (Mutual Authentication)

Both parties verify each other. Use a **trust store** for server verification AND a **key store** for your client certificate.

```bash
# Trust store (verify server)
kete.routes.myroute.destination.trust-store.loader.kind=pem-file-path
kete.routes.myroute.destination.trust-store.loader.path=/path/to/ca-cert.pem

# Key store (your client certificate)
kete.routes.myroute.destination.key-store.loader.kind=pkcs12-file-path
kete.routes.myroute.destination.key-store.loader.path=/path/to/client.p12
kete.routes.myroute.destination.key-store.loader.password=changeit
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
| **DER** | Binary format, single certificate |
| **PKCS#12** (.p12/.pfx) | Bundled certificate + private key, password protected |
| **JKS** | Java KeyStore format, legacy Java applications |
| **PKCS#7** (.p7b/.p7c) | Certificate chains, no private keys |

## Available Destinations

| `destination.kind` | Protocol | Compatible Systems |
|--------------------|----------|-------------------|
| **[kafka](kafka.md)** | Kafka Protocol | Kafka, Redpanda, Confluent, Azure Event Hubs, Amazon MSK |
| **[amqp-0.9.1](amqp-0.9.1.md)** | AMQP 0-9-1 | RabbitMQ, LavinMQ |
| **[amqp-1](amqp-1.md)** | AMQP 1 | ActiveMQ Artemis, Azure Service Bus, Azure Event Hubs, Qpid |
| **[mqtt-3](mqtt-3.md)** | MQTT 3 | Mosquitto, HiveMQ, AWS IoT, Azure IoT Hub |
| **[mqtt-5](mqtt-5.md)** | MQTT 5 | HiveMQ, EMQX, Mosquitto 2.0+ |
| **[http](http.md)** | HTTP/HTTPS | Webhooks, REST APIs, any HTTP endpoint |
| **[websocket](websocket.md)** | WebSocket | Real-time servers, custom backends, dashboards |
| **[stomp](stomp.md)** | STOMP 1.2 | ActiveMQ Classic, Amazon MQ, RabbitMQ, Artemis |

## Cloud Services Compatibility

KETE works with major cloud messaging services through protocol compatibility:

| Cloud Service | Use Destination | Documentation |
|---------------|-----------------|---------------|
| **Azure Event Hubs** | `kafka` or `amqp-1` | [Kafka](kafka.md) / [AMQP 1](amqp-1.md) |
| **Azure Service Bus** | `amqp-1` | [AMQP 1](amqp-1.md) |
| **Amazon MSK** | `kafka` | [Kafka](kafka.md) |
| **Amazon MQ (Artemis)** | `amqp-1` | [AMQP 1](amqp-1.md) |
| **Amazon MQ (ActiveMQ)** | `stomp` | [STOMP](stomp.md) |
| **Confluent Cloud** | `kafka` | [Kafka](kafka.md) |
| **AWS IoT Core** | `mqtt-3` / `mqtt-5` | [MQTT 3](mqtt-3.md) / [MQTT 5](mqtt-5.md) |
| **Azure IoT Hub** | `mqtt-3` / `mqtt-5` | [MQTT 3](mqtt-3.md) / [MQTT 5](mqtt-5.md) |

!!! tip "No SDK Required"
    Azure Event Hubs, Azure Service Bus, Amazon MSK, and Amazon MQ all work through standard protocols—no cloud-specific SDKs needed.

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
