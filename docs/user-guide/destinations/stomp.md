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
| **ActiveMQ Artemis** | 61613/61616 | Native support (61616 is multi-protocol) |
| **RabbitMQ** | 61613 | Via `rabbitmq_stomp` plugin |
| **EMQX** | 61613 | Via STOMP gateway |
| **Amazon MQ** | 61614 | Managed ActiveMQ |
| **Apache Apollo** | 61613 | Native support |
| **HornetQ** | 61613 | Legacy, native support |
| **TIBCO EMS** | 61613 | Native support |
| **OpenMQ** | 61613 | Native support |

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

=== "ActiveMQ Artemis"

    ```bash
    kete.routes.artemis.destination.kind=stomp
    kete.routes.artemis.destination.host=artemis.example.com
    kete.routes.artemis.destination.port=61613
    kete.routes.artemis.destination.destination=/queue/keycloak-events
    kete.routes.artemis.destination.username=admin
    kete.routes.artemis.destination.password=admin
    ```

    !!! warning "Artemis Broker Configuration Required"
        ActiveMQ Artemis requires the STOMP acceptor to be configured with `anycastPrefix` and `multicastPrefix` 
        for `/queue/*` destinations to create ANYCAST queues instead of MULTICAST addresses. See [Artemis Configuration](#artemis-configuration) below.

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

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Headers

Custom headers can be added to STOMP messages:

```bash
kete.routes.stomp.destination.headers.X-Source=keycloak
kete.routes.stomp.destination.headers.X-Environment=production
```

Headers are included in the STOMP message headers.

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `61613` (TCP) / `61614` (TLS) | STOMP broker port | `61614` |
| `destination.username` | `""` | STOMP login username | `admin` |
| `destination.password` | `""` | STOMP login passcode | `secret` |
| `destination.virtual-host` | Same as `host` | Virtual host for STOMP CONNECT | `/` |
| `destination.receipt-enabled` | `true` | Wait for broker receipt acknowledgment | `false` |
| `destination.heart-beat-outgoing-seconds` | `30` | Outgoing heart-beat interval in seconds, 0=disabled | `10` |
| `destination.heart-beat-incoming-seconds` | `30` | Incoming heart-beat interval in seconds, 0=disabled | `10` |
| `destination.read-timeout-seconds` | `30` | Socket read timeout in seconds | `60` |
| `destination.pool.min-idle` | `1` | Minimum idle connections in pool | `5` |
| `destination.pool.max-idle` | `10` | Maximum idle connections in pool | `20` |
| `destination.pool.max-total` | `20` | Maximum total connections in pool | `50` |

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



## Artemis Configuration

ActiveMQ Artemis requires special STOMP acceptor configuration for `/queue/*` destinations to work correctly.

!!! danger "Critical: Without this configuration, messages fail"
    By default, Artemis creates MULTICAST addresses for all STOMP destinations. This causes messages sent to 
    `/queue/keycloak-events` to be undeliverable because no queue is bound to the MULTICAST address.

Add the `anycastPrefix` and `multicastPrefix` parameters to your Artemis STOMP acceptor in `broker.xml`:

```xml
<acceptors>
   <!-- STOMP acceptor with prefix routing -->
   <acceptor name="stomp">
      tcp://0.0.0.0:61613?protocols=STOMP;anycastPrefix=/queue/;multicastPrefix=/topic/
   </acceptor>
</acceptors>
```

This configuration tells Artemis:

- Destinations starting with `/queue/` → Create **ANYCAST** addresses (point-to-point queues)
- Destinations starting with `/topic/` → Create **MULTICAST** addresses (pub/sub topics)

For a complete minimal `broker.xml` for Docker deployments, see the 
[stomp-artemis quickstart](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-artemis).

**References:**

- [Stack Overflow: STOMP sending to queue but arriving as topic on Artemis](https://stackoverflow.com/questions/68895552/stomp-sending-to-queue-but-arriving-as-topic-on-artemis)
- [Artemis STOMP Documentation](https://activemq.apache.org/components/artemis/documentation/latest/stomp.html)



## Message Headers

The following STOMP headers are always included with each message:

| Header | Description |
|--------|-------------|
| `content-type` | MIME type from serializer (e.g., `application/json`) |
| `content-length` | Message body size in bytes |
| `eventtype` | Keycloak event type (e.g., `LOGIN`, `LOGOUT`) |
| `eventkind` | Event kind (`EVENT` or `ADMIN_EVENT`) |
| `receipt` | Event ID, requesting a broker RECEIPT frame (only when `receipt-enabled=true`, the default) |



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
kete.routes.secure-stomp.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.secure-stomp.destination.tls.trust-store.loader.path=/certs/ca.pem
```

### With Heart-Beat

```bash
kete.routes.heartbeat.destination.kind=stomp
kete.routes.heartbeat.destination.host=activemq.example.com
kete.routes.heartbeat.destination.destination=/queue/keycloak-events
kete.routes.heartbeat.destination.heart-beat-outgoing-seconds=10
kete.routes.heartbeat.destination.heart-beat-incoming-seconds=10
```



## Quick Starts

| Broker | Quick Start |
|--------|-------------|
| ActiveMQ Classic | [stomp-activemq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-activemq/) |
| ActiveMQ Artemis | [stomp-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-artemis/) |
| RabbitMQ | [stomp-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-rabbitmq/) |
| EMQX | [stomp-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-emqx/) |
| Amazon MQ | [stomp-amazon-mq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-amazon-mq/) |



## See Also

- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
