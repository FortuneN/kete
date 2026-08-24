# Template Serializer

Compose custom message bodies using `${...}` placeholders resolved against raw Keycloak event properties.

## Configuration

```bash
kete.routes.<name>.serializer.kind=template
kete.routes.<name>.serializer.template-file-text=User ${userId} logged in from ${ipAddress}
```

### Required Properties

| Property | Description |
|----------|-------------|
| `kind` | `template` |
| **One of:**  | |
| `template-file-text` | Inline template string |
| `template-file-base64` | Base64-encoded template |
| `template-file-path` | Path to template file on disk |

Exactly **one** template source must be set. This follows the same triple-source pattern used by TLS certificates and credentials throughout KETE.

### Optional Properties

| Property | Default | Description |
|----------|---------|-------------|
| `content-type` | `text/plain` | MIME type sent as the `contenttype` header |
| `variable-escape` | `none` | Global escape mode applied to all variables |

### Template Variables

**For User Events (`Event`):**

| Variable | Source | Example |
|----------|--------|---------|
| `${id}` | Event ID | `evt-abc-123` |
| `${type}` | Event type name | `LOGIN` |
| `${realmId}` | Realm ID | `my-realm` |
| `${realmName}` | Realm name | `my-realm` |
| `${clientId}` | Client ID | `my-app` |
| `${userId}` | User ID | `abc-123-def` |
| `${sessionId}` | Session ID | `sess-456` |
| `${ipAddress}` | IP address | `192.168.1.1` |
| `${error}` | Error string (if any) | `invalid_user_credentials` |
| `${time}` | Epoch milliseconds | `1707500000000` |
| `${details.<key>}` | Event detail by key | `${details.username}` → `john` |

Event detail keys support both raw (`${details.redirect_uri}`) and camelCase (`${details.redirectUri}`) forms.

**For Admin Events (`AdminEvent`):**

| Variable | Source | Example |
|----------|--------|---------|
| `${id}` | Event ID | `adm-abc-123` |
| `${operationType}` | Operation type name | `CREATE` |
| `${resourceType}` | Resource type (raw string) | `USER` |
| `${realmId}` | Realm ID | `my-realm` |
| `${realmName}` | Realm name | `my-realm` |
| `${resourcePath}` | Resource path | `users/abc-123` |
| `${representation}` | JSON representation | `{"username":"john"}` |
| `${error}` | Error string (if any) | |
| `${time}` | Epoch milliseconds | `1707500000000` |
| `${authDetails.realmId}` | Auth realm | `master` |
| `${authDetails.realmName}` | Authenticating realm name | `master` |
| `${authDetails.clientId}` | Auth client | `admin-cli` |
| `${authDetails.userId}` | Auth user | `admin-uuid` |
| `${authDetails.ipAddress}` | Auth IP | `10.0.0.1` |

**Undefined variables** resolve to an empty string.

### Variable Escaping

Set a global escape mode with `variable-escape`, then override individual variables with the `:filter` suffix:

| Filter | Aliases | Description |
|--------|---------|-------------|
| `none` | | Raw passthrough (default) |
| `json` | `json-encode` | JSON string escaping (`"` → `\"`, `\n` → `\\n`, etc.) |
| `xml` | `xml-encode` | XML entity escaping (`<` → `&lt;`, `&` → `&amp;`, etc.) |
| `url` | `url-encode` | URL percent-encoding |
| `csv` | `csv-quote` | CSV quoting (wraps in quotes if value contains `,`, `"`, or newlines) |
| `b64` | `base64` | Base64 encoding |

Unrecognized filter names are treated as `none` (raw passthrough).

**Per-variable override:**

```bash
# Global: JSON-escape all variables
kete.routes.myroute.serializer.variable-escape=json

# Template: ${time:none} opts out of JSON escaping for this variable
kete.routes.myroute.serializer.template-file-text={"user": "${userId}", "time": ${time:none}}
```

## Example Output

### SMS Alert (plain text)

**Config:**
```bash
kete.routes.sms.serializer.kind=template
kete.routes.sms.serializer.template-file-text=ALERT: ${userId} ${type} from ${ipAddress} in ${realmId}
```

**Output:**
```
ALERT: abc-123-def LOGIN from 192.168.1.1 in production
```

### Slack Webhook (JSON payload)

**Config:**
```bash
kete.routes.slack.serializer.kind=template
kete.routes.slack.serializer.content-type=application/json
kete.routes.slack.serializer.variable-escape=json
kete.routes.slack.serializer.template-file-text={"text": "User ${userId} performed ${type} in ${realmId}"}
```

**Output:**
```json
{"text": "User abc-123-def performed LOGIN in production"}
```

### Admin Alert (HTML)

**Config:**
```bash
kete.routes.admin.serializer.kind=template
kete.routes.admin.serializer.content-type=text/html
kete.routes.admin.serializer.variable-escape=xml
kete.routes.admin.serializer.template-file-text=<h2>Admin Action</h2><p><b>${authDetails.userId}</b> performed <b>${operationType}</b> on <b>${resourceType}</b></p><p>Path: ${resourcePath}</p>
```

**Output:**
```html
<h2>Admin Action</h2><p><b>admin-uuid</b> performed <b>CREATE</b> on <b>USER</b></p><p>Path: users/abc-123</p>
```

### Lightweight IoT (pipe-delimited)

**Config:**
```bash
kete.routes.iot.serializer.kind=template
kete.routes.iot.serializer.template-file-text=${type}|${userId}|${realmId}
```

**Output:**
```
LOGIN|abc-123-def|production
```
