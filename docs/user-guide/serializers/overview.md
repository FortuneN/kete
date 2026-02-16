# Serializers

Format events before sending to a destination.

## Overview

Serializers convert Keycloak events into your desired output format.

## Available Formats

| Serializer | Config | Content-Type | Binary? |
|------------|--------|--------------|---------|
| **[Avro](avro.md)** | `avro` | `application/avro` | Yes |
| **[JSON](json.md)** | `json` (default) | `application/json` | No |
| **[YAML](yaml.md)** | `yaml` | `application/yaml` | No |
| **[XML](xml.md)** | `xml` | `application/xml` | No |
| **[CSV](csv.md)** | `csv` | `text/csv` | No |
| **[TOML](toml.md)** | `toml` | `application/toml` | No |
| **[Properties](properties.md)** | `properties` | `text/plain` | No |
| **[CBOR](cbor.md)** | `cbor` | `application/cbor` | Yes |
| **[Protobuf](protobuf.md)** | `protobuf` | `application/x-protobuf` | Yes |
| **[Smile](smile.md)** | `smile` | `application/x-jackson-smile` | Yes |
| **[URL-Encoded Form](url-encoded-form.md)** | `url-encoded-form` | `application/x-www-form-urlencoded` | No |
| **[Multipart Form](multipart-form.md)** | `multipart-form` | `multipart/form-data; boundary=kete-boundary` | No |
| **[Template](template.md)** | `template` | configurable (default: `text/plain`) | No |

The **Content-Type** is sent as the `contenttype` header (or native content-type property where supported). This allows consumers to know how to deserialize the message body.

## Quick Examples

**YAML for human-readable logs:**
```bash
kete.routes.audit.serializer.kind=yaml
```

**CBOR for IoT/low bandwidth:**
```bash
kete.routes.iot.serializer.kind=cbor
```

**Avro for schema-driven consumers:**
```bash
kete.routes.stream.serializer.kind=avro
```

**Protobuf for schema-driven consumers:**
```bash
kete.routes.stream.serializer.kind=protobuf
```

**CSV for data exports:**
```bash
kete.routes.export.serializer.kind=csv
```

## Configuration

Serializers are optional. To explicitly set one:

```bash
kete.routes.myroute.serializer.kind=json
```

Most routes don't need this — JSON is used automatically.
