# AWS Kinesis Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon Kinesis Data Streams**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create a Kinesis Stream

```bash
aws kinesis create-stream \
  --stream-name keycloak-events \
  --shard-count 1 \
  --region us-east-1
```

Wait for the stream to become active:

```bash
aws kinesis describe-stream \
  --stream-name keycloak-events \
  --query StreamDescription.StreamStatus
```

### 2. Create an IAM User (or Use Existing Credentials)

```bash
# Create an IAM user
aws iam create-user --user-name kete-kinesis

# Attach Kinesis permissions
aws iam attach-user-policy \
  --user-name kete-kinesis \
  --policy-arn arn:aws:iam::aws:policy/AmazonKinesisFullAccess

# Create access keys
aws iam create-access-key --user-name kete-kinesis
```

Save the `AccessKeyId` and `SecretAccessKey` from the output.

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-ACCESS-KEY-ID>` | AWS access key ID | `AKIAIOSFODNN7EXAMPLE` |
| `<YOUR-SECRET-ACCESS-KEY>` | AWS secret access key | `wJalrXUtnFEMI/K7MDENG/...` |

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
4. Check your Kinesis stream for received records:

```bash
# Get a shard iterator
SHARD_ITERATOR=$(aws kinesis get-shard-iterator \
  --stream-name keycloak-events \
  --shard-id shardId-000000000000 \
  --shard-iterator-type TRIM_HORIZON \
  --query ShardIterator --output text)

# Get records
aws kinesis get-records --shard-iterator $SHARD_ITERATOR
```

Or use the [AWS Console](https://console.aws.amazon.com/kinesis/) to view the stream.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| `AccessDeniedException` | Verify IAM user has Kinesis permissions |
| `ResourceNotFoundException` | Ensure the stream exists in the correct region |
| `ProvisionedThroughputExceededException` | Increase shard count or reduce event rate |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
aws kinesis delete-stream --stream-name keycloak-events
aws iam detach-user-policy --user-name kete-kinesis --policy-arn arn:aws:iam::aws:policy/AmazonKinesisFullAccess
aws iam delete-access-key --user-name kete-kinesis --access-key-id <key-id>
aws iam delete-user --user-name kete-kinesis
```
