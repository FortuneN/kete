# Google Cloud Memorystore for Redis (Stream) Quick Start

This quick start demonstrates forwarding Keycloak events to **Google Cloud Memorystore for Redis** using Redis Stream for persistent, ordered message storage.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- A Google Cloud account with a project
- [Google Cloud CLI](https://cloud.google.com/sdk/docs/install) (`gcloud`)

## GCP Setup

### 1. Enable the Memorystore API

```bash
gcloud services enable redis.googleapis.com
```

### 2. Create a Memorystore Instance

```bash
gcloud redis instances create keycloak-redis \
  --size=1 \
  --region=us-central1 \
  --redis-version=redis_7_0 \
  --tier=basic \
  --enable-auth
```

### 3. Get the Instance IP and AUTH String

```bash
# Get the instance IP
gcloud redis instances describe keycloak-redis \
  --region=us-central1 \
  --format="value(host)"

# Get the AUTH string
gcloud redis instances get-auth-string keycloak-redis \
  --region=us-central1
```

> **Note**: Memorystore is VPC-only. Your Docker host must be in the same VPC or connected via VPN.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-INSTANCE-IP>` | Memorystore instance IP | `10.0.0.3` |
| `<YOUR-AUTH-STRING>` | AUTH string | `abc123...` |

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
4. Connect to your Memorystore instance and read the stream:

```bash
redis-cli -h <instance-ip> -p 6379 -a <auth-string>
XREAD STREAMS keycloak-events 0
XREVRANGE keycloak-events + - COUNT 10
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure you're in the same VPC |
| Authentication failed | Verify AUTH string |
| Network unreachable | Memorystore is VPC-only — use VPN or Compute Engine |
| Streams not supported | Use Redis version 6.2 or higher |

## Cleanup

```bash
docker compose down

# Remove GCP resources (optional)
gcloud redis instances delete keycloak-redis --region=us-central1
```
