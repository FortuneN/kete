# Amazon ElastiCache for Redis (Pub/Sub) Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon ElastiCache for Redis** using Redis Pub/Sub.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create an ElastiCache Redis Cluster

```bash
# Create a subnet group (if needed)
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name keycloak-redis \
  --cache-subnet-group-description "Keycloak events" \
  --subnet-ids subnet-xxxxx

# Create a Redis cluster with encryption in transit
aws elasticache create-replication-group \
  --replication-group-id keycloak-redis \
  --replication-group-description "Keycloak events" \
  --engine redis \
  --cache-node-type cache.t3.micro \
  --num-cache-clusters 1 \
  --transit-encryption-enabled \
  --auth-token your-auth-token \
  --cache-subnet-group-name keycloak-redis
```

### 2. Get the Primary Endpoint

```bash
aws elasticache describe-replication-groups \
  --replication-group-id keycloak-redis \
  --query "ReplicationGroups[0].NodeGroups[0].PrimaryEndpoint" \
  --output table
```

### 3. Configure Security Group

Ensure the cluster's security group allows inbound traffic on port 6379 from your Docker host or VPC.

> **Note**: ElastiCache is VPC-only. Your Docker host must be in the same VPC or connected via VPN/peering.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-CLUSTER>` | ElastiCache primary endpoint | `keycloak-redis.abc123.0001.use1.cache.amazonaws.com` |
| `<YOUR-AUTH-TOKEN>` | AUTH token | `your-auth-token` |

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
4. Connect to your Redis cluster and subscribe:

```bash
redis-cli -h <endpoint> -p 6379 --tls -a <auth-token>
SUBSCRIBE keycloak-events
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure you're in the same VPC and security group allows port 6379 |
| Authentication failed | Verify AUTH token |
| TLS errors | Ensure `tls.enabled=true` |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
aws elasticache delete-replication-group --replication-group-id keycloak-redis
```
