# DER File Base64

Load Base64-encoded DER content.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `der-file-base64` |
| `loader.base64` | ✓ | Base64-encoded DER content |

## Notes

- DER is the binary form of PEM (same data, different encoding)
- Contains a single certificate (no private keys)
- Common extensions: `.der`, `.cer`
- Trust stores only — DER cannot hold private keys, so it cannot be used as a key store for mTLS

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificate)
kete.routes.myroute.destination.tls.trust-store.loader.kind=der-file-base64
kete.routes.myroute.destination.tls.trust-store.loader.base64=MIIDdzCCAl+gAwIBAgIE...
```
