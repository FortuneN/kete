# Azure Storage Queue Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Storage Queue**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) (`az`)

## Azure Setup

### 1. Authenticate with Azure

```bash
az login
```

### 2. Create a Storage Account

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create a storage account
az storage account create \
  --name <your-storage-account> \
  --resource-group keycloak-events-rg \
  --location eastus \
  --sku Standard_LRS
```

### 3. Create a Queue

```bash
# Get the connection string
az storage account show-connection-string \
  --name <your-storage-account> \
  --resource-group keycloak-events-rg \
  --query connectionString -o tsv

# Create the queue
az storage queue create \
  --name keycloak-events \
  --account-name <your-storage-account>
```

### 4. Get the Connection String

```bash
az storage account show-connection-string \
  --name <your-storage-account> \
  --resource-group keycloak-events-rg \
  --query connectionString -o tsv
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholder:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-AZURE-STORAGE-CONNECTION-STRING>` | Full Azure Storage connection string | `DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net` |

### Option 2: Use environment variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and fill in your connection string:

```env
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=mykeycloakstorage;AccountKey=your-key;EndpointSuffix=core.windows.net
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your Azure Storage Queue for received messages:

### Using Azure CLI

```bash
# Peek at messages in the queue
az storage message peek \
  --queue-name keycloak-events \
  --account-name <your-storage-account> \
  --num-messages 10
```

### Using Azure Portal

Navigate to your storage account → Queues → `keycloak-events` to view messages.

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
| `403 Forbidden` | Verify account name and key are correct |
| `404 Not Found` | Ensure the `keycloak-events` queue exists |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |
| `AuthenticationFailed` | Check that the account key has not been rotated |

## Cleanup

```bash
docker compose down

# Remove Azure resources (optional)
az storage queue delete \
  --name keycloak-events \
  --account-name <your-storage-account>

# Or delete the entire resource group
az group delete --name keycloak-events-rg --yes
```
