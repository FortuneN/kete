# Azure Event Grid (MQTT 5) Quick Start

This quick start demonstrates forwarding Keycloak events to **Azure Event Grid** using the MQTT 5 protocol with X.509 certificate authentication.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Azure subscription
- OpenSSL (for generating certificates)
- An Azure Event Grid Namespace with MQTT enabled

## Azure Setup

### 1. Create an Event Grid Namespace

```bash
# Create a resource group (if needed)
az group create --name keycloak-events-rg --location eastus

# Create an Event Grid namespace with MQTT enabled
az eventgrid namespace create \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --location eastus \
  --topic-spaces-configuration "{state:'Enabled'}"
```

### 2. Generate X.509 Certificates

Azure Event Grid MQTT requires client certificate authentication (mTLS).

```bash
# Create a certs directory
mkdir -p certs && cd certs

# Generate CA key and certificate
openssl genrsa -out ca.key 4096
openssl req -x509 -new -nodes -key ca.key -sha256 -days 365 \
  -out ca.crt -subj "/CN=Keycloak Events CA"

# Generate client key
openssl genrsa -out client.key 2048

# Generate client CSR
openssl req -new -key client.key \
  -out client.csr -subj "/CN=keycloak-client"

# Sign client certificate with CA
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key \
  -CAcreateserial -out client.crt -days 365 -sha256

# Combine key and certificate for KETE (PEM format)
cat client.crt client.key > client.pem

cd ..
```

### 3. Register the CA Certificate in Azure

```bash
# Get the CA certificate thumbprint
THUMBPRINT=$(openssl x509 -in certs/ca.crt -noout -fingerprint -sha256 | cut -d= -f2 | tr -d ':')

# Register the CA certificate
az eventgrid namespace ca-certificate create \
  --name keycloak-ca \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --certificate "$(cat certs/ca.crt | base64 -w0)"
```

### 4. Create a Client

```bash
# Get the client certificate thumbprint
CLIENT_THUMBPRINT=$(openssl x509 -in certs/client.crt -noout -fingerprint -sha256 | cut -d= -f2 | tr -d ':')

# Register the client
az eventgrid namespace client create \
  --name keycloak-client \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --authentication-name "keycloak-client" \
  --client-certificate-authentication "{validationScheme:'ThumbprintMatch',allowedThumbprints:['$CLIENT_THUMBPRINT']}"
```

### 5. Create Topic Space and Permission Binding

```bash
# Create a topic space
az eventgrid namespace topic-space create \
  --name keycloak-topics \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --topic-templates "keycloak/#"

# Create a permission binding (allow publish)
az eventgrid namespace permission-binding create \
  --name keycloak-publish \
  --namespace-name <your-namespace> \
  --resource-group keycloak-events-rg \
  --client-group-name "\$all" \
  --topic-space-name keycloak-topics \
  --permission publisher
```

### 6. Get the MQTT Hostname

```bash
az eventgrid namespace show \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query "topicSpacesConfiguration.hostname" -o tsv
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholder:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<your-namespace>` | Event Grid namespace hostname | `my-ns.westus2-1.ts.eventgrid.azure.net` |

### Option 2: Use environment variables

Create a `.env` file:

```env
EVENTGRID_MQTT_HOST=my-ns.westus2-1.ts.eventgrid.azure.net
```

Then update `docker-compose.yml`:

```yaml
environment:
  kete.routes.quick-start.destination.host: ${EVENTGRID_MQTT_HOST}
```

## Directory Structure

Ensure your certificates are in place:

```
mqtt-5-azure-event-grid/
├── docker-compose.yml
├── GUIDE.md
└── certs/
    ├── ca.crt       # CA certificate
    ├── ca.key       # CA private key (keep secure!)
    ├── client.crt   # Client certificate
    ├── client.key   # Client private key
    └── client.pem   # Combined cert+key for KETE
```

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Subscribe to receive events:

### Using Azure CLI

```bash
# Check namespace status
az eventgrid namespace show \
  --name <your-namespace> \
  --resource-group keycloak-events-rg \
  --query "provisioningState"
```

### Using mosquitto_sub

```bash
# Subscribe to receive events (requires mosquitto-clients)
mosquitto_sub \
  -h <your-namespace>.westus2-1.ts.eventgrid.azure.net \
  -p 8883 \
  -t "keycloak/#" \
  --cafile certs/ca.crt \
  --cert certs/client.crt \
  --key certs/client.key \
  -V mqttv5
```

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=...` - Success
- TLS/SSL errors - Check certificate configuration
- Authentication errors - Verify client registration

### Common Issues

| Issue | Solution |
|-------|----------|
| TLS handshake failed | Verify certificates are correctly generated |
| Client not authorized | Check client is registered with correct thumbprint |
| Topic not allowed | Verify topic space includes `keycloak/#` |
| Connection refused | Ensure port 8883 is used (not 1883) |

### Verify Certificate Thumbprint

```bash
# Verify the thumbprints match
openssl x509 -in certs/client.crt -noout -fingerprint -sha256
```

## Cleanup

```bash
docker compose down

# Remove Azure resources (optional)
az group delete --name keycloak-events-rg --yes
```

## See Also

- [Azure Event Grid MQTT Overview](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-overview)
- [MQTT Client Authentication](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-client-authentication)
- [Publish and Subscribe to MQTT Messages](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-publish-and-subscribe-portal)
