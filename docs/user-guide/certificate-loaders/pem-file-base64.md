# PEM File Base64

Load Base64-encoded PEM content.

| Property | Required | Description |
|----------|:--------:|-------------|
| `loader.kind` | ✓ | `pem-file-base64` |
| `loader.base64` | ✓ | Base64-encoded PEM content |

## Notes

- Supports certificates and private keys. For key stores the PEM must contain the private key **and** its certificate chain together; a key without a certificate is ignored
- Private keys must be unencrypted (PKCS#8 `BEGIN PRIVATE KEY` or PKCS#1 `BEGIN RSA PRIVATE KEY`); encrypted keys are rejected
- Multiple certificates can be concatenated
- Common extensions: `.pem`, `.crt`, `.cer`, `.key`

## Example

```bash
kete.routes.myroute.destination.tls.enabled=true

# Trust store (CA certificates)
kete.routes.myroute.destination.tls.trust-store.loader.kind=pem-file-base64
kete.routes.myroute.destination.tls.trust-store.loader.base64=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0t...

# Key store (client certificate + private key)
kete.routes.myroute.destination.tls.key-store.loader.kind=pem-file-base64
kete.routes.myroute.destination.tls.key-store.loader.base64=LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0t...
```
