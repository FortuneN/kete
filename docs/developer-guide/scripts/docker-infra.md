# docker-infra.ps1

**Purpose:** Manage Kafka and RabbitMQ infrastructure  
**Platform:** Windows PowerShell  
**Uses:** Docker Compose



## What You'll Learn

- How to start/stop infrastructure services
- Managing Kafka and RabbitMQ containers
- Viewing service logs and status
- Cleaning up resources



## Quick Start

**Start all services:**

```powershell
.\docker-infra.ps1 start
```

**Start specific service:**

```powershell
.\docker-infra.ps1 start kafka
.\docker-infra.ps1 start rabbitmq
```

**Stop all services:**

```powershell
.\docker-infra.ps1 stop
```



## Prerequisites

- Docker Desktop running
- PowerShell 5.1 or later
- `docker-compose.yml` in project root
- Docker Compose installed



## Commands

### Syntax

```powershell
.\docker-infra.ps1 [Action] [Service]
```

### Actions

| Action | Description | Example |
|--------|-------------|---------|
| `start` | Start services (default) | `.\docker-infra.ps1 start` |
| `stop` | Stop services | `.\docker-infra.ps1 stop` |
| `restart` | Restart services | `.\docker-infra.ps1 restart kafka` |
| `status` | Show service status | `.\docker-infra.ps1 status` |
| `logs` | Show service logs | `.\docker-infra.ps1 logs rabbitmq` |
| `clean` | Stop and remove all | `.\docker-infra.ps1 clean` |

### Services

| Service | Description | Default Ports |
|---------|-------------|---------------|
| `kafka` | Apache Kafka broker | 9092 |
| `rabbitmq` | RabbitMQ with management | 5672, 15672 |
| `all` | All services (default) | - |



## Usage Examples

### Example 1: Start Everything

```powershell
# Start Kafka and RabbitMQ
.\docker-infra.ps1 start

# Or explicitly
.\docker-infra.ps1 start all
```

**Output:**
```
[11:00:00] ▶ Starting kafka...
[11:00:02] ✓ kafka started
[11:00:02] ▶ Starting rabbitmq...
[11:00:05] ✓ rabbitmq started
```

### Example 2: Start Only Kafka

```powershell
.\docker-infra.ps1 start kafka
```

**Output:**
```
[11:00:00] ▶ Starting kafka...
[11:00:02] ✓ kafka started

Kafka ready at: localhost:9092
```

### Example 3: Check Status

```powershell
.\docker-infra.ps1 status
```

**Output:**
```
Service Status:

kafka      : Up 5 minutes
rabbitmq   : Up 5 minutes
```

### Example 4: View Logs

```powershell
# View Kafka logs
.\docker-infra.ps1 logs kafka

# View RabbitMQ logs
.\docker-infra.ps1 logs rabbitmq

# View all logs
.\docker-infra.ps1 logs all
```

### Example 5: Restart Services

```powershell
# Restart specific service
.\docker-infra.ps1 restart kafka

# Restart all
.\docker-infra.ps1 restart all
```

### Example 6: Clean Up

```powershell
# Stop and remove everything
.\docker-infra.ps1 clean
```

**Output:**
```
[11:30:00]  Removing all infrastructure...
[11:30:02] ✓ All services stopped and removed
```



## Service Details

### Kafka

**Container:** `kafka`  
**Image:** `apache/kafka:latest`  
**Ports:**
- `9092` - Kafka broker port
- `19092` - Internal controller port

**Environment:**
```yaml
KAFKA_NODE_ID: 1
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
KAFKA_PROCESS_ROLES: broker,controller
```

**Access:**
```bash
# From host
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test

# From Keycloak container
kete.routes.kafka-example.destination.bootstrap.servers=kafka:9092
```

### RabbitMQ

**Container:** `rabbitmq`  
**Image:** `rabbitmq:3-management`  
**Ports:**
- `5672` - AMQP port
- `15672` - Management UI

**Credentials:**
- Username: `guest`
- Password: `guest`

**Management UI:**
```
http://localhost:15672
```

**Access:**
```bash
# From Keycloak container
kete.routes.rabbitmq-example.destination.host=rabbitmq
kete.routes.rabbitmq-example.destination.port=5672
```



## Docker Compose Configuration

### Network

Services run on bridge network:
```yaml
networks:
  keycloak-network:
    driver: bridge
```

### Volumes

Kafka uses named volume for data persistence:
```yaml
volumes:
  kafka-data:
```



## Troubleshooting

### Issue: Services won't start

**Error:** Container fails to start

**Solution:**
```powershell
# Check Docker is running
docker info

# View container logs
.\docker-infra.ps1 logs kafka
.\docker-infra.ps1 logs rabbitmq

# Check port conflicts
Get-NetTCPConnection -LocalPort 9092
Get-NetTCPConnection -LocalPort 5672

# Clean and restart
.\docker-infra.ps1 clean
.\docker-infra.ps1 start
```

### Issue: Port already in use

**Error:** "Port 9092 is already allocated"

**Solution:**
```powershell
# Find process using port
Get-NetTCPConnection -LocalPort 9092

# Stop conflicting service
# Then restart
.\docker-infra.ps1 start
```

### Issue: Services show as unhealthy

**Error:** Status shows unhealthy

**Solution:**
```powershell
# View logs
.\docker-infra.ps1 logs kafka

# Restart service
.\docker-infra.ps1 restart kafka

# Or clean restart
.\docker-infra.ps1 clean
.\docker-infra.ps1 start
```

### Issue: Cannot connect from Keycloak

**Error:** Keycloak can't reach Kafka/RabbitMQ

**Solution:**
```powershell
# Ensure Keycloak is on same network
docker network inspect keycloak-network

# Use service name, not localhost
# Correct:
kete.routes.kafka-example.destination.bootstrap.servers=kafka:9092

# Incorrect:
kete.routes.kafka-example.destination.bootstrap.servers=localhost:9092
```



## Advanced Usage

### Custom Compose File

```powershell
# Edit script to use different file
$COMPOSE_FILE = "docker-compose.custom.yml"
```

### Add More Services

Edit `docker-compose.yml`:

```yaml
services:
  mqtt:
    image: eclipse-mosquitto:latest
    ports:
      - "1883:1883"
      - "9001:9001"
    networks:
      - keycloak-network
```

Then manage with script:
```powershell
docker-compose up -d mqtt
```

### Persistent Data

Kafka data is persistent by default. To reset:

```powershell
# Clean all including volumes
.\docker-infra.ps1 clean
docker volume rm kafka-data
```



## Integration with Keycloak

### Start Infrastructure Before Keycloak

```powershell
# 1. Start infrastructure
.\docker-infra.ps1 start

# 2. Wait for services to be ready
Start-Sleep -Seconds 10

# 3. Start Keycloak
.\docker-run.ps1
```

### Full Stack Startup

```powershell
# One-liner
.\docker-infra.ps1 start; .\docker-run.ps1
```



## Monitoring

### View Service Status

```powershell
# Status command
.\docker-infra.ps1 status

# Or use docker directly
docker-compose ps

# Check health
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### View Resource Usage

```powershell
# Container stats
docker stats kafka rabbitmq

# Individual service
docker stats kafka --no-stream
```

### Access Management UIs

**RabbitMQ Management:**
```
http://localhost:15672
Username: guest
Password: guest
```
