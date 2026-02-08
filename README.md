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

## Supported Destinations

| Protocol | Examples |
|----------|----------|
| **Kafka** | Apache Kafka, Confluent Platform, Redpanda, Azure Event Hubs, AWS MSK, Aiven for Apache Kafka, Instaclustr, CloudKarafka |
| **AMQP 1.0** | Apache ActiveMQ, Apache Artemis, Azure Service Bus, Azure Event Hubs, Apache Qpid, RabbitMQ, SwiftMQ, Solace PubSub+, IBM MQ |
| **AMQP 0-9-1** | RabbitMQ, LavinMQ, Apache Qpid, CloudAMQP, Amazon MQ |
| **MQTT 3.1.1** | Eclipse Mosquitto, EMQX, HiveMQ, VerneMQ, NanoMQ, RabbitMQ, AWS IoT Core, Azure IoT Hub |
| **MQTT 5.0** | Eclipse Mosquitto, EMQX, HiveMQ, VerneMQ, NanoMQ, RabbitMQ, Azure Event Grid, AWS IoT Core |
| **Redis** | Redis, Valkey, Dragonfly, KeyDB, AWS ElastiCache, Azure Cache for Redis, Upstash Redis, Google Cloud Memorystore (Pub/Sub & Streams) |
| **NATS** | NATS Server, NATS JetStream, Synadia Cloud, NGS (NATS Global Service) |
| **Pulsar** | Apache Pulsar, StreamNative Cloud, DataStax Astra Streaming, StreamNative Private Cloud, Clever Cloud Pulsar |
| **HTTP** | Webhooks, REST APIs, Azure Event Grid, AWS EventBridge, Google Cloud Pub/Sub Push, Twilio, Slack, Discord, Custom HTTP Endpoints |
| **STOMP** | Apache ActiveMQ, Apache Artemis, RabbitMQ, EMQX, HornetQ |
| **WebSocket** | Custom WebSocket Servers, Socket.IO, SignalR, Ably, Pusher, WebSocket-based Chat Applications |
| **ZeroMQ** | Any ZeroMQ peer — brokerless, 40+ language bindings (Python, C, .NET, Java, Node.js, Go, Rust) |

## Quick Start (5 minutes)

### Step 1: <a href="https://raw.githubusercontent.com/FortuneN/kete/develop/quick-starts/amqp-0.9.1-rabbitmq/docker-compose.yml" download>Download</a> or create docker-compose.yml

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
| [Nimbus OAuth SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk) | OAuth 2.0 client credentials |
| [Resilience4j](https://resilience4j.readme.io/) | Retry patterns with exponential backoff |
| [Jackson](https://github.com/FasterXML/jackson) | JSON, XML, YAML, CSV, CBOR, TOML, Smile, Properties |
| [hrakaroo/glob](https://github.com/hrakaroo/glob-library-java) | High-performance glob and SQL LIKE patterns |
| [Bouncy Castle](https://www.bouncycastle.org/) | TLS/SSL cryptography provider |
| [Reflections](https://github.com/ronmamo/reflections) | Runtime component discovery |
| [Google Guava](https://github.com/google/guava) | Cached matcher results |
| [SLF4J](https://www.slf4j.org/) | Logging facade |
| [JUnit 5](https://junit.org/junit5/) | Testing framework |
| [Mockito](https://site.mockito.org/) | Mocking framework for tests |
| [AssertJ](https://assertj.github.io/doc/) | Fluent assertions for tests |
| [Testcontainers](https://testcontainers.com/) | Docker-based integration testing |
