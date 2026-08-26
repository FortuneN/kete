# Amazon MSK Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon MSK** (Managed Streaming for Apache Kafka).

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)
- An Amazon MSK cluster with SASL/SCRAM authentication

## AWS Setup

### 1. Create an MSK Cluster

Use the [AWS Console](https://console.aws.amazon.com/msk/) or CLI to create an MSK cluster with SASL/SCRAM authentication enabled.

### 2. Create SASL/SCRAM Credentials

MSK uses AWS Secrets Manager for SASL/SCRAM credentials:

```bash
# Create a secret for MSK authentication
aws secretsmanager create-secret \
  --name AmazonMSK_keycloak \
  --secret-string '{"username":"kete","password":"your-secure-password"}'
```

Then associate the secret with your MSK cluster via the AWS Console or CLI.

### 3. Create a Kafka Topic

Connect to one of the brokers and create the topic:

```bash
kafka-topics.sh \
  --bootstrap-server <bootstrap-servers> \
  --create \
  --topic keycloak-events \
  --partitions 3 \
  --replication-factor 2 \
  --command-config client.properties
```

### 4. Get Bootstrap Servers

```bash
aws kafka get-bootstrap-brokers \
  --cluster-arn <cluster-arn> \
  --query BootstrapBrokerStringSaslScram \
  --output text
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-BROKER-1>` | First MSK broker endpoint | `b-1.mycluster.abc123.c2.kafka.us-east-1.amazonaws.com` |
| `<YOUR-BROKER-2>` | Second MSK broker endpoint | `b-2.mycluster.abc123.c2.kafka.us-east-1.amazonaws.com` |
| `<YOUR-USERNAME>` | SASL/SCRAM username | `kete` |
| `<YOUR-PASSWORD>` | SASL/SCRAM password | `your-secure-password` |

### Option 2: Use environment variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and fill in your values.

## Running

```bash
docker compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your MSK topic for received messages:

```bash
kafka-console-consumer.sh \
  --bootstrap-server <bootstrap-servers> \
  --topic keycloak-events \
  --from-beginning \
  --consumer.config client.properties
```

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure VPC/security group allows port 9096 |
| SASL authentication failed | Verify credentials in Secrets Manager |
| Topic not found | Create the topic using Kafka CLI |
| SSL handshake failed | Use `SASL_SSL` security protocol with port 9096 |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
# Delete MSK cluster via AWS Console
aws secretsmanager delete-secret --secret-id AmazonMSK_keycloak --force-delete-without-recovery
```
