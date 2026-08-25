# Confluent Cloud (Kafka) Quick Start

This quick start demonstrates forwarding Keycloak events to **Confluent Cloud** using the Kafka protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- A Confluent Cloud account ([sign up free](https://www.confluent.io/get-started/))
- A Confluent Cloud cluster

## Confluent Cloud Setup

### 1. Create a Kafka Cluster

1. Log in to [Confluent Cloud](https://confluent.cloud/)
2. Create or select an **Environment**
3. Create a **Kafka Cluster**:
   - Choose **Basic** (free tier available) or higher
   - Select your preferred cloud provider and region
   - Name your cluster (e.g., `keycloak-events`)

### 2. Create a Topic

1. Navigate to your cluster → **Topics**
2. Click **Create Topic**
3. Set:
   - **Topic name**: `keycloak-events`
   - **Partitions**: 6 (default)
4. Click **Create with defaults**

### 3. Create API Keys

1. Navigate to your cluster → **API Keys**
2. Click **Create Key**
3. Choose **Global access** or scope to your needs
4. **Save the API Key and Secret** - you'll need them!

### 4. Get Bootstrap Server

1. Navigate to your cluster → **Cluster Settings**
2. Copy the **Bootstrap server** URL
   - Example: `pkc-xxxxx.us-east-1.aws.confluent.cloud:9092`

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-CLUSTER>` | Bootstrap server hostname | `pkc-xxxxx.us-east-1.aws.confluent.cloud` |
| `<YOUR-API-KEY>` | Confluent Cloud API key | `ABCD1234` |
| `<YOUR-API-SECRET>` | Confluent Cloud API secret | `xyz789...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
CONFLUENT_BOOTSTRAP_SERVER=pkc-xxxxx.us-east-1.aws.confluent.cloud:9092
CONFLUENT_API_KEY=your-api-key
CONFLUENT_API_SECRET=your-api-secret
```

Then update `docker-compose.yml`:

```yaml
environment:
  kete.routes.quick-start.destination.bootstrap.servers: ${CONFLUENT_BOOTSTRAP_SERVER}
  kete.routes.quick-start.destination.sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="${CONFLUENT_API_KEY}" password="${CONFLUENT_API_SECRET}";
```

## Running

```bash
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check Confluent Cloud for received messages:

### Using Confluent Cloud Console

1. Navigate to your cluster → **Topics** → `keycloak-events`
2. Click **Messages**
3. You should see Keycloak events arriving

### Using Confluent CLI

```bash
# Install Confluent CLI
# https://docs.confluent.io/confluent-cli/current/install.html

# Login
confluent login

# Select environment and cluster
confluent environment use <env-id>
confluent kafka cluster use <cluster-id>

# Consume messages
confluent kafka topic consume keycloak-events --from-beginning
```

### Using kafka-console-consumer

```bash
# Create a client properties file
cat > client.properties << EOF
bootstrap.servers=<your-cluster>.confluent.cloud:9092
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="<api-key>" password="<api-secret>";
EOF

# Consume messages
kafka-console-consumer.sh \
  --bootstrap-server <your-cluster>.confluent.cloud:9092 \
  --topic keycloak-events \
  --consumer.config client.properties \
  --from-beginning
```

## Troubleshooting

### Check Keycloak Logs

```bash
docker-compose logs keycloak
```

Look for:
- `kete Route 'quick-start' initialized: destination=...` - Success
- SASL authentication errors - Check API key/secret
- Connection errors - Check bootstrap server URL

### Common Issues

| Issue | Solution |
|-------|----------|
| Authentication failed | Verify API key and secret are correct |
| Topic not found | Create the `keycloak-events` topic first |
| Connection timeout | Check bootstrap server URL includes port `:9092` |
| SSL handshake failed | Ensure `SASL_SSL` protocol is used |

### Verify Cluster Connectivity

```bash
# Test connection with openssl
openssl s_client -connect <your-cluster>.confluent.cloud:9092 -servername <your-cluster>.confluent.cloud
```

## Schema Registry (Optional)

If you want to use Avro/JSON Schema:

1. Enable **Schema Registry** in Confluent Cloud
2. Get Schema Registry credentials:
   - Navigate to **Schema Registry** → **API credentials**
3. Configure KETE with schema registry (requires additional configuration)

## Cleanup

```bash
docker-compose down
```

To remove Confluent Cloud resources:
1. Delete the topic
2. Delete the API keys
3. Delete the cluster (if no longer needed)

## See Also

- [Confluent Cloud Quick Start](https://docs.confluent.io/cloud/current/get-started/index.html)
- [Confluent Cloud Security](https://docs.confluent.io/cloud/current/security/index.html)
- [Kafka Clients for Confluent Cloud](https://docs.confluent.io/cloud/current/client-apps/config-client.html)
