# Azure Cache for Redis (Pub/Sub) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Cache for Redis** using Redis Pub/Sub.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- An Azure Cache for Redis instance

## Azure Setup

### 1. Create an Azure Cache for Redis

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create an Azure Cache for Redis (Basic C0 for testing)
az redis create \
  --name <your-cache-name> \
  --resource-group keycloak-events-rg \
  --location eastus \
  --sku Basic \
  --vm-size c0 \
  --enable-non-ssl-port false
```

> **Note**: Production workloads should use Standard or Premium tiers.

### 2. Get the Access Key

```bash
# Get the primary access key
az redis list-keys \
  --name <your-cache-name> \
  --resource-group keycloak-events-rg \
  --query primaryKey -o tsv
```

### 3. Get the Host Name

```bash
# Get the host name
az redis show \
  --name <your-cache-name> \
  --resource-group keycloak-events-rg \
  --query hostName -o tsv
```

The hostname will be: `<your-cache-name>.redis.cache.windows.net`

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-CACHE-NAME>` | Azure Cache for Redis name | `my-keycloak-cache` |
| `<YOUR-ACCESS-KEY>` | Primary or secondary access key | `abc123...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
AZURE_REDIS_HOST=my-keycloak-cache.redis.cache.windows.net
AZURE_REDIS_KEY=your-primary-access-key
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check Azure Cache for Redis for received messages:

### Using Azure CLI

```bash
# Connect to your Redis instance
az redis console --name <your-cache-name> --resource-group keycloak-events-rg

# Subscribe to the channel
SUBSCRIBE keycloak-events
```

### Using redis-cli with TLS

```bash
# Install redis-cli if needed
# Connect with TLS
redis-cli -h <your-cache-name>.redis.cache.windows.net \
  -p 6380 \
  --tls \
  -a <your-access-key>

# Subscribe to the channel
SUBSCRIBE keycloak-events
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure TLS is enabled (port 6380) |
| Authentication failed | Verify the access key is correct |
| Firewall blocked | Add your IP or enable Azure services access |

## Additional Configuration

For production, consider:

- **Premium tier**: For clustering, geo-replication, and data persistence
- **Private endpoints**: For network isolation
- **Managed Identity**: For password-less authentication (requires Premium tier)

## Cleanup

```bash
# Delete the Azure Cache for Redis
az redis delete \
  --name <your-cache-name> \
  --resource-group keycloak-events-rg

# Or delete the entire resource group
az group delete --name keycloak-events-rg
```
