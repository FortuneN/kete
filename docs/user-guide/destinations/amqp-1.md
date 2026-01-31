# AMQP 1 Destination

Stream Keycloak events to AMQP 1 brokers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `amqp-1` |
| **Protocol** | AMQP 1.0 (JMS 2.0) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Apache ActiveMQ Artemis** | Primary target, full JMS 2.0 support |
| **Azure Service Bus** | Auto-enables TLS when hostname contains `servicebus` |
| **Azure Event Hubs** | Via AMQP 1.0 |
| **Apache Qpid** | Full AMQP 1.0 support |
| **Amazon MQ for ActiveMQ** | Classic and Artemis flavors |

This destination uses AMQP 1.0 (OASIS standard). For RabbitMQ or LavinMQ, see the [AMQP 0-9-1 destination](amqp-0.9.1.md) (`kind=amqp-0.9.1`).



## Example Configurations

=== "ActiveMQ Artemis"

    ```bash
    kete.routes.artemis.destination.kind=amqp-1
    kete.routes.artemis.destination.host=artemis.example.com
    kete.routes.artemis.destination.port=5672
    kete.routes.artemis.destination.username=admin
    kete.routes.artemis.destination.password=secret
    kete.routes.artemis.destination.destination-name=keycloak.events
    kete.routes.artemis.destination.destination-type=queue
    kete.routes.artemis.destination.delivery-mode=persistent
    ```

=== "Azure Service Bus"

    ```bash
    # TLS auto-enabled when hostname contains 'servicebus'
    kete.routes.asb.destination.kind=amqp-1
    kete.routes.asb.destination.host=your-namespace.servicebus.windows.net
    kete.routes.asb.destination.port=5671
    kete.routes.asb.destination.username=your-policy-name
    kete.routes.asb.destination.password=your-policy-key
    kete.routes.asb.destination.destination-name=keycloak-events
    ```

=== "Amazon MQ (Artemis)"

    ```bash
    kete.routes.amazonmq.destination.kind=amqp-1
    kete.routes.amazonmq.destination.host=your-broker.mq.region.amazonaws.com
    kete.routes.amazonmq.destination.port=5671
    kete.routes.amazonmq.destination.username=admin
    kete.routes.amazonmq.destination.password=secret
    kete.routes.amazonmq.destination.destination-name=keycloak.events
    kete.routes.amazonmq.destination.tls.enabled=true
    ```



## Features

- Standard JMS 2.0 over AMQP 1.0 protocol
- Queue and Topic support
- TLS/SSL support with mutual TLS (mTLS)
- Persistent and non-persistent delivery
- Priority and TTL configuration
- Username/password authentication
- Configurable idle timeout for connection keep-alive



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `host` | AMQP broker hostname | `artemis.example.com` |
| `destination-name` | Queue or topic name (supports templating) | `keycloak.${realmLowerCase}.events` |

### Dynamic Destination Names (Templating)

The `destination-name` property supports template variables:

```bash
# Dynamic queue per realm
kete.routes.amqp.destination.destination-name=keycloak.${realmLowerCase}.events

# Dynamic queue per event type
kete.routes.amqp.destination.destination-name=keycloak.events.${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `port` | `5672` (TCP) / `5671` (TLS) | AMQP broker port | `5671` |
| `destination-type` | `queue` | Destination type: `queue` or `topic` | `topic` |
| `transport-type` | `amqp` | Transport: `amqp` or `amqp-web-sockets` | `amqp-web-sockets` |
| `username` | `""` | AMQP username | `admin` |
| `password` | `""` | AMQP password | `secret123` |
| `delivery-mode` | `persistent` | Message durability: `persistent` or `non-persistent` | `persistent` |
| `priority` | `4` | Message priority (0-9) | `7` |
| `time-to-live` | `0` | Message TTL in milliseconds (0 = never expires) | `60000` |
| `idle-timeout` | `60000` | Connection idle timeout in milliseconds for keep-alive (0 = disabled) | `30000` |
| `message-headers-enabled` | `true` | Include event metadata as JMS properties | `false` |
| `min-pool-size` | `5` | Minimum connections in pool | `10` |
| `max-pool-size` | `20` | Maximum connections in pool | `50` |
| `tls.*` | - | TLS/SSL configuration | See [TLS & mTLS](overview.md#tls-mtls) |

**Note**: Retry configuration (`retry.enabled`, `retry.max-attempts`, `retry.wait-duration`) is configured at the route level. See [Routes - Retry](../routes.md#retry) for details.



## Delivery Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| `persistent` | Messages survive broker restart | Audit logs, critical events |
| `non-persistent` | Messages lost on broker restart | High-throughput, non-critical events |



## Configuration Examples

### Example 1: Basic AMQP to ActiveMQ Artemis

```bash
kete.routes.artemis.destination.kind=amqp-1
kete.routes.artemis.realm-matchers.realm=list:master
kete.routes.artemis.event-matchers.filter=glob:*
kete.routes.artemis.destination.host=localhost
kete.routes.artemis.destination.port=5672
kete.routes.artemis.destination.destination-name=keycloak.events
kete.routes.artemis.destination.destination-type=queue
```

### Example 2: Azure Service Bus

```bash
kete.routes.azure-sb.destination.kind=amqp-1
kete.routes.azure-sb.realm-matchers.realm=list:master
kete.routes.azure-sb.retry.enabled=true
kete.routes.azure-sb.retry.max-attempts=3
kete.routes.azure-sb.retry.wait-duration=PT1S
kete.routes.azure-sb.destination.host=your-namespace.servicebus.windows.net
kete.routes.azure-sb.destination.port=5671
kete.routes.azure-sb.destination.destination-name=keycloak-events
kete.routes.azure-sb.destination.destination-type=queue
kete.routes.azure-sb.destination.username=RootManageSharedAccessKey
kete.routes.azure-sb.destination.password=your-sas-key
kete.routes.azure-sb.destination.delivery-mode=persistent
```

### Example 3: Qpid Broker-J with Topics

```bash
kete.routes.qpid-topics.destination.kind=amqp-1
kete.routes.qpid-topics.realm-matchers.realm=list:master
kete.routes.qpid-topics.destination.host=qpid.local
kete.routes.qpid-topics.destination.port=5672
kete.routes.qpid-topics.destination.destination-name=keycloak.events.topic
kete.routes.qpid-topics.destination.destination-type=topic
kete.routes.qpid-topics.destination.username=admin
kete.routes.qpid-topics.destination.password=admin123
```

### Example 3b: AMQP with Mutual TLS (mTLS) - File Path

```bash
kete.routes.secure-amqp.destination.kind=amqp-1
kete.routes.secure-amqp.realm-matchers.realm=list:master
kete.routes.secure-amqp.destination.host=secure-broker.example.com
kete.routes.secure-amqp.destination.port=5671
kete.routes.secure-amqp.destination.tls.enabled=true
kete.routes.secure-amqp.destination.destination-name=keycloak.events
kete.routes.secure-amqp.destination.destination-type=queue
# Client certificate authentication
kete.routes.secure-amqp.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure-amqp.destination.tls.key-store.loader.path=/path/to/client-keystore.p12
kete.routes.secure-amqp.destination.tls.key-store.password=keystorePassword
# Trust broker certificate
kete.routes.secure-amqp.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.secure-amqp.destination.tls.trust-store.loader.path=/path/to/truststore.jks
kete.routes.secure-amqp.destination.tls.trust-store.password=truststorePassword
```

### Example 3c: AMQP with Mutual TLS (mTLS) - Base64 Encoded

```bash
kete.routes.secure-amqp-b64.destination.kind=amqp-1
kete.routes.secure-amqp-b64.realm-matchers.realm=list:master
kete.routes.secure-amqp-b64.destination.host=secure-broker.example.com
kete.routes.secure-amqp-b64.destination.port=5671
kete.routes.secure-amqp-b64.destination.tls.enabled=true
kete.routes.secure-amqp-b64.destination.destination-name=keycloak.events
kete.routes.secure-amqp-b64.destination.destination-type=queue
# Base64-encoded client certificate (PKCS12 keystore)
kete.routes.secure-amqp-b64.destination.tls.key-store.loader.kind=pkcs12-file-base64
kete.routes.secure-amqp-b64.destination.tls.key-store.loader.base64=MIIKegIBAzCCCj4GCSqGSIb3DQEHAaCCCi8EggorMII...
kete.routes.secure-amqp-b64.destination.tls.key-store.password=keystorePassword
# Base64-encoded CA trust store (JKS)
kete.routes.secure-amqp-b64.destination.tls.trust-store.loader.kind=jks-file-base64
kete.routes.secure-amqp-b64.destination.tls.trust-store.loader.base64=/u3+7QAAAAIAAAABAAAA...
kete.routes.secure-amqp-b64.destination.tls.trust-store.password=truststorePassword
```

**Tip**: Generate base64-encoded keystores:
```bash
# Linux/Mac
base64 -i client-keystore.p12 -o keystore-base64.txt
base64 -i truststore.jks -o truststore-base64.txt

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("client-keystore.p12")) | Out-File keystore-base64.txt
[Convert]::ToBase64String([IO.File]::ReadAllBytes("truststore.jks")) | Out-File truststore-base64.txt
```

### Example 4: High-Priority Events with TTL

```bash
kete.routes.priority-events.destination.kind=amqp-1
kete.routes.priority-events.realm-matchers.realm=list:master
kete.routes.priority-events.event-matchers.login-error=glob:LOGIN_ERROR*
kete.routes.priority-events.event-matchers.verify-email=glob:VERIFY_EMAIL*
kete.routes.priority-events.destination.host=localhost
kete.routes.priority-events.destination.port=5672
kete.routes.priority-events.destination.destination-name=keycloak.high-priority
kete.routes.priority-events.destination.delivery-mode=persistent
kete.routes.priority-events.destination.priority=9
kete.routes.priority-events.destination.time-to-live=300000
```

### Example 5: Non-Persistent High-Throughput

```bash
kete.routes.fast-events.destination.kind=amqp-1
kete.routes.fast-events.realm-matchers.realm=list:master
kete.routes.fast-events.destination.host=localhost
kete.routes.fast-events.destination.port=5672
kete.routes.fast-events.destination.destination-name=keycloak.fast
kete.routes.fast-events.destination.delivery-mode=non-persistent
kete.routes.fast-events.destination.priority=4
```



## Destination Types

### Queues vs Topics

**Queues** (point-to-point):
- One consumer receives each message
- Load balancing across consumers

**Topics** (publish-subscribe):
- Multiple subscribers receive each message
- Fan-out pattern

### Message Persistence

```bash
# Persistent (survives broker restart)
kete.routes.amqp.destination.delivery-mode=persistent

# Non-persistent (faster, not durable)
kete.routes.amqp.destination.delivery-mode=non-persistent
```

### Priority and TTL

```bash
# High priority, expires in 5 minutes
kete.routes.amqp.destination.priority=9
kete.routes.amqp.destination.time-to-live=300000
```



## Common AMQP 1.0 Brokers

| Broker | URL Example | Notes |
|--------|-------------|-------|
| ActiveMQ Artemis | `amqp://localhost:5672` | High-performance, JMS 2.0 |
| Azure Service Bus | `amqps://xxx.servicebus.windows.net:5671` | Managed Azure service |
| Qpid Broker-J | `amqp://localhost:5672` | Full AMQP 1.0 broker |
| Qpid Dispatch Router | `amqp://localhost:5672` | High-performance router |
| Apache Qpid C++ | `amqp://localhost:5672` | C++ based broker |



## Azure Service Bus Configuration {#azure-servicebus}

Azure Service Bus is fully supported via the `amqp-1` destination. TLS is auto-enabled when the hostname contains `servicebus`.

### Basic Azure Service Bus Example

```bash
kete.routes.azure-events.destination.kind=amqp-1
kete.routes.azure-events.destination.host=your-namespace.servicebus.windows.net
kete.routes.azure-events.destination.username=RootManageSharedAccessKey
kete.routes.azure-events.destination.password=your-shared-access-key
kete.routes.azure-events.destination.destination-name=keycloak-events
```

### WebSocket Transport (for Firewall)

```bash
kete.routes.azure-ws.destination.kind=amqp-1
kete.routes.azure-ws.destination.host=your-namespace.servicebus.windows.net
kete.routes.azure-ws.destination.username=RootManageSharedAccessKey
kete.routes.azure-ws.destination.password=your-shared-access-key
kete.routes.azure-ws.destination.destination-name=keycloak-events
kete.routes.azure-ws.destination.transport-type=amqp-web-sockets
```

### Obtaining Credentials

1. Go to Azure Portal → Service Bus Namespace
2. Navigate to **Shared access policies**
3. Select or create a policy with **Send** permission
4. Use **Policy Name** as `username` and **Primary Key** as `password`
