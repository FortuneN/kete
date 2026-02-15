# AWS SNS Quick Start

This quick start demonstrates forwarding Keycloak events to **Amazon SNS**.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- An AWS account
- [AWS CLI](https://aws.amazon.com/cli/) (`aws`)

## AWS Setup

### 1. Create an SNS Topic

```bash
aws sns create-topic --name keycloak-events --region us-east-1
```

### 2. Create a Subscription (to verify events)

Subscribe an email address to receive notifications:

```bash
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:<account-id>:keycloak-events \
  --protocol email \
  --notification-endpoint your-email@example.com
```

Or subscribe an SQS queue for programmatic verification:

```bash
# Create a verification queue
aws sqs create-queue --queue-name keycloak-events-verification

# Subscribe the queue to the topic
aws sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:<account-id>:keycloak-events \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:<account-id>:keycloak-events-verification
```

### 3. Create an IAM User (or Use Existing Credentials)

```bash
# Create an IAM user
aws iam create-user --user-name kete-sns

# Attach SNS permissions
aws iam attach-user-policy \
  --user-name kete-sns \
  --policy-arn arn:aws:iam::aws:policy/AmazonSNSFullAccess

# Create access keys
aws iam create-access-key --user-name kete-sns
```

Save the `AccessKeyId` and `SecretAccessKey` from the output.

### 4. Get Your Account ID

```bash
aws sts get-caller-identity --query Account --output text
```

## Configuration

### Option 1: Edit docker-compose.yml directly

Edit [docker-compose.yml](docker-compose.yml) and replace the placeholders:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<YOUR-ACCOUNT-ID>` | AWS account ID (12 digits) | `123456789012` |
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
docker-compose up -d
```

## Testing

1. Open Keycloak at http://localhost:8080
2. Log in with `admin` / `admin`
3. Perform actions (create users, login, etc.)
4. Check your SNS topic for received messages:

```bash
# If subscribed via SQS, read from the verification queue
aws sqs receive-message \
  --queue-url https://sqs.us-east-1.amazonaws.com/<account-id>/keycloak-events-verification \
  --max-number-of-messages 10 \
  --wait-time-seconds 5
```

Or check your email if you subscribed via email.

## Troubleshooting

### Check Keycloak Logs

```bash
docker-compose logs keycloak
```

### Common Issues

| Issue | Solution |
|-------|----------|
| `AuthorizationError` | Verify IAM user has SNS permissions |
| `NotFound` | Ensure the topic exists in the correct region |
| `InvalidParameter` | Verify account-id is correct (12 digits) |
| Connection timeout | Verify outbound HTTPS (port 443) is not blocked |

## Cleanup

```bash
docker-compose down

# Remove AWS resources (optional)
aws sns delete-topic --topic-arn arn:aws:sns:us-east-1:<account-id>:keycloak-events
aws iam detach-user-policy --user-name kete-sns --policy-arn arn:aws:iam::aws:policy/AmazonSNSFullAccess
aws iam delete-access-key --user-name kete-sns --access-key-id <key-id>
aws iam delete-user --user-name kete-sns
```
