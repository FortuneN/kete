# MQTT 5 Destination

Stream Keycloak events to MQTT 5 brokers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `mqtt-5` |
| **Protocol** | MQTT 5 |



## Compatible Systems

| System | Notes |
|--------|-------|
| **HiveMQ** | Full MQTT 5 support, enterprise features |
| **EMQX** | High-performance, full MQTT 5 |
| **Eclipse Mosquitto 2.0+** | Open-source, MQTT 5 since v2.0 |
| **VerneMQ** | Distributed, full MQTT 5 |

Not all brokers support MQTT 5. Azure IoT Hub and older Mosquitto versions only support MQTT 3. For broader compatibility, see [mqtt-3](mqtt-3.md).



## MQTT 5 Features

MQTT 5 adds features not available in MQTT 3:

| Feature | Description |
|---------|-------------|
| **User Properties** | Custom key-value headers on messages |
| **Message Expiry** | TTL for messages |
| **Topic Aliases** | Reduce bandwidth for repeated topics |
| **Shared Subscriptions** | Load balancing across subscribers |
| **Request/Response** | Correlation and response topics |
| **Reason Codes** | Detailed error information |



## Example Configurations

=== "HiveMQ"

    ```bash
    kete.routes.hivemq.destination.kind=mqtt-5
    kete.routes.hivemq.destination.host=hivemq.example.com
    kete.routes.hivemq.destination.port=8883
    kete.routes.hivemq.destination.topic=keycloak/events
    kete.routes.hivemq.destination.qos=1
    kete.routes.hivemq.destination.tls.enabled=true
    kete.routes.hivemq.destination.username=keycloak
    kete.routes.hivemq.destination.password=secret
    ```

=== "EMQX"

    ```bash
    kete.routes.emqx.destination.kind=mqtt-5
    kete.routes.emqx.destination.host=emqx.example.com
    kete.routes.emqx.destination.port=8883
    kete.routes.emqx.destination.topic=keycloak/events
    kete.routes.emqx.destination.qos=1
    kete.routes.emqx.destination.tls.enabled=true
    kete.routes.emqx.destination.username=keycloak
    kete.routes.emqx.destination.password=secret
    ```

=== "Mosquitto 2.0+"

    ```bash
    kete.routes.mosquitto5.destination.kind=mqtt-5
    kete.routes.mosquitto5.destination.host=mosquitto.example.com
    kete.routes.mosquitto5.destination.port=1883
    kete.routes.mosquitto5.destination.topic=keycloak/events
    kete.routes.mosquitto5.destination.qos=1
    kete.routes.mosquitto5.destination.username=keycloak
    kete.routes.mosquitto5.destination.password=secret
    ```

=== "VerneMQ"

    ```bash
    kete.routes.vernemq.destination.kind=mqtt-5
    kete.routes.vernemq.destination.host=vernemq.example.com
    kete.routes.vernemq.destination.port=8883
    kete.routes.vernemq.destination.topic=keycloak/events
    kete.routes.vernemq.destination.qos=1
    kete.routes.vernemq.destination.tls.enabled=true
    ```



## Features

- All MQTT 3 features plus:
- User properties (custom headers)
- Message expiry interval
- Configurable QoS levels (0, 1, 2)
- TLS/SSL support with mutual TLS (mTLS)
- Automatic reconnection
- Clean start / session expiry



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `host` | MQTT broker hostname | `mqtt.example.com` |
| `topic` | MQTT topic to publish to (supports templating) | `keycloak/${realmLowerCase}/events` |

### Dynamic Topics (Templating)

The `topic` property supports template variables:

```bash
# Dynamic topic per realm
kete.routes.mqtt5.destination.topic=keycloak/${realmLowerCase}/events

# Dynamic topic per event type
kete.routes.mqtt5.destination.topic=keycloak/events/${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${resultLowerCase}`, `${resultUpperCase}`

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `port` | `1883` (TCP) / `8883` (TLS) | MQTT broker port | `8883` |
| `transport-type` | `tcp` | Transport: `tcp` or `websocket` | `websocket` |
| `qos` | `1` | Quality of Service (0, 1, or 2) | `2` |
| `retained` | `false` | Retain message on broker | `true` |
| `client-id-prefix` | `kete-` | Client ID prefix (UUID appended) | `keycloak-` |
| `clean-session` | `true` | Clean start (MQTT 5 term) | `false` |
| `connection-timeout` | `10` | Connection timeout in seconds | `60` |
| `keep-alive-interval` | `60` | Keep-alive ping interval in seconds | `120` |
| `username` | `""` | MQTT username | `admin` |
| `password` | `""` | MQTT password | `secret123` |
| `message-headers-enabled` | `true` | Include event metadata as user properties | `false` |
| `min-pool-size` | `5` | Minimum connections in pool | `10` |
| `max-pool-size` | `20` | Maximum connections in pool | `50` |

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `tls.enabled` | `false` | Enable TLS (auto-enabled for port 8883) |
| `tls.key-store.*` | - | Client certificate for mTLS |
| `tls.trust-store.*` | - | CA certificates |



## QoS Levels

| QoS | Name | Description | Use Case |
|-----|------|-------------|----------|
| 0 | At most once | Fire and forget, no acknowledgment | Non-critical events, high throughput |
| 1 | At least once | Guaranteed delivery, possible duplicates | Standard event streaming |
| 2 | Exactly once | Guaranteed delivery, no duplicates | Critical audit events |



## Transport Types

| Transport | TLS | Default Port | Scheme |
|-----------|-----|--------------|--------|
| `tcp` | No | 1883 | `tcp://` |
| `tcp` | Yes | 8883 | `ssl://` |
| `websocket` | No | 8000 | `ws://` |
| `websocket` | Yes | 443 | `wss://` |

TLS is controlled by `tls.enabled`, not by the transport type. The correct scheme and port are determined automatically.



## Configuration Examples

### Basic MQTT 5

```bash
kete.routes.mqtt5.destination.kind=mqtt-5
kete.routes.mqtt5.realm-matchers.realm=list:master
kete.routes.mqtt5.destination.host=hivemq.example.com
kete.routes.mqtt5.destination.port=1883
kete.routes.mqtt5.destination.topic=keycloak/events
kete.routes.mqtt5.destination.qos=1
```

### Secure MQTT 5.0 with TLS

```bash
kete.routes.secure-mqtt5.destination.kind=mqtt-5
kete.routes.secure-mqtt5.realm-matchers.realm=list:master
kete.routes.secure-mqtt5.destination.host=mqtt.example.com
kete.routes.secure-mqtt5.destination.port=8883
kete.routes.secure-mqtt5.destination.topic=keycloak/events
kete.routes.secure-mqtt5.destination.tls.enabled=true
kete.routes.secure-mqtt5.destination.username=keycloak
kete.routes.secure-mqtt5.destination.password=secret
```

### MQTT 5 with mTLS

```bash
kete.routes.mtls-mqtt5.destination.kind=mqtt-5
kete.routes.mtls-mqtt5.destination.host=secure-broker.example.com
kete.routes.mtls-mqtt5.destination.port=8883
kete.routes.mtls-mqtt5.destination.topic=keycloak/events
kete.routes.mtls-mqtt5.destination.tls.enabled=true
kete.routes.mtls-mqtt5.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-mqtt5.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-mqtt5.destination.tls.key-store.password=keystorepass
kete.routes.mtls-mqtt5.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-mqtt5.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-mqtt5.destination.tls.trust-store.password=truststorepass
```
