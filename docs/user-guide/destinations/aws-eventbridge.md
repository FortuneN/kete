# AWS EventBridge Destination

Stream Keycloak events to Amazon EventBridge.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `aws-eventbridge` |
| **Protocol** | AWS EventBridge API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Amazon EventBridge** | Fully managed serverless event bus |
| **LocalStack** | Local development and testing |



## Example Configurations

=== "AWS Cloud"

    ```bash
    kete.routes.eb.destination.kind=aws-eventbridge
    kete.routes.eb.destination.event-bus=keycloak-events
    kete.routes.eb.destination.source=kete.keycloak
    kete.routes.eb.destination.detail-type=KeycloakEvent
    kete.routes.eb.destination.region=us-east-1
    ```

=== "With Instance Metadata Credentials"

    ```bash
    kete.routes.eb.destination.kind=aws-eventbridge
    kete.routes.eb.destination.event-bus=keycloak-events
    kete.routes.eb.destination.source=kete.keycloak
    kete.routes.eb.destination.detail-type=KeycloakEvent
    kete.routes.eb.destination.region=eu-west-1
    kete.routes.eb.destination.authentication-type=instance-metadata
    ```

=== "LocalStack Emulator"

    ```bash
    kete.routes.eb.destination.kind=aws-eventbridge
    kete.routes.eb.destination.event-bus=keycloak-events
    kete.routes.eb.destination.source=kete.keycloak
    kete.routes.eb.destination.detail-type=KeycloakEvent
    kete.routes.eb.destination.region=us-east-1
    kete.routes.eb.destination.endpoint-url=http://localstack:4566
    ```



## Features

- AWS EventBridge SDK integration with automatic credential resolution
- Support for custom and default event buses
- Event bus name templating with variables
- Detail type templating with variables
- Source name templating with variables
- Configurable source, detail type, and event bus per route
- LocalStack emulator support for local development
- Multiple authentication modes (access key, instance metadata, credentials file, environment variables, default chain, web identity token)
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `aws-eventbridge` | `aws-eventbridge` |
| `destination.event-bus` | Event bus name (supports templating) | `keycloak-events` |
| `destination.source` | Event source identifier (supports templating) | `kete.keycloak` |
| `destination.detail-type` | Event detail type (supports templating) | `KeycloakEvent` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.region` | _(from env)_ | AWS region (falls back to `AWS_REGION` / `AWS_DEFAULT_REGION`) | `us-east-1` |
| `destination.endpoint-url` | _(empty)_ | Custom endpoint URL (for LocalStack or VPC endpoints) | `http://localstack:4566` |
| `destination.authentication-type` | _(empty)_ | Authentication type (see [Authentication](#authentication)) | `access-key` |
| `destination.access-key-id` | _(empty)_ | AWS access key ID (required when `authentication-type=access-key`) | `AKIAIOSFODNN7EXAMPLE` |
| `destination.secret-access-key` | _(empty)_ | AWS secret access key (required when `authentication-type=access-key`) | `wJalrXUtn...` |
| `destination.credentials-file-path` | _(empty)_ | Path to AWS credentials file | `/path/to/credentials` |
| `destination.credentials-file-text` | _(empty)_ | AWS credentials file content inline | `[default]\naws_access_key_id=...` |
| `destination.credentials-file-base64` | _(empty)_ | Base64-encoded AWS credentials file | `W2RlZmF1bHRd...` |
| `destination.credentials-profile` | `default` | Profile name within credentials file | `production` |
| `destination.timeout-seconds` | `10` | HTTP connect and request timeout in seconds | `30` |

### Dynamic Event Bus (Templating)

The `event-bus` property supports template variables:

```bash
# Dynamic event bus per realm
kete.routes.eb.destination.event-bus=keycloak-events-${realmLowerCase}
```

### Dynamic Source (Templating)

The `source` property supports template variables:

```bash
# Dynamic source per realm
kete.routes.eb.destination.source=kete.keycloak.${realmLowerCase}
```

### Dynamic Detail Type (Templating)

The `detail-type` property supports template variables:

```bash
# Dynamic detail type per event type
kete.routes.eb.destination.detail-type=Keycloak-${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

AWS authentication is controlled by the `authentication-type` property:

| `authentication-type` | Description | Required Properties |
|-----------------------|-------------|---------------------|
| `access-key` | Static access key credentials | `access-key-id`, `secret-access-key` |
| `environment-variables` | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` env vars | — |
| `instance-metadata` | EC2 instance profile / IMDSv2 | — |
| `container-credentials` | ECS/EKS container credentials via `AWS_CONTAINER_CREDENTIALS_*` | — |
| `credentials-file-path` | AWS credentials file on disk | `credentials-file-path`, optionally `credentials-profile` |
| `credentials-file-text` | AWS credentials file content inline | `credentials-file-text`, optionally `credentials-profile` |
| `credentials-file-base64` | Base64-encoded AWS credentials file | `credentials-file-base64`, optionally `credentials-profile` |
| `default-credentials-chain` | Full AWS default credential provider chain | — |
| `web-identity-token` | OIDC Web Identity Token (EKS IRSA) | `AWS_WEB_IDENTITY_TOKEN_FILE` env var |
| _(not set)_ | Anonymous credentials (e.g., LocalStack) | — |

#### Access Key

```bash
kete.routes.eb.destination.authentication-type=access-key
kete.routes.eb.destination.access-key-id=AKIAIOSFODNN7EXAMPLE
kete.routes.eb.destination.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

#### Credentials File

```bash
# From file path
kete.routes.eb.destination.authentication-type=credentials-file-path
kete.routes.eb.destination.credentials-file-path=/path/to/credentials
kete.routes.eb.destination.credentials-profile=production

# Inline text
kete.routes.eb.destination.authentication-type=credentials-file-text
kete.routes.eb.destination.credentials-file-text=[default]\naws_access_key_id=AKIA...\naws_secret_access_key=...

# Base64-encoded
kete.routes.eb.destination.authentication-type=credentials-file-base64
kete.routes.eb.destination.credentials-file-base64=W2RlZmF1bHRdCmF3c19hY2Nlc3Nfa2V5X2lk...
```

!!! note "LocalStack Mode"
    When using LocalStack, set `endpoint-url` to the LocalStack URL. No credentials are needed — anonymous authentication is used automatically when `authentication-type` is not set.

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "AWS EventBridge TLS"
    When connecting to the real AWS EventBridge service, TLS is handled automatically via HTTPS — no explicit TLS configuration needed. TLS properties are useful when connecting through a proxy or custom endpoint.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=aws-eventbridge
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.event-bus=keycloak-events
kete.routes.prod.destination.source=kete.keycloak
kete.routes.prod.destination.detail-type=KeycloakEvent
kete.routes.prod.destination.region=us-east-1
kete.routes.prod.destination.authentication-type=instance-metadata
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Per-Realm Event Buses

```bash
kete.routes.events.destination.kind=aws-eventbridge
kete.routes.events.destination.event-bus=keycloak-${realmLowerCase}-events
kete.routes.events.destination.source=kete.keycloak
kete.routes.events.destination.detail-type=KeycloakEvent
kete.routes.events.destination.region=us-east-1
```

### Example 3: Local Development with LocalStack

```bash
kete.routes.local.destination.kind=aws-eventbridge
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.event-matchers.filter=glob:*
kete.routes.local.destination.event-bus=keycloak-events
kete.routes.local.destination.source=kete.keycloak
kete.routes.local.destination.detail-type=KeycloakEvent
kete.routes.local.destination.region=us-east-1
kete.routes.local.destination.endpoint-url=http://localstack:4566
```

### Example 4: AWS Credentials from Environment

```bash
kete.routes.env.destination.kind=aws-eventbridge
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.event-bus=keycloak-events
kete.routes.env.destination.source=kete.keycloak
kete.routes.env.destination.detail-type=KeycloakEvent
kete.routes.env.destination.region=${AWS_REGION}
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [aws-eventbridge-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-eventbridge-emulator) | LocalStack Emulator (local) |
| [aws-eventbridge](https://github.com/FortuneN/kete/tree/release/quick-starts/aws-eventbridge/) | Real AWS EventBridge |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
