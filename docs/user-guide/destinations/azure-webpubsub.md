# Azure Web PubSub Destination

Stream Keycloak events to Azure Web PubSub.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `azure-webpubsub` |
| **Protocol** | Azure Web PubSub REST API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Web PubSub** | Fully managed real-time messaging service |



## Example Configurations

=== "Connection String"

    ```bash
    kete.routes.wps.destination.kind=azure-webpubsub
    kete.routes.wps.destination.connection-string=Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=your-access-key;Version=1.0;
    kete.routes.wps.destination.hub=keycloak-events
    ```

=== "Send to Group"

    ```bash
    kete.routes.wps.destination.kind=azure-webpubsub
    kete.routes.wps.destination.connection-string=Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=your-access-key;Version=1.0;
    kete.routes.wps.destination.hub=keycloak-events
    kete.routes.wps.destination.group=admin-events
    ```

=== "Managed Identity"

    ```bash
    kete.routes.wps.destination.kind=azure-webpubsub
    kete.routes.wps.destination.authentication-type=managed-identity
    kete.routes.wps.destination.endpoint=https://my-webpubsub.webpubsub.azure.com
    kete.routes.wps.destination.hub=keycloak-events
    ```

=== "Default Azure Credential"

    ```bash
    kete.routes.wps.destination.kind=azure-webpubsub
    kete.routes.wps.destination.authentication-type=default-azure-credential
    kete.routes.wps.destination.endpoint=https://my-webpubsub.webpubsub.azure.com
    kete.routes.wps.destination.hub=keycloak-events
    ```



## Features

- Azure Web PubSub SDK integration
- Send messages to all connected clients or a specific group
- Automatic JSON/text content type detection
- Three authentication methods: connection string, managed identity, default Azure credential
- Hub and group name templating with variables
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-webpubsub` | `azure-webpubsub` |
| `destination.hub` | Hub name for message routing (supports templating) | `keycloak-events` |
| `destination.connection-string` | Azure Web PubSub connection string (required for `connection-string` auth) | `Endpoint=https://...;AccessKey=...;Version=1.0;` |
| `destination.endpoint` | Azure Web PubSub endpoint URL (required for `managed-identity` / `default-azure-credential` auth) | `https://my-webpubsub.webpubsub.azure.com` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.authentication-type` | _(empty)_ | Authentication method: `connection-string`, `managed-identity`, or `default-azure-credential` | `managed-identity` |
| `destination.managed-identity-client-id` | _(empty)_ | Client ID for user-assigned managed identity (only for `managed-identity` auth) | `12345678-...` |
| `destination.group` | _(empty)_ | Group to send messages to — if empty, sends to all clients (supports templating) | `admin-events` |
| `destination.timeout-seconds` | `10` | HTTP request timeout in seconds | `30` |

### Dynamic Hub / Group (Templating)

The `hub` and `group` properties support template variables:

```bash
# Dynamic hub per realm
kete.routes.wps.destination.hub=keycloak-events-${realmLowerCase}

# Dynamic group per event type
kete.routes.wps.destination.group=${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

#### Connection String (Default)

Authentication via access key embedded in the connection string:

```bash
kete.routes.wps.destination.connection-string=Endpoint=https://<resource-name>.webpubsub.azure.com;AccessKey=<your-access-key>;Version=1.0;
```

#### Managed Identity

Authentication via Azure Managed Identity (system-assigned or user-assigned):

```bash
kete.routes.wps.destination.authentication-type=managed-identity
kete.routes.wps.destination.endpoint=https://my-webpubsub.webpubsub.azure.com
# Optional: specify client ID for user-assigned managed identity
kete.routes.wps.destination.managed-identity-client-id=12345678-1234-1234-1234-123456789012
```

#### Default Azure Credential

Authentication via the Azure Identity SDK credential chain (`DefaultAzureCredential`), which tries managed identity, environment variables, Azure CLI, and other credential sources:

```bash
kete.routes.wps.destination.authentication-type=default-azure-credential
kete.routes.wps.destination.endpoint=https://my-webpubsub.webpubsub.azure.com
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Azure Web PubSub TLS"
    When connecting to the real Azure Web PubSub service, TLS is handled automatically via HTTPS in the connection string — no explicit TLS configuration needed. TLS properties are useful when connecting through a proxy or custom endpoint.



## Configuration Examples

### Example 1: Production Setup — Broadcast to All Clients

```bash
kete.routes.prod.destination.kind=azure-webpubsub
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.connection-string=Endpoint=https://prod-webpubsub.webpubsub.azure.com;AccessKey=your-production-key;Version=1.0;
kete.routes.prod.destination.hub=keycloak-events
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Send to Specific Group

```bash
kete.routes.admin.destination.kind=azure-webpubsub
kete.routes.admin.realm-matchers.realm=list:master
kete.routes.admin.event-matchers.filter=glob:ADMIN_*
kete.routes.admin.destination.connection-string=Endpoint=https://my-webpubsub.webpubsub.azure.com;AccessKey=your-key;Version=1.0;
kete.routes.admin.destination.hub=keycloak-events
kete.routes.admin.destination.group=admin-events
```

### Example 3: Managed Identity

```bash
kete.routes.mi.destination.kind=azure-webpubsub
kete.routes.mi.realm-matchers.realm=list:master
kete.routes.mi.event-matchers.filter=glob:*
kete.routes.mi.destination.authentication-type=managed-identity
kete.routes.mi.destination.endpoint=https://my-webpubsub.webpubsub.azure.com
kete.routes.mi.destination.hub=keycloak-events
```

### Example 4: Connection String from Environment Variable

```bash
kete.routes.env.destination.kind=azure-webpubsub
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.connection-string=${AZURE_WEBPUBSUB_CONNECTION_STRING}
kete.routes.env.destination.hub=keycloak-events
```

KETE does not expand `${VARIABLE}` references itself — the value must be substituted by whatever sets the environment variable (Docker Compose, Kubernetes, a shell), otherwise the literal text is used.



## Quick Starts

| Quick Start | Description |
|-------------|-------------|
| [azure-webpubsub](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-webpubsub) | Azure Web PubSub (real cloud) |
| [azure-webpubsub-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/azure-webpubsub-emulator) | Azure Web PubSub Mock (local) |



## See Also

- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
