# AMQP 0.9.1 Destination

Stream Keycloak events to AMQP 0.9.1 brokers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `amqp-0.9.1` |
| **Protocol** | AMQP 0.9.1 |



## Compatible Systems

| System | Notes |
|--------|-------|
| **RabbitMQ** | Primary target, all features supported |
| **LavinMQ** | Lightweight alternative, fully compatible |
| **CloudAMQP** | Managed RabbitMQ (supports TLS) |
| **Amazon MQ for RabbitMQ** | AWS managed (supports TLS) |

This destination uses AMQP 0.9.1 (RabbitMQ's native protocol). For AMQP 1 brokers like ActiveMQ Artemis or Azure Service Bus, see the [AMQP 1 destination](amqp-1.md) (`kind=amqp-1`).



## Example Configurations

=== "RabbitMQ"

    ```bash
    kete.routes.rabbitmq.destination.kind=amqp-0.9.1
    kete.routes.rabbitmq.destination.host=rabbitmq.example.com
    kete.routes.rabbitmq.destination.port=5672
    kete.routes.rabbitmq.destination.username=keycloak
    kete.routes.rabbitmq.destination.password=secret
    kete.routes.rabbitmq.destination.virtual-host=/keycloak
    kete.routes.rabbitmq.destination.exchange=keycloak-events
    kete.routes.rabbitmq.destination.routing-key=events
    ```

=== "LavinMQ"

    ```bash
    kete.routes.lavinmq.destination.kind=amqp-0.9.1
    kete.routes.lavinmq.destination.host=lavinmq.example.com
    kete.routes.lavinmq.destination.port=5672
    kete.routes.lavinmq.destination.username=keycloak
    kete.routes.lavinmq.destination.password=secret
    kete.routes.lavinmq.destination.exchange=keycloak-events
    ```

=== "CloudAMQP (Managed)"

    ```bash
    kete.routes.cloudamqp.destination.kind=amqp-0.9.1
    kete.routes.cloudamqp.destination.host=your-instance.cloudamqp.com
    kete.routes.cloudamqp.destination.port=5671
    kete.routes.cloudamqp.destination.username=your-user
    kete.routes.cloudamqp.destination.password=your-password
    kete.routes.cloudamqp.destination.virtual-host=your-vhost
    kete.routes.cloudamqp.destination.exchange=keycloak-events
    kete.routes.cloudamqp.destination.tls.enabled=true
    ```



## Features

- Full AMQP 0.9.1 protocol support
- Exchange and routing key configuration
- TLS/SSL support with mutual TLS (mTLS)
- Persistent and non-persistent delivery modes
- Message priority and TTL configuration
- Automatic reconnection and topology recovery
- Custom headers on messages
- Virtual host support
- Dynamic exchange and routing key (templating)



## Configuration Properties

### Required Properties

```bash
kete.routes.<NAME>.destination.kind=amqp-0.9.1
kete.routes.<NAME>.destination.host=<RABBITMQ_HOST>
kete.routes.<NAME>.destination.exchange=<EXCHANGE_NAME>
```

### Basic Example

```bash
# Configure AMQP 0.9.1 destination
kete.routes.main-rabbitmq.destination.kind=amqp-0.9.1
kete.routes.main-rabbitmq.realm-matchers.realm=list:master
kete.routes.main-rabbitmq.event-matchers.filter=glob:*

# RabbitMQ-specific configuration
kete.routes.main-rabbitmq.destination.host=localhost
kete.routes.main-rabbitmq.destination.port=5672
kete.routes.main-rabbitmq.destination.username=guest
kete.routes.main-rabbitmq.destination.password=guest
kete.routes.main-rabbitmq.destination.exchange=keycloak-events
kete.routes.main-rabbitmq.destination.routing-key=events
```

### All RabbitMQ Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `host` | RabbitMQ server hostname (required) | - | `rabbitmq.example.com` |
| `exchange` | Exchange name, required (supports templating) | - | `keycloak-${realmLowerCase}` |
| `port` | RabbitMQ server port | `5672` | `5671` (AMQPS) |
| `username` | Authentication username | `""` | `keycloak` |
| `password` | Authentication password | `""` | `secret123` |
| `virtual-host` | Virtual host | `/` | `/production` |
| `routing-key` | Routing key (supports templating) | `""` | `${eventTypeLowerCase}` |
| `priority` | Message priority (0-9, only applied when explicitly set) | _(none)_ | `7` |
| `delivery-mode` | Message durability: `persistent` or `non-persistent` | `persistent` | `persistent` |
| `time-to-live-seconds` | Message TTL in seconds (0 = never expires) | `0` | `60` |
| `connection-timeout-seconds` | Connection timeout in seconds | `10` | `30` |
| `handshake-timeout-seconds` | Handshake timeout in seconds | `10` | `5` |
| `channel-rpc-timeout-seconds` | Channel RPC timeout in seconds | `10` | `15` |
| `requested-heartbeat-seconds` | Heartbeat interval in seconds | `30` | `60` |
| `automatic-recovery-enabled` | Enable automatic connection recovery | `true` | `false` |
| `network-recovery-interval-seconds` | Network recovery interval in seconds | `5` | `10` |
| `topology-recovery-enabled` | Enable topology recovery | Same as `automatic-recovery-enabled` | `true` |
| `pool.min-idle` | Minimum idle connections in pool | `1` | `5` |
| `pool.max-idle` | Maximum idle connections in pool | `10` | `20` |
| `pool.max-total` | Maximum total connections in pool | `20` | `50` |
| `tls.*` | TLS/SSL configuration | - | See [TLS & mTLS](overview.md#tls-mtls) |

### Custom Headers

Custom headers can be added to AMQP messages:

```bash
kete.routes.rabbitmq.destination.headers.X-Source=keycloak
kete.routes.rabbitmq.destination.headers.X-Environment=production
```

Headers are included in the AMQP message properties.

### Dynamic Exchange/Routing Key (Templating)

The `exchange` and `routing-key` properties support template variables:

```bash
# Dynamic exchange per realm
kete.routes.rabbitmq.destination.exchange=keycloak-${realmLowerCase}

# Dynamic routing key per event type
kete.routes.rabbitmq.destination.routing-key=${eventTypeLowerCase}

# Combine both
kete.routes.rabbitmq.destination.exchange=keycloak-events
kete.routes.rabbitmq.destination.routing-key=${realmLowerCase}.${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Delivery Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| `persistent` | Messages survive broker restart | Audit logs, critical events |
| `non-persistent` | Messages lost on broker restart | High-throughput, non-critical events |

### Configuration Examples

#### Production Configuration

```bash
kete.routes.prod.destination.host=rabbitmq-cluster.prod.internal
kete.routes.prod.destination.port=5672
kete.routes.prod.destination.username=keycloak-prod
kete.routes.prod.destination.password=secure-password
kete.routes.prod.destination.virtual-host=/production
kete.routes.prod.destination.exchange=keycloak-events
kete.routes.prod.destination.routing-key=events.production
```

#### TLS/SSL Configuration - File Path

```bash
kete.routes.secure.destination.host=rabbitmq-secure.example.com
kete.routes.secure.destination.port=5671
kete.routes.secure.destination.username=keycloak
kete.routes.secure.destination.password=secret
kete.routes.secure.destination.exchange=keycloak-events
kete.routes.secure.destination.tls.enabled=true
kete.routes.secure.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.secure.destination.tls.trust-store.loader.path=/opt/keycloak/certs/truststore.jks
kete.routes.secure.destination.tls.trust-store.password=truststorepass

# With client certificate authentication (mTLS)
kete.routes.secure-mutual.destination.host=rabbitmq-secure.example.com
kete.routes.secure-mutual.destination.port=5671
kete.routes.secure-mutual.destination.exchange=keycloak-events
kete.routes.secure-mutual.destination.tls.enabled=true
kete.routes.secure-mutual.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.secure-mutual.destination.tls.key-store.loader.path=/opt/keycloak/certs/client.p12
kete.routes.secure-mutual.destination.tls.key-store.password=keystorepass
kete.routes.secure-mutual.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.secure-mutual.destination.tls.trust-store.loader.path=/opt/keycloak/certs/truststore.jks
kete.routes.secure-mutual.destination.tls.trust-store.password=truststorepass
```

#### TLS/SSL Configuration - Base64 Encoded

```bash
kete.routes.secure-b64.destination.host=rabbitmq-secure.example.com
kete.routes.secure-b64.destination.port=5671
kete.routes.secure-b64.destination.username=keycloak
kete.routes.secure-b64.destination.password=secret
kete.routes.secure-b64.destination.exchange=keycloak-events
kete.routes.secure-b64.destination.tls.enabled=true
# Base64-encoded client certificate (PKCS12 keystore)
kete.routes.secure-b64.destination.tls.key-store.loader.kind=pkcs12-file-base64
kete.routes.secure-b64.destination.tls.key-store.loader.base64=MIIKegIBAzCCCj4GCSqGSIb3DQEHAaCCCi8EggorMII...
kete.routes.secure-b64.destination.tls.key-store.password=keystorepass
# Base64-encoded CA trust store (JKS)
kete.routes.secure-b64.destination.tls.trust-store.loader.kind=jks-file-base64
kete.routes.secure-b64.destination.tls.trust-store.loader.base64=/u3+7QAAAAIAAAABAAAA...
kete.routes.secure-b64.destination.tls.trust-store.password=truststorepass
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

#### Multi-VHost Setup

```bash
# Production vhost
kete.routes.prod-example.destination.host=rabbitmq
kete.routes.prod-example.destination.virtual-host=/production
kete.routes.prod-example.destination.exchange=events

# Development vhost
kete.routes.dev-example.destination.host=rabbitmq
kete.routes.dev-example.destination.virtual-host=/development
kete.routes.dev-example.destination.exchange=events
```



## Exchange Patterns

### Topic Exchange

Best for flexible routing based on event types:

**Setup**:

=== "Linux/macOS"

    ```bash
    # Create topic exchange
    rabbitmqadmin declare exchange name=keycloak-events type=topic

    # Create queues
    rabbitmqadmin declare queue name=all-events
    rabbitmqadmin declare queue name=login-events
    rabbitmqadmin declare queue name=admin-events

    # Bind queues
    rabbitmqadmin declare binding source=keycloak-events \
      destination=all-events routing_key='#'
      
    rabbitmqadmin declare binding source=keycloak-events \
      destination=login-events routing_key='LOGIN*'
      
    rabbitmqadmin declare binding source=keycloak-events \
      destination=admin-events routing_key='*:*'
    ```

=== "Windows (PowerShell)"

    ```powershell
    # Create topic exchange
    rabbitmqadmin declare exchange name=keycloak-events type=topic

    # Create queues
    rabbitmqadmin declare queue name=all-events
    rabbitmqadmin declare queue name=login-events
    rabbitmqadmin declare queue name=admin-events

    # Bind queues
    rabbitmqadmin declare binding source=keycloak-events `
      destination=all-events routing_key='#'
      
    rabbitmqadmin declare binding source=keycloak-events `
      destination=login-events routing_key='LOGIN*'
      
    rabbitmqadmin declare binding source=keycloak-events `
      destination=admin-events routing_key='*:*'
    ```

**Configuration**:
```bash
kete.routes.topic.destination.exchange=keycloak-events
kete.routes.topic.destination.routing-key=events
```

### Direct Exchange

Best for simple point-to-point messaging:

**Setup**:

=== "Linux/macOS"

    ```bash
    # Create direct exchange
    rabbitmqadmin declare exchange name=keycloak-events type=direct

    # Create queue
    rabbitmqadmin declare queue name=event-processor

    # Bind queue
    rabbitmqadmin declare binding source=keycloak-events \
      destination=event-processor routing_key='events'
    ```

=== "Windows (PowerShell)"

    ```powershell
    # Create direct exchange
    rabbitmqadmin declare exchange name=keycloak-events type=direct

    # Create queue
    rabbitmqadmin declare queue name=event-processor

    # Bind queue
    rabbitmqadmin declare binding source=keycloak-events `
      destination=event-processor routing_key='events'
    ```

**Configuration**:
```bash
kete.routes.direct.destination.exchange=keycloak-events
kete.routes.direct.destination.routing-key=events
```

### Fanout Exchange

Best for broadcasting to all consumers:

**Setup**:
```bash
# Create fanout exchange
rabbitmqadmin declare exchange name=keycloak-events type=fanout

# Create queues
rabbitmqadmin declare queue name=consumer1
rabbitmqadmin declare queue name=consumer2

# Bind queues (routing key ignored for fanout)
rabbitmqadmin declare binding source=keycloak-events destination=consumer1
rabbitmqadmin declare binding source=keycloak-events destination=consumer2
```

**Configuration**:
```bash
kete.routes.fanout.destination.exchange=keycloak-events
kete.routes.fanout.destination.routing-key=  # Empty or anything
```

### Headers Exchange

Route based on message headers. KETE sends the `eventtype` header which can be used for routing:

**Setup**:

=== "Linux/macOS"

    ```bash
    # Create headers exchange
    rabbitmqadmin declare exchange name=keycloak-events type=headers

    # Create queues
    rabbitmqadmin declare queue name=login-events
    rabbitmqadmin declare queue name=logout-events

    # Bind queues (match on eventtype header)
    rabbitmqadmin declare binding source=keycloak-events \
      destination=login-events \
      arguments='{"x-match":"all","eventtype":"LOGIN"}'
      
    rabbitmqadmin declare binding source=keycloak-events \
      destination=logout-events \
      arguments='{"x-match":"all","eventtype":"LOGOUT"}'
    ```

=== "Windows (PowerShell)"

    ```powershell
    # Create headers exchange
    rabbitmqadmin declare exchange name=keycloak-events type=headers

    # Create queues
    rabbitmqadmin declare queue name=login-events
    rabbitmqadmin declare queue name=logout-events

    # Bind queues (match on eventtype header)
    rabbitmqadmin declare binding source=keycloak-events `
      destination=login-events `
      arguments='{"x-match":"all","eventtype":"LOGIN"}'
      
    rabbitmqadmin declare binding source=keycloak-events `
      destination=logout-events `
      arguments='{"x-match":"all","eventtype":"LOGOUT"}'
    ```

**Configuration**:
```bash
kete.routes.headers.destination.exchange=keycloak-events
kete.routes.headers.destination.routing-key=  # Ignored for headers
```

**Tip:** For routing admin vs user events, use separate KETE routes with different event matchers instead of headers exchange.



## Configuration Examples

### Example 1: Separate Exchanges by Event Category

```bash
# User authentication events (regex)
kete.routes.auth-events.destination.kind=amqp-0.9.1
kete.routes.auth-events.realm-matchers.realm=list:master
kete.routes.auth-events.event-matchers.pattern=regex:^LOG(IN|OUT)
kete.routes.auth-events.destination.host=rabbitmq
kete.routes.auth-events.destination.exchange=auth-events
kete.routes.auth-events.destination.routing-key=auth

# Admin events (admin event format: RESOURCETYPE_OPERATIONTYPE)
kete.routes.example-admin.destination.kind=amqp-0.9.1
kete.routes.example-admin.realm-matchers.realm=list:master
kete.routes.example-admin.event-matchers.filter=glob:*_*
kete.routes.example-admin.destination.host=rabbitmq
kete.routes.example-admin.destination.exchange=admin-events
kete.routes.example-admin.destination.routing-key=admin
```

### Example 2: Per-Realm Routing

```bash
# Production realm
kete.routes.prod.destination.kind=amqp-0.9.1
kete.routes.prod.realm-matchers.realm=list:production
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.host=rabbitmq
kete.routes.prod.destination.exchange=keycloak-events
kete.routes.prod.destination.routing-key=prod.events

# Staging realm
kete.routes.staging.destination.kind=amqp-0.9.1
kete.routes.staging.realm-matchers.realm=list:staging
kete.routes.staging.event-matchers.filter=glob:*
kete.routes.staging.destination.host=rabbitmq
kete.routes.staging.destination.exchange=keycloak-events
kete.routes.staging.destination.routing-key=staging.events
```

### Example 3: High-Availability Setup

```bash
kete.routes.ha.destination.kind=amqp-0.9.1
kete.routes.ha.realm-matchers.realm=list:master
kete.routes.ha.event-matchers.filter=glob:*
kete.routes.ha.destination.host=rabbitmq-cluster.local
kete.routes.ha.destination.port=5672
kete.routes.ha.destination.username=keycloak
kete.routes.ha.destination.password=secret
kete.routes.ha.destination.virtual-host=/ha
kete.routes.ha.destination.exchange=keycloak-events-ha
kete.routes.ha.destination.routing-key=events
```

**RabbitMQ HA Queue Setup**:

=== "Linux/macOS"

    ```bash
    rabbitmqadmin declare queue name=keycloak-events-ha \
      arguments='{"x-ha-policy":"all","x-ha-sync-mode":"automatic"}'
    ```

=== "Windows (PowerShell)"

    ```powershell
    rabbitmqadmin declare queue name=keycloak-events-ha `
      arguments='{"x-ha-policy":"all","x-ha-sync-mode":"automatic"}'
    ```



## Quick Starts

| Broker | Quick Start |
|--------|-------------|
| RabbitMQ | [amqp-0.9.1-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-rabbitmq/) |
| LavinMQ | [amqp-0.9.1-lavinmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-lavinmq/) |
| Amazon MQ (RabbitMQ) | [amqp-0.9.1-amazon-mq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-amazon-mq/) |
| CloudAMQP | [amqp-0.9.1-cloudamqp](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-cloudamqp/) |



## See Also

- [AMQP 1.0 Destination](amqp-1.md) — Advanced AMQP with richer messaging features
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
