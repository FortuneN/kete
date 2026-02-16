# Azure Event Grid Destination

Stream Keycloak events to Azure Event Grid.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `azure-eventgrid` |
| **Protocol** | Azure Event Grid REST API (SDK) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Event Grid** | Fully managed event routing service |



## Example Configurations

=== "Access Key"

    ```bash
    kete.routes.eg.destination.kind=azure-eventgrid
    kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
    kete.routes.eg.destination.access-key=your-event-grid-access-key
    ```

=== "Managed Identity"

    ```bash
    kete.routes.eg.destination.kind=azure-eventgrid
    kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
    kete.routes.eg.destination.authentication-type=managed-identity
    ```

=== "Default Azure Credential"

    ```bash
    kete.routes.eg.destination.kind=azure-eventgrid
    kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
    kete.routes.eg.destination.authentication-type=default-azure-credential
    ```



## Features

- Azure Event Grid SDK integration
- Authentication via access key, Managed Identity, or Default Azure Credential
- Configurable event subject, event type, and data version
- Subject and event type templating with variables
- Automatic subject fallback to Keycloak event type when not set
- TLS/mTLS support



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-eventgrid` | `azure-eventgrid` |
| `destination.endpoint` | Event Grid topic endpoint URL | `https://my-topic.westus2-1.eventgrid.azure.net/api/events` |

One of the following authentication configurations is also required — see [Authentication](#authentication).

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.subject` | _(event type)_ | Event subject (supports templating). If blank, uses the Keycloak event type. | `keycloak/${realmLowerCase}` |
| `destination.event-type` | `KeycloakEvent` | Event Grid event type field (supports templating) | `Keycloak.${eventTypeLowerCase}` |
| `destination.data-version` | `1.0` | Data version for the event | `2.0` |
| `destination.timeout-seconds` | `10` | HTTP request timeout in seconds | `30` |

### Dynamic Subject and Event Type (Templating)

The `subject` and `event-type` properties support template variables:

```bash
# Dynamic subject per realm and event type
kete.routes.eg.destination.subject=keycloak/${realmLowerCase}/${eventTypeLowerCase}

# Dynamic event type
kete.routes.eg.destination.event-type=Keycloak.${kindLowerCase}.${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Authentication

#### Access Key (default)

When `authentication-type` is not set, `access-key` is required:

```bash
kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.eg.destination.access-key=your-event-grid-access-key
```

#### Explicit Access Key

```bash
kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.eg.destination.authentication-type=access-key
kete.routes.eg.destination.access-key=your-event-grid-access-key
```

#### Managed Identity

```bash
kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.eg.destination.authentication-type=managed-identity

# Optional: specify a user-assigned managed identity
kete.routes.eg.destination.managed-identity-client-id=your-client-id
```

| Property | Default | Description |
|----------|---------|-------------|
| `destination.authentication-type` | _(empty)_ | Set to `managed-identity` |
| `destination.managed-identity-client-id` | _(empty)_ | Client ID for user-assigned managed identity. Omit for system-assigned. |

#### Default Azure Credential

```bash
kete.routes.eg.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.eg.destination.authentication-type=default-azure-credential
```

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Azure Event Grid TLS"
    When connecting to the real Azure Event Grid service, TLS is handled automatically via HTTPS — no explicit TLS configuration needed. TLS properties are useful when connecting through a proxy or custom endpoint.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=azure-eventgrid
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.endpoint=https://prod-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.prod.destination.access-key=your-production-access-key
kete.routes.prod.destination.subject=keycloak/events
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Per-Realm Event Subjects

```bash
kete.routes.events.destination.kind=azure-eventgrid
kete.routes.events.destination.endpoint=https://my-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.events.destination.access-key=your-access-key
kete.routes.events.destination.subject=keycloak/${realmLowerCase}/${eventTypeLowerCase}
kete.routes.events.destination.event-type=Keycloak.${kindLowerCase}
```

### Example 3: Managed Identity in Azure

```bash
kete.routes.azure.destination.kind=azure-eventgrid
kete.routes.azure.realm-matchers.realm=list:master
kete.routes.azure.event-matchers.filter=glob:*
kete.routes.azure.destination.endpoint=https://prod-topic.westus2-1.eventgrid.azure.net/api/events
kete.routes.azure.destination.authentication-type=managed-identity
```

### Example 4: Access Key from Environment Variable

```bash
kete.routes.env.destination.kind=azure-eventgrid
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.endpoint=${EVENTGRID_ENDPOINT}
kete.routes.env.destination.access-key=${EVENTGRID_ACCESS_KEY}
```



## Quick Starts

No dedicated quick start available for the native `azure-eventgrid` destination.

Azure Event Grid can also be accessed via the [HTTP Destination](http.md) — see [http-azure-event-grid](https://github.com/FortuneN/kete/tree/release/quick-starts/http-azure-event-grid) quick start.



## See Also

- [HTTP Destination](http.md) - Alternative way to publish to Azure Event Grid
- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
