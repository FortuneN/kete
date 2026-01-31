# DER File Base64

Load Base64-encoded DER content.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `der-file-base64` |
| `loader.base64` | ✓ | Base64-encoded DER content |

## Notes

- DER is the binary form of PEM (same data, different encoding)
- Contains a single certificate
- Common extensions: `.der`, `.cer`

## Example

```bash
kete.routes.myroute.destination.tls=true

# Trust store (CA certificate)
kete.routes.myroute.destination.trust-store.loader.kind=der-file-base64
kete.routes.myroute.destination.trust-store.loader.base64=MIIDdzCCAl+gAwIBAgIE...

# Key store (client certificate)
kete.routes.myroute.destination.key-store.loader.kind=der-file-base64
kete.routes.myroute.destination.key-store.loader.base64=MIIEvgIBADANBgkqhkiG...
```
