<p align="center">
  <img src="https://raw.githubusercontent.com/FortuneN/kete/release/logo/png/kete-hexagon-128.png" alt="KETE Logo" width="128" height="128" />
</p>

<h1 align="center">
  <strong>KETE</strong>
</h1>

<p align="center">
  Keycloak Events To Everywhere<br>
  A flexible, high-performance Keycloak extension that streams matched events to various destinations, in various formats
</p>

<p align="center">
  <a href="https://github.com/FortuneN/kete/actions/workflows/release.yml">
    <img src="https://github.com/FortuneN/kete/actions/workflows/release.yml/badge.svg" alt="Build Status"  >
  </a>
  <a href="https://github.com/FortuneN/kete/releases/latest">
    <img src="https://img.shields.io/github/v/release/FortuneN/kete?label=Release" alt="Latest Release" />
  </a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="Apache 2.0 License" />
  </a>
  <a href="https://github.com/FortuneN/kete/releases">
    <img src="https://img.shields.io/github/downloads/FortuneN/kete/total?label=Downloads" alt="Downloads" />
  </a>
  <a href="https://fortunen.github.io/kete/developer-guide/overview">
    <img src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/FortuneN/kete/release/coverage-badge.json" alt="Code Coverage" />
  </a>
</p>

<p align="center">
  <a href="https://fortunen.github.io/kete/user-guide/overview">User Guide</a> •
  <a href="https://fortunen.github.io/kete/developer-guide/overview">Developer Guide</a> •
  <a href="https://github.com/FortuneN/kete/releases">Releases</a> •
  <a href="https://github.com/FortuneN/kete/tree/release/quick-starts">Quick Starts</a>
</p>

---

## Use Cases

| Use Case | Description |
|----------|-------------|
| **Synchronization** | Keep user directories, databases, CRMs, and other systems in sync with Keycloak |
| **Security Monitoring** | Stream login attempts, failed authentications, and admin actions to SIEM systems |
| **Audit & Compliance** | Maintain immutable audit logs in message queues or event streams |
| **User Analytics** | Track user behavior and authentication patterns |
| **Event-Driven Architecture** | Trigger downstream services based on Keycloak events |
| **Multi-Destination Routing** | Send different event types to different systems simultaneously |

## Quick Start (5 minutes)

### Step 1: <a href="https://raw.githubusercontent.com/FortuneN/kete/release/quick-starts/amqp-0.9.1-rabbitmq/docker-compose.yml" download>Download</a> or create docker-compose.yml

```yaml
services:

  rabbitmq:
    image: ghcr.io/fortunen/kete/quick-start-rabbitmq
    ports:
      - 5672:5672
      - 15672:15672
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 5s
      timeout: 5s
      retries: 30

  rabbitmq-init:
    image: ghcr.io/fortunen/kete/quick-start-curl
    depends_on:
      rabbitmq:
        condition: service_healthy
    entrypoint: >
      sh -c '
        for i in $(seq 1 30); do curl -sf -u guest:guest http://rabbitmq:15672/api/overview > /dev/null && break || sleep 1; done &&
        curl -s -u guest:guest -X PUT http://rabbitmq:15672/api/queues/%2f/keycloak-events -H "content-type: application/json" -d "{\"durable\":true}" &&
        curl -s -u guest:guest -X POST http://rabbitmq:15672/api/bindings/%2f/e/amq.direct/q/keycloak-events -H "content-type: application/json" -d "{\"routing_key\":\"keycloak-events\"}"
      '

  keycloak:
    image: ghcr.io/fortunen/kete/quick-start-keycloak
    command: start-dev
    ports:
      - 8080:8080
      - 9000:9000
    environment:
      kete.routes.quick-start.destination.kind: amqp-0.9.1
      kete.routes.quick-start.destination.host: rabbitmq
      kete.routes.quick-start.destination.username: guest
      kete.routes.quick-start.destination.password: guest
      kete.routes.quick-start.destination.exchange: amq.direct
      kete.routes.quick-start.destination.routing-key: keycloak-events
    depends_on:
      rabbitmq-init:
        condition: service_completed_successfully
```

### Step 2: Start the stack

```bash
docker compose up -d
```

### Step 3: See events flowing

1. Open Keycloak: [http://localhost:8080](http://localhost:8080) (admin/admin)
2. Do something in Keycloak (log in/out, create a user, ...)
3. Open RabbitMQ: [http://localhost:15672/#/queues/%2F/keycloak-events](http://localhost:15672/#/queues/%2F/keycloak-events) (guest/guest)
4. See events arriving!

## Other Quick Starts

[Browse →](https://github.com/FortuneN/kete/tree/release/quick-starts)

## Releases

[See releases →](https://github.com/FortuneN/kete/releases)

## Documentation

[User Guide →](https://fortunen.github.io/kete/user-guide/overview)

[Developer Guide →](https://fortunen.github.io/kete/developer-guide/overview)

## License

[Apache 2.0 →](https://www.apache.org/licenses/LICENSE-2.0)

## Issues

[Create new issue →](https://github.com/FortuneN/kete/issues/new)

## Supported Systems

| Category | Compatible Services |
|----------|---------------------|
| **Kafka** | Apache Kafka, Confluent, Redpanda, AWS MSK, Azure Event Hubs, Aiven, Strimzi, WarpStream, Instaclustr |
| **AMQP 1.0** | Apache ActiveMQ, Apache Artemis, Apache Qpid, RabbitMQ, Solace PubSub+, Amazon MQ, Azure Event Hubs, Azure Service Bus |
| **AMQP 0-9-1** | RabbitMQ, LavinMQ, CloudAMQP, Amazon MQ |
| **MQTT 3.1.1** | Eclipse Mosquitto, EMQX, HiveMQ, VerneMQ, NanoMQ, RabbitMQ, ActiveMQ Artemis, Solace, Azure Event Grid, AWS IoT Core, Azure IoT Hub |
| **MQTT 5.0** | Eclipse Mosquitto, EMQX, HiveMQ, VerneMQ, NanoMQ, RabbitMQ, ActiveMQ Artemis, Solace, Azure Event Grid |
| **Redis** | Redis, Valkey, Dragonfly, KeyDB, Garnet, Upstash, AWS ElastiCache, Azure Cache, Google Memorystore (Pub/Sub & Streams) |
| **NATS** | NATS Server, Synadia Cloud (NATS Core & JetStream) |
| **Pulsar** | Apache Pulsar, StreamNative Cloud, DataStax Astra Streaming, DataStax Luna Streaming |
| **HTTP** | Webhooks, REST APIs, Custom HTTP Endpoints |
| **STOMP** | Apache ActiveMQ, Apache Artemis, RabbitMQ, EMQX, Amazon MQ |
| **WebSocket** | Custom WebSocket Servers |
| **ZeroMQ** | Any ZeroMQ peer — brokerless, 40+ language bindings |
| **AWS** | EventBridge, Kinesis Data Streams, SNS, SQS |
| **Azure** | Event Grid, Event Hubs, Service Bus, Storage Queue, Web PubSub |
| **GCP** | Cloud Tasks, Pub/Sub |
| **gRPC** | Any gRPC Server |
| **SOAP** | Any SOAP Endpoint |
| **SignalR** | ASP.NET SignalR Hubs |
| **Socket.IO** | Socket.IO Servers |

## Please consider supporting the project

| Platform | Type | Link |
|----------|------|------|
| **GitHub (Stars)** | Free | [Give the project a star](https://github.com/FortuneN/kete) |
| **GitHub (Sponsors)** | One-time / Recurring | [Sponsor on GitHub](https://github.com/sponsors/FortuneN) |
| **PayPal** | One-time / Recurring | [Donate using PayPal](https://paypal.me/FortuneNgwenya) |
| **Buy Me a Coffee** | One-time / Recurring | [Donate using Buy Me a Coffee](https://www.buymeacoffee.com/FortuneN) |
| **Ko-fi** | One-time / Recurring | [Donate using Ko-fi](https://ko-fi.com/FortuneN) |
| **Liberapay** | Recurring | [Donate using Liberapay](https://liberapay.com/FortuneN) |

## Credits

| Library | Description |
|---------|-------------|
| [Keycloak](https://www.keycloak.org/) | Open source identity and access management |
| [Lombok](https://projectlombok.org/) | Boilerplate reduction for Java |
| [Apache Commons](https://commons.apache.org/) | Configuration2, Lang3, Text, IO, Pool2 utilities |
| [Apache Kafka Client](https://kafka.apache.org/) | Kafka producer library |
| [Apache Pulsar Client](https://pulsar.apache.org/) | Pulsar producer library |
| [RabbitMQ Client](https://www.rabbitmq.com/java-client.html) | AMQP 0-9-1 client |
| [Eclipse Paho](https://www.eclipse.org/paho/) | MQTT 3.1.1 and MQTT 5.0 clients |
| [Apache Qpid JMS](https://qpid.apache.org/components/jms/) | AMQP 1.0 JMS client |
| [Apache ActiveMQ](https://activemq.apache.org/) | STOMP protocol client |
| [Pooled JMS](https://github.com/messaginghub/pooled-jms) | JMS connection pooling |
| [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) | WebSocket client library |
| [OkHttp](https://square.github.io/okhttp/) | HTTP client with TLS support |
| [Lettuce](https://lettuce.io/) | Redis client for Pub/Sub and Streams |
| [NATS Java Client](https://github.com/nats-io/nats.java) | NATS and JetStream messaging |
| [JeroMQ](https://github.com/zeromq/jeromq) | Pure Java ZeroMQ implementation |
| [AWS SDK for Java v2](https://aws.amazon.com/sdk-for-java/) | SQS, SNS, Kinesis, EventBridge clients |
| [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java) | Event Hubs, Service Bus, Storage Queue, Web PubSub, Event Grid, Identity |
| [Google Cloud Java SDK](https://cloud.google.com/java/docs/reference) | Pub/Sub and Cloud Tasks clients |
| [Google Auth Library](https://github.com/googleapis/google-auth-library-java) | OAuth2 and credential support for GCP services |
| [gRPC Java](https://grpc.io/) | gRPC destination and Cloud Tasks transport |
| [Microsoft SignalR Java Client](https://learn.microsoft.com/aspnet/core/signalr/java-client) | ASP.NET SignalR hub client |
| [Socket.IO Java Client](https://github.com/socketio/socket.io-client-java) | Socket.IO protocol client |
| [Nimbus OAuth SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) | OAuth 2.0 client credentials |
| [Resilience4j](https://resilience4j.readme.io/) | Retry patterns |
| [Jackson](https://github.com/FasterXML/jackson) | JSON, XML, YAML, CSV, CBOR, TOML, Smile, Properties |
| [hrakaroo/glob](https://github.com/hrakaroo/glob-library-java) | High-performance glob and SQL LIKE patterns |
| [Bouncy Castle](https://www.bouncycastle.org/) | TLS/SSL cryptography provider |
| [Reflections](https://github.com/ronmamo/reflections) | Runtime component discovery |
| [Google Guava](https://github.com/google/guava) | Caching and case-format transformations |
| [SLF4J](https://www.slf4j.org/) | Logging facade |
| [JUnit 5](https://junit.org/junit5/) | Testing framework |
| [Mockito](https://site.mockito.org/) | Mocking framework for tests |
| [AssertJ](https://assertj.github.io/doc/) | Fluent assertions for tests |
| [Awaitility](https://github.com/awaitility/awaitility) | Asynchronous readiness probes for tests |
| [Testcontainers](https://testcontainers.com/) | Docker-based integration testing |
| [Apache Avro](https://avro.apache.org/) | Avro serialization format |
| [Google Protobuf](https://protobuf.dev/) | Protocol Buffers serialization |
