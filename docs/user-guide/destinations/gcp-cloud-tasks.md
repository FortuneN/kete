# GCP Cloud Tasks Destination

Stream Keycloak events to Google Cloud Tasks.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `gcp-cloud-tasks` |
| **Protocol** | Cloud Tasks gRPC API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Google Cloud Tasks** | Fully managed task queue for asynchronous HTTP invocations |
| **Cloud Tasks Emulator** | Local development and testing |



## Example Configurations

=== "GCP Cloud (Service Account)"

    ```bash
    kete.routes.tasks.destination.kind=gcp-cloud-tasks
    kete.routes.tasks.destination.project=my-gcp-project
    kete.routes.tasks.destination.location=us-central1
    kete.routes.tasks.destination.queue=keycloak-events
    kete.routes.tasks.destination.target-url=https://my-service.run.app/events
    kete.routes.tasks.destination.authentication-type=service-account-file-path
    kete.routes.tasks.destination.credentials-file-path=/secrets/service-account.json
    ```

=== "GCP Cloud (Base64 Credentials)"

    ```bash
    kete.routes.tasks.destination.kind=gcp-cloud-tasks
    kete.routes.tasks.destination.project=my-gcp-project
    kete.routes.tasks.destination.location=us-central1
    kete.routes.tasks.destination.queue=keycloak-events
    kete.routes.tasks.destination.target-url=https://my-service.run.app/events
    kete.routes.tasks.destination.authentication-type=service-account-file-base64
    kete.routes.tasks.destination.credentials-file-base64=ewogICJ0eXBlIjogInNlcnZpY2VfYWNj...
    ```

=== "Custom API Endpoint"

    ```bash
    kete.routes.tasks.destination.kind=gcp-cloud-tasks
    kete.routes.tasks.destination.project=demo-project
    kete.routes.tasks.destination.location=us-central1
    kete.routes.tasks.destination.queue=keycloak-events
    kete.routes.tasks.destination.target-url=http://localhost/handler
    kete.routes.tasks.destination.endpoint=localhost:8090
    kete.routes.tasks.destination.use-plaintext=true
    # No credentials needed for custom endpoint
    ```



## Features

- Google Cloud Tasks gRPC API integration via the official SDK stubs
- Creates HTTP tasks that invoke a target URL with the event payload
- Message headers (`eventkind`, `eventtype`, `contenttype`) sent as HTTP headers on the task request
- Custom headers support via `destination.headers.*`
- Service account authentication with automatic OAuth 2.0 token management
- Three credential loading methods (file path, inline text, Base64)
- Custom API endpoint support for local development and testing
- Configurable HTTP method for target invocation (POST, PUT, etc.)
- Queue name templating with variables
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `gcp-cloud-tasks` | `gcp-cloud-tasks` |
| `destination.project` | GCP project ID | `my-gcp-project` |
| `destination.location` | GCP location (region) | `us-central1` |
| `destination.queue` | Cloud Tasks queue name (supports templating) | `keycloak-events` |
| `destination.target-url` | HTTP URL that the task will invoke | `https://my-service.run.app/events` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.endpoint` | `cloudtasks.googleapis.com:443` | Cloud Tasks gRPC API endpoint (override for emulators/testing) | `localhost:8090` |
| `destination.use-plaintext` | `false` | Use plaintext gRPC (no TLS) — for emulators | `true` |
| `destination.http-method` | `POST` | HTTP method for the target invocation | `PUT` |
| `destination.timeout-seconds` | `10` | gRPC deadline for the start-up queue check (task creation calls are not deadlined) | `30` |
| `destination.authentication-type` | _(empty)_ | Authentication method (see [Authentication](#authentication-credentials)) | `service-account-file-path` |

### Dynamic Queue Name (Templating)

The `queue` property supports template variables:

```bash
# Dynamic queue per realm
kete.routes.tasks.destination.queue=keycloak-events-${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication (Credentials)

GCP Cloud Tasks uses service account credentials for authentication. Set `destination.authentication-type` and provide credentials using the corresponding method:

| Authentication Type | Required Property | Description |
|---------------------|-------------------|-------------|
| `service-account-file-path` | `destination.credentials-file-path` | Filesystem path to a GCP service account JSON file |
| `service-account-file-text` | `destination.credentials-file-text` | GCP service account JSON provided inline as plain text |
| `service-account-file-base64` | `destination.credentials-file-base64` | GCP service account JSON provided as a Base64-encoded string |
| `application-default` | _(none)_ | Uses Application Default Credentials (ADC) |

The three `credentials-file-*` properties are mutually exclusive, and none of them may be set together with `authentication-type=application-default`.

!!! note "Custom Endpoint Mode"
    Credentials are **not required** when using a custom `destination.endpoint` (e.g., a local emulator). Set `destination.endpoint` to the custom endpoint, enable `destination.use-plaintext=true`, and omit all credential properties.

!!! warning "Credential Validation"
    If `destination.endpoint` is the default (`cloudtasks.googleapis.com:443`), `destination.authentication-type` must be set (together with its matching credentials property). Credentials properties alone, without `authentication-type`, are ignored.

### Custom Headers

Custom headers can be added to the HTTP request of the Cloud Task:

```bash
kete.routes.tasks.destination.headers.X-Source=keycloak
kete.routes.tasks.destination.headers.X-Environment=production
```

Headers are included as HTTP headers on the task's target request alongside the automatic `eventkind`, `eventtype`, and `contenttype` headers.

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Google Cloud Tasks TLS"
    When connecting to the real Google Cloud Tasks service (`cloudtasks.googleapis.com:443`), the gRPC channel negotiates TLS with the system trust store — no explicit TLS configuration needed. `tls.*` properties supply a custom trust store / client certificate for the gRPC channel, which is useful when connecting through a proxy or custom endpoint.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=gcp-cloud-tasks
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.project=my-production-project
kete.routes.prod.destination.location=us-central1
kete.routes.prod.destination.queue=keycloak-events
kete.routes.prod.destination.target-url=https://my-service.run.app/events
kete.routes.prod.destination.authentication-type=service-account-file-path
kete.routes.prod.destination.credentials-file-path=/secrets/service-account.json
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Per-Realm Queues

```bash
kete.routes.events.destination.kind=gcp-cloud-tasks
kete.routes.events.destination.project=my-gcp-project
kete.routes.events.destination.location=us-central1
kete.routes.events.destination.queue=keycloak-${realmLowerCase}-events
kete.routes.events.destination.target-url=https://my-service.run.app/events
kete.routes.events.destination.authentication-type=service-account-file-path
kete.routes.events.destination.credentials-file-path=/secrets/service-account.json
```

### Example 3: Kubernetes with Base64 Credentials

```bash
kete.routes.k8s.destination.kind=gcp-cloud-tasks
kete.routes.k8s.realm-matchers.realm=list:master
kete.routes.k8s.event-matchers.filter=glob:*
kete.routes.k8s.destination.project=my-gcp-project
kete.routes.k8s.destination.location=us-central1
kete.routes.k8s.destination.queue=keycloak-events
kete.routes.k8s.destination.target-url=https://my-service.run.app/events
kete.routes.k8s.destination.authentication-type=service-account-file-base64
kete.routes.k8s.destination.credentials-file-base64=${GCP_SA_KEY_BASE64}
```

### Example 4: Local Development

```bash
kete.routes.local.destination.kind=gcp-cloud-tasks
kete.routes.local.realm-matchers.realm=list:master
kete.routes.local.event-matchers.filter=glob:*
kete.routes.local.destination.project=demo-project
kete.routes.local.destination.location=us-central1
kete.routes.local.destination.queue=keycloak-events
kete.routes.local.destination.target-url=http://localhost/handler
kete.routes.local.destination.endpoint=localhost:8090
kete.routes.local.destination.use-plaintext=true
```



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [gcp-cloud-tasks](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-cloud-tasks) | Google Cloud Tasks (real cloud) |
| [gcp-cloud-tasks-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/gcp-cloud-tasks-emulator) | GCP Cloud Tasks Emulator (local) |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
