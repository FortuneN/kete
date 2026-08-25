# AWS EventBridge Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon EventBridge**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create a Custom Event Bus

```bash
aws events create-event-bus --name keycloak-events --region us-east-1
```

### 2. Create a Rule and Target (to verify events)

Create a rule to match events from KETE, and route them to an SQS queue or CloudWatch Logs for verification:

```bash
# Create a rule that matches KETE events
aws events put-rule \
  --name catch-all \
  --event-bus-name keycloak-events \
  --event-pattern '{"source":["kete.keycloak"]}' \
  --state ENABLED

# Create an SQS queue for verification
aws sqs create-queue --queue-name keycloak-events-verification

# Add SQS as a target
aws events put-targets \
  --rule catch-all \
  --event-bus-name keycloak-events \
  --targets "Id=1,Arn=arn:aws:sqs:us-east-1:<account-id>:keycloak-events-verification"
```

> **Note**: You may need to add a resource-based policy on the SQS queue to allow EventBridge to send messages to it.

### 3. Create an IAM User (or Use Existing Credentials)

```bash
# Create an IAM user
aws iam create-user --user-name kete-eventbridge

# Attach EventBridge permissions
aws iam attach-user-policy \
  --user-name kete-eventbridge \
  --policy-arn arn:aws:iam::aws:policy/AmazonEventBridgeFullAccess

# Create access keys
aws iam create-access-key --user-name kete-eventbridge
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
4. Check your EventBridge for received events:

```bash
# If using SQS as target, read from the verification queue
aws sqs receive-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/<account-id>/keycloak-events-verification \
  --max-number-of-messages 10 \
  --wait-time-seconds 5
```

Or use the [AWS Console](https://console.aws.amazon.com/events/) to view the event bus.

## Troubleshooting

### Check Keycloak Logs

```bash
docker compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| `AccessDeniedException` | Verify IAM user has EventBridge permissions |
| `ResourceNotFoundException` | Ensure the event bus exists in the correct region |
| No events in target | Verify rule pattern matches `source` value |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker compose down

# Remove AWS resources (optional)
aws events remove-targets --rule catch-all --event-bus-name keycloak-events --ids 1
aws events delete-rule --name catch-all --event-bus-name keycloak-events
aws events delete-event-bus --name keycloak-events
aws sqs delete-queue --queue-url https://sqs.us-east-1.amazonaws.com/<account-id>/keycloak-events-verification
aws iam detach-user-policy --user-name kete-eventbridge --policy-arn arn:aws:iam::aws:policy/AmazonEventBridgeFullAccess
aws iam delete-access-key --user-name kete-eventbridge --access-key-id <key-id>
aws iam delete-user --user-name kete-eventbridge
```
