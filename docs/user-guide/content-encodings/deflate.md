# Deflate Content Encoding

Compress payloads using the DEFLATE algorithm (RFC 1951).

## Configuration

```bash
kete.routes.<name>.destination.content-encoding=deflate
```

## Details

| Property | Value |
|----------|-------|
| Config | `deflate` |
| Algorithm | DEFLATE (RFC 1951) |
| Java class | `java.util.zip.DeflaterOutputStream` |

## Notes

- Slightly faster and smaller output than gzip (no gzip header/trailer)
- Less universally supported than gzip in HTTP tooling
- Consumers decompress with any standard inflate/deflate library

## Example

```bash
kete.routes.compressed.destination.kind=http
kete.routes.compressed.destination.url=https://example.com/events
kete.routes.compressed.destination.content-encoding=deflate
```
