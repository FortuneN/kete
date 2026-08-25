# Amazon MQ for RabbitMQ (AMQP 0.9.1) Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon MQ for RabbitMQ** using the AMQP 0.9.1 protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create an Amazon MQ Broker (RabbitMQ)

```bash
aws mq create-broker \
  --broker-name keycloak-rabbitmq \
  --engine-type RABBITMQ \
  --engine-version "3.13" \
  --deployment-mode SINGLE_INSTANCE \
  --host-instance-type mq.t3.micro \
  --publicly-accessible \
  --users Username=admin,Password=your-secure-password \
  --region us-east-1
```

Wait for the broker to become available:

```bash
aws mq describe-broker --broker-id <broker-id> --query BrokerState
```

### 2. Get the Broker Endpoint

```bash
aws mq describe-broker \
  --broker-id <broker-id> \
  --query "BrokerInstances[0].Endpoints" \
  --output table
```

Find the AMQP endpoint (port 5671).

### 3. Create a Queue

Access the RabbitMQ Management Console via the AWS Console, then create a queue named `keycloak-events` and bind it to `amq.direct` with routing key `keycloak-events`.

### 4. Configure Security Group

Ensure the broker's security group allows inbound traffic on port 5671 from your IP or Docker host.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-BROKER-ID>` | Amazon MQ broker endpoint | `b-1234abcd...` |
| `<YOUR-REGION>` | AWS region | `us-east-1` |
| `<YOUR-USERNAME>` | Broker username | `admin` |
| `<YOUR-PASSWORD>` | Broker password | `your-secure-password` |

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
4. Check the RabbitMQ Management Console (available through AWS Console) to view messages in the `keycloak-events` queue

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure security group allows port 5671 |
| Authentication failed | Verify username and password |
| TLS errors | Amazon MQ requires TLS — ensure `tls.enabled=true` |
| Queue not found | Create the queue via RabbitMQ Management Console |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
aws mq delete-broker --broker-id <broker-id>
```
