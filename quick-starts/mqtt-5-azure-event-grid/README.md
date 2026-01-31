# Azure Event Grid (MQTT Broker)

Stream Keycloak events to Azure Event Grid's MQTT broker using MQTT 5 protocol.

## Azure Setup

### 1. Create Event Grid Namespace

```bash
# Create resource group
az group create --name keycloak-rg --location westus2

# Create Event Grid namespace with MQTT enabled
az eventgrid namespace create \
  --resource-group keycloak-rg \
  --name keycloak-eventgrid \
  --location westus2 \
  --topic-spaces-configuration '{maximumSessionExpiryInHours:1}' \
  --is-zone-redundant true

# Get the MQTT endpoint
az eventgrid namespace show \
  --resource-group keycloak-rg \
  --name keycloak-eventgrid \
  --query "topicSpacesConfiguration.hostname" -o tsv
```

### 2. Create Client Certificate

Generate a self-signed certificate for X.509 authentication:

```bash
# Create certs directory
mkdir -p certs

# Generate private key and certificate (combined in single file)
openssl req -x509 -newkey rsa:2048 -keyout certs/client-key.pem \
  -out certs/client-cert.pem -days 365 -nodes \
  -subj "/CN=keycloak-kete"

# Combine certificate and private key into single PEM file (required by KETE)
cat certs/client-cert.pem certs/client-key.pem > certs/client.pem

# Get certificate thumbprint for Azure registration
openssl x509 -in certs/client-cert.pem -noout -fingerprint -sha256 | \
  sed 's/://g' | sed 's/SHA256 Fingerprint=/'
```

### 3. Register MQTT Client

```bash
# Register client with X.509 certificate authentication
az eventgrid namespace client create \
  --resource-group keycloak-rg \
  --namespace-name keycloak-eventgrid \
  --client-name keycloak-kete \
  --authentication-name keycloak-kete-cert \
  --authentication certificate \
  --client-certificate-authentication '{validationScheme:ThumbprintMatch,allowedThumbprints:[<YOUR_THUMBPRINT>]}'
```

Replace `<YOUR_THUMBPRINT>` with the output from the previous step.

### 4. Create Topic Space

```bash
# Create topic space for Keycloak events
az eventgrid namespace topic-space create \
  --resource-group keycloak-rg \
  --namespace-name keycloak-eventgrid \
  --name keycloak-events \
  --topic-templates 'keycloak/#'
```

### 5. Create Permission Binding

```bash
# Create client group
az eventgrid namespace client-group create \
  --resource-group keycloak-rg \
  --namespace-name keycloak-eventgrid \
  --client-group-name keycloak-publishers \
  --query 'clientGroupName=="keycloak-kete"'

# Grant publish permission
az eventgrid namespace permission-binding create \
  --resource-group keycloak-rg \
  --namespace-name keycloak-eventgrid \
  --permission-binding-name keycloak-publish \
  --client-group-name keycloak-publishers \
  --topic-space-name keycloak-events \
  --permission publisher
```

## KETE Configuration

Update `docker-compose.yml` with your Event Grid namespace hostname:

```yaml
kete.routes.quick-start.destination.host: <your-namespace>.westus2-1.ts.eventgrid.azure.net
```

Replace `<your-namespace>` with your Event Grid namespace name.

## Run

```bash
docker compose up -d
```

## Test

1. Open Keycloak Admin Console: http://localhost:8080/admin
2. Login with `admin`/`admin`
3. Events will be published to Event Grid topic `keycloak/events`

## Verify Events in Azure

```bash
# View client connections
az eventgrid namespace client show \
  --resource-group keycloak-rg \
  --namespace-name keycloak-eventgrid \
  --client-name keycloak-kete

# Subscribe to view published messages (requires separate subscriber setup)
# See: https://learn.microsoft.com/en-us/azure/event-grid/mqtt-publish-and-subscribe-portal
```

## Configuration Options

### Authentication Methods

Event Grid supports multiple authentication methods:

- **X.509 Certificate** (shown above) - Industry standard for IoT
- **Microsoft Entra ID** - Azure AD integration
- **JWT OAuth 2.0** - For non-Azure clients
- **Custom Webhook** - External auth via HTTP endpoint

See: [MQTT Client Authentication](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-client-authentication)

### QoS Levels

Event Grid supports QoS 0 and 1:

```yaml
kete.routes.quick-start.destination.qos: 0  # At most once
kete.routes.quick-start.destination.qos: 1  # At least once (default)
```

### Topic Templates

Customize the MQTT topic structure:

```yaml
# Include realm in topic
kete.routes.quick-start.destination.topic: keycloak/${realmLowerCase}/events

# Separate topics by event type
kete.routes.quick-start.destination.topic: keycloak/${eventTypeLowerCase}
```

## Integration with Azure Services

Route MQTT messages from Event Grid to other Azure services:

- **Event Hubs** - Stream analytics
- **Azure Functions** - Serverless processing
- **Azure Stream Analytics** - Real-time insights
- **Microsoft Fabric** - Data warehousing

See: [MQTT Message Routing](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-routing)

## Resources

- [Azure Event Grid MQTT Overview](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-overview)
- [Publish and Subscribe to MQTT messages](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-publish-and-subscribe-portal)
- [MQTT Client Authentication](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-client-authentication)
- [MQTT Access Control](https://learn.microsoft.com/en-us/azure/event-grid/mqtt-access-control)
