# Docker Compose Reference

Docker Compose patterns used across quickstart configurations.



## Overview

There is no root-level `docker-compose.yml`. Each quickstart has its own self-contained `docker-compose.yml` at `quick-starts/<name>/docker-compose.yml`. All quickstarts follow a common pattern.



## Common Structure

Every quickstart docker-compose file follows this pattern:

```yaml
services:

  <broker>:
    image: ghcr.io/fortunen/kete/quick-start-<broker>
    ports:
      - <host-port>:<container-port>
    healthcheck:
      test: [...]
      interval: 5s
      timeout: 5s
      retries: 30

  keycloak:
    image: ghcr.io/fortunen/kete/quick-start-keycloak
    command: start-dev
    ports:
      - 8080:8080
      - 9000:9000
    environment:
      kete.routes.quick-start.destination.kind: <kind>
      kete.routes.quick-start.destination.<property>: <value>
    depends_on:
      <broker>:
        condition: service_healthy
```

Key conventions:

- **Custom images**: All services use pre-built images from `ghcr.io/fortunen/kete/quick-start-<name>` (built from `quick-starts/$images/<name>/Dockerfile`)
- **Health checks**: Most local broker services define a health check (typically `interval: 5s`, `timeout: 5s`, `retries: 30`); echo servers and cloud-service quick-starts (no local broker) have none
- **Dependency ordering**: Keycloak depends on the broker becoming healthy before starting
- **Route name**: All quickstarts use `quick-start` as the route name
- **Ports**: Keycloak always exposes `8080` (HTTP) and `9000` (health/metrics)



## Image Sources

All Docker images are built from Dockerfiles in `quick-starts/$images/`:

### Core

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-keycloak` | `quick-starts/$images/keycloak/Dockerfile` | Keycloak with KETE pre-installed |
| `quick-start-curl` | `quick-starts/$images/curl/Dockerfile` | Lightweight curl client for init containers |
| `quick-start-alpine` | `quick-starts/$images/alpine/Dockerfile` | Minimal Alpine base image |
| `quick-start-python-alpine` | `quick-starts/$images/python-alpine/Dockerfile` | Python on Alpine for utility scripts |

### Messaging Brokers

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-activemq-artemis` | `quick-starts/$images/activemq-artemis/Dockerfile` | ActiveMQ Artemis (AMQP 1 / MQTT / STOMP) |
| `quick-start-activemq-classic` | `quick-starts/$images/activemq-classic/Dockerfile` | ActiveMQ Classic |
| `quick-start-dragonfly` | `quick-starts/$images/dragonfly/Dockerfile` | Dragonfly (Redis-compatible) |
| `quick-start-emqx` | `quick-starts/$images/emqx/Dockerfile` | EMQX MQTT broker |
| `quick-start-garnet` | `quick-starts/$images/garnet/Dockerfile` | Microsoft Garnet (Redis-compatible) |
| `quick-start-hivemq` | `quick-starts/$images/hivemq/Dockerfile` | HiveMQ CE MQTT broker |
| `quick-start-kafka` | `quick-starts/$images/kafka/Dockerfile` | Apache Kafka |
| `quick-start-keydb` | `quick-starts/$images/keydb/Dockerfile` | KeyDB (Redis-compatible) |
| `quick-start-lavinmq` | `quick-starts/$images/lavinmq/Dockerfile` | LavinMQ (AMQP 0-9-1) |
| `quick-start-mosquitto` | `quick-starts/$images/mosquitto/Dockerfile` | Eclipse Mosquitto MQTT broker |
| `quick-start-nanomq` | `quick-starts/$images/nanomq/Dockerfile` | NanoMQ MQTT broker |
| `quick-start-nanomq-mqtt5` | `quick-starts/$images/nanomq-mqtt5/Dockerfile` | NanoMQ configured for MQTT 5 |
| `quick-start-nats` | `quick-starts/$images/nats/Dockerfile` | NATS server |
| `quick-start-pulsar` | `quick-starts/$images/pulsar/Dockerfile` | Apache Pulsar |
| `quick-start-qpid` | `quick-starts/$images/qpid/Dockerfile` | Apache Qpid Broker-J (AMQP 1) |
| `quick-start-solace` | `quick-starts/$images/solace/Dockerfile` | Solace PubSub+ Standard (AMQP, MQTT) |
| `quick-start-rabbitmq` | `quick-starts/$images/rabbitmq/Dockerfile` | RabbitMQ with management plugin |
| `quick-start-redis` | `quick-starts/$images/redis/Dockerfile` | Redis |
| `quick-start-redpanda` | `quick-starts/$images/redpanda/Dockerfile` | Redpanda (Kafka-compatible) |
| `quick-start-datastax-luna` | `quick-starts/$images/datastax-luna/Dockerfile` | DataStax Luna Streaming (Pulsar-compatible) |
| `quick-start-stomp-emqx` | `quick-starts/$images/stomp-emqx/Dockerfile` | EMQX configured for STOMP |
| `quick-start-valkey` | `quick-starts/$images/valkey/Dockerfile` | Valkey (Redis fork) |
| `quick-start-vernemq` | `quick-starts/$images/vernemq/Dockerfile` | VerneMQ MQTT broker |

### Subscribers / Consumers

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-amqp1-subscriber` | `quick-starts/$images/amqp1-subscriber/Dockerfile` | AMQP 1.0 subscriber (Python) |
| `quick-start-kafka-subscriber` | `quick-starts/$images/kafka-subscriber/Dockerfile` | Kafka consumer (Python) |
| `quick-start-mqtt-subscriber` | `quick-starts/$images/mqtt-subscriber/Dockerfile` | MQTT subscriber (Mosquitto client) |
| `quick-start-nats-subscriber` | `quick-starts/$images/nats-subscriber/Dockerfile` | NATS subscriber (nats-box) |
| `quick-start-redis-subscriber` | `quick-starts/$images/redis-subscriber/Dockerfile` | Redis Pub/Sub subscriber |
| `quick-start-stomp-subscriber` | `quick-starts/$images/stomp-subscriber/Dockerfile` | STOMP subscriber (Python) |
| `quick-start-zeromq-subscriber` | `quick-starts/$images/zeromq-subscriber/Dockerfile` | ZeroMQ subscriber (Python) |

### Cloud Emulators

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-azurite` | `quick-starts/$images/azurite/Dockerfile` | Azurite (Azure Storage emulator) |
| `quick-start-eventhubs-emulator` | `quick-starts/$images/eventhubs-emulator/Dockerfile` | Azure Event Hubs emulator |
| `quick-start-gcp-cloud-tasks-emulator` | `quick-starts/$images/gcp-cloud-tasks-emulator/Dockerfile` | GCP Cloud Tasks emulator |
| `quick-start-gcp-pubsub-emulator` | `quick-starts/$images/gcp-pubsub-emulator/Dockerfile` | GCP Pub/Sub emulator |
| `quick-start-localstack` | `quick-starts/$images/localstack/Dockerfile` | LocalStack (AWS emulator) |
| `quick-start-servicebus-emulator` | `quick-starts/$images/servicebus-emulator/Dockerfile` | Azure Service Bus emulator |
| `quick-start-sql-edge` | `quick-starts/$images/sql-edge/Dockerfile` | Azure SQL Edge (Azure emulator dependency) |

### UI / Management

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-kafka-ui` | `quick-starts/$images/kafka-ui/Dockerfile` | Kafka UI web interface |
| `quick-start-nats-box` | `quick-starts/$images/nats-box/Dockerfile` | NATS CLI toolbox |
| `quick-start-redpanda-console` | `quick-starts/$images/redpanda-console/Dockerfile` | Redpanda Console web interface |

### Utility

| Image | Dockerfile | Description |
|-------|-----------|-------------|
| `quick-start-azure-queue-setup` | `quick-starts/$images/azure-queue-setup/Dockerfile` | Azure Storage Queue provisioning (Python) |
| `quick-start-grpc-echo` | `quick-starts/$images/grpc-echo/Dockerfile` | gRPC echo server |
| `quick-start-http-echo` | `quick-starts/$images/http-echo/Dockerfile` | HTTP echo server |
| `quick-start-signalr-echo` | `quick-starts/$images/signalr-echo/Dockerfile` | SignalR echo server (.NET) |
| `quick-start-socketio-echo` | `quick-starts/$images/socketio-echo/Dockerfile` | Socket.IO echo server (Node.js) |
| `quick-start-websocket-echo` | `quick-starts/$images/websocket-echo/Dockerfile` | WebSocket echo server |

The keycloak image uses the root project directory as its build context (its Dockerfile copies the packaged `target/kete.jar`; the root `.dockerignore` sends nothing else), while all other images use their own `$images/<name>/` directory.



## Init Containers

Some quickstarts require broker-side setup (creating queues, exchanges, topics) before Keycloak connects. This is done with init containers:

```yaml
  rabbitmq-init:
    image: ghcr.io/fortunen/kete/quick-start-curl
    depends_on:
      rabbitmq:
        condition: service_healthy
    entrypoint: >
      sh -c '
        curl -s -u guest:guest -X PUT http://rabbitmq:15672/api/queues/%2f/keycloak-events ...
      '

  keycloak:
    depends_on:
      rabbitmq-init:
        condition: service_completed_successfully
```

When init containers are used, Keycloak depends on `service_completed_successfully` instead of `service_healthy`.



## Examples

### Kafka (quick-starts/kafka-apache/)

```yaml
services:
  kafka:
    image: ghcr.io/fortunen/kete/quick-start-kafka
    ports:
      - 9092:9092
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
    healthcheck:
      test: ["CMD-SHELL", "/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list"]

  keycloak:
    image: ghcr.io/fortunen/kete/quick-start-keycloak
    command: start-dev
    ports:
      - 8080:8080
      - 9000:9000
    environment:
      kete.routes.quick-start.destination.kind: kafka
      kete.routes.quick-start.destination.bootstrap.servers: kafka:9092
      kete.routes.quick-start.destination.topic: keycloak-events
    depends_on:
      kafka:
        condition: service_healthy
```

### RabbitMQ (quick-starts/amqp-0.9.1-rabbitmq/)

```yaml
services:
  rabbitmq:
    image: ghcr.io/fortunen/kete/quick-start-rabbitmq
    ports:
      - 5672:5672
      - 15672:15672
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]

  rabbitmq-init:
    image: ghcr.io/fortunen/kete/quick-start-curl
    depends_on:
      rabbitmq:
        condition: service_healthy
    entrypoint: >
      sh -c '...'

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



## Usage

### Start a Quickstart

```bash
cd quick-starts/kafka-apache
docker compose up -d
```

### Stop and Clean Up

```bash
docker compose down -v
```

### View Logs

```bash
docker compose logs -f keycloak
```
