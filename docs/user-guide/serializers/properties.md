# Properties Serializer

Java properties key-value format.

## Configuration

```bash
kete.routes.<name>.serializer.kind=properties
```

## Example Output

### User Event

```properties
id=a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f
time=1704816000000
type=LOGIN
realmId=master
realmName=master
clientId=my-app
userId=550e8400-e29b-41d4-a716-446655440000
sessionId=abc-session-123
ipAddress=192.168.1.100
error=
details.username=john.doe
details.remember_me=true
```

### Admin Event

```properties
id=b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e
time=1704816000000
realmId=master
realmName=master
authDetails.realmId=master
authDetails.realmName=master
authDetails.clientId=admin-cli
authDetails.userId=admin-uuid
authDetails.ipAddress=192.168.1.100
resourceType=USER
operationType=CREATE
resourcePath=users/user-uuid
representation={"username":"newuser"}
error=
resourceTypeAsString=USER
```

## Nested Structure Handling

Nested objects are flattened with dot notation:

- `details.username` instead of nested structure
- `authDetails.realmId` instead of nested structure


