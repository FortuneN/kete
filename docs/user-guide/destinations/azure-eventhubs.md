# Azure Event Hubs Destination

Stream Keycloak events to Azure Event Hubs.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `azure-eventhubs` |
| **Protocol** | Azure Event Hubs SDK (AMQP) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Azure Event Hubs** | Fully managed event streaming service |
| **Azure Event Hubs Emulator** | Local development and testing |



## Example Configurations

=== "Connection String"

    ```bash
    kete.routes.eh.destination.kind=azure-eventhubs
    kete.routes.eh.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
    kete.routes.eh.destination.event-hub=keycloak-events
    ```

=== "Connection String (Event Hub embedded)"

    ```bash
    kete.routes.eh.destination.kind=azure-eventhubs
    kete.routes.eh.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key;EntityPath=keycloak-events
    ```

=== "Managed Identity"

    ```bash
    kete.routes.eh.destination.kind=azure-eventhubs
    kete.routes.eh.destination.authentication-type=managed-identity
    kete.routes.eh.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
    kete.routes.eh.destination.event-hub=keycloak-events
    ```



## Features

- Azure Event Hubs SDK integration (AMQP transport)
- Authentication via connection string, Managed Identity, or Default Azure Credential
- Partition key templating with variables
- Explicit partition ID targeting
- Custom headers as Event Hubs application properties
- Standard event metadata headers (event kind, event type, content type)
- Emulator support via custom endpoint address
- TLS handled automatically by the SDK



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `azure-eventhubs` | `azure-eventhubs` |

One of the following authentication configurations is required — see [Authentication](#authentication).

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.event-hub` | _(empty)_ | Event Hub name. Can be embedded in connection string instead. Required for Managed Identity / Default Azure Credential. | `keycloak-events` |
| `destination.partition-key` | _(empty)_ | Partition key for routing (supports templating). Mutually exclusive with `partition-id`. | `${realmLowerCase}` |
| `destination.partition-id` | _(empty)_ | Specific partition ID to target. Mutually exclusive with `partition-key`. | `0` |
| `destination.custom-endpoint-address` | _(empty)_ | Custom endpoint for emulators. Must be a valid URL if set. | `http://localhost:5672` |
| `destination.timeout-seconds` | `10` | Timeout in seconds | `30` |

### Dynamic Partition Key (Templating)

The `partition-key` property supports template variables:

```bash
# Partition by realm
kete.routes.eh.destination.partition-key=${realmLowerCase}

# Partition by event type
kete.routes.eh.destination.partition-key=${eventTypeLowerCase}
```

Available variables: `${realmLowerCase}`, `${realmUpperCase}`, `${realmKebabCase}`, `${realmPascalCase}`, `${realmCamelCase}`, `${eventTypeLowerCase}`, `${eventTypeUpperCase}`, `${eventTypeKebabCase}`, `${eventTypePascalCase}`, `${eventTypeCamelCase}`, `${kindLowerCase}`, `${kindUpperCase}`, `${kindKebabCase}`, `${kindPascalCase}`, `${kindCamelCase}`, `${resourceTypeLowerCase}`, `${resourceTypeUpperCase}`, `${resourceTypeKebabCase}`, `${resourceTypePascalCase}`, `${resourceTypeCamelCase}`, `${operationTypeLowerCase}`, `${operationTypeUpperCase}`, `${operationTypeKebabCase}`, `${operationTypePascalCase}`, `${operationTypeCamelCase}`, `${resultLowerCase}`, `${resultUpperCase}`, `${resultKebabCase}`, `${resultPascalCase}`, `${resultCamelCase}`

### Custom Headers

Custom headers are added as Event Hubs application properties on each event:

```bash
kete.routes.eh.destination.headers.environment=production
kete.routes.eh.destination.headers.source=keycloak
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
kete.routes.eh.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
kete.routes.eh.destination.event-hub=keycloak-events
```

The Event Hub name can be embedded in the connection string as `EntityPath=<name>` instead of being set separately.

#### Explicit Connection String

```bash
kete.routes.eh.destination.authentication-type=connection-string
kete.routes.eh.destination.connection-string=Endpoint=sb://my-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=your-key
kete.routes.eh.destination.event-hub=keycloak-events
```

#### Managed Identity

```bash
kete.routes.eh.destination.authentication-type=managed-identity
kete.routes.eh.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
kete.routes.eh.destination.event-hub=keycloak-events

# Optional: specify a user-assigned managed identity
kete.routes.eh.destination.managed-identity-client-id=your-client-id
```

| Property | Default | Description |
|----------|---------|-------------|
| `destination.authentication-type` | _(empty)_ | Set to `managed-identity` |
| `destination.fully-qualified-namespace` | _(empty)_ | Event Hubs namespace FQDN (required) |
| `destination.event-hub` | _(empty)_ | Event Hub name (required) |
| `destination.managed-identity-client-id` | _(empty)_ | Client ID for user-assigned managed identity. Omit for system-assigned. |

#### Default Azure Credential

```bash
kete.routes.eh.destination.authentication-type=default-azure-credential
kete.routes.eh.destination.fully-qualified-namespace=my-namespace.servicebus.windows.net
kete.routes.eh.destination.event-hub=keycloak-events
```

### TLS Properties

Azure Event Hubs uses AMQPS (port 5671) by default, which is TLS-encrypted. No explicit TLS configuration is needed.

!!! note "Azure Event Hubs TLS"
    TLS is handled automatically by the Azure SDK's AMQP transport layer. The `tls.*` properties are not used for Event Hubs connections.



## Configuration Examples

### Example 1: Production Setup

```bash
kete.routes.prod.destination.kind=azure-eventhubs
kete.routes.prod.realm-matchers.realm=list:master
kete.routes.prod.event-matchers.filter=glob:*
kete.routes.prod.destination.connection-string=Endpoint=sb://prod-namespace.servicebus.windows.net/;SharedAccessKeyName=SendPolicy;SharedAccessKey=your-key
kete.routes.prod.destination.event-hub=keycloak-events
kete.routes.prod.destination.partition-key=${realmLowerCase}
kete.routes.prod.destination.timeout-seconds=30
```

### Example 2: Managed Identity in Azure

```bash
kete.routes.azure.destination.kind=azure-eventhubs
kete.routes.azure.realm-matchers.realm=list:master
kete.routes.azure.event-matchers.filter=glob:*
kete.routes.azure.destination.authentication-type=managed-identity
kete.routes.azure.destination.fully-qualified-namespace=prod-namespace.servicebus.windows.net
kete.routes.azure.destination.event-hub=keycloak-events
```

### Example 3: Emulator with Custom Endpoint

```bash
kete.routes.local.destination.kind=azure-eventhubs
kete.routes.local.destination.connection-string=Endpoint=sb://emulator;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;
kete.routes.local.destination.event-hub=keycloak-events
kete.routes.local.destination.custom-endpoint-address=http://eventhubs-emulator:5672
```

### Example 4: Connection String from Environment Variable

```bash
kete.routes.env.destination.kind=azure-eventhubs
kete.routes.env.realm-matchers.realm=list:master
kete.routes.env.event-matchers.filter=glob:*
kete.routes.env.destination.connection-string=${EVENTHUBS_CONNECTION_STRING}
kete.routes.env.destination.event-hub=keycloak-events
```



## Quick Starts

No dedicated quick start available for the native `azure-eventhubs` destination.

Azure Event Hubs can also be accessed via the [Kafka Destination](kafka.md) or [AMQP 1 Destination](amqp-1.md):

| Quick Start | Protocol | Description |
|-------------|----------|-------------|
| [kafka-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs) | Kafka | Azure Event Hubs via Kafka protocol (cloud) |
| [kafka-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/kafka-azure-event-hubs-emulator) | Kafka | Azure Event Hubs Emulator via Kafka protocol (local) |
| [amqp-1-azure-event-hubs](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs) | AMQP 1.0 | Azure Event Hubs via AMQP 1.0 (cloud) |
| [amqp-1-azure-event-hubs-emulator](https://github.com/FortuneN/kete/tree/release/quick-starts/amqp-1-azure-event-hubs-emulator) | AMQP 1.0 | Azure Event Hubs Emulator via AMQP 1.0 (local) |



## See Also

- [Kafka Destination](kafka.md) - Access Event Hubs via Kafka protocol
- [AMQP 1 Destination](amqp-1.md) - Access Event Hubs via AMQP 1.0
- [Serializers](../serializers/overview.md) - Choose JSON, YAML, CBOR, Properties, etc.
- [Matchers](../matchers/overview.md) - Filter events by realm, type, resource, operation
- [Event Types](../event-types.md) - List of all event types
- [Certificate Loaders](../certificate-loaders/overview.md) - For TLS configuration
