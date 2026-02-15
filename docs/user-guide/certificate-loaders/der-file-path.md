# DER File Path

Load a certificate from a DER file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `der-file-path` |
| `loader.path` | ✓ | Absolute path to DER file |

## Notes

- DER is the binary form of PEM (same data, different encoding)
- Contains a single certificate (no private keys)
- Common extensions: `.der`, `.cer`
- Trust stores only — DER cannot hold private keys, so it cannot be used as a key store for mTLS

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificate)
kete.routes.myroute.destination.tls.trust-store.loader.kind=der-file-path
kete.routes.myroute.destination.tls.trust-store.loader.path=/certs/ca.der
```
