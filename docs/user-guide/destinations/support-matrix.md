# Destination Support Matrix

This page provides a comprehensive cross-reference of message brokers and the destinations KETE supports for each.

## Quick Reference Matrix

| Broker | AMQP 0.9.1 | AMQP 1.0 | MQTT 3 | MQTT 5 | STOMP | Kafka | Redis | NATS | Pulsar | HTTP | WebSocket | ZeroMQ | GCP Pub/Sub |
|--------|:----------:|:--------:|:------:|:------:|:-----:|:-----:|:-----:|:----:|:------:|:----:|:---------:|:------:|:-----------:|
| **RabbitMQ** | ✅ | ✅ 4.0+ | ✅ Plugin | ✅ Plugin | ✅ Plugin | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ Plugin | ❌ | ❌ |
| **LavinMQ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Redis** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Valkey** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Dragonfly** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **KeyDB** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Microsoft Garnet** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ Pub/Sub only | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Amazon ElastiCache** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Azure Cache for Redis** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Google Cloud Memorystore** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Upstash** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ActiveMQ Artemis** | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ActiveMQ Classic** | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Eclipse Mosquitto** | ❌ | ❌ | ✅ | ✅ 2.0+ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **HiveMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **HiveMQ Cloud** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **EMQX** | ❌ | ❌ | ✅ | ✅ | ✅ Gateway | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **EMQX Cloud** | ❌ | ❌ | ✅ | ✅ | ✅ Gateway | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **NanoMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **VerneMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Apache Qpid** | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **IBM MQ** | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **NATS Server** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Synadia Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Apache Pulsar** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **StreamNative Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **DataStax Astra Streaming** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **DataStax Luna Streaming** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Azure Service Bus** | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Azure Event Hubs** | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Azure Event Grid** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Google Cloud Pub/Sub** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Apache Kafka** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Amazon MSK** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Redpanda** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Confluent Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Aiven for Kafka** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Strimzi** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **WarpStream** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Instaclustr** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Solace PubSub+** | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Apache RocketMQ** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **AWS IoT Core** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Azure IoT Hub** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Any ZeroMQ peer** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |

!!! note "Brokerless Destination"
    ZeroMQ is a brokerless messaging library — it connects directly to peers without a message broker. The "Any ZeroMQ peer" row represents any application using a ZeroMQ binding (Python, C, .NET, Java, Node.js, etc.).

!!! note "Native SDK Destinations"
    The following destinations use vendor-specific SDKs and are not shown as columns in the protocol matrix above: `azure-eventhubs`, `azure-servicebus`, `azure-eventgrid`, `azure-storage-queue`, `azure-webpubsub`, `aws-eventbridge`, `aws-sqs`, `aws-sns`, `aws-kinesis`, `gcp-cloud-tasks`, `grpc`, `signalr`, `soap`, `socketio`. See the [By Destination](#by-destination) section for details.



## Multi-Protocol Brokers

These brokers support **multiple protocols**, giving you flexibility in how you connect:

### RabbitMQ

RabbitMQ is highly versatile, supporting many protocols via plugins:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| AMQP 0.9.1 | `amqp-0.9.1` | 5672 | Native protocol |
| AMQP 1.0 | `amqp-1` | 5672 | Native in RabbitMQ 4.0+ |
| MQTT 3.1.1 | `mqtt-3` | 1883 | Via `rabbitmq_mqtt` plugin |
| MQTT 5 | `mqtt-5` | 1883 | Via `rabbitmq_mqtt` plugin (3.13+) |
| STOMP | `stomp` | 61613 | Via `rabbitmq_stomp` plugin |

**Quickstarts available:** [amqp-0.9.1-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-rabbitmq/), [amqp-1-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-rabbitmq/), [mqtt-3-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-rabbitmq/), [mqtt-5-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-rabbitmq/), [stomp-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-rabbitmq/)


### ActiveMQ Artemis

Apache ActiveMQ Artemis is a high-performance multi-protocol broker:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| AMQP 1.0 | `amqp-1` | 5672 | Native support |
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Native support |
| STOMP | `stomp` | 61613/61616 | Requires `anycastPrefix` config (see [STOMP docs](stomp.md#artemis-configuration)) |

**Quickstarts available:** [amqp-1-activemq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-activemq/), [mqtt-3-activemq-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-activemq-artemis/), [mqtt-5-activemq-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-activemq-artemis/), [stomp-activemq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-activemq/), [stomp-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-artemis/)


### EMQX

EMQX is a high-performance MQTT broker with multi-protocol gateway support:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Full MQTT 5 compliance |
| STOMP | `stomp` | 61613 | Via STOMP gateway |

**Quickstarts available:** [mqtt-3-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-emqx/), [mqtt-5-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-emqx/), [stomp-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-emqx/)


### Solace PubSub+

Solace supports multiple open protocols:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| AMQP 1.0 | `amqp-1` | 5672 | Native support |
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Native support |


## By Destination

### AMQP 0.9.1 Brokers

Use `destination.kind=amqp-0.9.1`:

- **RabbitMQ** - Primary target
- **LavinMQ** - Lightweight alternative
- **CloudAMQP** - Managed RabbitMQ
- **Amazon MQ for RabbitMQ** - AWS managed

**Quickstarts available:** [amqp-0.9.1-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-rabbitmq/), [amqp-0.9.1-lavinmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-lavinmq/), [amqp-0.9.1-amazon-mq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-amazon-mq/), [amqp-0.9.1-cloudamqp](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-0.9.1-cloudamqp/)

See: [AMQP 0.9.1 Destination](amqp-0.9.1.md)


### AMQP 1.0 Brokers

Use `destination.kind=amqp-1`:

- **Apache ActiveMQ Artemis** - Primary target
- **RabbitMQ 4.0+** - Native AMQP 1.0 support
- **Azure Service Bus** - TLS required
- **Azure Event Hubs** - Event streaming
- **Apache Qpid** - Reference implementation
- **Amazon MQ** - Managed ActiveMQ
- **Solace PubSub+** - Multi-protocol

**Quickstarts available:** [amqp-1-activemq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-activemq/), [amqp-1-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs/), [amqp-1-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs-emulator/), [amqp-1-azure-service-bus](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus/), [amqp-1-azure-service-bus-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus-emulator/), [amqp-1-qpid](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-qpid/), [amqp-1-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-rabbitmq/), [amqp-1-amazon-mq](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-amazon-mq/), [amqp-1-solace](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-solace/)

See: [AMQP 1 Destination](amqp-1.md)


### MQTT 3.1.1 Brokers

Use `destination.kind=mqtt-3`:

- **Eclipse Mosquitto** - Most popular open-source
- **HiveMQ** - Enterprise, clustering
- **EMQX** - High-performance
- **VerneMQ** - Distributed
- **NanoMQ** - Lightweight
- **RabbitMQ** - Via plugin
- **Azure Event Grid** - MQTT Broker feature
- **Solace PubSub+** - Native
- **Apache RocketMQ** - Via rocketmq-mqtt gateway
- **AWS IoT Core** - Managed
- **Azure IoT Hub** - Managed

**Quickstarts available:** [mqtt-3-mosquitto](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-mosquitto/), [mqtt-3-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-emqx/), [mqtt-3-hivemq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-hivemq/), [mqtt-3-vernemq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-vernemq/), [mqtt-3-nanomq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-nanomq/), [mqtt-3-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-rabbitmq/), [mqtt-3-activemq-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-activemq-artemis/), [mqtt-3-solace](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-3-solace/)

See: [MQTT 3 Destination](mqtt-3.md)


### MQTT 5 Brokers

Use `destination.kind=mqtt-5`:

- **HiveMQ** - Full MQTT 5
- **EMQX** - Full MQTT 5
- **Eclipse Mosquitto 2.0+** - MQTT 5 since v2.0
- **VerneMQ** - Full MQTT 5
- **NanoMQ** - Lightweight
- **RabbitMQ** - Via plugin (3.13+)
- **Azure Event Grid** - MQTT Broker
- **Solace PubSub+** - Native

**Quickstarts available:** [mqtt-5-mosquitto](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-mosquitto/), [mqtt-5-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-emqx/), [mqtt-5-hivemq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-hivemq/), [mqtt-5-vernemq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-vernemq/), [mqtt-5-nanomq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-nanomq/), [mqtt-5-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-rabbitmq/), [mqtt-5-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-azure-event-grid/), [mqtt-5-activemq-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-activemq-artemis/), [mqtt-5-solace](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-solace/)

See: [MQTT 5 Destination](mqtt-5.md)


### STOMP Brokers

Use `destination.kind=stomp`:

- **ActiveMQ Classic** - Native, widely deployed
- **ActiveMQ Artemis** - Native
- **RabbitMQ** - Via plugin
- **EMQX** - Via gateway
- **Amazon MQ** - Managed ActiveMQ
- **TIBCO EMS** - Native

**Quickstarts available:** [stomp-activemq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-activemq/), [stomp-artemis](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-artemis/), [stomp-rabbitmq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-rabbitmq/), [stomp-emqx](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-emqx/), [stomp-amazon-mq](https://github.com/FortuneN/kete/tree/release/quick-starts/stomp-amazon-mq/)

See: [STOMP Destination](stomp.md)


### Kafka-Compatible Systems

Use `destination.kind=kafka`:

- **Apache Kafka** - Reference implementation
- **Redpanda** - Kafka-compatible
- **Confluent Cloud** - Managed Kafka
- **Azure Event Hubs** - Kafka protocol support
- **Amazon MSK** - Managed Kafka
- **Aiven for Kafka** - Managed
- **Strimzi** - Kubernetes Kafka operator
- **WarpStream** - Zero-disk Kafka
- **Instaclustr** - Multi-cloud managed Kafka

**Quickstarts available:** [kafka-apache](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-apache/), [kafka-redpanda](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-redpanda/), [kafka-confluent](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-confluent/), [kafka-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs/), [kafka-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs-emulator/), [kafka-amazon-msk](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-amazon-msk/)

See: [Kafka Destination](kafka.md)


### Redis-Compatible Systems

Use `destination.kind=redis-pubsub` or `destination.kind=redis-stream`:

- **Redis** - Open-source, in-memory data store
- **Redis Stack** - Redis with additional modules
- **Valkey** - Redis fork, fully compatible
- **Dragonfly** - Redis-compatible, multi-threaded
- **KeyDB** - Redis-compatible, multi-threaded
- **Microsoft Garnet** - Redis-compatible, .NET-based (Pub/Sub only, no Streams)
- **Amazon ElastiCache** - AWS-managed Redis
- **Azure Cache for Redis** - Azure-managed Redis
- **Google Cloud Memorystore** - GCP-managed Redis
- **Upstash** - Serverless Redis

**Quickstarts available (Pub/Sub):** [redis-pubsub-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-redis/), [redis-pubsub-valkey](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-valkey/), [redis-pubsub-dragonfly](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-dragonfly/), [redis-pubsub-keydb](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-keydb/), [redis-pubsub-garnet](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-garnet/), [redis-pubsub-azure-cache-for-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-azure-cache-for-redis/), [redis-pubsub-upstash](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-upstash/), [redis-pubsub-amazon-elasticache](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-amazon-elasticache/), [redis-pubsub-google-memorystore](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-pubsub-google-memorystore/)

**Quickstarts available (Streams):** [redis-stream-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-redis/), [redis-stream-valkey](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-valkey/), [redis-stream-dragonfly](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-dragonfly/), [redis-stream-keydb](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-keydb/), [redis-stream-azure-cache-for-redis](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-azure-cache-for-redis/), [redis-stream-upstash](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-upstash/), [redis-stream-amazon-elasticache](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-amazon-elasticache/), [redis-stream-google-memorystore](https://github.com/FortuneN/kete/tree/release/quick-starts/redis-stream-google-memorystore/)

!!! warning "Microsoft Garnet"
    Garnet supports Redis Pub/Sub but does **not** support Redis Streams (`XADD`). See [github.com/microsoft/garnet/issues/64](https://github.com/microsoft/garnet/issues/64).

See: [Redis Pub/Sub Destination](redis-pubsub.md), [Redis Stream Destination](redis-stream.md)


### NATS-Compatible Systems

Use `destination.kind=nats` or `destination.kind=nats-jetstream`:

- **NATS Server** - Open-source messaging system
- **Synadia Cloud** - Managed NATS service
- **NATS Kubernetes** - Self-hosted NATS on Kubernetes

**Quickstarts available:** [nats-nats-server](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-nats-server/), [nats-jetstream-nats-server](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-jetstream-nats-server/), [nats-synadia-cloud](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-synadia-cloud/), [nats-jetstream-synadia-cloud](https://github.com/FortuneN/kete/tree/release/quick-starts/nats-jetstream-synadia-cloud/)

See: [NATS Destination](nats.md), [NATS JetStream Destination](nats-jetstream.md)


### Pulsar-Compatible Systems

Use `destination.kind=pulsar`:

- **Apache Pulsar** — Cloud-native, distributed messaging and streaming platform
- **StreamNative Cloud** — Managed Pulsar service
- **DataStax Astra Streaming** — Managed Pulsar service
- **DataStax Luna Streaming** — Self-managed Pulsar distribution

**Quickstarts available:** [pulsar-apache](https://github.com/FortuneN/kete/tree/release/quick-starts/pulsar-apache/), [pulsar-datastax](https://github.com/FortuneN/kete/tree/release/quick-starts/pulsar-datastax/)

See: [Pulsar Destination](pulsar.md)


### ZeroMQ Peers

Use `destination.kind=zeromq`:

ZeroMQ is **brokerless** — no broker to deploy or manage. KETE connects directly to ZeroMQ peers.

- **Any ZeroMQ application** — 40+ language bindings (Python, C, .NET, Java, Node.js, Go, Rust, etc.)
- **PUB/SUB pattern** — Fan-out to all subscribers
- **PUSH/PULL pattern** — Round-robin distribution to workers

**Quickstarts available:** [zeromq-publish](https://github.com/FortuneN/kete/tree/release/quick-starts/zeromq-publish/), [zeromq-push](https://github.com/FortuneN/kete/tree/release/quick-starts/zeromq-push/)

See: [ZeroMQ Destination](zeromq.md)


### GCP Pub/Sub

Use `destination.kind=gcp-pubsub`:

- **Google Cloud Pub/Sub** — Fully managed, global messaging service
- **GCP Pub/Sub Emulator** — Local development and testing

**Quickstarts available:** [gcp-pubsub](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-pubsub/), [gcp-pubsub-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-pubsub-emulator/)

See: [GCP Pub/Sub Destination](gcp-pubsub.md)


### Azure Storage Queue

Use `destination.kind=azure-storage-queue`:

- **Azure Storage Queue** — Simple, cost-effective cloud message queue
- **Azurite Emulator** — Local development and testing

**Quickstarts available:** [azure-storage-queue](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-storage-queue/), [azure-storage-queue-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-storage-queue-emulator/)

See: [Azure Storage Queue Destination](azure-storage-queue.md)


### AWS EventBridge

Use `destination.kind=aws-eventbridge`:

- **Amazon EventBridge** — Serverless event bus for AWS services and SaaS integrations
- **LocalStack** — Local development and testing

**Quickstarts available:** [aws-eventbridge-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-eventbridge-emulator/), [aws-eventbridge](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-eventbridge/)

See: [AWS EventBridge Destination](aws-eventbridge.md)


### AWS SQS

Use `destination.kind=aws-sqs`:

- **Amazon SQS** — Fully managed message queue service
- **LocalStack** — Local development and testing

**Quickstarts available:** [aws-sqs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sqs-emulator/), [aws-sqs](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sqs/)

See: [AWS SQS Destination](aws-sqs.md)


### AWS SNS

Use `destination.kind=aws-sns`:

- **Amazon SNS** — Fully managed pub/sub messaging service
- **LocalStack** — Local development and testing

**Quickstarts available:** [aws-sns-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sns-emulator/), [aws-sns](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sns/)

See: [AWS SNS Destination](aws-sns.md)


### AWS Kinesis

Use `destination.kind=aws-kinesis`:

- **Amazon Kinesis Data Streams** — Real-time data streaming service
- **LocalStack** — Local development and testing

**Quickstarts available:** [aws-kinesis-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-kinesis-emulator/), [aws-kinesis](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-kinesis/)

See: [AWS Kinesis Destination](aws-kinesis.md)


### Azure Event Grid

Use `destination.kind=azure-eventgrid` (native SDK) or protocol-based destinations:

- **Azure Event Grid** — Fully managed event routing service

The native `azure-eventgrid` destination uses the Azure Event Grid SDK and supports access key, Managed Identity, and Default Azure Credential authentication. Alternatively, use `http` or `mqtt-5` for protocol-based access.

**Quickstarts available (via HTTP):** [http-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/http-azure-event-grid/)

**Quickstarts available (via MQTT 5):** [mqtt-5-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/mqtt-5-azure-event-grid/)

See: [Azure Event Grid Destination](azure-eventgrid.md), [HTTP Destination](http.md), [MQTT 5 Destination](mqtt-5.md)


### Azure Event Hubs

Use `destination.kind=azure-eventhubs` (native SDK) or protocol-based destinations:

- **Azure Event Hubs** — Fully managed event streaming service
- **Azure Event Hubs Emulator** — Local development and testing

The native `azure-eventhubs` destination uses the Azure Event Hubs SDK and supports connection string, Managed Identity, and Default Azure Credential authentication. Alternatively, use `kafka` or `amqp-1` for protocol-based access.

**Quickstarts available (via Kafka):** [kafka-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs/), [kafka-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs-emulator/)

**Quickstarts available (via AMQP 1.0):** [amqp-1-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs/), [amqp-1-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs-emulator/)

See: [Azure Event Hubs Destination](azure-eventhubs.md), [Kafka Destination](kafka.md), [AMQP 1 Destination](amqp-1.md)


### Azure Service Bus

Use `destination.kind=azure-servicebus` (native SDK) or protocol-based destinations:

- **Azure Service Bus** — Fully managed enterprise message broker
- **Azure Service Bus Emulator** — Local development and testing

The native `azure-servicebus` destination uses the Azure Service Bus SDK and supports connection string, Managed Identity, and Default Azure Credential authentication. Alternatively, use `amqp-1` for protocol-based access.

**Quickstarts available (via AMQP 1.0):** [amqp-1-azure-service-bus](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus/), [amqp-1-azure-service-bus-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus-emulator/)

See: [Azure Service Bus Destination](azure-servicebus.md), [AMQP 1 Destination](amqp-1.md)


### Azure Web PubSub

Use `destination.kind=azure-webpubsub`:

- **Azure Web PubSub** — Real-time messaging service for WebSocket clients

**Quickstarts available:** [azure-webpubsub](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-webpubsub/), [azure-webpubsub-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-webpubsub-emulator/)

See: [Azure Web PubSub Destination](azure-webpubsub.md)


### GCP Cloud Tasks

Use `destination.kind=gcp-cloud-tasks`:

- **Google Cloud Tasks** — Managed task queue for asynchronous HTTP request dispatch

**Quickstarts available:** [gcp-cloud-tasks](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-cloud-tasks/), [gcp-cloud-tasks-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-cloud-tasks-emulator/)

See: [GCP Cloud Tasks Destination](gcp-cloud-tasks.md)


### gRPC Servers

Use `destination.kind=grpc`:

- **Any gRPC server** — Sends serialized events as the request body via unary RPC

**Quickstarts available:** [grpc](https://github.com/FortuneN/kete/tree/release/quick-starts/grpc/)

See: [gRPC Destination](grpc.md)


### HTTP Endpoints

Use `destination.kind=http`:

- **Webhooks** — POST events to any HTTP/HTTPS endpoint
- **REST APIs** — Forward events to REST services
- **Azure Event Grid** — Via HTTP push (EventGridEvent schema)

**Quickstarts available:** [http-webhook](https://github.com/FortuneN/kete/tree/release/quick-starts/http-webhook/), [http-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/http-azure-event-grid/)

See: [HTTP Destination](http.md)


### WebSocket Servers

Use `destination.kind=websocket`:

- **Custom WebSocket servers** — Real-time servers, dashboards, backends

**Quickstarts available:** [websocket-echo](https://github.com/FortuneN/kete/tree/release/quick-starts/websocket-echo/)

See: [WebSocket Destination](websocket.md)


### SignalR Hubs

Use `destination.kind=signalr`:

- **ASP.NET Core SignalR** — Self-hosted SignalR hubs
- **Azure SignalR Service** — Managed real-time messaging

**Quickstarts available:** [signalr](https://github.com/FortuneN/kete/tree/release/quick-starts/signalr/)

See: [SignalR Destination](signalr.md)


### SOAP Endpoints

Use `destination.kind=soap`:

- **Any SOAP endpoint** — Sends serialized events wrapped in a SOAP envelope (1.1 or 1.2)

**Quickstarts available:** [soap-webhook](https://github.com/FortuneN/kete/tree/release/quick-starts/soap-webhook/)

See: [SOAP Destination](soap.md)


### Socket.IO Servers

Use `destination.kind=socketio`:

- **Socket.IO v3/v4 servers** — Node.js, Python, Java implementations
- **Any Engine.IO-compatible server** — EIO protocol v3 and v4

**Quickstarts available:** [socketio](https://github.com/FortuneN/kete/tree/release/quick-starts/socketio/)

See: [Socket.IO Destination](socketio.md)


## Choosing the Right Protocol

### Decision Guide

```
Is your broker RabbitMQ or LavinMQ?
├── Yes → Use amqp-0.9.1 (native protocol, best performance)
└── No → Continue...

Is your broker Kafka, Redpanda, or Event Hubs (Kafka mode)?
├── Yes → Use kafka
└── No → Continue...

Is your broker Redis, ElastiCache, Azure Cache for Redis, Dragonfly, KeyDB, or Upstash?
├── Yes → Do you need message persistence?
│   ├── Yes → Use redis-stream (persistent, consumer groups, headers)
│   └── No → Use redis-pubsub (fire-and-forget, lower latency)
└── No → Continue...

Is your broker NATS Server or Synadia Cloud?
├── Yes → Do you need message persistence?
│   ├── Yes → Use nats-jetstream (persistent, acknowledgments)
│   └── No → Use nats (fire-and-forget, lowest latency)
└── No → Continue...

Is your broker ActiveMQ Artemis, Azure Service Bus, or Qpid?
├── Yes → Use amqp-1
└── No → Continue...

Is your broker an MQTT broker (Mosquitto, HiveMQ, EMQX, VerneMQ)?
├── Yes → Does it support MQTT 5?
│   ├── Yes → Use mqtt-5 (for user properties/headers)
│   └── No → Use mqtt-3
└── No → Continue...

Is your broker ActiveMQ Classic or needs STOMP?
├── Yes → Use stomp
└── No → Continue...

Is it a REST API, webhook, or HTTP endpoint?
├── Yes → Use http
└── No → Continue...

Do you need brokerless peer-to-peer messaging?
├── Yes → Use zeromq (no broker, direct peer connection)
└── No → Continue...

Is it Google Cloud Pub/Sub?
├── Yes → Use gcp-pubsub
└── No → Continue...

Is it Azure Storage Queue?
├── Yes → Use azure-storage-queue
└── No → Continue...

Is it Azure Event Hubs?
├── Yes → Do you need Managed Identity or Default Azure Credential?
│   ├── Yes → Use azure-eventhubs (native SDK)
│   └── No → Use kafka (Kafka protocol) or amqp-1 (AMQP 1.0) or azure-eventhubs
└── No → Continue...

Is it Azure Service Bus?
├── Yes → Do you need Managed Identity or Default Azure Credential?
│   ├── Yes → Use azure-servicebus (native SDK)
│   └── No → Use amqp-1 (AMQP 1.0) or azure-servicebus
└── No → Continue...

Is it Azure Event Grid?
├── Yes → Do you need Managed Identity or native Event Grid events?
│   ├── Yes → Use azure-eventgrid (native SDK)
│   └── No → Use http (HTTP push) or mqtt-5 (MQTT broker feature)
└── No → Continue...

Is it Amazon EventBridge?
├── Yes → Use aws-eventbridge
└── No → Continue...

Is it Amazon SQS?
├── Yes → Use aws-sqs
└── No → Continue...

Is it Amazon SNS?
├── Yes → Use aws-sns
└── No → Continue...

Is it Amazon Kinesis Data Streams?
├── Yes → Use aws-kinesis
└── No → Continue...

Is it Azure Web PubSub?
├── Yes → Use azure-webpubsub
└── No → Continue...

Is it Google Cloud Tasks?
├── Yes → Use gcp-cloud-tasks
└── No → Continue...

Is it a gRPC server?
├── Yes → Use grpc (unary RPC, identity marshaller)
└── No → Continue...

Is it a SOAP endpoint?
├── Yes → Use soap (SOAP 1.1/1.2 envelope)
└── No → Continue...

Is it an ASP.NET SignalR hub or Azure SignalR Service?
├── Yes → Use signalr (native SignalR protocol)
└── No → Continue...

Is it a Socket.IO server?
├── Yes → Use socketio (Engine.IO protocol)
└── No → Use websocket for generic WebSocket servers
```

### Performance Considerations

| Protocol | Throughput | Latency | Best For |
|----------|:----------:|:-------:|----------|
| `kafka` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | High-volume event streaming |
| `pulsar` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | Multi-tenancy, geo-replication |
| `nats-jetstream` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Persistent messaging with acknowledgments |
| `nats` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Fire-and-forget, ultra-low latency |
| `redis-stream` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Persistent messaging, consumer groups |
| `redis-pubsub` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Fire-and-forget, lowest latency |
| `amqp-0.9.1` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | RabbitMQ workloads |
| `amqp-1` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Enterprise messaging |
| `mqtt-5` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | IoT, lightweight clients |
| `mqtt-3` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | IoT, legacy support |
| `stomp` | ⭐⭐⭐ | ⭐⭐⭐ | Text-based, debugging |
| `http` | ⭐⭐ | ⭐⭐ | Webhooks, integrations |
| `zeromq` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Brokerless, peer-to-peer, ultra-low latency |
| `gcp-pubsub` | ⭐⭐⭐⭐ | ⭐⭐⭐ | Google Cloud managed messaging |
| `azure-storage-queue` | ⭐⭐⭐ | ⭐⭐⭐ | Simple Azure cloud queue messaging |
| `aws-eventbridge` | ⭐⭐⭐ | ⭐⭐⭐ | AWS serverless event routing |
| `aws-sqs` | ⭐⭐⭐ | ⭐⭐⭐ | AWS managed message queuing |
| `aws-sns` | ⭐⭐⭐ | ⭐⭐⭐ | AWS managed pub/sub messaging |
| `aws-kinesis` | ⭐⭐⭐⭐ | ⭐⭐⭐ | AWS real-time data streaming |
| `azure-webpubsub` | ⭐⭐⭐ | ⭐⭐⭐⭐ | Real-time WebSocket messaging via Azure |
| `azure-eventhubs` | ⭐⭐⭐⭐ | ⭐⭐⭐ | Azure Event Hubs native SDK (AMQP), Managed Identity |
| `azure-servicebus` | ⭐⭐⭐⭐ | ⭐⭐⭐ | Azure Service Bus native SDK (AMQP), Managed Identity |
| `azure-eventgrid` | ⭐⭐⭐ | ⭐⭐⭐ | Azure Event Grid native SDK, Managed Identity |
| `gcp-cloud-tasks` | ⭐⭐⭐ | ⭐⭐⭐ | Asynchronous HTTP task dispatch |
| `grpc` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | gRPC unary RPC, HTTP/2, service mesh |
| `soap` | ⭐⭐ | ⭐⭐ | SOAP endpoints, legacy enterprise services |
| `signalr` | ⭐⭐⭐ | ⭐⭐⭐⭐ | ASP.NET SignalR hubs, Azure SignalR Service |
| `socketio` | ⭐⭐⭐ | ⭐⭐⭐⭐ | Socket.IO real-time servers |
| `websocket` | ⭐⭐⭐ | ⭐⭐⭐⭐ | Real-time dashboards |



## Available Quickstarts

All quickstarts are in the `quick-starts/` directory:

| Protocol | Broker | Quickstart Folder |
|----------|--------|-------------------|
| AMQP 0.9.1 | RabbitMQ | `amqp-0.9.1-rabbitmq/` |
| AMQP 0.9.1 | LavinMQ | `amqp-0.9.1-lavinmq/` |
| AMQP 0.9.1 | Amazon MQ (RabbitMQ) | `amqp-0.9.1-amazon-mq/` |
| AMQP 0.9.1 | CloudAMQP | `amqp-0.9.1-cloudamqp/` |
| AMQP 1.0 | ActiveMQ | `amqp-1-activemq/` |
| AMQP 1.0 | Azure Event Hubs | `amqp-1-azure-event-hubs/` |
| AMQP 1.0 | Azure Event Hubs Emulator | `amqp-1-azure-event-hubs-emulator/` |
| AMQP 1.0 | Azure Service Bus | `amqp-1-azure-service-bus/` |
| AMQP 1.0 | Azure Service Bus Emulator | `amqp-1-azure-service-bus-emulator/` |
| AMQP 1.0 | Apache Qpid | `amqp-1-qpid/` |
| AMQP 1.0 | RabbitMQ | `amqp-1-rabbitmq/` |
| AMQP 1.0 | Amazon MQ (ActiveMQ) | `amqp-1-amazon-mq/` |
| AMQP 1.0 | Solace PubSub+ | `amqp-1-solace/` |
| MQTT 3 | Mosquitto | `mqtt-3-mosquitto/` |
| MQTT 3 | EMQX | `mqtt-3-emqx/` |
| MQTT 3 | RabbitMQ | `mqtt-3-rabbitmq/` |
| MQTT 3 | HiveMQ | `mqtt-3-hivemq/` |
| MQTT 3 | VerneMQ | `mqtt-3-vernemq/` |
| MQTT 3 | NanoMQ | `mqtt-3-nanomq/` |
| MQTT 3 | ActiveMQ Artemis | `mqtt-3-activemq-artemis/` |
| MQTT 3 | Solace | `mqtt-3-solace/` |
| MQTT 5 | Mosquitto | `mqtt-5-mosquitto/` |
| MQTT 5 | EMQX | `mqtt-5-emqx/` |
| MQTT 5 | HiveMQ | `mqtt-5-hivemq/` |
| MQTT 5 | Azure Event Grid | `mqtt-5-azure-event-grid/` |
| MQTT 5 | RabbitMQ | `mqtt-5-rabbitmq/` |
| MQTT 5 | VerneMQ | `mqtt-5-vernemq/` |
| MQTT 5 | NanoMQ | `mqtt-5-nanomq/` |
| MQTT 5 | ActiveMQ Artemis | `mqtt-5-activemq-artemis/` |
| MQTT 5 | Solace | `mqtt-5-solace/` |
| STOMP | ActiveMQ | `stomp-activemq/` |
| STOMP | Artemis | `stomp-artemis/` |
| STOMP | RabbitMQ | `stomp-rabbitmq/` |
| STOMP | EMQX | `stomp-emqx/` |
| STOMP | Amazon MQ (ActiveMQ) | `stomp-amazon-mq/` |
| Kafka | Apache Kafka | `kafka-apache/` |
| Kafka | Azure Event Hubs | `kafka-azure-event-hubs/` |
| Kafka | Azure Event Hubs Emulator | `kafka-azure-event-hubs-emulator/` |
| Kafka | Redpanda | `kafka-redpanda/` |
| Kafka | Confluent | `kafka-confluent/` |
| Kafka | Amazon MSK | `kafka-amazon-msk/` |
| Redis Pub/Sub | Redis | `redis-pubsub-redis/` |
| Redis Pub/Sub | Valkey | `redis-pubsub-valkey/` |
| Redis Pub/Sub | Dragonfly | `redis-pubsub-dragonfly/` |
| Redis Pub/Sub | KeyDB | `redis-pubsub-keydb/` |
| Redis Pub/Sub | Microsoft Garnet | `redis-pubsub-garnet/` |
| Redis Pub/Sub | Azure Cache for Redis | `redis-pubsub-azure-cache-for-redis/` |
| Redis Pub/Sub | Upstash | `redis-pubsub-upstash/` |
| Redis Pub/Sub | Amazon ElastiCache | `redis-pubsub-amazon-elasticache/` |
| Redis Pub/Sub | Google Memorystore | `redis-pubsub-google-memorystore/` |
| Redis Stream | Redis | `redis-stream-redis/` |
| Redis Stream | Valkey | `redis-stream-valkey/` |
| Redis Stream | Dragonfly | `redis-stream-dragonfly/` |
| Redis Stream | KeyDB | `redis-stream-keydb/` |
| Redis Stream | Azure Cache for Redis | `redis-stream-azure-cache-for-redis/` |
| Redis Stream | Upstash | `redis-stream-upstash/` |
| Redis Stream | Amazon ElastiCache | `redis-stream-amazon-elasticache/` |
| Redis Stream | Google Memorystore | `redis-stream-google-memorystore/` |
| NATS | NATS Server | `nats-nats-server/` |
| NATS JetStream | NATS Server | `nats-jetstream-nats-server/` |
| NATS | Synadia Cloud | `nats-synadia-cloud/` |
| NATS JetStream | Synadia Cloud | `nats-jetstream-synadia-cloud/` |
| Pulsar | Apache Pulsar | `pulsar-apache/` |
| Pulsar | DataStax Astra Streaming | `pulsar-datastax/` |
| HTTP | Azure Event Grid | `http-azure-event-grid/` |
| HTTP | Webhook | `http-webhook/` |
| WebSocket | Echo Server | `websocket-echo/` |
| ZeroMQ | PUB/SUB | `zeromq-publish/` |
| ZeroMQ | PUSH/PULL | `zeromq-push/` |
| GCP Pub/Sub | GCP Pub/Sub Emulator | `gcp-pubsub-emulator/` |
| GCP Pub/Sub | Google Cloud Pub/Sub | `gcp-pubsub/` |
| Azure Storage Queue | Azure (Cloud) | `azure-storage-queue/` |
| Azure Storage Queue | Azurite Emulator | `azure-storage-queue-emulator/` |
| AWS EventBridge | LocalStack Emulator | `aws-eventbridge-emulator/` |
| AWS EventBridge | Amazon EventBridge | `aws-eventbridge/` |
| AWS SQS | LocalStack Emulator | `aws-sqs-emulator/` |
| AWS SQS | Amazon SQS | `aws-sqs/` |
| AWS SNS | LocalStack Emulator | `aws-sns-emulator/` |
| AWS SNS | Amazon SNS | `aws-sns/` |
| AWS Kinesis | LocalStack Emulator | `aws-kinesis-emulator/` |
| AWS Kinesis | Amazon Kinesis | `aws-kinesis/` |
| Azure Web PubSub | Azure (Cloud) | `azure-webpubsub/` |
| Azure Web PubSub | Mock Server (Local) | `azure-webpubsub-emulator/` |
| GCP Cloud Tasks | Google Cloud Tasks | `gcp-cloud-tasks/` |
| GCP Cloud Tasks | Local Emulator | `gcp-cloud-tasks-emulator/` |
| gRPC | Echo Server | `grpc/` |
| SOAP | Webhook | `soap-webhook/` |
| SignalR | ASP.NET Core Echo | `signalr/` |
| Socket.IO | Node.js Echo | `socketio/` |
