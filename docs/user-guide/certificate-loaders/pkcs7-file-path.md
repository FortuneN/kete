# PKCS7 File Path

Load a certificate chain from a PKCS#7 file on the filesystem.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pkcs7-file-path` |
| `loader.path` | ✓ | Absolute path to .p7b or .p7c file |

## Notes

- **Trust stores only** — PKCS#7 cannot contain private keys
- Common extensions: `.p7b`, `.p7c`
- Often used for CA certificate chains from enterprise PKI

## Example

```bash
kete.routes.myroute.destination.tls=true

# Trust store only (PKCS#7 cannot contain private keys)
kete.routes.myroute.destination.trust-store.loader.kind=pkcs7-file-path
kete.routes.myroute.destination.trust-store.loader.path=/certs/ca-chain.p7b
```
