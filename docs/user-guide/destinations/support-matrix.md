# Destination Support Matrix

This page provides a comprehensive cross-reference of message brokers and the destinations KETE supports for each.

## Quick Reference Matrix

| Broker | AMQP 0.9.1 | AMQP 1.0 | MQTT 3 | MQTT 5 | STOMP | Kafka | Redis | NATS | Pulsar | HTTP | WebSocket | ZeroMQ |
|--------|:----------:|:--------:|:------:|:------:|:-----:|:-----:|:-----:|:----:|:------:|:----:|:---------:|:------:|
| **RabbitMQ** | ✅ | ✅ 4.0+ | ✅ Plugin | ✅ Plugin | ✅ Plugin | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ Plugin | ❌ |
| **LavinMQ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Redis** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Valkey** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Dragonfly** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **KeyDB** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Microsoft Garnet** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Amazon ElastiCache** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Azure Cache for Redis** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Google Cloud Memorystore** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Upstash** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ActiveMQ Artemis** | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **ActiveMQ Classic** | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Eclipse Mosquitto** | ❌ | ❌ | ✅ | ✅ 2.0+ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **HiveMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **HiveMQ Cloud** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **EMQX** | ❌ | ❌ | ✅ | ✅ | ✅ Gateway | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **EMQX Cloud** | ❌ | ❌ | ✅ | ✅ | ✅ Gateway | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **NanoMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **VerneMQ** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Apache Qpid** | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **IBM MQ** | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **NATS Server** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Synadia Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Apache Pulsar** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **StreamNative Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **DataStax Astra Streaming** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Azure Service Bus** | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Azure Event Hubs** | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Azure Event Grid** | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Apache Kafka** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Amazon MSK** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Redpanda** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Confluent Cloud** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Aiven for Kafka** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Strimzi** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Solace PubSub+** | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **AWS IoT Core** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Azure IoT Hub** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Any ZeroMQ peer** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

!!! note "Brokerless Destination"
    ZeroMQ is a brokerless messaging library — it connects directly to peers without a message broker. The "Any ZeroMQ peer" row represents any application using a ZeroMQ binding (Python, C, .NET, Java, Node.js, etc.).



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

**Quickstarts available:** [amqp-0.9.1-rabbitmq](../../quick-starts/amqp-0.9.1-rabbitmq/), [amqp-1-rabbitmq](../../quick-starts/amqp-1-rabbitmq/), [mqtt-3-rabbitmq](../../quick-starts/mqtt-3-rabbitmq/), [mqtt-5-rabbitmq](../../quick-starts/mqtt-5-rabbitmq/), [stomp-rabbitmq](../../quick-starts/stomp-rabbitmq/)


### ActiveMQ Artemis

Apache ActiveMQ Artemis is a high-performance multi-protocol broker:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| AMQP 1.0 | `amqp-1` | 5672 | Native support |
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Native support |
| STOMP | `stomp` | 61613/61616 | Requires `anycastPrefix` config (see [STOMP docs](stomp.md#artemis-configuration)) |

**Quickstarts available:** [amqp-1-activemq](../../quick-starts/amqp-1-activemq/), [stomp-activemq](../../quick-starts/stomp-activemq/), [stomp-artemis](../../quick-starts/stomp-artemis/)


### EMQX

EMQX is a high-performance MQTT broker with multi-protocol gateway support:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Full MQTT 5 compliance |
| STOMP | `stomp` | 61613 | Via STOMP gateway |

**Quickstarts available:** [mqtt-3-emqx](../../quick-starts/mqtt-3-emqx/), [mqtt-5-emqx](../../quick-starts/mqtt-5-emqx/)


### Solace PubSub+

Solace supports multiple open protocols:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| MQTT 3.1.1 | `mqtt-3` | 1883 | Native support |
| MQTT 5 | `mqtt-5` | 1883 | Native support |
| STOMP | `stomp` | 61613 | Native support |


### Apache Pulsar

Apache Pulsar is a cloud-native, distributed messaging and streaming platform:

| Protocol | KETE Destination | Port | Notes |
|----------|------------------|:----:|-------|
| Pulsar | `pulsar` | 6650 | Native protocol, TLS support on 6651 |

**Quickstarts available:** [pulsar-apache](../../quick-starts/pulsar-apache/)





Use `destination.kind=amqp-0.9.1`:

- **RabbitMQ** - Primary target
- **LavinMQ** - Lightweight alternative
- **CloudAMQP** - Managed RabbitMQ
- **Amazon MQ for RabbitMQ** - AWS managed

**Quickstarts available:** [amqp-0.9.1-rabbitmq](../../quick-starts/amqp-0.9.1-rabbitmq/), [amqp-0.9.1-lavinmq](../../quick-starts/amqp-0.9.1-lavinmq/)

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

**Quickstarts available:** [amqp-1-activemq](../../quick-starts/amqp-1-activemq/), [amqp-1-azure-event-hubs](../../quick-starts/amqp-1-azure-event-hubs/), [amqp-1-azure-service-bus](../../quick-starts/amqp-1-azure-service-bus/), [amqp-1-azure-service-bus-emulator](../../quick-starts/amqp-1-azure-service-bus-emulator/), [amqp-1-qpid](../../quick-starts/amqp-1-qpid/), [amqp-1-rabbitmq](../../quick-starts/amqp-1-rabbitmq/)

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
- **AWS IoT Core** - Managed
- **Azure IoT Hub** - Managed

**Quickstarts available:** [mqtt-3-mosquitto](../../quick-starts/mqtt-3-mosquitto/), [mqtt-3-emqx](../../quick-starts/mqtt-3-emqx/), [mqtt-3-hivemq](../../quick-starts/mqtt-3-hivemq/), [mqtt-3-vernemq](../../quick-starts/mqtt-3-vernemq/), [mqtt-3-nanomq](../../quick-starts/mqtt-3-nanomq/), [mqtt-3-rabbitmq](../../quick-starts/mqtt-3-rabbitmq/)

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

**Quickstarts available:** [mqtt-5-mosquitto](../../quick-starts/mqtt-5-mosquitto/), [mqtt-5-emqx](../../quick-starts/mqtt-5-emqx/), [mqtt-5-hivemq](../../quick-starts/mqtt-5-hivemq/), [mqtt-5-vernemq](../../quick-starts/mqtt-5-vernemq/), [mqtt-5-nanomq](../../quick-starts/mqtt-5-nanomq/), [mqtt-5-rabbitmq](../../quick-starts/mqtt-5-rabbitmq/), [mqtt-5-azure-event-grid](../../quick-starts/mqtt-5-azure-event-grid/)

See: [MQTT 5 Destination](mqtt-5.md)


### STOMP Brokers

Use `destination.kind=stomp`:

- **ActiveMQ Classic** - Native, widely deployed
- **ActiveMQ Artemis** - Native
- **RabbitMQ** - Via plugin
- **EMQX** - Via gateway
- **Solace PubSub+** - Native
- **Amazon MQ** - Managed ActiveMQ
- **TIBCO EMS** - Native

**Quickstarts available:** [stomp-activemq](../../quick-starts/stomp-activemq/), [stomp-artemis](../../quick-starts/stomp-artemis/), [stomp-rabbitmq](../../quick-starts/stomp-rabbitmq/), [stomp-emqx](../../quick-starts/stomp-emqx/)

See: [STOMP Destination](stomp.md)


### Kafka-Compatible Systems

Use `destination.kind=kafka`:

- **Apache Kafka** - Reference implementation
- **Redpanda** - Kafka-compatible
- **Confluent Cloud** - Managed Kafka
- **Azure Event Hubs** - Kafka protocol support
- **Amazon MSK** - Managed Kafka
- **Aiven for Kafka** - Managed

**Quickstarts available:** [kafka-apache](../../quick-starts/kafka-apache/), [kafka-redpanda](../../quick-starts/kafka-redpanda/), [kafka-confluent](../../quick-starts/kafka-confluent/), [kafka-azure-event-hubs](../../quick-starts/kafka-azure-event-hubs/), [kafka-azure-event-hubs-emulator](../../quick-starts/kafka-azure-event-hubs-emulator/)

See: [Kafka Destination](kafka.md)


### Redis-Compatible Systems

Use `destination.kind=redis-pubsub` or `destination.kind=redis-streams`:

- **Redis** - Open-source, in-memory data store
- **Redis Stack** - Redis with additional modules
- **Valkey** - Redis fork, fully compatible
- **Dragonfly** - Redis-compatible, multi-threaded
- **KeyDB** - Redis-compatible, multi-threaded
- **Amazon ElastiCache** - AWS-managed Redis
- **Azure Cache for Redis** - Azure-managed Redis
- **Google Cloud Memorystore** - GCP-managed Redis
- **Upstash** - Serverless Redis

**Quickstarts available (Pub/Sub):** [redis-pubsub-redis](../../quick-starts/redis-pubsub-redis/), [redis-pubsub-valkey](../../quick-starts/redis-pubsub-valkey/), [redis-pubsub-dragonfly](../../quick-starts/redis-pubsub-dragonfly/), [redis-pubsub-keydb](../../quick-starts/redis-pubsub-keydb/), [redis-pubsub-azure-cache-for-redis](../../quick-starts/redis-pubsub-azure-cache-for-redis/), [redis-pubsub-upstash](../../quick-starts/redis-pubsub-upstash/)

**Quickstarts available (Streams):** [redis-streams-redis](../../quick-starts/redis-streams-redis/), [redis-streams-valkey](../../quick-starts/redis-streams-valkey/), [redis-streams-dragonfly](../../quick-starts/redis-streams-dragonfly/), [redis-streams-keydb](../../quick-starts/redis-streams-keydb/), [redis-streams-azure-cache-for-redis](../../quick-starts/redis-streams-azure-cache-for-redis/), [redis-streams-upstash](../../quick-starts/redis-streams-upstash/)

See: [Redis Pub/Sub Destination](redis-pubsub.md), [Redis Streams Destination](redis-streams.md)


### NATS-Compatible Systems

Use `destination.kind=nats` or `destination.kind=nats-jetstream`:

- **NATS Server** - Open-source messaging system
- **Synadia Cloud** - Managed NATS service
- **NATS Kubernetes** - Self-hosted NATS on Kubernetes

**Quickstarts available:** [nats-nats-server](../../quick-starts/nats-nats-server/), [nats-jetstream-nats-server](../../quick-starts/nats-jetstream-nats-server/)

See: [NATS Destination](nats.md), [NATS JetStream Destination](nats-jetstream.md)


### ZeroMQ Peers

Use `destination.kind=zeromq`:

ZeroMQ is **brokerless** — no broker to deploy or manage. KETE connects directly to ZeroMQ peers.

- **Any ZeroMQ application** — 40+ language bindings (Python, C, .NET, Java, Node.js, Go, Rust, etc.)
- **PUB/SUB pattern** — Fan-out to all subscribers
- **PUSH/PULL pattern** — Round-robin distribution to workers

**Quickstarts available:** [zeromq-publish](../../quick-starts/zeromq-publish/), [zeromq-push](../../quick-starts/zeromq-push/)

See: [ZeroMQ Destination](zeromq.md)



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
│   ├── Yes → Use redis-streams (persistent, consumer groups, headers)
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
└── No → Use websocket for generic WebSocket servers
```

### Performance Considerations

| Protocol | Throughput | Latency | Best For |
|----------|:----------:|:-------:|----------|
| `kafka` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | High-volume event streaming |
| `pulsar` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | Multi-tenancy, geo-replication |
| `nats-jetstream` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Persistent messaging with acknowledgments |
| `nats` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Fire-and-forget, ultra-low latency |
| `redis-streams` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Persistent messaging, consumer groups |
| `redis-pubsub` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Fire-and-forget, lowest latency |
| `amqp-0.9.1` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | RabbitMQ workloads |
| `amqp-1` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Enterprise messaging |
| `mqtt-5` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | IoT, lightweight clients |
| `mqtt-3` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | IoT, legacy support |
| `stomp` | ⭐⭐⭐ | ⭐⭐⭐ | Text-based, debugging |
| `http` | ⭐⭐ | ⭐⭐ | Webhooks, integrations |
| `zeromq` | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Brokerless, peer-to-peer, ultra-low latency |
| `websocket` | ⭐⭐⭐ | ⭐⭐⭐⭐ | Real-time dashboards |



## Available Quickstarts

All quickstarts are in the `quick-starts/` directory:

| Protocol | Broker | Quickstart Folder |
|----------|--------|-------------------|
| AMQP 0.9.1 | RabbitMQ | `amqp-0.9.1-rabbitmq/` |
| AMQP 0.9.1 | LavinMQ | `amqp-0.9.1-lavinmq/` |
| AMQP 1.0 | ActiveMQ | `amqp-1-activemq/` |
| AMQP 1.0 | Azure Event Hubs | `amqp-1-azure-event-hubs/` |
| AMQP 1.0 | Azure Service Bus | `amqp-1-azure-service-bus/` |
| AMQP 1.0 | Azure Service Bus Emulator | `amqp-1-azure-service-bus-emulator/` |
| AMQP 1.0 | Apache Qpid | `amqp-1-qpid/` |
| AMQP 1.0 | RabbitMQ | `amqp-1-rabbitmq/` |
| MQTT 3 | Mosquitto | `mqtt-3-mosquitto/` |
| MQTT 3 | EMQX | `mqtt-3-emqx/` |
| MQTT 3 | RabbitMQ | `mqtt-3-rabbitmq/` |
| MQTT 3 | HiveMQ | `mqtt-3-hivemq/` |
| MQTT 3 | VerneMQ | `mqtt-3-vernemq/` |
| MQTT 3 | NanoMQ | `mqtt-3-nanomq/` |
| MQTT 5 | Mosquitto | `mqtt-5-mosquitto/` |
| MQTT 5 | EMQX | `mqtt-5-emqx/` |
| MQTT 5 | HiveMQ | `mqtt-5-hivemq/` |
| MQTT 5 | Azure Event Grid | `mqtt-5-azure-event-grid/` |
| MQTT 5 | RabbitMQ | `mqtt-5-rabbitmq/` |
| MQTT 5 | VerneMQ | `mqtt-5-vernemq/` |
| MQTT 5 | NanoMQ | `mqtt-5-nanomq/` |
| STOMP | ActiveMQ | `stomp-activemq/` |
| STOMP | Artemis | `stomp-artemis/` |
| STOMP | RabbitMQ | `stomp-rabbitmq/` |
| STOMP | EMQX | `stomp-emqx/` |
| Kafka | Apache Kafka | `kafka-apache/` |
| Kafka | Azure Event Hubs | `kafka-azure-event-hubs/` |
| Kafka | Azure Event Hubs Emulator | `kafka-azure-event-hubs-emulator/` |
| Kafka | Redpanda | `kafka-redpanda/` |
| Kafka | Confluent | `kafka-confluent/` |
| Redis Pub/Sub | Redis | `redis-pubsub-redis/` |
| Redis Pub/Sub | Valkey | `redis-pubsub-valkey/` |
| Redis Pub/Sub | Dragonfly | `redis-pubsub-dragonfly/` |
| Redis Pub/Sub | KeyDB | `redis-pubsub-keydb/` |
| Redis Pub/Sub | Azure Cache for Redis | `redis-pubsub-azure-cache-for-redis/` |
| Redis Pub/Sub | Upstash | `redis-pubsub-upstash/` |
| Redis Streams | Redis | `redis-streams-redis/` |
| Redis Streams | Valkey | `redis-streams-valkey/` |
| Redis Streams | Dragonfly | `redis-streams-dragonfly/` |
| Redis Streams | KeyDB | `redis-streams-keydb/` |
| Redis Streams | Azure Cache for Redis | `redis-streams-azure-cache-for-redis/` |
| Redis Streams | Upstash | `redis-streams-upstash/` |
| NATS | NATS Server | `nats-nats-server/` |
| NATS JetStream | NATS Server | `nats-jetstream-nats-server/` |
| Pulsar | Apache Pulsar | `pulsar-apache/` |
| HTTP | Azure Event Grid | `http-azure-event-grid/` |
| HTTP | Webhook | `http-webhook/` |
| WebSocket | Echo Server | `websocket-echo/` |
| ZeroMQ | PUB/SUB | `zeromq-publish/` |
| ZeroMQ | PUSH/PULL | `zeromq-push/` |
