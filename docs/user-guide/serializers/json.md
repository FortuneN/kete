# JSON Serializer

The default serializer. Human-readable, universally supported.

## Configuration

```bash
kete.routes.<name>.serializer.kind=json
```

## Example Output

### User Event

```json
{
  "id": "a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f",
  "time": 1704816000000,
  "type": "LOGIN",
  "realmId": "master",
  "realmName": "master",
  "clientId": "my-app",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "abc-session-123",
  "ipAddress": "192.168.1.100",
  "error": null,
  "details": {
    "username": "john.doe",
    "remember_me": "true"
  }
}
```

### Admin Event

```json
{
  "id": "b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e",
  "time": 1704816000000,
  "realmId": "master",
  "realmName": "master",
  "authDetails": {
    "realmId": "master",
    "realmName": "master",
    "clientId": "admin-cli",
    "userId": "admin-uuid",
    "ipAddress": "192.168.1.100"
  },
  "resourceType": "USER",
  "operationType": "CREATE",
  "resourcePath": "users/user-uuid",
  "representation": "{\"username\":\"newuser\"}",
  "error": null,
  "resourceTypeAsString": "USER"
}
```

## Schemas

JSON Schema (draft 2020-12) files describing both message shapes are in the [`schemas/json/`](https://github.com/FortuneN/kete/tree/develop/schemas/json) directory. Use them to validate or generate types for messages on the consumer side (e.g. `ajv`, `jsonschema`, `json-schema-validator`). The `eventkind` message header (`x-eventkind` for HTTP and SOAP) tells you which schema applies: `EVENT` or `ADMIN_EVENT`.

### Event

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://raw.githubusercontent.com/FortuneN/kete/develop/schemas/json/event.json",
  "title": "Event",
  "description": "Keycloak user event as produced by the KETE JSON serializer (message header eventkind = EVENT; x-eventkind for HTTP and SOAP)",
  "type": "object",
  "properties": {
    "id": {
      "type": ["string", "null"],
      "description": "Unique event identifier (UUID)"
    },
    "time": {
      "type": "integer",
      "description": "Event timestamp in milliseconds since Unix epoch (0 when unset)"
    },
    "type": {
      "type": ["string", "null"],
      "description": "Event type name (e.g. LOGIN, LOGOUT, REGISTER, CODE_TO_TOKEN, REFRESH_TOKEN, …)"
    },
    "realmId": {
      "type": ["string", "null"],
      "description": "Keycloak realm ID"
    },
    "realmName": {
      "type": ["string", "null"],
      "description": "Keycloak realm name"
    },
    "clientId": {
      "type": ["string", "null"],
      "description": "OAuth2/OIDC client ID that triggered the event"
    },
    "userId": {
      "type": ["string", "null"],
      "description": "Keycloak user ID (UUID)"
    },
    "sessionId": {
      "type": ["string", "null"],
      "description": "Keycloak session ID"
    },
    "ipAddress": {
      "type": ["string", "null"],
      "description": "IP address of the client"
    },
    "error": {
      "type": ["string", "null"],
      "description": "Error code when the event represents a failure (null on success)"
    },
    "details": {
      "type": ["object", "null"],
      "description": "Arbitrary key-value pairs with extra context (e.g. username, redirect_uri, response_type)",
      "additionalProperties": {
        "type": "string"
      }
    }
  },
  "required": ["id", "time", "type", "realmId", "realmName", "clientId", "userId", "sessionId", "ipAddress", "error", "details"]
}
```

### Admin Event

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://raw.githubusercontent.com/FortuneN/kete/develop/schemas/json/admin_event.json",
  "title": "AdminEvent",
  "description": "Keycloak admin event as produced by the KETE JSON serializer (message header eventkind = ADMIN_EVENT; x-eventkind for HTTP and SOAP)",
  "type": "object",
  "properties": {
    "id": {
      "type": ["string", "null"],
      "description": "Unique event identifier (UUID)"
    },
    "time": {
      "type": "integer",
      "description": "Event timestamp in milliseconds since Unix epoch (0 when unset)"
    },
    "realmId": {
      "type": ["string", "null"],
      "description": "Keycloak realm ID"
    },
    "realmName": {
      "type": ["string", "null"],
      "description": "Keycloak realm name"
    },
    "authDetails": {
      "description": "Details about the admin who performed the action",
      "oneOf": [
        { "type": "null" },
        { "$ref": "#/$defs/AuthDetails" }
      ]
    },
    "resourceType": {
      "type": ["string", "null"],
      "description": "Resource type name (e.g. USER, CLIENT, REALM, ROLE, GROUP, …); CUSTOM when Keycloak does not recognise the raw value — see resourceTypeAsString"
    },
    "resourceTypeAsString": {
      "type": ["string", "null"],
      "description": "Raw resource type string as recorded by Keycloak (same as resourceType unless resourceType is CUSTOM)"
    },
    "operationType": {
      "type": ["string", "null"],
      "description": "Operation performed (CREATE, UPDATE, DELETE, ACTION)"
    },
    "resourcePath": {
      "type": ["string", "null"],
      "description": "Path to the affected resource within the realm (e.g. users/uuid)"
    },
    "representation": {
      "type": ["string", "null"],
      "description": "JSON representation of the resource state after the operation (only when include-representation is enabled)"
    },
    "error": {
      "type": ["string", "null"],
      "description": "Error code when the event represents a failure (null on success)"
    },
    "details": {
      "type": ["object", "null"],
      "description": "Arbitrary key-value pairs with extra context; only present when KETE runs on Keycloak 26.0 or later",
      "additionalProperties": {
        "type": "string"
      }
    },
    "resourceId": {
      "type": ["string", "null"],
      "description": "Segment after the last '/' of resourcePath; only present when KETE runs on Keycloak 26.4 or later"
    }
  },
  "required": ["id", "time", "realmId", "realmName", "authDetails", "resourceType", "resourceTypeAsString", "operationType", "resourcePath", "representation", "error"],
  "$defs": {
    "AuthDetails": {
      "title": "AuthDetails",
      "description": "Identity of the admin who triggered the event",
      "type": "object",
      "properties": {
        "realmId": {
          "type": ["string", "null"],
          "description": "Realm ID of the authenticating admin"
        },
        "realmName": {
          "type": ["string", "null"],
          "description": "Realm name of the authenticating admin"
        },
        "clientId": {
          "type": ["string", "null"],
          "description": "Client ID used by the admin (e.g. security-admin-console, admin-cli)"
        },
        "userId": {
          "type": ["string", "null"],
          "description": "Keycloak user ID of the admin (UUID)"
        },
        "ipAddress": {
          "type": ["string", "null"],
          "description": "IP address of the admin client"
        }
      },
      "required": ["realmId", "realmName", "clientId", "userId", "ipAddress"]
    }
  }
}
```

## Null Fields

Every property is always present. Unset fields serialize as `null` (`time` is a primitive and serializes as `0` when unset), so consumers can distinguish "not set" from "empty string".

## Keycloak Version Differences

Messages are serialized straight from Keycloak's own `Event` and `AdminEvent` classes, so the exact set of properties depends on the Keycloak version KETE runs in:

| Property | Message | Available since |
|----------|---------|-----------------|
| `details` | Admin Event | Keycloak 26.0 |
| `resourceId` | Admin Event | Keycloak 26.4 |

The schemas describe these as optional properties. Consumers should ignore properties they do not recognise so that future Keycloak versions do not break them.
