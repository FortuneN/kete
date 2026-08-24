# AWS SQS Destination

Stream Keycloak events to Amazon Simple Queue Service (SQS).

| Property | Value |
|----------|-------|
| **`destination.kind`** | `aws-sqs` |
| **Protocol** | AWS SQS API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Amazon SQS** | Fully managed message queue service |
| **LocalStack** | Local development and testing |



## Example Configurations

=== "AWS Cloud"

    ```bash
    kete.routes.sqs.destination.kind=aws-sqs
    kete.routes.sqs.destination.queue=keycloak-events
    kete.routes.sqs.destination.region=us-east-1
    kete.routes.sqs.destination.account-id=123456789012
    kete.routes.sqs.destination.authentication-type=access-key
    kete.routes.sqs.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
    kete.routes.sqs.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    ```

=== "LocalStack Emulator"

    ```bash
    kete.routes.sqs.destination.kind=aws-sqs
    kete.routes.sqs.destination.queue=keycloak-events
    kete.routes.sqs.destination.region=us-east-1
    kete.routes.sqs.destination.endpoint-url=http://localstack:4566
    ```

=== "FIFO Queue"

    ```bash
    kete.routes.sqs.destination.kind=aws-sqs
    kete.routes.sqs.destination.queue=keycloak-events.fifo
    kete.routes.sqs.destination.region=us-east-1
    kete.routes.sqs.destination.account-id=123456789012
    kete.routes.sqs.destination.message-group-id=keycloak
    ```



## Features

- AWS SQS SDK integration with automatic credential resolution
- Standard and FIFO queue support
- Queue name templating with variables
- Custom message attributes
- Message group ID and deduplication ID for FIFO queues
- LocalStack emulator support for local development
- Multiple authentication modes (access key, instance metadata, credentials file, environment variables, default chain, web identity token)
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `aws-sqs` | `aws-sqs` |
| `destination.queue` | Queue name (supports templating) | `keycloak-events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.region` | _(from env)_ | AWS region (falls back to `AWS_REGION` / `AWS_DEFAULT_REGION`) | `us-east-1` |
| `destination.account-id` | _(required for real AWS; `000000000000` when `endpoint-url` is set)_ | AWS account ID used to build the queue URL | `123456789012` |
| `destination.endpoint-url` | _(empty)_ | Custom endpoint URL (for LocalStack or VPC endpoints) | `http://localstack:4566` |
| `destination.authentication-type` | _(empty)_ | Authentication type (see [EventBridge Authentication](aws-eventbridge.md#authentication)) | `access-key` |
| `destination.access-key-id` | _(empty)_ | AWS access key ID (required when `authentication-type=access-key`) | `AKIAIOSFODNN7EXAMPLE` |
| `destination.secret-access-key` | _(empty)_ | AWS secret access key (required when `authentication-type=access-key`) | `wJalrXUtn...` |
| `destination.credentials-file-path` | _(empty)_ | Path to AWS credentials file | `/path/to/credentials` |
| `destination.credentials-file-text` | _(empty)_ | AWS credentials file content inline | `[default]\naws_access_key_id=...` |
| `destination.credentials-file-base64` | _(empty)_ | Base64-encoded AWS credentials file | `W2RlZmF1bHRd...` |
| `destination.credentials-profile` | _(`AWS_PROFILE` env var, else the SDK default profile)_ | Profile name within credentials file | `production` |
| `destination.timeout-seconds` | `10` | HTTP connect and request timeout in seconds | `30` |
| `destination.content-encoding` | _(empty)_ | Compress the event payload (`gzip`, `deflate`) — see [Content Encodings](../content-encodings/overview.md) | `gzip` |
| `destination.content-transfer-encoding` | _(empty)_ | Encode the event payload (`base64`) — see [Content Transfer Encodings](../content-transfer-encodings/overview.md) | `base64` |
| `destination.message-group-id` | _(empty)_ | Message group ID (supports templating; required for `.fifo` queues, rejected for non-FIFO queues) | `keycloak` |
| `destination.message-deduplication-id` | _(empty)_ | Message deduplication ID for `.fifo` queues (supports templating) | `${eventTypeLowerCase}` |

### Dynamic Queue Name (Templating)

The `queue` property supports template variables:

```bash
kete.routes.sqs.destination.queue=keycloak-events-${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Attributes

Custom message attributes can be added:

```bash
kete.routes.sqs.destination.attributes.Source=keycloak
kete.routes.sqs.destination.attributes.Environment=production
```

### Custom Headers

Custom headers are also included as SQS message attributes:

```bash
kete.routes.sqs.destination.headers.X-Source=keycloak
kete.routes.sqs.destination.headers.X-Environment=production
```

Both `attributes.*` and `headers.*` entries are sent as SQS message attributes. On key conflict, `headers.*` values take precedence. The standard `eventkind`, `eventtype` and `contenttype` attributes are always added and override any custom attribute with the same name.

### Authentication

AWS SQS uses the AWS SDK credential provider chain. See [AWS EventBridge Authentication](aws-eventbridge.md#authentication) for the full list of authentication methods — they are identical across all AWS destinations.

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
kete.routes.prod.destination.kind=aws-sqs
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.queue=keycloak-events
kete.routes.prod.destination.region=us-east-1
kete.routes.prod.destination.account-id=123456789012
kete.routes.prod.destination.authentication-type=access-key
kete.routes.prod.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
kete.routes.prod.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

### Example 2: FIFO Queue

```bash
kete.routes.fifo.destination.kind=aws-sqs
kete.routes.fifo.destination.queue=keycloak-events.fifo
kete.routes.fifo.destination.region=us-east-1
kete.routes.fifo.destination.account-id=123456789012
kete.routes.fifo.destination.message-group-id=keycloak
kete.routes.fifo.destination.message-deduplication-id=${eventTypeLowerCase}
```

### Example 3: Local Development with LocalStack

```bash
kete.routes.local.destination.kind=aws-sqs
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.destination.queue=keycloak-events
kete.routes.local.destination.region=us-east-1
kete.routes.local.destination.endpoint-url=http://localstack:4566
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [aws-sqs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sqs-emulator/) | LocalStack Emulator (local) |
| [aws-sqs](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-sqs/) | Real AWS SQS |



## See Also

- [AWS SNS Destination](aws-sns.md) — Pub/sub messaging
- [AWS Kinesis Destination](aws-kinesis.md) — Real-time data streaming
- [AWS EventBridge Destination](aws-eventbridge.md) — Serverless event routing
- [Serializers](../serializers/overview.md)
- [Matchers](../matchers/overview.md)
- [Event Types](../event-types.md)
- [Certificate Loaders](../certificate-loaders/overview.md)
