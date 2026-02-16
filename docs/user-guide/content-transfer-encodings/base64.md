# Base64 Content Transfer Encoding

Encode payloads using Base64 (RFC 4648) for safe transport over text-based protocols.

## Configuration

```bash
kete.routes.<name>.destination.content-transfer-encoding=base64
```

## Details

| Property | Value |
|----------|-------|
| Config | `base64` |
| Standard | RFC 4648 |
| Java class | `java.util.Base64` |

## Notes

- Increases payload size by ~33%
- Essential for binary payloads (Protobuf, Avro, CBOR, Smile) over text-only transports
- Consumers decode with any standard Base64 library
- Often combined with gzip content encoding to offset the size increase

## Examples

**Binary serializer over text transport:**
```bash
kete.routes.sqs.destination.kind=aws-sqs
kete.routes.sqs.serializer.kind=protobuf
kete.routes.sqs.destination.content-transfer-encoding=base64
```

**Gzip + Base64 combination:**
```bash
kete.routes.compressed.destination.kind=http
kete.routes.compressed.destination.url=https://example.com/events
kete.routes.compressed.destination.content-encoding=gzip
kete.routes.compressed.destination.content-transfer-encoding=base64
```
