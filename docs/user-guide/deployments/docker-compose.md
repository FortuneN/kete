# Docker Compose Deployment

Run KETE with Keycloak and message brokers in a complete local stack.



## Quick Start

Create a `docker-compose.yml`:

```yaml
services:
  keycloak:
    image: ghcr.io/fortunen/kete:latest
    command: start-dev
    ports:
      - "8080:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      # Route events to Kafka
      kete.routes.events.destination.kind: kafka
      kete.routes.events.destination.bootstrap.servers: kafka:9092
      kete.routes.events.destination.topic: keycloak-events
    depends_on:
      - kafka

  kafka:
    image: bitnami/kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_NODE_ID: 1
      KAFKA_CFG_PROCESS_ROLES: broker,controller
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE: "true"
```

Start the stack:

```bash
docker compose up -d
```



## With RabbitMQ

```yaml
services:
  keycloak:
    image: ghcr.io/fortunen/kete:latest
    command: start-dev
    ports:
      - "8080:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      kete.routes.events.destination.kind: amqp-0.9.1
      kete.routes.events.destination.host: rabbitmq
      kete.routes.events.destination.port: "5672"
      kete.routes.events.destination.username: guest
      kete.routes.events.destination.password: guest
      kete.routes.events.destination.routing-key: keycloak.events
    depends_on:
      - rabbitmq

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"  # Management UI
```

Access RabbitMQ management at [http://localhost:15672](http://localhost:15672) (guest/guest).



## With MQTT (Mosquitto)

```yaml
services:
  keycloak:
    image: ghcr.io/fortunen/kete:latest
    command: start-dev
    ports:
      - "8080:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      kete.routes.events.destination.kind: mqtt-5
      kete.routes.events.destination.host: mosquitto
      kete.routes.events.destination.port: "1883"
      kete.routes.events.destination.topic: keycloak/events
    depends_on:
      - mosquitto

  mosquitto:
    image: eclipse-mosquitto:2
    ports:
      - "1883:1883"
    volumes:
      - ./mosquitto.conf:/mosquitto/config/mosquitto.conf
```

Create `mosquitto.conf`:

```
listener 1883
allow_anonymous true
```



## Multiple Routes

Send events to multiple destinations:

```yaml
services:
  keycloak:
    image: ghcr.io/fortunen/kete:latest
    command: start-dev
    ports:
      - "8080:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      
      # Route 1: All events to Kafka
      kete.routes.kafka.destination.kind: kafka
      kete.routes.kafka.destination.bootstrap.servers: kafka:9092
      kete.routes.kafka.destination.topic: all-events
      
      # Route 2: Login events to webhook
      kete.routes.logins.destination.kind: http
      kete.routes.logins.destination.url: http://webhook:3000/events
      kete.routes.logins.event-matchers.filter: "glob:LOGIN*"
```



## With Database (Production-like)

```yaml
services:
  keycloak:
    image: ghcr.io/fortunen/kete:latest
    command: start --optimized
    ports:
      - "8080:8080"
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: secret
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      kete.routes.events.destination.kind: kafka
      kete.routes.events.destination.bootstrap.servers: kafka:9092
      kete.routes.events.destination.topic: keycloak-events
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_started

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: secret
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keycloak"]
      interval: 5s
      timeout: 5s
      retries: 5

  kafka:
    image: bitnami/kafka:latest
    environment:
      KAFKA_CFG_NODE_ID: 1
      KAFKA_CFG_PROCESS_ROLES: broker,controller
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER

volumes:
  postgres-data:
```
