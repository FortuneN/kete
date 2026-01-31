# XML Serializer

Standard XML 1.0 format.

## Configuration

```bash
kete.routes.<name>.serializer.kind=xml
```

## Example Output

### User Event

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Event>
  <id>a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f</id>
  <time>1704816000000</time>
  <type>LOGIN</type>
  <realmId>master</realmId>
  <clientId>my-app</clientId>
  <userId>550e8400-e29b-41d4-a716-446655440000</userId>
  <ipAddress>192.168.1.100</ipAddress>
  <details>
    <username>john.doe</username>
    <remember_me>true</remember_me>
  </details>
</Event>
```

### Admin Event

```xml
<?xml version="1.0" encoding="UTF-8"?>
<AdminEvent>
  <id>b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e</id>
  <time>1704816000000</time>
  <operationType>CREATE</operationType>
  <resourceType>USER</resourceType>
  <realmId>master</realmId>
  <authDetails>
    <realmId>master</realmId>
    <clientId>admin-cli</clientId>
    <userId>admin-uuid</userId>
  </authDetails>
  <resourcePath>users/user-uuid</resourcePath>
</AdminEvent>
```


