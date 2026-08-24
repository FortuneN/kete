# Content Encodings

Compress event payloads before sending to a destination.

## Overview

Content encodings apply compression to the serialized event body. They're **optional** — without a content encoding, the payload is sent uncompressed.

Content encoding runs **after** serialization but **before** content transfer encoding.


## Supported Destinations

Encodings are applied by the destinations that carry an opaque payload over HTTP-style or SDK transports: `http`, `soap`, `grpc`, `aws-eventbridge`, `aws-sns`, `aws-sqs`, `azure-eventgrid`, `azure-storage-queue`, `azure-webpubsub` and `gcp-cloud-tasks`. All other destinations accept the property but do not apply it (the payload is sent unencoded).

## Available Content Encodings

| Encoding | Config | Algorithm |
|----------|--------|-----------|
| **[Gzip](gzip.md)** | `gzip` | RFC 1952 (GZIP) |
| **[Deflate](deflate.md)** | `deflate` | RFC 1951 (DEFLATE) |

## Quick Examples

**Gzip compression:**
```bash
kete.routes.compressed.destination.content-encoding=gzip
```

**Deflate compression:**
```bash
kete.routes.compressed.destination.content-encoding=deflate
```

## Processing Order

The encoding pipeline runs in this order:

1. **Serialization** — event → bytes (e.g., JSON, Protobuf)
2. **Content Encoding** — compress bytes (e.g., gzip)
3. **Content Transfer Encoding** — encode bytes (e.g., base64)

## See Also

- [Serializers](../serializers/overview.md) — format events before encoding
- [Content Transfer Encodings](../content-transfer-encodings/overview.md) — encode after compression
- [Destinations](../destinations/overview.md) — where events are sent
