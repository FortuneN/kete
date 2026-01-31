# CSV Serializer

Comma-separated values format.

## Configuration

```bash
kete.routes.<name>.serializer.kind=csv
```

## Example Output

### User Event

```csv
id,time,type,realmId,clientId,userId,ipAddress,error
a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f,1704816000000,LOGIN,master,my-app,550e8400-e29b-41d4-a716-446655440000,192.168.1.100,
```

### Admin Event

```csv
id,time,operationType,resourceType,realmId,resourcePath
b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e,1704816000000,CREATE,USER,master,users/user-uuid
```

## Limitations

CSV cannot represent nested objects. Fields like `details` and `authDetails` are not included in the output.


