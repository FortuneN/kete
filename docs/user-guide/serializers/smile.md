# Smile Serializer

Binary JSON format (Jackson Smile).

## Configuration

```bash
kete.routes.<name>.serializer.kind=smile
```

## Example Output

Smile produces binary data, not human-readable text:

```
3A 29 0A 01 FA 81 69 64 63 61 37 63 32 66 38 65
31 2D 34 62 33 64 2D 34 61 39 65 2D 38 66 37 63
2D 32 64 31 65 35 62 39 61 33 63 34 66 83 74 69 ...
```

The stream starts with the Smile header `3A 29 0A 01` followed by a start-object marker (`FA`) and the same property names and order as the JSON serializer.

## Binary Format

Smile is a binary JSON format optimized for Jackson. Use a Smile library to decode:

- Java: `com.fasterxml.jackson.dataformat:jackson-dataformat-smile`
- Other languages: Limited library support
