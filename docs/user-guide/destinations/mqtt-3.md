# MQTT 3 Destination

Stream Keycloak events to MQTT 3 brokers.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `mqtt-3` |
| **Protocol** | MQTT 3.1.1 |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Eclipse Mosquitto** | Most popular open-source broker |
| **HiveMQ** | Enterprise MQTT, clustering |
| **EMQX** | High-performance, clustering |
| **VerneMQ** | Distributed, Erlang-based |
| **AWS IoT Core** | Managed, auto-scaling |
| **Azure IoT Hub** | Managed, device management |



## Example Configurations

=== "Mosquitto"

    ```bash
    kete.routes.mosquitto.destination.kind=mqtt-3
    kete.routes.mosquitto.destination.host=mosquitto.example.com
    kete.routes.mosquitto.destination.port=1883
    kete.routes.mosquitto.destination.topic=keycloak/events
    kete.routes.mosquitto.destination.qos=1
    kete.routes.mosquitto.destination.username=keycloak
    kete.routes.mosquitto.destination.password=secret
    ```

=== "AWS IoT Core"

    ```bash
    kete.routes.awsiot.destination.kind=mqtt-3
    kete.routes.awsiot.destination.host=your-endpoint.iot.region.amazonaws.com
    kete.routes.awsiot.destination.port=8883
    kete.routes.awsiot.destination.topic=keycloak/events
    kete.routes.awsiot.destination.qos=1
    kete.routes.awsiot.destination.tls.enabled=true
    kete.routes.awsiot.destination.tls.key-store.loader.kind=pkcs12-file-path
    kete.routes.awsiot.destination.tls.key-store.loader.path=/certs/device.p12
    kete.routes.awsiot.destination.tls.key-store.password=keystorepass
    ```

=== "Azure IoT Hub"

    ```bash
    kete.routes.azureiot.destination.kind=mqtt-3
    kete.routes.azureiot.destination.host=your-hub.azure-devices.net
    kete.routes.azureiot.destination.port=8883
    kete.routes.azureiot.destination.topic=devices/keycloak/messages/events/
    kete.routes.azureiot.destination.qos=1
    kete.routes.azureiot.destination.tls.enabled=true
    kete.routes.azureiot.destination.username=your-hub.azure-devices.net/keycloak
    kete.routes.azureiot.destination.password=SharedAccessSignature...
    ```

=== "HiveMQ Cloud"

    ```bash
    kete.routes.hivemq.destination.kind=mqtt-3
    kete.routes.hivemq.destination.host=xxxxx.s1.eu.hivemq.cloud
    kete.routes.hivemq.destination.port=8883
    kete.routes.hivemq.destination.topic=keycloak/events
    kete.routes.hivemq.destination.qos=1
    kete.routes.hivemq.destination.tls.enabled=true
    kete.routes.hivemq.destination.username=your-username
    kete.routes.hivemq.destination.password=your-password
    ```



## Features

- Configurable QoS levels (0, 1, 2)
- TLS/SSL support with mutual TLS (mTLS)
- Automatic reconnection
- Clean session support
- Username/password authentication

**Limitation:** MQTT 3.1.1 does not support message headers. Event metadata (event type, admin flag) is not transmitted with messages. For header support, use [MQTT 5](mqtt-5.md).



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
kete.routes.mqtt.destination.topic=keycloak/${realmLowerCase}/events

# Dynamic topic per event type
kete.routes.mqtt.destination.topic=keycloak/events/${eventTypeLowerCase}
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
| `clean-session` | `true` | Start with clean session | `false` |
| `connection-timeout` | `10` | Connection timeout in seconds | `60` |
| `keep-alive-interval` | `60` | Keep-alive ping interval in seconds | `120` |
| `username` | `""` | MQTT username | `admin` |
| `password` | `""` | MQTT password | `secret123` |
| `message-headers-enabled` | `false` | **Not supported** — must remain `false` | - |
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

### Basic MQTT

```bash
kete.routes.mqtt.destination.kind=mqtt-3
kete.routes.mqtt.realm-matchers.realm=list:master
kete.routes.mqtt.destination.host=mosquitto.local
kete.routes.mqtt.destination.port=1883
kete.routes.mqtt.destination.topic=keycloak/events
kete.routes.mqtt.destination.qos=1
```

### Secure MQTT with TLS

```bash
kete.routes.secure-mqtt.destination.kind=mqtt-3
kete.routes.secure-mqtt.realm-matchers.realm=list:master
kete.routes.secure-mqtt.destination.host=mqtt.example.com
kete.routes.secure-mqtt.destination.port=8883
kete.routes.secure-mqtt.destination.topic=keycloak/events
kete.routes.secure-mqtt.destination.tls.enabled=true
kete.routes.secure-mqtt.destination.username=keycloak
kete.routes.secure-mqtt.destination.password=secret
```

### MQTT with mTLS

```bash
kete.routes.mtls-mqtt.destination.kind=mqtt-3
kete.routes.mtls-mqtt.destination.host=secure-broker.example.com
kete.routes.mtls-mqtt.destination.port=8883
kete.routes.mtls-mqtt.destination.topic=keycloak/events
kete.routes.mtls-mqtt.destination.tls.enabled=true
kete.routes.mtls-mqtt.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.mtls-mqtt.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.mtls-mqtt.destination.tls.key-store.password=keystorepass
kete.routes.mtls-mqtt.destination.tls.trust-store.loader.kind=jks-file-path
kete.routes.mtls-mqtt.destination.tls.trust-store.loader.path=/certs/truststore.jks
kete.routes.mtls-mqtt.destination.tls.trust-store.password=truststorepass
```
