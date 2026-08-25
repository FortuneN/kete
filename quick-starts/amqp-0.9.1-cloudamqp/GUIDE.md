# CloudAMQP (AMQP 0.9.1) Quick Start

This quick start demonstrates forwarding Keycloak events to **CloudAMQP** (managed RabbitMQ) using the AMQP 0.9.1 protocol.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- A CloudAMQP account ([sign up free](https://www.cloudamqp.com/))

## CloudAMQP Setup

### 1. Create a CloudAMQP Instance

1. Log in to [CloudAMQP](https://customer.cloudamqp.com/)
2. Click **Create New Instance**
3. Configure:
   - **Name**: `keycloak-events`
   - **Plan**: Little Lemur (free) or higher
   - **Region**: Choose the closest region
4. Click **Create**

### 2. Get Connection Details

After creation, navigate to your instance details:

| Field | Description | Example |
|-------|-------------|---------|
| **Host** | RabbitMQ hostname | `sparrow-01.rmq.cloudamqp.com` |
| **User & Vhost** | Username and virtual host (same value) | `abcdefgh` |
| **Password** | Authentication password | `xyz123...` |

### 3. Create a Queue

1. Navigate to **RabbitMQ Manager** from your instance dashboard
2. Go to **Queues** tab
3. Click **Add a new queue**
4. Set **Name** to `keycloak-events`, **Durability** to `Durable`
5. Click **Add queue**
6. Go to the queue's **Bindings** section
7. Bind from `amq.direct` with routing key `keycloak-events`

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-INSTANCE>` | CloudAMQP hostname (without `.rmq.cloudamqp.com`) | `sparrow-01` |
| `<YOUR-USERNAME>` | Username | `abcdefgh` |
| `<YOUR-PASSWORD>` | Password | `xyz123...` |
| `<YOUR-VHOST>` | Virtual host (same as username on shared plans) | `abcdefgh` |

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
4. Check CloudAMQP for received messages:

### Using CloudAMQP Manager

1. Navigate to your instance in [CloudAMQP Console](https://customer.cloudamqp.com/)
2. Click **RabbitMQ Manager**
3. Go to **Queues** → `keycloak-events`
4. Click **Get messages** to peek at messages

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Verify hostname and port 5671 |
| Authentication failed | Check username, password, and vhost |
| TLS errors | Ensure `tls.enabled=true` |
| NOT_ALLOWED - vhost | Username and vhost must match on shared plans |

## Cleanup

```bash
docker compose down
```

To remove CloudAMQP resources:
1. Navigate to your instance in [CloudAMQP Console](https://customer.cloudamqp.com/)
2. Click **Delete** to remove the instance
