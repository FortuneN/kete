# XML Serializer

Standard XML 1.0 format.

## Configuration

```bash
kete.routes.<name>.serializer.kind=xml
```

## Example Output

### User Event

```xml
<Event>
  <id>a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f</id>
  <time>1704816000000</time>
  <type>LOGIN</type>
  <realmId>master</realmId>
  <realmName>master</realmName>
  <clientId>my-app</clientId>
  <userId>550e8400-e29b-41d4-a716-446655440000</userId>
  <sessionId>abc-session-123</sessionId>
  <ipAddress>192.168.1.100</ipAddress>
  <error/>
  <details>
    <username>john.doe</username>
    <remember_me>true</remember_me>
  </details>
</Event>
```

### Admin Event

```xml
<AdminEvent>
  <id>b9d3e7f2-5c4e-4b1f-9a8d-3e2f6c1b4a5e</id>
  <time>1704816000000</time>
  <realmId>master</realmId>
  <realmName>master</realmName>
  <authDetails>
    <realmId>master</realmId>
    <realmName>master</realmName>
    <clientId>admin-cli</clientId>
    <userId>admin-uuid</userId>
    <ipAddress>192.168.1.100</ipAddress>
  </authDetails>
  <resourceType>USER</resourceType>
  <operationType>CREATE</operationType>
  <resourcePath>users/user-uuid</resourcePath>
  <representation>{"username":"newuser"}</representation>
  <error/>
  <resourceTypeAsString>USER</resourceTypeAsString>
</AdminEvent>
```


