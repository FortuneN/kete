# Multipart Form Serializer

Serialize events as multipart form data (`multipart/form-data`).

## Configuration

```bash
kete.routes.<name>.serializer.kind=multipart-form
```

### Optional Properties

| Property | Default | Description |
|----------|---------|-------------|
| `serializer.nesting-notation` | `bracket` | Notation for nested fields: `bracket` or `dot` |

### Nesting Notation

Nested fields (e.g., `details`) can use bracket or dot notation:

=== "Bracket (Default)"

    ```bash
    kete.routes.<name>.serializer.kind=multipart-form
    # Content-Disposition: form-data; name="details[username]"
    ```

=== "Dot"

    ```bash
    kete.routes.<name>.serializer.kind=multipart-form
    kete.routes.<name>.serializer.nesting-notation=dot
    # Content-Disposition: form-data; name="details.username"
    ```

## Example Output

### User Event (Bracket Notation)

```
--kete-boundary
Content-Disposition: form-data; name="id"

a7c2f8e1-4b3d-4a9e-8f7c-2d1e5b9a3c4f
--kete-boundary
Content-Disposition: form-data; name="type"

LOGIN
--kete-boundary
Content-Disposition: form-data; name="realmId"

master
--kete-boundary
Content-Disposition: form-data; name="clientId"

my-app
--kete-boundary
Content-Disposition: form-data; name="userId"

550e8400-e29b-41d4-a716-446655440000
--kete-boundary
Content-Disposition: form-data; name="details[username]"

john.doe
--kete-boundary
Content-Disposition: form-data; name="details[remember_me]"

true
--kete-boundary--
```
