# Azure Service Bus (AMQP 1.0) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Service Bus** using the AMQP 1.0 protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- An Azure Service Bus namespace

## Azure Setup

### 1. Create a Service Bus Namespace

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create a Service Bus namespace
az servicebus namespace create \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --sku Standard
```

### 2. Create a Queue or Topic

**Option A: Create a Queue**
```bash
az servicebus queue create \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg
```

**Option B: Create a Topic (for pub/sub)**
```bash
az servicebus topic create \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg

# Create a subscription to receive messages
az servicebus topic subscription create \
  --name all-events \
  --topic-name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg
```

### 3. Get the Connection Credentials

```bash
# Get the primary connection string
az servicebus namespace authorization-rule keys list \
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
| `<YOUR-NAMESPACE>` | Service Bus namespace name | `my-keycloak-sb` |
| `<YOUR-SAS-KEY-NAME>` | Shared Access Signature key name | `RootManageSharedAccessKey` |
| `<YOUR-SAS-KEY>` | Shared Access Signature key | `abc123...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
SERVICEBUS_NAMESPACE=my-keycloak-sb
SERVICEBUS_SAS_KEY_NAME=RootManageSharedAccessKey
SERVICEBUS_SAS_KEY=your-sas-key-here
```

Then update `docker-compose.yml` to use these variables:

```yaml
environment:
  kete.routes.quick-start.destination.host: ${SERVICEBUS_NAMESPACE}.servicebus.windows.net
  kete.routes.quick-start.destination.username: ${SERVICEBUS_SAS_KEY_NAME}
  kete.routes.quick-start.destination.password: ${SERVICEBUS_SAS_KEY}
```

## Running

```bash
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your Service Bus for received messages:

```bash
# Peek at messages in the queue
az servicebus queue show \
  --name keycloak-events \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query "countDetails"
```

Or use [Azure Service Bus Explorer](https://github.com/paolosalvatori/ServiceBusExplorer) or the Azure Portal to view messages.

## Troubleshooting

### Check Keycloak Logs

```bash
docker-compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=...` - Success
- Connection errors - Check credentials and network

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Verify namespace name and port 5671 is accessible |
| Authentication failed | Check SAS key name and value |
| Queue/Topic not found | Ensure `keycloak-events` queue or topic exists |

## Cleanup

```bash
docker-compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```
