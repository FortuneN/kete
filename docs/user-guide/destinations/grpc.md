# gRPC Destination

Stream Keycloak events to gRPC services.

| Property | Value |
|----------|-------|
| **`destination.kind`** | `grpc` |
| **Protocol** | gRPC (HTTP/2) |



## Compatible Systems

| System | Notes |
|--------|-------|
| **Any gRPC Server** | Any unary RPC endpoint accepting raw bytes |
| **gRPC Microservices** | Go, Java, Python, C#, Node.js, Rust, etc. |
| **Envoy Proxy** | gRPC load balancing and routing |
| **gRPC-Web** | Browser-compatible gRPC via proxy |



## Example Configurations

=== "Plaintext (Development)"

    ```bash
    kete.routes.grpc.destination.kind=grpc
    kete.routes.grpc.destination.host=grpc-server
    kete.routes.grpc.destination.port=50051
    kete.routes.grpc.destination.service=EventService
    kete.routes.grpc.destination.method=SendEvent
    kete.routes.grpc.destination.use-plaintext=true
    ```

=== "TLS (Production)"

    ```bash
    kete.routes.grpc.destination.kind=grpc
    kete.routes.grpc.destination.host=grpc.example.com
    kete.routes.grpc.destination.port=443
    kete.routes.grpc.destination.service=EventService
    kete.routes.grpc.destination.method=SendEvent
    kete.routes.grpc.destination.tls.enabled=true
    kete.routes.grpc.destination.tls.trust-store.loader.kind=pem-file-path
    kete.routes.grpc.destination.tls.trust-store.loader.path=/certs/ca.pem
    ```

=== "With Encoding"

    ```bash
    kete.routes.grpc.destination.kind=grpc
    kete.routes.grpc.destination.host=grpc-server
    kete.routes.grpc.destination.service=EventService
    kete.routes.grpc.destination.method=SendEvent
    kete.routes.grpc.destination.use-plaintext=true
    kete.routes.grpc.destination.content-encoding=gzip
    ```



## Features

- Unary RPC calls with raw byte payload (identity marshaller)
- Event metadata sent as gRPC metadata headers (`eventkind`, `eventtype`, `contenttype`)
- Custom metadata headers support via `destination.headers.*`
- Configurable service name and method name
- Plaintext or TLS/mTLS connections via OkHttp transport
- Content encoding (gzip) and content transfer encoding (base64)
- Configurable call timeout
- Connection verification during initialization



## Configuration Properties

### Required Properties

| Property | Description | Example |
|----------|-------------|---------|
| `destination.kind` | Must be `grpc` | `grpc` |
| `destination.host` | gRPC server hostname | `grpc-server.example.com` |
| `destination.service` | gRPC service name | `EventService` |
| `destination.method` | gRPC method name | `SendEvent` |

### Optional Properties

| Property | Default | Description | Example |
|----------|---------|-------------|---------|
| `destination.port` | `50051` | gRPC server port | `443` |
| `destination.timeout-seconds` | `10` | Call timeout in seconds | `30` |
| `destination.use-plaintext` | `false` | Use plaintext (no TLS) | `true` |
| `destination.content-encoding` | _(empty)_ | Compress body (e.g., `gzip`) | `gzip` |
| `destination.content-transfer-encoding` | _(empty)_ | Encode body (e.g., `base64`) | `base64` |

### Custom Headers (Metadata)

Custom gRPC metadata headers are configured under `destination.headers.<NAME>`:

```bash
kete.routes.grpc.destination.headers.x-source=keycloak
kete.routes.grpc.destination.headers.x-environment=production
```

These are sent as gRPC `Metadata` keys alongside the automatic `eventkind`, `eventtype`, and `contenttype` metadata entries.

### TLS Properties

See [TLS & mTLS](overview.md#tls-mtls) for full details on TLS options.

| Property | Default | Description |
|----------|---------|-------------|
| `destination.tls.enabled` | `false` | Enable TLS |
| `destination.tls.key-store.*` | - | Client certificate for mTLS |
| `destination.tls.trust-store.*` | - | CA certificates |

!!! note "Plaintext Mode"
    Set `destination.use-plaintext=true` for development or when connecting to services without TLS (e.g., local gRPC servers, emulators). When `use-plaintext` is `false` (default), TLS is used and you may need to configure trust-store properties.



## How It Works

The gRPC destination uses an **identity marshaller** — the serialized event bytes are sent as-is without any protobuf wrapping. The gRPC server receives raw bytes and can deserialize them according to the serializer used (JSON, XML, Protobuf, etc.).

The full gRPC method name is constructed from the service and method properties:

```
# service=EventService, method=SendEvent
# → Full method: EventService/SendEvent

# service=com.example.EventService, method=SendEvent
# → Full method: com.example.EventService/SendEvent
```

### Connection Verification

During initialization, KETE sends a verification call to confirm the gRPC server is reachable. Connection errors (`UNAVAILABLE`, `DEADLINE_EXCEEDED`) cause initialization to fail. Other gRPC status codes (e.g., `UNIMPLEMENTED`, `INTERNAL`) are treated as proof the server is reachable.



## Configuration Examples

### Example 1: Local Development

```bash
kete.routes.events.realm-matchers.realm=list:master
kete.routes.events.destination.kind=grpc
kete.routes.events.destination.host=localhost
kete.routes.events.destination.port=50051
kete.routes.events.destination.service=EventService
kete.routes.events.destination.method=SendEvent
kete.routes.events.destination.use-plaintext=true
kete.routes.events.event-matchers.filter=glob:*
```

### Example 2: Production with TLS

```bash
kete.routes.events.destination.kind=grpc
kete.routes.events.destination.host=grpc.example.com
kete.routes.events.destination.port=443
kete.routes.events.destination.service=com.example.events.EventService
kete.routes.events.destination.method=IngestEvent
kete.routes.events.destination.timeout-seconds=30
kete.routes.events.destination.tls.enabled=true
kete.routes.events.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.events.destination.tls.trust-store.loader.path=/certs/ca.pem
```

### Example 3: mTLS with Custom Metadata

```bash
kete.routes.events.destination.kind=grpc
kete.routes.events.destination.host=grpc.internal.example.com
kete.routes.events.destination.port=443
kete.routes.events.destination.service=EventService
kete.routes.events.destination.method=SendEvent
kete.routes.events.destination.headers.x-tenant=keycloak
kete.routes.events.destination.headers.x-source=iam
kete.routes.events.destination.tls.enabled=true
kete.routes.events.destination.tls.trust-store.loader.kind=pem-file-path
kete.routes.events.destination.tls.trust-store.loader.path=/certs/ca.pem
kete.routes.events.destination.tls.key-store.loader.kind=pkcs12-file-path
kete.routes.events.destination.tls.key-store.loader.path=/certs/client.p12
kete.routes.events.destination.tls.key-store.password=changeit
```



## Quick Starts

| Target | Quick Start |
|--------|-------------|
| gRPC Echo Server | [grpc](https://github.com/FortuneN/kete/tree/release/quick-starts/grpc/) |



## See Also

- [Serializers](../serializers/overview.md) — Format events before sending
- [Matchers](../matchers/overview.md) — Filter which events are forwarded
- [Event Types](../event-types.md) — Available event types
- [Certificate Loaders](../certificate-loaders/overview.md) — TLS certificate formats
- [GCP Cloud Tasks](gcp-cloud-tasks.md) — Another gRPC-based destination (via SDK)
