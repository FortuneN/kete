# Content Transfer Encodings

Encode event payloads for safe transport over text-based protocols.

## Overview

Content transfer encodings transform binary data into a text-safe representation. They're **optional** — without a content transfer encoding, the payload is sent as-is.

Content transfer encoding runs **after** both serialization and content encoding.

## Available Content Transfer Encodings

| Encoding | Config | Description |
|----------|--------|-------------|
| **[Base64](base64.md)** | `base64` | RFC 4648 Base64 encoding |

## Quick Example

**Base64 encode for text-only transports:**
```bash
kete.routes.encoded.destination.content-transfer-encoding=base64
```

## When to Use

Content transfer encoding is useful when:

- The destination only accepts text (e.g., AWS SQS message bodies)
- You're using a binary serializer (Protobuf, Avro, CBOR) over a text-based protocol
- You're combining with content encoding (gzip + base64)

## Processing Order

The encoding pipeline runs in this order:

1. **Serialization** — event → bytes (e.g., JSON, Protobuf)
2. **Content Encoding** — compress bytes (e.g., gzip)
3. **Content Transfer Encoding** — encode bytes (e.g., base64)

## See Also

- [Serializers](../serializers/overview.md) — format events before encoding
- [Content Encodings](../content-encodings/overview.md) — compress before transfer encoding
- [Destinations](../destinations/overview.md) — where events are sent
