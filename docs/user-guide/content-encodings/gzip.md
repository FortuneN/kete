# Gzip Content Encoding

Compress payloads using the GZIP algorithm (RFC 1952).

## Configuration

```bash
kete.routes.<name>.destination.content-encoding=gzip
```

## Details

| Property | Value |
|----------|-------|
| Config | `gzip` |
| Algorithm | GZIP (RFC 1952) |
| Java class | `java.util.zip.GZIPOutputStream` |

## Notes

- Widely supported by HTTP servers and clients
- Good compression ratio for text-based formats (JSON, XML, YAML)
- Adds a small overhead for already-compact binary formats (Protobuf, Avro, CBOR)
- Consumers decompress with any standard gzip library

## Example

```bash
kete.routes.compressed.destination.kind=http
kete.routes.compressed.destination.url=https://example.com/events
kete.routes.compressed.destination.content-encoding=gzip
```
