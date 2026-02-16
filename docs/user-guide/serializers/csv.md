# CSV Serializer

Comma-separated values format.

## Configuration

```bash
kete.routes.<name>.serializer.kind=csv
```

## Example Output

### User Event

```csv
clientId,details,error,id,ipAddress,realmId,realmName,sessionId,time,type,userId
my-app,,,a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f,192.168.1.100,master,master,abc-session-123,1704816000000,LOGIN,550e8400-e29b-41d4-a716-446655440000
```

### Admin Event

```csv
authDetails,error,id,operationType,realmId,realmName,representation,resourcePath,resourceType,resourceTypeAsString,time
,,b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e,CREATE,master,master,"{""username"":""newuser""}",users/user-uuid,USER,USER,1704816000000
```

## Limitations

Columns are alphabetically ordered. All bean properties appear as columns, but nested objects (`details`, `authDetails`) cause serialization errors when non-null. The examples above show these fields as empty.


