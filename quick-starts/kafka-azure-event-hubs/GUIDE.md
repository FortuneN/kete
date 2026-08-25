# Azure Event Hubs (Kafka Protocol) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Event Hubs** using the Kafka protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- An Azure Event Hubs namespace (Standard tier or higher for Kafka support)

## Azure Setup

### 1. Create an Event Hubs Namespace

> **Important**: Kafka support requires **Standard** tier or higher.

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create an Event Hubs namespace (Standard tier for Kafka)
az eventhubs namespace create \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --sku Standard \
  --enable-kafka true
```

### 2. Create an Event Hub (Kafka Topic)

```bash
az eventhubs eventhub create \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --partition-count 2
```

### 3. Get the Connection String

```bash
# Get the primary connection string
az eventhubs namespace authorization-rule keys list \
  --name RootManageSharedAccessKey \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query primaryConnectionString -o tsv
```

The connection string looks like:
```
Endpoint=sb://<namespace>.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=<key>
```

**Keep the entire connection string** - you'll use it as the SASL password.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-NAMESPACE>` | Event Hubs namespace name | `my-keycloak-ns` |
| `<YOUR-CONNECTION-STRING>` | Full connection string | `Endpoint=sb://...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
EVENTHUB_NAMESPACE=my-keycloak-ns
EVENTHUB_CONNECTION_STRING=Endpoint=sb://my-keycloak-ns.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key-here
```

Then update `docker-compose.yml` to use these variables:

```yaml
environment:
  kete.routes.quick-start.destination.bootstrap.servers: ${EVENTHUB_NAMESPACE}.servicebus.windows.net:9093
  kete.routes.quick-start.destination.sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="$$ConnectionString" password="${EVENTHUB_CONNECTION_STRING}";
```

## Running

```bash
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your Event Hub for received events:

### Using Azure CLI

```bash
# Check the Event Hub exists
az eventhubs eventhub show \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg
```

### Using Kafka CLI Tools

You can use standard Kafka tools to consume from Event Hubs:

```bash
# Create a consumer properties file
cat > consumer.properties << EOF
bootstrap.servers=<your-namespace>.servicebus.windows.net:9093
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="\$ConnectionString" password="<your-connection-string>";
EOF

# Consume messages
kafka-console-consumer.sh \
  --bootstrap-server <your-namespace>.servicebus.windows.net:9093 \
  --topic keycloak-events \
  --consumer.config consumer.properties \
  --from-beginning
```

## Troubleshooting

### Check Keycloak Logs

```bash
docker-compose logs keycloak
```

Look for:
- `KETE: Route 'quick-start' initialized` - Success
- Connection errors - Check credentials and network

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Verify namespace and port 9093 is accessible |
| SASL authentication failed | Check connection string is complete |
| Topic not found | Ensure `keycloak-events` Event Hub exists |
| "Kafka is not enabled" | Upgrade to Standard tier or higher |

### Verify Kafka Support

```bash
# Check if Kafka is enabled
az eventhubs namespace show \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query "kafkaEnabled"
```

## Cleanup

```bash
docker-compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```

## See Also

- [Azure Event Hubs for Apache Kafka](https://learn.microsoft.com/en-us/azure/event-hubs/azure-event-hubs-kafka-overview)
- [Use Event Hubs with Kafka Applications](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-for-kafka-ecosystem-overview)
