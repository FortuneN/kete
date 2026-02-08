# Quickstart Testing Guide

This document provides guidance for testing KETE quickstarts to verify event flow.

## Testing Approach

Each quickstart can be tested manually using the following general pattern:

### 1. Start the Quickstart

```bash
cd quick-starts/<quickstart-name>
docker compose up -d
```

### 2. Wait for Services

Wait for all containers to be healthy:

```bash
docker compose ps
```

Most quickstarts use Keycloak on port 8080. Some use 8180 or other ports.

### 3. Trigger an Event

Login to Keycloak using the admin CLI:

```bash
curl -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=admin-cli' \
  -d 'username=admin' \
  -d 'password=admin' \
  -d 'grant_type=password'
```

Replace `8080` with the appropriate port for the quickstart.

### 4. Verify Event Delivery

#### For Message Brokers (Kafka, MQTT, AMQP, etc.)

Each quickstart README contains broker-specific commands to consume messages. Examples:

**Kafka:**
```bash
docker exec <kafka-container> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic keycloak-events \
  --from-beginning
```

**MQTT:**
```bash
docker exec <mosquitto-container> mosquitto_sub \
  -h localhost \
  -t keycloak-events \
  -v
```

**AMQP (RabbitMQ):**
```bash
docker exec <rabbitmq-container> rabbitmqadmin get queue=keycloak-events
```

#### For HTTP/WebSocket

Check the target service logs for incoming requests:

```bash
docker compose logs <destination-service>
```

#### For Redis

**Redis Pub/Sub:**
```bash
docker exec <redis-container> redis-cli SUBSCRIBE keycloak-events
```

**Redis Stream:**
```bash
docker exec <redis-container> redis-cli XREAD STREAMS keycloak-events 0
```

### 5. Check KETE Logs

Always verify KETE is initialized correctly:

```bash
docker compose logs keycloak | grep kete
```

Look for:
- `kete (x.x.x) initializing`
- `kete Route 'quick-start' initialized`
- `kete initialized`

If events aren't flowing, check for errors in Keycloak logs:

```bash
docker compose logs keycloak --tail=100
```

### 6. Cleanup

```bash
docker compose down -v
```

## Known Issues

### Pulsar

❌ **Pulsar quickstart is not yet available**

The Pulsar destination exists in the code but is not included in the `quick-start-keycloak` Docker image yet. It will be added in a future release.

### Quickstart Port Conflicts

If you're running multiple quickstarts simultaneously, you may encounter port conflicts. Make sure to stop one quickstart before starting another, or modify the port mappings in `docker-compose.yml`.

## Quickstart Inventory

Total quickstarts: 42

### AMQP 0.9.1 (2)
- amqp-0.9.1-lavinmq
- amqp-0.9.1-rabbitmq

### AMQP 1.0 (5)
- amqp-1-activemq
- amqp-1-azure-event-hubs
- amqp-1-azure-service-bus
- amqp-1-qpid
- amqp-1-rabbitmq

### HTTP (2)
- http-azure-event-grid
- http-webhook

### Kafka (5)
- kafka-apache
- kafka-azure-event-hubs
- kafka-azure-event-hubs-emulator
- kafka-confluent
- kafka-redpanda

### MQTT 3 (5)
- mqtt-3-emqx
- mqtt-3-hivemq
- mqtt-3-mosquitto
- mqtt-3-rabbitmq
- mqtt-3-vernemq

### MQTT 5 (6)
- mqtt-5-azure-event-grid
- mqtt-5-emqx
- mqtt-5-hivemq
- mqtt-5-mosquitto
- mqtt-5-rabbitmq
- mqtt-5-vernemq

### NATS (2)
- nats-jetstream-nats-server
- nats-nats-server

### Redis (10)
- redis-pubsub-azure-cache-for-redis
- redis-pubsub-dragonfly
- redis-pubsub-keydb
- redis-pubsub-redis
- redis-pubsub-upstash
- redis-stream-azure-cache-for-redis
- redis-stream-dragonfly
- redis-stream-keydb
- redis-stream-redis
- redis-stream-upstash

### STOMP (3)
- stomp-activemq
- stomp-artemis
- stomp-rabbitmq

### WebSocket (1)
- websocket-echo

### Utility (2)
- quick-start-curl
- quick-start-keycloak

## Automated Testing

The `test-all-quickstarts.ps1` script provides basic automated testing functionality. However, manual verification of event delivery is recommended for thorough testing.

### Usage

Test a single quickstart:

```powershell
.\test-all-quickstarts.ps1 -QuickStart mqtt-3-mosquitto
```

Test all quickstarts:

```powershell
.\test-all-quickstarts.ps1
```

**Note:** The automated script has limitations:
- Does not verify actual message delivery to destinations
- Only checks that Keycloak starts and can be accessed
- May have false negatives if services take longer to initialize

## Manual Test Checklist

For each quickstart:

- [ ] Services start without errors
- [ ] Services become healthy (where health checks exist)
- [ ] Keycloak web UI accessible
- [ ] KETE initializes correctly (check logs)
- [ ] Route configuration loads successfully
- [ ] Destination connection succeeds
- [ ] Login event triggers successfully
- [ ] Event appears in destination
- [ ] Event structure is valid JSON (or configured format)
- [ ] Event contains expected fields (id, type, realm, etc.)
- [ ] Services shut down cleanly

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Port already in use | Stop conflicting container or change port in docker-compose.yml |
| Keycloak fails to start | Check logs: `docker compose logs keycloak` |
| KETE not loading | Verify `ghcr.io/fortunen/kete/quick-start-keycloak` image version |
| Events not appearing | Check destination connection settings and credentials |
| Health check failing | Wait longer or check service-specific requirements |

### Debug Commands

```bash
# Check all container status
docker compose ps

# View logs for specific service
docker compose logs <service-name> --tail=100 --follow

# Check network connectivity
docker compose exec keycloak ping <destination-host>

# Verify destination is listening
docker compose exec <destination-container> netstat -ln | grep <port>

# Restart specific service
docker compose restart <service-name>
```

## Contributing

When adding a new quickstart:

1. Create folder in `quick-starts/` following naming convention
2. Add `docker-compose.yml` with all required services
3. Add `README.md` with specific testing instructions
4. Test manually following this guide
5. Update this document with new quickstart entry
6. Update `docs/user-guide/destinations/<destination>.md` with quickstart link

## See Also

- [Architecture Documentation](architecture.md)
- [Integration Tests](integration-tests.md)
- [User Guide](../user-guide/overview.md)
