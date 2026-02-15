# URL-Encoded Form Serializer

Serialize events as URL-encoded form data (`application/x-www-form-urlencoded`).

## Configuration

```bash
kete.routes.<name>.serializer.kind=url-encoded-form
```

### Optional Properties

| Property | Default | Description |
|----------|---------|-------------|
| `serializer.nesting-notation` | `bracket` | Notation for nested fields: `bracket` or `dot` |

### Nesting Notation

Nested fields (e.g., `details`) can use bracket or dot notation:

=== "Bracket (Default)"

    ```bash
    kete.routes.<name>.serializer.kind=url-encoded-form
    # details[username]=john → default behavior
    ```

=== "Dot"

    ```bash
    kete.routes.<name>.serializer.kind=url-encoded-form
    kete.routes.<name>.serializer.nesting-notation=dot
    # details.username=john
    ```

## Example Output

### User Event (Bracket Notation)

```
id=a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f&type=LOGIN&realmId=master&realmName=master&clientId=my-app&userId=550e8400-e29b-41d4-a716-446655440000&ipAddress=192.168.1.100&time=1704816000000&details%5Busername%5D=john.doe&details%5Bremember_me%5D=true
```

### User Event (Dot Notation)

```
id=a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f&type=LOGIN&realmId=master&realmName=master&clientId=my-app&userId=550e8400-e29b-41d4-a716-446655440000&ipAddress=192.168.1.100&time=1704816000000&details.username=john.doe&details.remember_me=true
```
