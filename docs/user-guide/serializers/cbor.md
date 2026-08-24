# CBOR Serializer

Concise Binary Object Representation (RFC 8949).

## Configuration

```bash
kete.routes.<name>.serializer.kind=cbor
```

## Example Output

CBOR produces binary data, not human-readable text:

```
BF 62 69 64 78 24 61 37 63 32 66 38 65 31 2D 34
62 33 64 2D 34 61 39 65 2D 38 66 37 63 2D 32 64
31 65 35 62 39 61 33 63 34 66 64 74 69 6D 65 1B ...
```

Jackson writes an indefinite-length map (`BF` … `FF`) with the same property names and order as the JSON serializer.

## Binary Format

CBOR is a binary format that maintains JSON data model semantics. Use a CBOR library to decode:

- Java: `com.fasterxml.jackson.dataformat:jackson-dataformat-cbor`
- Python: `cbor2`
- JavaScript: `cbor`
