# Azure Event Grid (HTTP Webhook) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Event Grid** using HTTP webhooks.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- An Azure Event Grid Topic or Custom Topic

## Azure Setup

### 1. Create an Event Grid Topic

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create an Event Grid topic
az eventgrid topic create \
  --name keycloak-events-topic \
  --resource-group keycloak-events-rg \
  --location eastus
```

### 2. Get the Topic Endpoint and Key

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

### 3. Create an Event Subscription (to receive events)

**Option A: Azure Function**
```bash
az eventgrid event-subscription create \
  --name keycloak-events-sub \
  --source-resource-id $(az eventgrid topic show --name keycloak-events-topic --resource-group keycloak-events-rg --query id -o tsv) \
  --endpoint <your-function-url> \
  --endpoint-type webhook
```

**Option B: Storage Queue**
```bash
az eventgrid event-subscription create \
  --name keycloak-events-sub \
  --source-resource-id $(az eventgrid topic show --name keycloak-events-topic --resource-group keycloak-events-rg --query id -o tsv) \
  --endpoint <storage-queue-resource-id> \
  --endpoint-type storagequeue
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<your-event-grid-topic-endpoint>` | Event Grid topic endpoint URL | `https://keycloak-events-topic.eastus-1.eventgrid.azure.net/api/events` |
| `<your-access-key>` | Event Grid topic access key | `abc123...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
EVENTGRID_ENDPOINT=https://keycloak-events-topic.eastus-1.eventgrid.azure.net/api/events
EVENTGRID_KEY=your-access-key-here
```

Then update `docker-compose.yml` to use these variables:

```yaml
environment:
  kete.routes.quick-start.destination.url: ${EVENTGRID_ENDPOINT}
  kete.routes.quick-start.destination.headers.aeg-sas-key: ${EVENTGRID_KEY}
```

## Running

```bash
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your Event Grid subscription for received events:

```bash
# View metrics for the topic
az eventgrid topic show \
  --name keycloak-events-topic \
  --resource-group keycloak-events-rg \
  --query "{published: provisioningState}"
```

Or use the Azure Portal to view:
- **Event Grid Topic** → **Metrics** → Published Events
- **Event Subscription** → **Metrics** → Delivered Events

## Event Format

Events are published to Event Grid in the CloudEvents format:

```json
{
  "specversion": "1.0",
  "type": "io.keycloak.LOGIN",
  "source": "keycloak",
  "id": "event-id",
  "time": "2024-01-15T10:30:00Z",
  "data": {
    "type": "LOGIN",
    "realmId": "master",
    "userId": "user-123",
    "clientId": "account-console"
  }
}
```

## Troubleshooting

### Check Keycloak Logs

```bash
docker-compose logs keycloak
```

Look for:
- `KETE: Route 'quick-start' initialized` - Success
- HTTP 4xx/5xx errors - Check endpoint URL and key

### Common Issues

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check the `aeg-sas-key` header value |
| 404 Not Found | Verify the topic endpoint URL |
| Connection timeout | Ensure outbound internet access from Docker |

## Cleanup

```bash
docker-compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```

## See Also

- [Azure Event Grid Documentation](https://learn.microsoft.com/en-us/azure/event-grid/)
- [Webhook Event Delivery](https://learn.microsoft.com/en-us/azure/event-grid/webhook-event-delivery)
