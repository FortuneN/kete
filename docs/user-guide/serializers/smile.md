# Smile Serializer

Binary JSON format (Jackson Smile).

## Configuration

```bash
kete.routes.<name>.serializer.kind=smile
```

## Example Output

Smile produces binary data, not human-readable text:

```
3A 29 0A 00 80 69 64 FA 65 76 65 6E 74 2D 75 75
69 64 80 74 69 6D 65 25 00 00 01 8D 7C E8 90 00
80 74 79 70 65 FA 4C 4F 47 49 4E ...
```

## Binary Format

Smile is a binary JSON format optimized for Jackson. Use a Smile library to decode:

- Java: `com.fasterxml.jackson.dataformat:jackson-dataformat-smile`
- Other languages: Limited library support
