# STOMP Destination

Stream Keycloak events to STOMP-compatible message brokers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `stomp` |
| **Protocol** | STOMP 1.2 |



## Compatible Systems

STOMP (Simple Text Oriented Messaging Protocol) is supported by many enterprise message brokers:

| System | STOMP Port | Notes |
|--------|:----------:|-------|
| **ActiveMQ Classic** | 61613 | Native support, widely deployed |
| **ActiveMQ Artemis** | 61613 | Native support |
| **RabbitMQ** | 61613 | Via STOMP plugin |
| **Amazon MQ** | 61614 | Managed ActiveMQ |
| **Apache Apollo** | 61613 | Native support |
| **HornetQ** | 61613 | Legacy, native support |
| **Solace PubSub+** | 61613 | Native support |
| **TIBCO EMS** | 61613 | Native support |
| **OpenMQ** | 61613 | Native support |
| **LavinMQ** | 61613 | Native support |

!!! tip "When to Use STOMP"
    STOMP is particularly useful for **ActiveMQ Classic**, which doesn't support AMQP 1.0 natively.
    If your broker supports AMQP 1.0, consider using the [amqp-1](amqp-1.md) destination instead.



## Example Configurations

=== "ActiveMQ Classic"

    ```bash
    kete.routes.activemq.destination.kind=stomp
    kete.routes.activemq.destination.host=activemq.example.com
    kete.routes.activemq.destination.port=61613
    kete.routes.activemq.destination.destination=/queue/keycloak-events
    kete.routes.activemq.destination.username=admin
    kete.routes.activemq.destination.password=admin
    ```

=== "Amazon MQ"

    ```bash
    kete.routes.amazonmq.destination.kind=stomp
    kete.routes.amazonmq.destination.host=b-xxxx.mq.us-east-1.amazonaws.com
    kete.routes.amazonmq.destination.port=61614
    kete.routes.amazonmq.destination.destination=/queue/keycloak-events
    kete.routes.amazonmq.destination.username=admin
    kete.routes.amazonmq.destination.password=secret
    kete.routes.amazonmq.destination.tls.enabled=true
    ```

=== "RabbitMQ"

    ```bash
    kete.routes.rabbitmq-stomp.destination.kind=stomp
    kete.routes.rabbitmq-stomp.destination.host=rabbitmq.example.com
    kete.routes.rabbitmq-stomp.destination.port=61613
    kete.routes.rabbitmq-stomp.destination.destination=/queue/keycloak-events
    kete.routes.rabbitmq-stomp.destination.username=guest
    kete.routes.rabbitmq-stomp.destination.password=guest
    ```

=== "With Receipt Acknowledgment"

    ```bash
    kete.routes.reliable.destination.kind=stomp
    kete.routes.reliable.destination.host=activemq.example.com
    kete.routes.reliable.destination.port=61613
    kete.routes.reliable.destination.destination=/queue/keycloak-events
    kete.routes.reliable.destination.receipt-enabled=true
    ```



## Features

- ✅ STOMP 1.2 protocol support
- ✅ Queue and topic destinations
- ✅ Optional receipt acknowledgment
- ✅ Heart-beat for connection health
- ✅ TLS/SSL support with mutual TLS (mTLS)
- ✅ Custom headers on messages



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `stomp` | `stomp` |
| `destination.host` | STOMP broker hostname | `activemq.example.com` |
| `destination.destination` | STOMP destination (supports templating) | `/queue/keycloak-events` |

### Dynamic Destinations (Templating)

The `destination` property supports template variables:

```bash
# Dynamic destination per realm
kete.routes.stomp.destination.destination=/queue/keycloak/${realmLowerCase}/events

# Dynamic destination per event type
kete.routes.stomp.destination.destination=/topic/keycloak/${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `61613` (TCP) / `61614` (TLS) | STOMP broker port | `61614` |
| `destination.username` | `""` | STOMP login username | `admin` |
| `destination.password` | `""` | STOMP login passcode | `secret` |
| `destination.virtual-host` | Same as `host` | Virtual host for STOMP CONNECT | `/` |
| `destination.receipt-enabled` | `false` | Wait for broker receipt acknowledgment | `true` |
| `destination.heart-beat-outgoing` | `30000` | Outgoing heart-beat interval (ms), 0=disabled | `10000` |
| `destination.heart-beat-incoming` | `30000` | Incoming heart-beat interval (ms), 0=disabled | `10000` |
| `destination.read-timeout-millis` | `30000` | Socket read timeout in milliseconds | `60000` |
| `destination.message-headers-enabled` | `true` | Include event metadata as STOMP headers | `false` |
| `destination.min-pool-size` | `5` | Minimum connections in pool | `10` |
| `destination.max-pool-size` | `20` | Maximum connections in pool | `50` |

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## STOMP Destination Format

STOMP destinations follow broker-specific conventions:

| Broker | Queue Format | Topic Format |
|--------|--------------|--------------|
| ActiveMQ | `/queue/name` | `/topic/name` |
| RabbitMQ | `/queue/name` | `/topic/name` or `/exchange/name` |
| Artemis | `/queue/name` | `/topic/name` |



## Message Headers

When `message-headers-enabled=true` (default), the following STOMP headers are included:

| Header | Description |
|--------|-------------|
| `content-type` | MIME type from serializer (e.g., `application/json`) |
| `content-length` | Message body size in bytes |
| `event-type` | Keycloak event type (e.g., `LOGIN`, `LOGOUT`) |
| `event-kind` | Event kind (`USER_EVENT` or `ADMIN_EVENT`) |



## Receipt Acknowledgment

When `receipt-enabled=true`, KETE waits for the broker to acknowledge each message:

```bash
kete.routes.reliable.destination.receipt-enabled=true
```

This provides at-least-once delivery guarantee but increases latency.



## Configuration Examples

### Basic ActiveMQ Classic

```bash
kete.routes.stomp.destination.kind=stomp
kete.routes.stomp.realm-matchers.realm=list:master
kete.routes.stomp.destination.host=activemq
kete.routes.stomp.destination.port=61613
kete.routes.stomp.destination.destination=/queue/keycloak-events
```

### Secure STOMP with TLS

```bash
kete.routes.secure-stomp.destination.kind=stomp
kete.routes.secure-stomp.realm-matchers.realm=list:master
kete.routes.secure-stomp.destination.host=activemq.example.com
kete.routes.secure-stomp.destination.port=61614
kete.routes.secure-stomp.destination.destination=/queue/keycloak-events
kete.routes.secure-stomp.destination.username=admin
kete.routes.secure-stomp.destination.password=secret
kete.routes.secure-stomp.destination.tls.enabled=true
kete.routes.secure-stomp.destination.tls.trust-store.path=/certs/ca.pem
```

### With Heart-Beat

```bash
kete.routes.heartbeat.destination.kind=stomp
kete.routes.heartbeat.destination.host=activemq.example.com
kete.routes.heartbeat.destination.destination=/queue/keycloak-events
kete.routes.heartbeat.destination.heart-beat-outgoing=10000
kete.routes.heartbeat.destination.heart-beat-incoming=10000
```
