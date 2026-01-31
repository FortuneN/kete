# docker-compose.yml Reference

Complete Docker Compose configuration for infrastructure services.



## Overview

This file defines the infrastructure services needed for testing and running Keycloak Events to Everywhere:
- **Kafka** - Event streaming platform
- **RabbitMQ** - Message broker with management UI



## Services

### Kafka Service

**Image:** `confluentinc/cp-kafka:7.8.0`  
**Container Name:** `keycloak-kafka`  
**Mode:** KRaft (no ZooKeeper required)

#### Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 9092 | PLAINTEXT_HOST | External access from host |
| 29092 | PLAINTEXT | Internal broker communication |
| 29093 | CONTROLLER | KRaft controller |
| 9101 | JMX | Metrics and monitoring |

#### Environment Variables

**KRaft Configuration:**
```yaml
KAFKA_NODE_ID: 1
KAFKA_PROCESS_ROLES: 'broker,controller'
KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:29093'
CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'
```

**Listeners:**
```yaml
KAFKA_LISTENERS: 'PLAINTEXT://kafka:29092,CONTROLLER://kafka:29093,PLAINTEXT_HOST://0.0.0.0:9092'
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092'
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
```

**Replication (Single-Node):**
```yaml
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

#### Health Check

```yaml
test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
interval: 10s
timeout: 10s
retries: 5
start_period: 30s
```

#### Volume

Persistent storage for Kafka data:
```yaml
volumes:
  - kafka-data:/var/lib/kafka/data
```



### RabbitMQ Service

**Image:** `rabbitmq:4.0-management-alpine`  
**Container Name:** `keycloak-rabbitmq`  
**Features:** AMQP 0-9-1 + Management UI

#### Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 5672 | AMQP | Message broker |
| 15672 | HTTP | Management UI |

#### Environment Variables

```yaml
RABBITMQ_DEFAULT_USER: admin
RABBITMQ_DEFAULT_PASS: admin
RABBITMQ_DEFAULT_VHOST: /
```

#### Health Check

```yaml
test: ["CMD", "rabbitmq-diagnostics", "ping"]
interval: 10s
timeout: 10s
retries: 5
start_period: 30s
```

#### Volume

Persistent storage for RabbitMQ data:
```yaml
volumes:
  - rabbitmq-data:/var/lib/rabbitmq
```



## Networks

### keycloak-network

Bridge network for service communication:

```yaml
networks:
  keycloak-network:
    driver: bridge
```

**Connected services:**
- kafka
- rabbitmq
- keycloak (when run with `--network keycloak-network`)



## Volumes

### Persistent Volumes

```yaml
volumes:
  kafka-data:
    driver: local
  rabbitmq-data:
    driver: local
```

**Location (Linux/Mac):**
```
/var/lib/docker/volumes/kete_kafka-data
/var/lib/docker/volumes/kete_rabbitmq-data
```

**Location (Windows):**
```
\\wsl$\docker-desktop-data\data\docker\volumes\...
```



## Usage

### Start All Services

```bash
docker-compose up -d
```

### Start Specific Service

```bash
docker-compose up -d kafka
docker-compose up -d rabbitmq
```

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Volumes

```bash
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f kafka
docker-compose logs -f rabbitmq
```

### Check Status

```bash
docker-compose ps
```



## Connecting from Keycloak

### From Host (Development)

```bash
# Kafka
kete.routes.kafka-example.destination.bootstrap.servers=localhost:9092

# RabbitMQ
kete.routes.rabbitmq-example.destination.host=localhost
kete.routes.rabbitmq-example.destination.port=5672
```

### From Container (Same Network)

```bash
# Kafka
kete.routes.kafka-example.destination.bootstrap.servers=kafka:29092

# RabbitMQ
kete.routes.rabbitmq-example.destination.host=rabbitmq
kete.routes.rabbitmq-example.destination.port=5672
```



## Management UIs

### RabbitMQ Management

**URL:** http://localhost:15672  
**Username:** admin  
**Password:** admin

**Features:**
- Queue/exchange management
- Message monitoring
- Connection tracking
- Virtual host management



## Customization

### Change Kafka Port

```yaml
ports:
  - "19092:9092"  # Host:Container

# Update advertised listeners
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:19092'
```

### Change RabbitMQ Credentials

```yaml
environment:
  RABBITMQ_DEFAULT_USER: myuser
  RABBITMQ_DEFAULT_PASS: mypassword
```

### Add More Kafka Brokers

For multi-node cluster:

```yaml
kafka-1:
  # ... config for node 1
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka-1:29093,2@kafka-2:29093,3@kafka-3:29093'

kafka-2:
  # ... config for node 2
  environment:
    KAFKA_NODE_ID: 2
    KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka-1:29093,2@kafka-2:29093,3@kafka-3:29093'
```



## Troubleshooting

### Kafka Won't Start

**Check logs:**
```bash
docker-compose logs kafka
```

**Common issues:**
- Port 9092 in use
- Invalid CLUSTER_ID
- Volume permission issues

**Solution:**
```bash
# Stop and remove
docker-compose down -v

# Restart
docker-compose up -d kafka
```

### RabbitMQ Connection Refused

**Check status:**
```bash
docker-compose ps rabbitmq
docker-compose logs rabbitmq
```

**Test connection:**
```bash
# From host
telnet localhost 5672

# Management UI
curl http://localhost:15672
```

### Volumes Full

**Check disk usage:**
```bash
docker system df -v
```

**Clean up:**
```bash
# Remove unused volumes
docker volume prune

# Remove specific volumes
docker-compose down -v
```
