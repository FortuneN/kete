# Azure Event Grid Quick Start

This quick start demonstrates forwarding Keycloak events to an **Azure Event Grid** custom topic with the native `azure-eventgrid` destination.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) (`az`)

## Azure Setup

### 1. Authenticate with Azure

```bash
az login
```

### 2. Create an Event Grid Topic

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create a custom topic (default Event Grid schema)
az eventgrid topic create \
  --name keycloak-events-topic \
  --resource-group keycloak-events-rg \
  --location eastus
```

KETE publishes events in the **Event Grid schema**, so keep the topic's input schema at its default (`EventGridSchema`).

### 3. Get the Topic Endpoint and Key

```bash
# Get the topic endpoint
az eventgrid topic show \
  --name keycloak-events-topic \
  --resource-group keycloak-events-rg \
  --query "endpoint" -o tsv

# Get the access key
az eventgrid topic key list \
  --name keycloak-events-topic \
  --resource-group keycloak-events-rg \
  --query "key1" -o tsv
```

### 4. Create an Event Subscription (to receive events)

**Option A: Storage Queue**

```bash
az storage account create --name <your-storage-account> --resource-group keycloak-events-rg --location eastus --sku Standard_LRS
az storage queue create --name keycloak-events --account-name <your-storage-account>

az eventgrid event-subscription create \
  --name keycloak-events-sub \
  --source-resource-id $(az eventgrid topic show --name keycloak-events-topic --resource-group keycloak-events-rg --query id -o tsv) \
  --endpoint-type storagequeue \
  --endpoint $(az storage account show --name <your-storage-account> --resource-group keycloak-events-rg --query id -o tsv)/queueservices/default/queues/keycloak-events
```

**Option B: Webhook (for example an Azure Function)**

```bash
az eventgrid event-subscription create \
  --name keycloak-events-sub \
  --source-resource-id $(az eventgrid topic show --name keycloak-events-topic --resource-group keycloak-events-rg --query id -o tsv) \
  --endpoint <your-webhook-url> \
  --endpoint-type webhook
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-EVENT-GRID-TOPIC-ENDPOINT>` | Event Grid topic endpoint URL | `https://keycloak-events-topic.eastus-1.eventgrid.azure.net/api/events` |
| `<YOUR-ACCESS-KEY>` | Event Grid topic access key | `abc123...` |

### Option 2: Use environment variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and fill in your values:

```env
EVENTGRID_ENDPOINT=https://keycloak-events-topic.eastus-1.eventgrid.azure.net/api/events
EVENTGRID_KEY=your-access-key-here
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check that the events arrived:

```bash
# Peek at the storage queue subscription (Option A)
az storage message peek \
  --queue-name keycloak-events \
  --account-name <your-storage-account> \
  --num-messages 10

# Or watch the topic's publish metrics
az monitor metrics list \
  --resource $(az eventgrid topic show --name keycloak-events-topic --resource-group keycloak-events-rg --query id -o tsv) \
  --metric PublishSuccessCount
```

Or use the Azure Portal: **Event Grid Topic → Metrics → Published Events** and **Event Subscription → Metrics → Delivered Events**.

## Event Format

KETE publishes one Event Grid event (Event Grid schema) per Keycloak event:

| Field | Value |
|-------|-------|
| `subject` | The Keycloak event type (`LOGIN`, `USER_CREATE`, ...) unless `destination.subject` is set |
| `eventType` | `KeycloakEvent` unless `destination.event-type` is set |
| `dataVersion` | `1.0` unless `destination.data-version` is set |
| `data` | The serialized Keycloak event (JSON with the default serializer) |
| `id`, `eventTime` | Generated when the event is published |

Example as delivered to a subscriber:

```json
{
  "id": "6d2f3a3e-1c1b-4c3e-9c2e-1f4e0d8f1a2b",
  "subject": "LOGIN",
  "eventType": "KeycloakEvent",
  "eventTime": "2026-01-15T10:30:00.000Z",
  "dataVersion": "1.0",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "time": 1705314600000,
    "type": "LOGIN",
    "realmId": "abc-123",
    "realmName": "master",
    "clientId": "account-console",
    "userId": "user-123",
    "ipAddress": "192.168.1.100",
    "details": {
      "username": "alice"
    }
  }
}
```

Use `subject` and `eventType` for subscription filters, for example per realm:

```yaml
kete.routes.quick-start.destination.subject: keycloak/${realmLowerCase}/${eventTypeLowerCase}
kete.routes.quick-start.destination.event-type: Keycloak.${eventTypeLowerCase}
```

```bash
az eventgrid event-subscription create \
  --name production-logins \
  --source-resource-id <topic-id> \
  --endpoint <destination> \
  --subject-begins-with keycloak/production/login
```

See the [Azure Event Grid destination](https://fortunen.github.io/kete/user-guide/destinations/azure-eventgrid/) page for every option, including Managed Identity authentication.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=azure-eventgrid, ...` - Success
- `Failed to initialize route : quick-start` - Check the endpoint URL and access key

### Common Issues

| Issue | Solution |
|-------|----------|
| `401 Unauthorized` | Check the access key |
| `404 Not Found` | Verify the topic endpoint URL (it ends with `/api/events`) |
| `400 Bad Request` | The topic must use the Event Grid input schema, not CloudEvents |
| Connection timeout | Ensure outbound HTTPS (port 443) from Docker is allowed |

## Cleanup

```bash
docker compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```

## See Also

- [Azure Event Grid Documentation](https://learn.microsoft.com/en-us/azure/event-grid/)
- [Event Grid Event Schema](https://learn.microsoft.com/en-us/azure/event-grid/event-schema)
- [Event Filtering](https://learn.microsoft.com/en-us/azure/event-grid/how-to-filter-events)
