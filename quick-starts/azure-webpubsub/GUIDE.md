# Azure Web PubSub Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Web PubSub**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) (`az`)

## Azure Setup

### 1. Authenticate with Azure

```bash
az login
```

### 2. Create a Web PubSub Service

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create a Web PubSub service
az webpubsub create \
  --name <your-webpubsub-name> \
  --resource-group keycloak-events-rg \
  --location eastus \
  --sku Free_F1
```

### 3. Create a Hub

Hubs are created automatically when the first message is sent. No explicit creation is needed.

### 4. Get the Connection String

```bash
az webpubsub key show \
  --name <your-webpubsub-name> \
  --resource-group keycloak-events-rg \
  --query primaryConnectionString -o tsv
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholder:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-AZURE-WEBPUBSUB-CONNECTION-STRING>` | Azure Web PubSub connection string | `Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=...;Version=1.0;` |

### Option 2: Use environment variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and fill in your connection string:

```env
AZURE_WEBPUBSUB_CONNECTION_STRING=Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=your-access-key;Version=1.0;
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Connect a WebSocket client to your hub to receive messages:

```bash
# Using the Azure CLI Web PubSub extension (install: az extension add --name webpubsub)
az webpubsub client start \
  --name <your-webpubsub-name> \
  --resource-group keycloak-events-rg \
  --hub-name keycloak_events
```

Or use the [Azure Portal](https://portal.azure.com) to view Web PubSub service metrics.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=...` - Success
- Connection errors - Check credentials and network

### Common Issues

| Issue | Solution |
|-------|----------|
| `401 Unauthorized` | Verify the connection string is correct and not expired |
| `403 Forbidden` | Check the access key has not been regenerated |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker compose down

# Remove Azure resources (optional)
az webpubsub delete --name <your-webpubsub-name> --resource-group keycloak-events-rg
az group delete --name keycloak-events-rg
```
