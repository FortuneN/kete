# Deflate Content Encoding

Compress payloads using zlib-wrapped DEFLATE (RFC 1950 framing around RFC 1951 data) — the format HTTP `Content-Encoding: deflate` expects.

## Configuration

```bash
kete.routes.<name>.destination.content-encoding=deflate
```

## Details

| Property | Value |
|----------|-------|
| Config | `deflate` |
| Algorithm | zlib (RFC 1950) wrapping DEFLATE (RFC 1951) |
| Java class | `java.util.zip.DeflaterOutputStream` |

## Notes

- Slightly smaller output than gzip: a 2-byte zlib header and 4-byte Adler-32 trailer instead of gzip's 10-byte header and 8-byte trailer
- HTTP and SOAP destinations also send a `Content-Encoding: deflate` request header
- Less universally supported than gzip in HTTP tooling
- Consumers decompress with any standard inflate/deflate library

## Example

```bash
kete.routes.compressed.destination.kind=http
kete.routes.compressed.destination.url=https://example.com/events
kete.routes.compressed.destination.content-encoding=deflate
```
