# Azure Event Grid (HTTP Webhook)

Stream Keycloak events to Azure Event Grid custom topics via HTTP POST.

## Azure Setup

### 1. Create Event Grid Topic

```bash
# Create resource group
az group create --name keycloak-rg --location westus2

# Create custom topic
az eventgrid topic create \
  --resource-group keycloak-rg \
  --name keycloak-events \
  --location westus2

# Get the topic endpoint
az eventgrid topic show \
  --resource-group keycloak-rg \
  --name keycloak-events \
  --query "endpoint" -o tsv

# Get the access key
az eventgrid topic key list \
  --resource-group keycloak-rg \
  --name keycloak-events \
  --query "key1" -o tsv
```

### 2. Create Event Subscription (Optional)

Forward events to Azure services:

```bash
# Example: Forward to Azure Function
az eventgrid event-subscription create \
  --resource-group keycloak-rg \
  --topic-name keycloak-events \
  --name keycloak-to-function \
  --endpoint-type azurefunction \
  --endpoint <your-function-resource-id>
```

## KETE Configuration

Update `docker-compose.yml` with your Event Grid topic details:

```yaml
kete.routes.quick-start.destination.url: https://keycloak-events.westus2-1.eventgrid.azure.net/api/events
kete.routes.quick-start.destination.headers.aeg-sas-key: <your-access-key>
```

## Run

```bash
docker compose up -d
```

## Test

1. Open Keycloak Admin Console: http://localhost:8080/admin
2. Login with `admin`/`admin`
3. Events will be POSTed to Event Grid topic

## Verify Events in Azure

### View Metrics

```bash
# View published event metrics
az monitor metrics list \
  --resource-group keycloak-rg \
  --resource-type "Microsoft.EventGrid/topics" \
  --resource keycloak-events \
  --metric "PublishSuccessCount" \
  --start-time 2026-01-01T00:00:00Z \
  --end-time 2026-01-31T23:59:59Z
```

### Query Events (with Event Subscription)

If you created an event subscription to a storage account or Event Hubs, query the destination to view events.

## Event Schema

KETE sends events in CloudEvents format:

```json
{
  "specversion": "1.0",
  "type": "keycloak.event.LOGIN",
  "source": "/realms/master",
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "time": "2026-01-28T18:00:00Z",
  "datacontenttype": "application/json",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "type": "LOGIN",
    "realmId": "master",
    "clientId": "admin-cli",
    "userId": "user-123",
    "ipAddress": "192.168.1.100",
    "time": 1706468400000,
    "details": {
      "username": "admin"
    }
  }
}
```

## Configuration Options

### Authentication Methods

**SAS Key (Default)**
```yaml
kete.routes.quick-start.destination.headers.aeg-sas-key: <your-key>
```

**Microsoft Entra ID (Azure AD)**
```yaml
kete.routes.quick-start.destination.oauth.enabled: "true"
kete.routes.quick-start.destination.oauth.token-url: https://login.microsoftonline.com/<tenant-id>/oauth2/v2.0/token
kete.routes.quick-start.destination.oauth.client-id: <app-client-id>
kete.routes.quick-start.destination.oauth.client-secret: <app-secret>
kete.routes.quick-start.destination.oauth.scope: https://eventgrid.azure.net/.default
```

### Dynamic Topics

Route different events to different topics:

```yaml
# Separate topic per realm
kete.routes.quick-start.destination.url: https://keycloak-events-${realmLowerCase}.westus2-1.eventgrid.azure.net/api/events

# Separate topic per event type
kete.routes.quick-start.destination.url: https://keycloak-${eventTypeLowerCase}.westus2-1.eventgrid.azure.net/api/events
```

### Retry Configuration

Configure retry behavior for transient failures:

```yaml
kete.routes.quick-start.retry.enabled: "true"
kete.routes.quick-start.retry.max-attempts: 5         # Max retry attempts
kete.routes.quick-start.retry.wait-duration: PT2S     # Wait 2 seconds between retries
kete.routes.quick-start.retry.max-duration: PT1M      # Give up after 1 minute
```

## Integration with Azure Services

Create Event Subscriptions to route events to:

- **Azure Functions** - Serverless event processing
- **Azure Logic Apps** - Workflow automation
- **Event Hubs** - Stream analytics
- **Service Bus** - Message queuing
- **Storage Queue** - Simple queue
- **Webhooks** - Any HTTP endpoint

### Example: Forward to Azure Function

```bash
az eventgrid event-subscription create \
  --resource-group keycloak-rg \
  --topic-name keycloak-events \
  --name to-function \
  --endpoint-type azurefunction \
  --endpoint /subscriptions/<sub-id>/resourceGroups/<rg>/providers/Microsoft.Web/sites/<function-app>/functions/<function-name>
```

### Example: Forward to Storage Queue

```bash
az eventgrid event-subscription create \
  --resource-group keycloak-rg \
  --topic-name keycloak-events \
  --name to-storage \
  --endpoint-type storagequeue \
  --endpoint /subscriptions/<sub-id>/resourceGroups/<rg>/providers/Microsoft.Storage/storageAccounts/<account>/queueServices/default/queues/<queue>
```

## Event Filtering

Filter events at the Event Grid subscription level:

```bash
# Filter by event type
az eventgrid event-subscription create \
  --resource-group keycloak-rg \
  --topic-name keycloak-events \
  --name login-events-only \
  --endpoint <destination> \
  --included-event-types keycloak.event.LOGIN

# Advanced filter by subject (realm)
az eventgrid event-subscription create \
  --resource-group keycloak-rg \
  --topic-name keycloak-events \
  --name production-realm-only \
  --endpoint <destination> \
  --subject-begins-with /realms/production
```

## Resources

- [Azure Event Grid Overview](https://learn.microsoft.com/en-us/azure/event-grid/overview)
- [Webhook Event Delivery](https://learn.microsoft.com/en-us/azure/event-grid/webhook-event-delivery)
- [Event Schema](https://learn.microsoft.com/en-us/azure/event-grid/event-schema)
- [Event Filtering](https://learn.microsoft.com/en-us/azure/event-grid/how-to-filter-events)
- [Dead-Letter and Retry Policies](https://learn.microsoft.com/en-us/azure/event-grid/manage-event-delivery)
