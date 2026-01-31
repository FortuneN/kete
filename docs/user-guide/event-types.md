# Event Types Reference

Quick reference for Keycloak event types. 

For authoritative details, see the Keycloak Javadocs:

[EventType](https://www.keycloak.org/docs-api/latest/javadocs/org/keycloak/events/EventType.html)

[OperationType](https://www.keycloak.org/docs-api/latest/javadocs/org/keycloak/events/admin/OperationType.html)

[ResourceType](https://www.keycloak.org/docs-api/latest/javadocs/org/keycloak/events/admin/ResourceType.html)



## Event Kinds

Keycloak emits two kinds of events:

| Kind | Template Variable | Description | Event Type Format |
|------|-------------------|-------------|-------------------|
| **User Events** | `event` | Authentication and account events | Single word: `LOGIN`, `LOGOUT`, `REGISTER` |
| **Admin Events** | `admin-event` | Administrative operations via Admin Console/API | Combined: `USER_CREATE`, `CLIENT_UPDATE` |

Use the `${kindLowerCase}` or `${kindUpperCase}` template variables to route events by kind.



## User Events

### Authentication

| Event Type | Triggered When |
|------------|----------------|
| `LOGIN` | User successfully logs in |
| `LOGIN_ERROR` | Login attempt fails |
| `LOGOUT` | User logs out |
| `LOGOUT_ERROR` | Logout fails |

### Token

| Event Type | Triggered When |
|------------|----------------|
| `CODE_TO_TOKEN` | Authorization code exchanged for token |
| `CODE_TO_TOKEN_ERROR` | Authorization code exchange fails |
| `REFRESH_TOKEN` | Access token refreshed |
| `REFRESH_TOKEN_ERROR` | Token refresh fails |
| `INTROSPECT_TOKEN` | Token introspection requested |
| `INTROSPECT_TOKEN_ERROR` | Token introspection fails |
| `VALIDATE_ACCESS_TOKEN` | Access token validated |
| `VALIDATE_ACCESS_TOKEN_ERROR` | Access token validation fails |
| `REVOKE_GRANT` | User revokes consent/permissions |
| `REVOKE_GRANT_ERROR` | Grant revocation fails |

### Registration

| Event Type | Triggered When |
|------------|----------------|
| `REGISTER` | New user registers |
| `REGISTER_ERROR` | Registration fails |
| `VERIFY_EMAIL` | User verifies email address |
| `VERIFY_EMAIL_ERROR` | Email verification fails |
| `SEND_VERIFY_EMAIL` | Verification email sent |
| `SEND_VERIFY_EMAIL_ERROR` | Verification email fails |

### Account Management

| Event Type | Triggered When |
|------------|----------------|
| `UPDATE_EMAIL` | User changes email |
| `UPDATE_EMAIL_ERROR` | Email update fails |
| `UPDATE_PASSWORD` | User changes password |
| `UPDATE_PASSWORD_ERROR` | Password change fails |
| `UPDATE_PROFILE` | User updates profile |
| `UPDATE_PROFILE_ERROR` | Profile update fails |
| `UPDATE_TOTP` | User configures TOTP/MFA |
| `UPDATE_TOTP_ERROR` | TOTP setup fails |
| `REMOVE_TOTP` | User removes TOTP/MFA |
| `REMOVE_TOTP_ERROR` | TOTP removal fails |

### Password Recovery

| Event Type | Triggered When |
|------------|----------------|
| `SEND_RESET_PASSWORD` | Password reset email sent |
| `SEND_RESET_PASSWORD_ERROR` | Reset email fails |
| `RESET_PASSWORD` | User resets password |
| `RESET_PASSWORD_ERROR` | Password reset fails |

### Credential

| Event Type | Triggered When |
|------------|----------------|
| `VERIFY_PROFILE` | User profile verified |
| `VERIFY_PROFILE_ERROR` | Profile verification fails |
| `UPDATE_CONSENT` | User updates consent |
| `UPDATE_CONSENT_ERROR` | Consent update fails |
| `GRANT_CONSENT` | User grants consent |
| `GRANT_CONSENT_ERROR` | Consent grant fails |

### Federation

| Event Type | Triggered When |
|------------|----------------|
| `FEDERATED_IDENTITY_LINK` | External identity linked |
| `FEDERATED_IDENTITY_LINK_ERROR` | Identity linking fails |
| `REMOVE_FEDERATED_IDENTITY` | External identity unlinked |
| `REMOVE_FEDERATED_IDENTITY_ERROR` | Identity removal fails |

### Identity Provider

| Event Type | Triggered When |
|------------|----------------|
| `IDENTITY_PROVIDER_LOGIN` | User logs in via IdP |
| `IDENTITY_PROVIDER_LOGIN_ERROR` | IdP login fails |
| `IDENTITY_PROVIDER_FIRST_LOGIN` | First login via IdP |
| `IDENTITY_PROVIDER_FIRST_LOGIN_ERROR` | First IdP login fails |
| `IDENTITY_PROVIDER_POST_LOGIN` | Post-login IdP processing |
| `IDENTITY_PROVIDER_POST_LOGIN_ERROR` | Post-login processing fails |
| `IDENTITY_PROVIDER_RESPONSE` | IdP response received |
| `IDENTITY_PROVIDER_RESPONSE_ERROR` | IdP response invalid |
| `IDENTITY_PROVIDER_RETRIEVE_TOKEN` | Token retrieved from IdP |
| `IDENTITY_PROVIDER_RETRIEVE_TOKEN_ERROR` | Token retrieval fails |

### Client

| Event Type | Triggered When |
|------------|----------------|
| `CLIENT_LOGIN` | Client authenticates |
| `CLIENT_LOGIN_ERROR` | Client authentication fails |
| `CLIENT_REGISTER` | Client dynamically registers |
| `CLIENT_REGISTER_ERROR` | Client registration fails |
| `CLIENT_UPDATE` | Client configuration updated |
| `CLIENT_UPDATE_ERROR` | Client update fails |
| `CLIENT_DELETE` | Client deleted |
| `CLIENT_DELETE_ERROR` | Client deletion fails |
| `CLIENT_INITIATED_ACCOUNT_LINKING` | Client initiates account link |
| `CLIENT_INITIATED_ACCOUNT_LINKING_ERROR` | Account linking fails |

### Account Console

| Event Type | Triggered When |
|------------|----------------|
| `DELETE_ACCOUNT` | User deletes account |
| `DELETE_ACCOUNT_ERROR` | Account deletion fails |
| `DELETE_CREDENTIAL` | User deletes credential |
| `DELETE_CREDENTIAL_ERROR` | Credential deletion fails |

### Custom Authentication

| Event Type | Triggered When |
|------------|----------------|
| `CUSTOM_REQUIRED_ACTION` | Custom required action executes |
| `CUSTOM_REQUIRED_ACTION_ERROR` | Custom action fails |
| `EXECUTE_ACTIONS` | Admin executes actions on user |
| `EXECUTE_ACTIONS_ERROR` | Action execution fails |
| `EXECUTE_ACTION_TOKEN` | Action token processed |
| `EXECUTE_ACTION_TOKEN_ERROR` | Action token processing fails |

### Permission

| Event Type | Triggered When |
|------------|----------------|
| `PERMISSION_TOKEN` | Permission token requested |
| `PERMISSION_TOKEN_ERROR` | Permission token request fails |

### OAuth/Device Flow

| Event Type | Triggered When |
|------------|----------------|
| `OAUTH2_DEVICE_AUTH` | Device authorization started |
| `OAUTH2_DEVICE_AUTH_ERROR` | Device auth fails |
| `OAUTH2_DEVICE_VERIFY_USER_CODE` | User code verified |
| `OAUTH2_DEVICE_VERIFY_USER_CODE_ERROR` | User code verification fails |
| `OAUTH2_DEVICE_CODE_TO_TOKEN` | Device code exchanged for token |
| `OAUTH2_DEVICE_CODE_TO_TOKEN_ERROR` | Device token exchange fails |

### Pushed Authorization Request

| Event Type | Triggered When |
|------------|----------------|
| `PUSHED_AUTHORIZATION_REQUEST` | PAR initiated |
| `PUSHED_AUTHORIZATION_REQUEST_ERROR` | PAR fails |

### Impersonation

| Event Type | Triggered When |
|------------|----------------|
| `IMPERSONATE` | Admin impersonates user |
| `IMPERSONATE_ERROR` | Impersonation fails |

### Token Exchange

| Event Type | Triggered When |
|------------|----------------|
| `TOKEN_EXCHANGE` | Token exchange request |
| `TOKEN_EXCHANGE_ERROR` | Token exchange fails |

### User Info

| Event Type | Triggered When |
|------------|----------------|
| `USER_INFO_REQUEST` | UserInfo endpoint called |
| `USER_INFO_REQUEST_ERROR` | UserInfo request fails |
| `CLIENT_INFO` | Client info requested |
| `CLIENT_INFO_ERROR` | Client info request fails |

### Security

| Event Type | Triggered When |
|------------|----------------|
| `USER_DISABLED_BY_PERMANENT_LOCKOUT` | User permanently locked out |
| `USER_DISABLED_BY_PERMANENT_LOCKOUT_ERROR` | Permanent lockout fails |
| `USER_DISABLED_BY_TEMPORARY_LOCKOUT` | User temporarily locked out |
| `USER_DISABLED_BY_TEMPORARY_LOCKOUT_ERROR` | Temporary lockout fails |
| `INVALID_SIGNATURE` | Invalid signature detected |
| `INVALID_SIGNATURE_ERROR` | Signature validation error |
| `RESTART_AUTHENTICATION` | Authentication restarted |
| `RESTART_AUTHENTICATION_ERROR` | Authentication restart fails |

### Session

| Event Type | Triggered When |
|------------|----------------|
| `USER_SESSION_DELETED` | User session deleted |
| `USER_SESSION_DELETED_ERROR` | Session deletion fails |

### Credential Management

| Event Type | Triggered When |
|------------|----------------|
| `UPDATE_CREDENTIAL` | User updates credential |
| `UPDATE_CREDENTIAL_ERROR` | Credential update fails |
| `REMOVE_CREDENTIAL` | User removes credential |
| `REMOVE_CREDENTIAL_ERROR` | Credential removal fails |

### Cluster

| Event Type | Triggered When |
|------------|----------------|
| `REGISTER_NODE` | Cluster node registered |
| `REGISTER_NODE_ERROR` | Node registration fails |
| `UNREGISTER_NODE` | Cluster node unregistered |
| `UNREGISTER_NODE_ERROR` | Node unregistration fails |

### Organization

| Event Type | Triggered When |
|------------|----------------|
| `INVITE_ORG` | Organization invitation sent |
| `INVITE_ORG_ERROR` | Org invitation fails |

### Extended OAuth

| Event Type | Triggered When |
|------------|----------------|
| `OAUTH2_EXTENSION_GRANT` | Extension grant used |
| `OAUTH2_EXTENSION_GRANT_ERROR` | Extension grant fails |
| `AUTHREQID_TO_TOKEN` | CIBA auth request to token |
| `AUTHREQID_TO_TOKEN_ERROR` | CIBA token exchange fails |

### Identity Provider (Additional)

| Event Type | Triggered When |
|------------|----------------|
| `IDENTITY_PROVIDER_LINK_ACCOUNT` | IdP account linked |
| `IDENTITY_PROVIDER_LINK_ACCOUNT_ERROR` | IdP account linking fails |
| `SEND_IDENTITY_PROVIDER_LINK` | IdP link email sent |
| `SEND_IDENTITY_PROVIDER_LINK_ERROR` | IdP link email fails |
| `FEDERATED_IDENTITY_OVERRIDE_LINK` | Federated identity overridden |
| `FEDERATED_IDENTITY_OVERRIDE_LINK_ERROR` | Identity override fails |



## Admin Events

Keycloak admin events have separate `resourceType` and `operationType` fields. KETE concatenates these into a single `eventType` field as `RESOURCETYPE_OPERATIONTYPE` (e.g., `USER_CREATE`, `CLIENT_UPDATE`, `REALM_DELETE`), so all events have a consistent `eventType` property for filtering.

### Operation Types

| Operation | Description |
|-----------|-------------|
| `CREATE` | Resource created |
| `UPDATE` | Resource modified |
| `DELETE` | Resource deleted |
| `ACTION` | Custom action performed |

### Resource Types

| Category | Resources |
|----------|-----------|
| Realm | `REALM`, `REALM_ROLE`, `REALM_ROLE_MAPPING`, `REALM_SCOPE_MAPPING` |
| Client | `CLIENT`, `CLIENT_ROLE`, `CLIENT_ROLE_MAPPING`, `CLIENT_SCOPE`, `CLIENT_SCOPE_MAPPING`, `CLIENT_INITIAL_ACCESS_MODEL` |
| User | `USER`, `USER_FEDERATION_PROVIDER`, `USER_FEDERATION_MAPPER` |
| Group | `GROUP`, `GROUP_MEMBERSHIP` |
| Identity Provider | `IDENTITY_PROVIDER`, `IDENTITY_PROVIDER_MAPPER` |
| Authentication | `AUTH_FLOW`, `AUTH_EXECUTION_FLOW`, `AUTH_EXECUTION`, `AUTHENTICATOR_CONFIG`, `REQUIRED_ACTION` |
| Component | `COMPONENT`, `PROTOCOL_MAPPER` |
| Authorization | `AUTHORIZATION_RESOURCE_SERVER`, `AUTHORIZATION_RESOURCE`, `AUTHORIZATION_SCOPE`, `AUTHORIZATION_POLICY` |
| Other | `CLUSTER_NODE` |



## Filter Examples

### List

```bash
# Include specific events
kete.routes.kafka-example.event-matchers.list=list:LOGIN,LOGOUT,REGISTER

# Exclude events
kete.routes.kafka-example.event-matchers.list=list:not:REFRESH_TOKEN,CODE_TO_TOKEN

# Admin events
kete.routes.admin.event-matchers.list=list:USER_CREATE,USER_UPDATE,USER_DELETE
```

### Glob

```bash
# All login events
kete.routes.kafka-example.event-matchers.login=glob:LOGIN*

# All error events
kete.routes.kafka-example.event-matchers.filter=glob:*_ERROR

# Identity provider events
kete.routes.kafka-example.event-matchers.match=glob:IDENTITY_PROVIDER_*

# All user admin events
kete.routes.admin.event-matchers.user=glob:USER_*
```

### Regex

```bash
# Update events
kete.routes.kafka-example.event-matchers.pattern=regex:UPDATE_.*

# Token events
kete.routes.kafka-example.event-matchers.pattern=regex:(CODE_TO_TOKEN|REFRESH_TOKEN|INTROSPECT_TOKEN)

# Admin create events
kete.routes.admin.event-matchers.create=regex:.*_CREATE
```

### SQL

```bash
# All login-related events
kete.routes.kafka-example.event-matchers.login=sql:LOGIN%

# All error events
kete.routes.kafka-example.event-matchers.errors=sql:%_ERROR

# Admin operations on users
kete.routes.admin.event-matchers.user=sql:USER_%
```



## Event Fields

### User Events

| Field | Description |
|-------|-------------|
| `type` | EventType (e.g., LOGIN, REGISTER) |
| `realmId` | Realm identifier |
| `clientId` | Client application ID |
| `userId` | User identifier |
| `sessionId` | Session identifier |
| `ipAddress` | Client IP address |
| `time` | Timestamp (epoch ms) |
| `error` | Error code (for *_ERROR events) |
| `details` | Event-specific details map |

### Admin Events

| Field | Description |
|-------|-------------|
| `operationType` | CREATE, UPDATE, DELETE, ACTION |
| `resourceType` | Type of resource affected |
| `resourcePath` | Path to the resource |
| `realmId` | Realm identifier |
| `time` | Timestamp (epoch ms) |
| `authDetails.realmId` | Authenticating realm |
| `authDetails.clientId` | Client performing action |
| `authDetails.userId` | Admin user ID |
| `authDetails.ipAddress` | Admin IP address |
| `representation` | JSON representation of resource |
| `error` | Error message (if failed) |
