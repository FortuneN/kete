# CBOR Serializer

Concise Binary Object Representation (RFC 8949).

## Configuration

```bash
kete.routes.<name>.serializer.kind=cbor
```

## Example Output

CBOR produces binary data, not human-readable text:

```
A9 62 69 64 6B 65 76 65 6E 74 2D 75 75 69 64 64
74 69 6D 65 1B 00 00 01 8D 7C E8 90 00 64 74 79
70 65 65 4C 4F 47 49 4E ...
```

## Binary Format

CBOR is a binary format that maintains JSON data model semantics. Use a CBOR library to decode:

- Java: `com.fasterxml.jackson.dataformat:jackson-dataformat-cbor`
- Python: `cbor2`
- JavaScript: `cbor`
