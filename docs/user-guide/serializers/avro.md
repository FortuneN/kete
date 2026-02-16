# Avro Serializer

Binary Apache Avro format using schema-based encoding without code generation.

## Configuration

```bash
kete.routes.<name>.serializer.kind=avro
```

## Binary Format

Avro produces compact binary data, not human-readable text. Consumers **must** have the schema to decode messages (raw binary encoding, no container/header). Use an Avro library to decode:

- Java: `org.apache.avro:avro`
- Python: `avro` or `fastavro`
- JavaScript: `avsc`
- Go: `github.com/linkedin/goavro`
- .NET: `Apache.Avro`

The `.avsc` schema files needed to decode messages are in the [`schemas/avro/`](https://github.com/FortuneN/kete/tree/develop/schemas/avro) directory.

## Schemas

### Event

```json
{
  "type": "record",
  "name": "Event",
  "namespace": "io.github.fortunen.kete.events",
  "fields": [
    { "name": "id",         "type": ["null", "string"], "default": null },
    { "name": "time",       "type": ["null", "long"],   "default": null },
    { "name": "type",       "type": ["null", "string"], "default": null },
    { "name": "realm_id",   "type": ["null", "string"], "default": null },
    { "name": "realm_name", "type": ["null", "string"], "default": null },
    { "name": "client_id",  "type": ["null", "string"], "default": null },
    { "name": "user_id",    "type": ["null", "string"], "default": null },
    { "name": "session_id", "type": ["null", "string"], "default": null },
    { "name": "ip_address", "type": ["null", "string"], "default": null },
    { "name": "error",      "type": ["null", "string"], "default": null },
    { "name": "details",    "type": ["null", { "type": "map", "values": "string" }], "default": null }
  ]
}
```

### Admin Event

```json
{
  "type": "record",
  "name": "AdminEvent",
  "namespace": "io.github.fortunen.kete.events",
  "fields": [
    { "name": "id",              "type": ["null", "string"], "default": null },
    { "name": "time",            "type": ["null", "long"],   "default": null },
    { "name": "realm_id",        "type": ["null", "string"], "default": null },
    { "name": "realm_name",      "type": ["null", "string"], "default": null },
    {
      "name": "auth_details",
      "type": ["null", {
        "type": "record",
        "name": "AuthDetails",
        "fields": [
          { "name": "realm_id",   "type": ["null", "string"], "default": null },
          { "name": "realm_name", "type": ["null", "string"], "default": null },
          { "name": "client_id",  "type": ["null", "string"], "default": null },
          { "name": "user_id",    "type": ["null", "string"], "default": null },
          { "name": "ip_address", "type": ["null", "string"], "default": null }
        ]
      }],
      "default": null
    },
    { "name": "resource_type",   "type": ["null", "string"], "default": null },
    { "name": "operation_type",  "type": ["null", "string"], "default": null },
    { "name": "resource_path",   "type": ["null", "string"], "default": null },
    { "name": "representation",  "type": ["null", "string"], "default": null },
    { "name": "error",           "type": ["null", "string"], "default": null }
  ]
}
```

## Nullable Fields

All fields use Avro unions (`["null", "type"]`) with a default of `null`. Unset fields serialize as `null` — there is a clear distinction between "not set" and "empty string", unlike Protobuf's proto3 defaults.

## Decoding Example (Python)

```python
import avro.schema
import avro.io
import io

# Load the schema
schema = avro.schema.parse(open("event.avsc").read())

# Decode raw bytes
reader = avro.io.DatumReader(schema)
decoder = avro.io.BinaryDecoder(io.BytesIO(raw_bytes))
event = reader.read(decoder)

print(event["type"], event["realm_id"], event.get("details"))
```

## Decoding Example (Java)

```java
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;

Schema schema = new Schema.Parser().parse(new File("event.avsc"));
GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
GenericRecord event = reader.read(null,
    DecoderFactory.get().binaryDecoder(rawBytes, null));

String type = event.get("type").toString();
String realmId = event.get("realm_id").toString();
```
