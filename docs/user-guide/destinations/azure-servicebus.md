# Azure Service Bus Destination

Stream Keycloak events to Azure Service Bus.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `azure-servicebus` |
| **Protocol** | Azure Service Bus SDK (AMQP) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Service Bus** | Fully managed enterprise message broker |
| **Azure Service Bus Emulator** | Local development and testing |



## Example Configurations

=== "Queue (Connection String)"

    ```bash
    kete.routes.sb.destination.kind=azure-servicebus
    kete.routes.sb.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
    kete.routes.sb.destination.queue=keycloak-events
    ```

=== "Topic (Connection String)"

    ```bash
    kete.routes.sb.destination.kind=azure-servicebus
    kete.routes.sb.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
    kete.routes.sb.destination.topic=keycloak-events
    ```

=== "Managed Identity"

    ```bash
    kete.routes.sb.destination.kind=azure-servicebus
    kete.routes.sb.destination.authentication-type=managed-identity
    kete.routes.sb.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
    kete.routes.sb.destination.queue=keycloak-events
    ```



## Features

- Azure Service Bus SDK integration (AMQP transport)
- Send to queues or topics (mutually exclusive)
- Authentication via connection string, Managed Identity, or Default Azure Credential
- Queue, topic, subject, and session ID templating with variables
- Session-enabled queues and topics support
- Custom headers as Service Bus application properties
- Standard event metadata headers (event kind, event type, content type)
- Content type set on the Service Bus message
- Dynamic sender client caching for templated queue/topic names
- Emulator support via custom endpoint address
- TLS handled automatically by the SDK



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-servicebus` | `azure-servicebus` |
| `destination.queue` _or_ `destination.topic` | Queue or topic name (mutually exclusive, supports templating) | `keycloak-events` |

One of the following authentication configurations is also required — see [Authentication](#authentication).

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.subject` | _(empty)_ | Message subject/label (supports templating) | `keycloak/${eventTypeLowerCase}` |
| `destination.session-id` | _(empty)_ | Session ID for session-enabled queues/topics (supports templating) | `${realmLowerCase}` |
| `destination.custom-endpoint-address` | _(empty)_ | Custom endpoint for emulators. Must be a valid URL if set. | `http://localhost:5672` |
| `destination.timeout-seconds` | `10` | TCP connection timeout in seconds for the start-up check. When set explicitly it is also used as the AMQP try timeout of every send | `30` |

### Dynamic Queue, Topic, Subject, and Session ID (Templating)

The `queue`, `topic`, `subject`, and `session-id` properties support template variables:

```bash
# Dynamic queue per realm
kete.routes.sb.destination.queue=keycloak-events-${realmLowerCase}

# Dynamic topic per event kind
kete.routes.sb.destination.topic=keycloak-${kindLowerCase}

# Dynamic subject
kete.routes.sb.destination.subject=keycloak/${realmLowerCase}/${eventTypeLowerCase}

# Dynamic session ID per realm
kete.routes.sb.destination.session-id=${realmLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

!!! note "Dynamic Queue/Topic Names"
    When `queue` or `topic` uses template variables, a separate sender client is lazily created and cached for each resolved entity name. This enables routing events to different queues or topics based on event properties.

### Custom Headers

Custom headers are added as Service Bus application properties on each message:

```bash
kete.routes.sb.destination.headers.environment=production
kete.routes.sb.destination.headers.source=keycloak
```

Standard headers are always included:

| Header | Value |
|--------|-------|
| `eventkind` | `EVENT` or `ADMIN_EVENT` |
| `eventtype` | Keycloak event type (e.g. `LOGIN`) |
| `contenttype` | Content type of the serialized body (e.g. `application/json`) |

### Authentication

#### Connection String (default)

When `authentication-type` is not set, `connection-string` is required:

```bash
kete.routes.sb.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
kete.routes.sb.destination.queue=keycloak-events
```

#### Explicit Connection String

```bash
kete.routes.sb.destination.authentication-type=connection-string
kete.routes.sb.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
kete.routes.sb.destination.queue=keycloak-events
```

#### Managed Identity

```bash
kete.routes.sb.destination.authentication-type=managed-identity
kete.routes.sb.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
kete.routes.sb.destination.queue=keycloak-events

# Optional: specify a user-assigned managed identity
kete.routes.sb.destination.managed-identity-client-id=your-client-id
```

| Property | Default | Description |
|----------|---------|-------------|
| `destination.authentication-type` | _(empty)_ | Set to `managed-identity` |
| `destination.fully-qualified-namespace` | _(empty)_ | Service Bus namespace FQDN (required) |
| `destination.managed-identity-client-id` | _(empty)_ | Client ID for user-assigned managed identity. Omit for system-assigned. |

#### Default Azure Credential

```bash
kete.routes.sb.destination.authentication-type=default-azure-credential
kete.routes.sb.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
kete.routes.sb.destination.queue=keycloak-events
```

### TLS Properties

Azure Service Bus uses AMQPS (port 5671) by default, which is TLS-encrypted. No explicit TLS configuration is needed.

!!! note "Azure Service Bus TLS"
    TLS is handled automatically by the Azure SDK's AMQP transport layer. The `tls.*` properties are not used for Service Bus connections.



## Configuration Examples

### Example 1: Production Queue

```bash
kete.routes.prod.destination.kind=azure-servicebus
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.connection-string=Endpoint=sb://prod-namespace.servicebus.windows.net/;SharedAccessKeyName=SendPolicy;SharedAccessKey=your-key
kete.routes.prod.destination.queue=keycloak-events
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Production Topic

```bash
kete.routes.prod.destination.kind=azure-servicebus
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.connection-string=Endpoint=sb://prod-namespace.servicebus.windows.net/;SharedAccessKeyName=SendPolicy;SharedAccessKey=your-key
kete.routes.prod.destination.topic=keycloak-events
```

### Example 3: Session-Enabled Queue

```bash
kete.routes.sessions.destination.kind=azure-servicebus
kete.routes.sessions.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=SendPolicy;SharedAccessKey=your-key
kete.routes.sessions.destination.queue=keycloak-events
kete.routes.sessions.destination.session-id=${realmLowerCase}
kete.routes.sessions.destination.subject=keycloak/${eventTypeLowerCase}
```

### Example 4: Managed Identity in Azure

```bash
kete.routes.azure.destination.kind=azure-servicebus
kete.routes.azure.realm-matchers.realm=list:master
kete.routes.azure.event-matchers.filter=glob:*
kete.routes.azure.destination.authentication-type=managed-identity
kete.routes.azure.destination.fully-qualified-namespace=prod-namespace.servicebus.windows.net
kete.routes.azure.destination.queue=keycloak-events
```

### Example 5: Per-Realm Dynamic Queues

```bash
kete.routes.dynamic.destination.kind=azure-servicebus
kete.routes.dynamic.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=SendPolicy;SharedAccessKey=your-key
kete.routes.dynamic.destination.queue=keycloak-${realmLowerCase}-events
```

### Example 6: Connection String from Environment Variable

```bash
kete.routes.env.destination.kind=azure-servicebus
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.connection-string=${SERVICEBUS_CONNECTION_STRING}
kete.routes.env.destination.queue=keycloak-events
```

KETE does not expand `${VARIABLE}` references itself — the value must be substituted by whatever sets the environment variable (Docker Compose, Kubernetes, a shell), otherwise the literal text is used.



## Quick Starts

No dedicated quick start available for the native `azure-servicebus` destination.

Azure Service Bus can also be accessed via the [AMQP 1 Destination](amqp-1.md):

| Quick Start | Protocol | Description |
|-------------|----------|-------------|
| [amqp-1-azure-service-bus](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus) | AMQP 1.0 | Azure Service Bus via AMQP 1.0 (cloud) |
| [amqp-1-azure-service-bus-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-service-bus-emulator) | AMQP 1.0 | Azure Service Bus Emulator via AMQP 1.0 (local) |



## See Also

- [AMQP 1 Destination](amqp-1.md) - Access Service Bus via AMQP 1.0
- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
