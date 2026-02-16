# GCP Pub/Sub Destination

Stream Keycloak events to Google Cloud Pub/Sub.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `gcp-pubsub` |
| **Protocol** | HTTP/REST (Pub/Sub API) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Google Cloud Pub/Sub** | Fully managed, global messaging service |
| **GCP Pub/Sub Emulator** | Local development and testing |



## Example Configurations

=== "GCP Pub/Sub (Service Account)"

    ```bash
    kete.routes.pubsub.destination.kind=gcp-pubsub
    kete.routes.pubsub.destination.project=my-gcp-project
    kete.routes.pubsub.destination.topic=keycloak-events
    kete.routes.pubsub.destination.credentials-file-path=/secrets/service-account.json
    ```

=== "GCP Pub/Sub (Base64 Credentials)"

    ```bash
    kete.routes.pubsub.destination.kind=gcp-pubsub
    kete.routes.pubsub.destination.project=my-gcp-project
    kete.routes.pubsub.destination.topic=keycloak-events
    kete.routes.pubsub.destination.credentials-file-base64=ewogICJ0eXBlIjogInNlcnZpY2VfYWNj...
    ```

=== "GCP Pub/Sub (Inline Credentials)"

    ```bash
    kete.routes.pubsub.destination.kind=gcp-pubsub
    kete.routes.pubsub.destination.project=my-gcp-project
    kete.routes.pubsub.destination.topic=keycloak-events
    kete.routes.pubsub.destination.credentials-file-text={"type":"service_account","project_id":"my-gcp-project",...}
    ```

=== "GCP Pub/Sub Emulator"

    ```bash
    kete.routes.pubsub.destination.kind=gcp-pubsub
    kete.routes.pubsub.destination.project=demo-project
    kete.routes.pubsub.destination.topic=keycloak-events
    kete.routes.pubsub.destination.url=http://pubsub-emulator:8085
    # No credentials needed for emulator
    ```

=== "With Ordering Key"

    ```bash
    kete.routes.pubsub.destination.kind=gcp-pubsub
    kete.routes.pubsub.destination.project=my-gcp-project
    kete.routes.pubsub.destination.topic=keycloak-events
    kete.routes.pubsub.destination.credentials-file-path=/secrets/service-account.json
    kete.routes.pubsub.destination.ordering-key=keycloak
    ```



## Features

- Google Cloud Pub/Sub REST API integration (no GCP SDK required)
- Service account authentication with automatic OAuth 2.0 token management
- Three credential loading methods (file path, inline text, Base64)
- Emulator support for local development and testing
- Topic templating with variables
- Message ordering key support
- Event metadata sent as Pub/Sub message attributes
- Custom user-defined Pub/Sub message attributes
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `gcp-pubsub` | `gcp-pubsub` |
| `destination.project` | GCP project ID | `my-gcp-project` |
| `destination.topic` | Pub/Sub topic name (supports templating) | `keycloak-events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.url` | `https://pubsub.googleapis.com` | Pub/Sub REST API base URL (override for emulators) | `http://localhost:8085` |
| `destination.timeout-seconds` | `10` | HTTP request and connect timeout in seconds | `30` |
| `destination.ordering-key` | _(empty)_ | Ordering key for all published messages (supports templating) | `keycloak` |
| `destination.authentication-type` | _(empty)_ | Authentication method (see [Authentication](#authentication-credentials)) | `service-account-file-path` |

### Dynamic Properties (Templating)

The `topic` and `ordering-key` properties support template variables:

```bash
# Dynamic topic per realm
kete.routes.pubsub.destination.topic=keycloak-events-${realmLowerCase}

# Dynamic topic per event type
kete.routes.pubsub.destination.topic=keycloak-${eventTypeLowerCase}

# Dynamic ordering key
kete.routes.pubsub.destination.ordering-key=${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Message Attributes

Every published message includes these Pub/Sub message attributes automatically:

| Attribute | Description | Example |
|-----------|-------------|---------|
| `eventkind` | Whether it's a user event or admin event | `EVENT`, `ADMIN_EVENT` |
| `eventtype` | The Keycloak event type | `LOGIN`, `USER_CREATE` |
| `contenttype` | MIME type of the message body | `application/json` |

### Custom Attributes

Custom attributes can be added to Pub/Sub messages using the `headers.*` prefix:

```bash
kete.routes.pubsub.destination.headers.source=keycloak
kete.routes.pubsub.destination.headers.environment=production
```

These are forwarded as Pub/Sub message attributes alongside the built-in attributes above. The 3 built-in attributes always take priority on key conflict.

### Authentication (Credentials)

GCP Pub/Sub uses service account credentials for authentication. Set `destination.authentication-type` and provide credentials using the corresponding method:

| Authentication Type | Required Property | Description |
|---------------------|-------------------|-------------|
| `service-account-file-path` | `destination.credentials-file-path` | Filesystem path to a GCP service account JSON file |
| `service-account-file-text` | `destination.credentials-file-text` | GCP service account JSON provided inline as plain text |
| `service-account-file-base64` | `destination.credentials-file-base64` | GCP service account JSON provided as a Base64-encoded string |
| `application-default` | _(none)_ | Uses Application Default Credentials (ADC) |

!!! note "Emulator Mode"
    Credentials are **not required** when using the GCP Pub/Sub Emulator. Set `destination.url` to the emulator endpoint (e.g., `http://localhost:8085`) and omit all credential properties.

!!! warning "Credential Validation"
    If `destination.url` is the default (`https://pubsub.googleapis.com`), at least one credential property must be set. Using the emulator URL without credentials is allowed.

#### How Authentication Works

1. KETE reads the service account JSON (containing `client_email` and `private_key`)
2. Creates a signed JWT (RS256) with scope `https://www.googleapis.com/auth/pubsub`
3. Exchanges the JWT at Google's token endpoint for an OAuth 2.0 access token
4. Caches and automatically refreshes the access token
5. Includes the access token as a `Bearer` token in all Pub/Sub API requests

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Google Cloud Pub/Sub TLS"
    When connecting to the real Google Cloud Pub/Sub service (`https://pubsub.googleapis.com`), TLS is handled automatically via HTTPS — no explicit TLS configuration needed. TLS properties are useful when connecting through a proxy or custom endpoint.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=gcp-pubsub
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.project=my-production-project
kete.routes.prod.destination.topic=keycloak-events
kete.routes.prod.destination.authentication-type=service-account-file-path
kete.routes.prod.destination.credentials-file-path=/secrets/service-account.json
kete.routes.prod.destination.timeout-seconds=30
kete.routes.prod.destination.ordering-key=keycloak
```

### Example 2: Per-Realm Topics

```bash
# Production realm events
kete.routes.prod-events.destination.kind=gcp-pubsub
kete.routes.prod-events.realm-matchers.realm=list:production
kete.routes.prod-events.event-matchers.filter=glob:*
kete.routes.prod-events.destination.project=my-gcp-project
kete.routes.prod-events.destination.topic=prod-keycloak-events
kete.routes.prod-events.destination.authentication-type=service-account-file-path
kete.routes.prod-events.destination.credentials-file-path=/secrets/service-account.json

# Development realm events
kete.routes.dev-events.destination.kind=gcp-pubsub
kete.routes.dev-events.realm-matchers.realm=list:development
kete.routes.dev-events.event-matchers.filter=glob:*
kete.routes.dev-events.destination.project=my-gcp-project
kete.routes.dev-events.destination.topic=dev-keycloak-events
kete.routes.dev-events.destination.authentication-type=service-account-file-path
kete.routes.dev-events.destination.credentials-file-path=/secrets/service-account.json
```

### Example 3: Local Development with Emulator

```bash
kete.routes.local.destination.kind=gcp-pubsub
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.event-matchers.filter=glob:*
kete.routes.local.destination.project=demo-project
kete.routes.local.destination.topic=keycloak-events
kete.routes.local.destination.url=http://localhost:8085
```

### Example 4: Kubernetes with Base64 Credentials

```bash
# Credentials injected as env var from Kubernetes secret
kete.routes.k8s.destination.kind=gcp-pubsub
kete.routes.k8s.realm-matchers.realm=list:master
kete.routes.k8s.event-matchers.filter=glob:*
kete.routes.k8s.destination.project=my-gcp-project
kete.routes.k8s.destination.topic=keycloak-events
kete.routes.k8s.destination.authentication-type=service-account-file-base64
kete.routes.k8s.destination.credentials-file-base64=${GCP_SA_KEY_BASE64}
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [gcp-pubsub](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-pubsub) | Google Cloud Pub/Sub (real cloud) |
| [gcp-pubsub-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-pubsub-emulator) | GCP Pub/Sub Emulator (local) |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
