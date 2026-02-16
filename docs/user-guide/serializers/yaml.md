# YAML Serializer

Human-readable format with minimal syntax.

## Configuration

```bash
kete.routes.<name>.serializer.kind=yaml
```

## Example Output

### User Event

```yaml
id: a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f
time: 1704816000000
type: LOGIN
realmId: master
realmName: master
clientId: my-app
userId: 550e8400-e29b-41d4-a716-446655440000
sessionId: abc-session-123
ipAddress: 192.168.1.100
error: null
details:
  username: john.doe
  remember_me: "true"
```

### Admin Event

```yaml
id: b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e
time: 1704816000000
realmId: master
realmName: master
authDetails:
  realmId: master
  realmName: master
  clientId: admin-cli
  userId: admin-uuid
  ipAddress: 192.168.1.100
resourceType: USER
operationType: CREATE
resourcePath: users/user-uuid
representation: "{\\\"username\\\":\\\"newuser\\\"}"
error: null
resourceTypeAsString: USER
```


