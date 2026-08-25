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

Every quickstart maps Keycloak to `8080` (HTTP) and `9000` (health/metrics). Where a broker admin UI is exposed on `8180` it is the broker, not Keycloak.

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


### 4. Verify Event Delivery

#### For Message Brokers (Kafka, MQTT, AMQP, etc.)

Almost every quickstart (87 of 89) ships a `check-event-reception.ps1` that consumes from the destination and reports whether the event arrived; run it after triggering the login. Cloud-service quickstarts also carry a `GUIDE.md` and `.env.example`. Manual equivalents:

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

### Quickstart Port Conflicts

If you're running multiple quickstarts simultaneously, you may encounter port conflicts. Make sure to stop one quickstart before starting another, or modify the port mappings in `docker-compose.yml`.

## Quickstart Inventory

Total quickstarts: 89 (plus `$images/` with the 50 Dockerfiles they use)

### AMQP 0.9.1 (4)
- amqp-0.9.1-amazon-mq
- amqp-0.9.1-cloudamqp
- amqp-0.9.1-lavinmq
- amqp-0.9.1-rabbitmq

### AMQP 1.0 (9)
- amqp-1-activemq
- amqp-1-amazon-mq
- amqp-1-azure-event-hubs
- amqp-1-azure-event-hubs-emulator
- amqp-1-azure-service-bus
- amqp-1-azure-service-bus-emulator
- amqp-1-qpid
- amqp-1-rabbitmq
- amqp-1-solace

### AWS (8)
- aws-eventbridge
- aws-eventbridge-emulator
- aws-kinesis
- aws-kinesis-emulator
- aws-sns
- aws-sns-emulator
- aws-sqs
- aws-sqs-emulator

### Azure (5)
- azure-eventgrid
- azure-storage-queue
- azure-storage-queue-emulator
- azure-webpubsub
- azure-webpubsub-emulator

### GCP (4)
- gcp-cloud-tasks
- gcp-cloud-tasks-emulator
- gcp-pubsub
- gcp-pubsub-emulator

### gRPC (1)
- grpc

### HTTP (1)
- http-webhook

### Kafka (6)
- kafka-amazon-msk
- kafka-apache
- kafka-azure-event-hubs
- kafka-azure-event-hubs-emulator
- kafka-confluent
- kafka-redpanda

### MQTT 3 (8)
- mqtt-3-activemq-artemis
- mqtt-3-emqx
- mqtt-3-hivemq
- mqtt-3-mosquitto
- mqtt-3-nanomq
- mqtt-3-rabbitmq
- mqtt-3-solace
- mqtt-3-vernemq

### MQTT 5 (9)
- mqtt-5-activemq-artemis
- mqtt-5-azure-event-grid
- mqtt-5-emqx
- mqtt-5-hivemq
- mqtt-5-mosquitto
- mqtt-5-nanomq
- mqtt-5-rabbitmq
- mqtt-5-solace
- mqtt-5-vernemq

### NATS (4)
- nats-jetstream-nats-server
- nats-jetstream-synadia-cloud
- nats-nats-server
- nats-synadia-cloud

### Pulsar (2)
- pulsar-apache
- pulsar-datastax

### Redis Pub/Sub (9)
- redis-pubsub-amazon-elasticache
- redis-pubsub-azure-cache-for-redis
- redis-pubsub-dragonfly
- redis-pubsub-garnet
- redis-pubsub-google-memorystore
- redis-pubsub-keydb
- redis-pubsub-redis
- redis-pubsub-upstash
- redis-pubsub-valkey

### Redis Stream (8)
- redis-stream-amazon-elasticache
- redis-stream-azure-cache-for-redis
- redis-stream-dragonfly
- redis-stream-google-memorystore
- redis-stream-keydb
- redis-stream-redis
- redis-stream-upstash
- redis-stream-valkey

### SignalR (1)
- signalr

### SOAP (1)
- soap-webhook

### Socket.IO (1)
- socketio

### STOMP (5)
- stomp-activemq
- stomp-amazon-mq
- stomp-artemis
- stomp-emqx
- stomp-rabbitmq

### WebSocket (1)
- websocket-echo

### ZeroMQ (2)
- zeromq-publish
- zeromq-push

## Automated Testing

`run-quick-starts.ps1` (repository root) runs quickstarts end to end: it starts each compose stack, waits for Keycloak, triggers a login, checks the `kete_events_forwarded_total` metric on port 9000 and then runs the quickstart's `check-event-reception.ps1` to confirm the event reached the destination. Quickstarts containing a `dont-run-this-quickstart` marker (the 29 cloud-service ones that need real credentials) are skipped.

```powershell
# One quickstart
.\run-quick-starts.ps1 -Filter mqtt-3-mosquitto

# All runnable quickstarts
.\run-quick-starts.ps1
```

See [run-quick-starts](scripts/run-quick-starts.md) for the options and output.

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

1. Create folder in `quick-starts/` following the `<destination>-<system>` naming convention
2. Add `docker-compose.yml` with all required services (route name `quick-start`, Keycloak on `8080`/`9000`)
3. Add `check-event-reception.ps1` that reads the event back from the destination; for cloud services add `GUIDE.md`, `.env.example` and a `dont-run-this-quickstart` marker
4. Test with `.\run-quick-starts.ps1 -Filter <name>`
5. Update this document with new quickstart entry
6. Update `docs/user-guide/destinations/<destination>.md` and `docs/user-guide/destinations/support-matrix.md` with the quickstart link

## See Also

- [Architecture Documentation](architecture.md)
- [Integration Tests](integration-tests.md)
- [User Guide](../user-guide/overview.md)
