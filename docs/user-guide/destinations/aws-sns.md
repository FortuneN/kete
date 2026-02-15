# AWS SNS Destination

Stream Keycloak events to Amazon Simple Notification Service (SNS).

| Property | Value |
|----------|-------|
| **`destination.kind`** | `aws-sns` |
| **Protocol** | AWS SNS API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Amazon SNS** | Fully managed pub/sub messaging service |
| **LocalStack** | Local development and testing |



## Example Configurations

=== "AWS Cloud"

    ```bash
    kete.routes.sns.destination.kind=aws-sns
    kete.routes.sns.destination.topic=keycloak-events
    kete.routes.sns.destination.region=us-east-1
    kete.routes.sns.destination.account-id=123456789012
    kete.routes.sns.destination.authentication-type=access-key
    kete.routes.sns.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
    kete.routes.sns.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    ```

=== "LocalStack Emulator"

    ```bash
    kete.routes.sns.destination.kind=aws-sns
    kete.routes.sns.destination.topic=keycloak-events
    kete.routes.sns.destination.region=us-east-1
    kete.routes.sns.destination.endpoint-url=http://localstack:4566
    ```

=== "FIFO Topic"

    ```bash
    kete.routes.sns.destination.kind=aws-sns
    kete.routes.sns.destination.topic=keycloak-events.fifo
    kete.routes.sns.destination.region=us-east-1
    kete.routes.sns.destination.account-id=123456789012
    kete.routes.sns.destination.message-group-id=keycloak
    ```



## Features

- AWS SNS SDK integration with automatic credential resolution
- Standard and FIFO topic support
- Topic name templating with variables
- Custom message attributes
- Optional message subject
- Message group ID and deduplication ID for FIFO topics
- LocalStack emulator support for local development
- Multiple authentication modes (access key, instance metadata, credentials file, environment variables, default chain, web identity token)
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `aws-sns` | `aws-sns` |
| `destination.topic` | Topic name (supports templating) | `keycloak-events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.region` | _(from env)_ | AWS region (falls back to `AWS_REGION` / `AWS_DEFAULT_REGION`) | `us-east-1` |
| `destination.account-id` | _(required for real AWS)_ | AWS account ID | `123456789012` |
| `destination.endpoint-url` | _(empty)_ | Custom endpoint URL (for LocalStack or VPC endpoints) | `http://localstack:4566` |
| `destination.authentication-type` | _(empty)_ | Authentication type (see [EventBridge Authentication](aws-eventbridge.md#authentication)) | `access-key` |
| `destination.access-key-id` | _(empty)_ | AWS access key ID (required when `authentication-type=access-key`) | `AKIAIOSFODNN7EXAMPLE` |
| `destination.secret-access-key` | _(empty)_ | AWS secret access key (required when `authentication-type=access-key`) | `wJalrXUtn...` |
| `destination.credentials-file-path` | _(empty)_ | Path to AWS credentials file | `/path/to/credentials` |
| `destination.credentials-file-text` | _(empty)_ | AWS credentials file content inline | `[default]\naws_access_key_id=...` |
| `destination.credentials-file-base64` | _(empty)_ | Base64-encoded AWS credentials file | `W2RlZmF1bHRd...` |
| `destination.credentials-profile` | `default` | Profile name within credentials file | `production` |
| `destination.timeout-seconds` | `10` | HTTP connect and request timeout in seconds | `30` |
| `destination.subject` | _(empty)_ | SNS message subject | `KeycloakEvent` |
| `destination.message-group-id` | _(empty)_ | Message group ID (required for FIFO topics) | `keycloak` |
| `destination.message-deduplication-id` | _(empty)_ | Message deduplication ID for FIFO topics | `${eventTypeLowerCase}` |

### Dynamic Topic Name (Templating)

The `topic` property supports template variables:

```bash
kete.routes.sns.destination.topic=keycloak-events-${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Attributes

Custom message attributes can be added:

```bash
kete.routes.sns.destination.attributes.Source=keycloak
kete.routes.sns.destination.attributes.Environment=production
```

### Custom Headers

Custom headers are also included as SNS message attributes:

```bash
kete.routes.sns.destination.headers.X-Source=keycloak
kete.routes.sns.destination.headers.X-Environment=production
```

Both `attributes.*` and `headers.*` entries are sent as SNS message attributes. On key conflict, `headers.*` values take precedence.

### Authentication

AWS SNS uses the AWS SDK credential provider chain. See [AWS EventBridge Authentication](aws-eventbridge.md#authentication) for the full list of authentication methods — they are identical across all AWS destinations.

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
kete.routes.prod.destination.kind=aws-sns
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.topic=keycloak-events
kete.routes.prod.destination.region=us-east-1
kete.routes.prod.destination.account-id=123456789012
kete.routes.prod.destination.authentication-type=access-key
kete.routes.prod.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
kete.routes.prod.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

### Example 2: FIFO Topic with Subject

```bash
kete.routes.fifo.destination.kind=aws-sns
kete.routes.fifo.destination.topic=keycloak-events.fifo
kete.routes.fifo.destination.region=us-east-1
kete.routes.fifo.destination.account-id=123456789012
kete.routes.fifo.destination.subject=KeycloakEvent
kete.routes.fifo.destination.message-group-id=keycloak
```

### Example 3: Local Development with LocalStack

```bash
kete.routes.local.destination.kind=aws-sns
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.destination.topic=keycloak-events
kete.routes.local.destination.region=us-east-1
kete.routes.local.destination.endpoint-url=http://localstack:4566
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [aws-sns-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sns-emulator/) | LocalStack Emulator (local) |
| [aws-sns](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sns/) | Real AWS SNS |



## See Also

- [AWS SQS Destination](aws-sqs.md) — Message queuing
- [AWS Kinesis Destination](aws-kinesis.md) — Real-time data streaming
- [AWS EventBridge Destination](aws-eventbridge.md) — Serverless event routing
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
