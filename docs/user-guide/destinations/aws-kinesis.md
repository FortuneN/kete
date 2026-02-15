# AWS Kinesis Destination

Stream Keycloak events to Amazon Kinesis Data Streams.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `aws-kinesis` |
| **Protocol** | AWS Kinesis API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Amazon Kinesis Data Streams** | Real-time data streaming service |
| **LocalStack** | Local development and testing |



## Example Configurations

=== "AWS Cloud"

    ```bash
    kete.routes.kinesis.destination.kind=aws-kinesis
    kete.routes.kinesis.destination.stream=keycloak-events
    kete.routes.kinesis.destination.partition-key=keycloak
    kete.routes.kinesis.destination.region=us-east-1
    kete.routes.kinesis.destination.authentication-type=access-key
    kete.routes.kinesis.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
    kete.routes.kinesis.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    ```

=== "LocalStack Emulator"

    ```bash
    kete.routes.kinesis.destination.kind=aws-kinesis
    kete.routes.kinesis.destination.stream=keycloak-events
    kete.routes.kinesis.destination.partition-key=keycloak
    kete.routes.kinesis.destination.region=us-east-1
    kete.routes.kinesis.destination.endpoint-url=http://localstack:4566
    ```

=== "Dynamic Partition Key"

    ```bash
    kete.routes.kinesis.destination.kind=aws-kinesis
    kete.routes.kinesis.destination.stream=keycloak-events
    kete.routes.kinesis.destination.partition-key=${realmLowerCase}
    kete.routes.kinesis.destination.region=us-east-1
    ```



## Features

- AWS Kinesis SDK integration with automatic credential resolution
- Stream name templating with variables
- Partition key templating for shard distribution
- LocalStack emulator support for local development
- Multiple authentication modes (access key, instance metadata, credentials file, environment variables, default chain, web identity token)
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `aws-kinesis` | `aws-kinesis` |
| `destination.stream` | Stream name (supports templating) | `keycloak-events` |
| `destination.partition-key` | Partition key (supports templating) | `keycloak` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.region` | _(from env)_ | AWS region (falls back to `AWS_REGION` / `AWS_DEFAULT_REGION`) | `us-east-1` |
| `destination.endpoint-url` | _(empty)_ | Custom endpoint URL (for LocalStack or VPC endpoints) | `http://localstack:4566` |
| `destination.authentication-type` | _(empty)_ | Authentication type (see [EventBridge Authentication](aws-eventbridge.md#authentication)) | `access-key` |
| `destination.access-key-id` | _(empty)_ | AWS access key ID (required when `authentication-type=access-key`) | `AKIAIOSFODNN7EXAMPLE` |
| `destination.secret-access-key` | _(empty)_ | AWS secret access key (required when `authentication-type=access-key`) | `wJalrXUtn...` |
| `destination.credentials-file-path` | _(empty)_ | Path to AWS credentials file | `/path/to/credentials` |
| `destination.credentials-file-text` | _(empty)_ | AWS credentials file content inline | `[default]\naws_access_key_id=...` |
| `destination.credentials-file-base64` | _(empty)_ | Base64-encoded AWS credentials file | `W2RlZmF1bHRd...` |
| `destination.credentials-profile` | `default` | Profile name within credentials file | `production` |
| `destination.timeout-seconds` | `10` | HTTP connect and request timeout in seconds | `30` |

### Dynamic Stream Name (Templating)

The `stream` property supports template variables:

```bash
kete.routes.kinesis.destination.stream=keycloak-events-${realmLowerCase}
```

### Dynamic Partition Key (Templating)

The `partition-key` property supports template variables for shard distribution:

```bash
kete.routes.kinesis.destination.partition-key=${realmLowerCase}-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

AWS Kinesis uses the AWS SDK credential provider chain. See [AWS EventBridge Authentication](aws-eventbridge.md#authentication) for the full list of authentication methods — they are identical across all AWS destinations.

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=aws-kinesis
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.stream=keycloak-events
kete.routes.prod.destination.partition-key=keycloak
kete.routes.prod.destination.region=us-east-1
kete.routes.prod.destination.authentication-type=access-key
kete.routes.prod.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
kete.routes.prod.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

### Example 2: Per-Realm Partition Keys

```bash
kete.routes.events.destination.kind=aws-kinesis
kete.routes.events.destination.stream=keycloak-events
kete.routes.events.destination.partition-key=${realmLowerCase}
kete.routes.events.destination.region=us-east-1
```

### Example 3: Local Development with LocalStack

```bash
kete.routes.local.destination.kind=aws-kinesis
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.destination.stream=keycloak-events
kete.routes.local.destination.partition-key=keycloak
kete.routes.local.destination.region=us-east-1
kete.routes.local.destination.endpoint-url=http://localstack:4566
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [aws-kinesis-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-kinesis-emulator/) | LocalStack Emulator (local) |
| [aws-kinesis](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-kinesis/) | Real AWS Kinesis |



## See Also

- [AWS SQS Destination](aws-sqs.md) — Message queuing
- [AWS SNS Destination](aws-sns.md) — Pub/sub messaging
- [AWS EventBridge Destination](aws-eventbridge.md) — Serverless event routing
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
