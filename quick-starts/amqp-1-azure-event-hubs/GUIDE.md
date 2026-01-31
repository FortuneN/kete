# Azure Event Hubs (AMQP 1.0) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Event Hubs** using the AMQP 1.0 protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- An Azure Event Hubs namespace with an Event Hub

## Azure Setup

### 1. Create an Event Hubs Namespace

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create an Event Hubs namespace
az eventhubs namespace create \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --sku Standard
```

### 2. Create an Event Hub

```bash
az eventhubs eventhub create \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --partition-count 2
```

### 3. Get the Connection Credentials

```bash
# Get the primary connection string
az eventhubs namespace authorization-rule keys list \
  --name RootManageSharedAccessKey \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query primaryConnectionString -o tsv
```

From the connection string, extract:
- **Namespace**: `<your-namespace>.servicebus.windows.net`
- **SAS Key Name**: Usually `RootManageSharedAccessKey`
- **SAS Key**: The key value from the connection string (after `SharedAccessKey=`)

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-NAMESPACE>` | Event Hubs namespace name | `my-keycloak-ns` |
| `<YOUR-SAS-KEY-NAME>` | Shared Access Signature key name | `RootManageSharedAccessKey` |
| `<YOUR-SAS-KEY>` | Shared Access Signature key | `abc123...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
EVENTHUB_NAMESPACE=my-keycloak-ns
EVENTHUB_SAS_KEY_NAME=RootManageSharedAccessKey
EVENTHUB_SAS_KEY=your-sas-key-here
```

Then update `docker-compose.yml` to use these variables:

```yaml
environment:
  kete.routes.quick-start.destination.host: ${EVENTHUB_NAMESPACE}.servicebus.windows.net
  kete.routes.quick-start.destination.username: ${EVENTHUB_SAS_KEY_NAME}
  kete.routes.quick-start.destination.password: ${EVENTHUB_SAS_KEY}
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

```bash
# Using Azure CLI to read events (requires azure-eventhubs extension)
az eventhubs eventhub consumer-group list \
  --eventhub-name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg
```

Or use [Azure Service Bus Explorer](https://github.com/paolosalvatori/ServiceBusExplorer) or the Azure Portal to view messages.

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
| Connection refused | Verify namespace name and port 5671 is accessible |
| Authentication failed | Check SAS key name and value |
| Event Hub not found | Ensure `keycloak-events` Event Hub exists |

## Cleanup

```bash
docker-compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```
