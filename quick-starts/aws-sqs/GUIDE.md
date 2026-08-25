# AWS SQS Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon SQS**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create an SQS Queue

```bash
aws sqs create-queue --queue-name keycloak-events --region us-east-1
```

For a FIFO queue:

```bash
aws sqs create-queue \
  --queue-name keycloak-events.fifo \
  --region us-east-1 \
  --attributes FifoQueue=true,ContentBasedDeduplication=true
```

### 2. Create an IAM User (or Use Existing Credentials)

```bash
# Create an IAM user
aws iam create-user --user-name kete-sqs

# Attach SQS permissions
aws iam attach-user-policy \
  --user-name kete-sqs \
  --policy-arn arn:aws:iam::aws:policy/AmazonSQSFullAccess

# Create access keys
aws iam create-access-key --user-name kete-sqs
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
4. Check your SQS queue for received messages:

```bash
aws sqs receive-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/<account-id>/keycloak-events \
  --max-number-of-messages 10 \
  --wait-time-seconds 5
```

Or use the [AWS Console](https://console.aws.amazon.com/sqs/) to view messages.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| `AccessDenied` | Verify IAM user has SQS permissions |
| `NonExistentQueue` | Ensure the queue exists in the correct region |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
aws sqs delete-queue --queue-url https://sqs.us-east-1.amazonaws.com/<account-id>/keycloak-events
aws iam detach-user-policy --user-name kete-sqs --policy-arn arn:aws:iam::aws:policy/AmazonSQSFullAccess
aws iam delete-access-key --user-name kete-sqs --access-key-id <key-id>
aws iam delete-user --user-name kete-sqs
```
