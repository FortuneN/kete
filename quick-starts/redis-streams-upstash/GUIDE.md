# Upstash Redis Streams Quick Start

This quick start demonstrates forwarding Keycloak events to **Upstash** serverless Redis using Redis Streams for persistent, ordered message storage.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An Upstash account ([sign up free](https://upstash.com/))

## Upstash Setup

### 1. Create a Redis Database

1. Log in to [Upstash Console](https://console.upstash.com/)
2. Click **Create Database**
3. Configure:
   - **Name**: `keycloak-events`
   - **Region**: Choose the closest region
   - **Type**: Regional (or Global for multi-region)
   - **TLS**: Enabled (recommended)
4. Click **Create**

### 2. Get Connection Details

After creation, navigate to your database and find:

| Field | Description | Example |
|-------|-------------|---------|
| **Endpoint** | Redis hostname | `evident-frog-12345.upstash.io` |
| **Port** | Redis port (usually 6379) | `6379` |
| **Password** | Authentication password | `AXk3AAIncDE...` |

You can find these in the **REST API** or **Redis** tabs of your database details.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-ENDPOINT>` | Upstash endpoint (without `.upstash.io`) | `evident-frog-12345` |
| `<YOUR-PASSWORD>` | Upstash password | `AXk3AAIncDE...` |

### Option 2: Use environment variables

Create a `.env` file:

```env
UPSTASH_REDIS_HOST=evident-frog-12345.upstash.io
UPSTASH_REDIS_PASSWORD=your-upstash-password
```

## Running

```bash
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check Upstash for received messages:

### Using Upstash Console

1. Navigate to your database in [Upstash Console](https://console.upstash.com/)
2. Go to the **CLI** tab
3. Run:
   ```
   XREAD STREAMS keycloak-events 0
   ```

### Using redis-cli

```bash
# Connect with TLS
redis-cli -h evident-frog-12345.upstash.io \
  -p 6379 \
  --tls \
  -a your-upstash-password

# Read from the stream
XREAD STREAMS keycloak-events 0

# Read the last 10 entries
XREVRANGE keycloak-events + - COUNT 10

# Get stream info
XINFO STREAM keycloak-events
```

### Using Consumer Groups

Redis Streams support consumer groups for load balancing:

```bash
# Create a consumer group
XGROUP CREATE keycloak-events mygroup 0 MKSTREAM

# Read as a consumer
XREADGROUP GROUP mygroup consumer1 COUNT 10 STREAMS keycloak-events >

# Acknowledge processed messages
XACK keycloak-events mygroup <message-id>
```

## Redis Streams Features

| Feature | Description |
|---------|-------------|
| **Persistence** | Messages are stored until explicitly deleted |
| **Ordering** | Messages are strictly ordered by ID |
| **Consumer Groups** | Load balancing across multiple consumers |
| **Acknowledgment** | At-least-once delivery semantics |
| **Trimming** | Automatic size management with `max-len` |

## Upstash Features

| Feature | Description |
|---------|-------------|
| **Serverless** | Pay-per-request pricing, no infrastructure management |
| **Global** | Optional multi-region replication |
| **REST API** | HTTP-based Redis access |
| **TLS** | Encrypted connections by default |
| **Free Tier** | 10,000 commands/day free |

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection timeout | Verify endpoint is correct |
| Authentication failed | Check password (it's the long token, not the REST token) |
| TLS errors | Ensure `tls.enabled=true` |

## Cleanup

1. Log in to [Upstash Console](https://console.upstash.com/)
2. Navigate to your database
3. Click **Delete Database**
