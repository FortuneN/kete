# Protobuf Serializer

Binary Protocol Buffers format (proto3) without code generation.

## Configuration

```bash
kete.routes.<name>.serializer.kind=protobuf
```

## Binary Format

Protobuf produces compact binary data, not human-readable text. Use a Protobuf library to decode:

- Java: `com.google.protobuf:protobuf-java`
- Python: `protobuf`
- JavaScript: `protobufjs`
- Go: `google.golang.org/protobuf`
- .NET: `Google.Protobuf`

The `.proto` schema files needed to decode messages are in the [`schemas/protobuf/`](https://github.com/FortuneN/kete/tree/develop/schemas/protobuf) directory.

## Schemas

### Event

```protobuf
syntax = "proto3";
package io.github.fortunen.kete.events;

message Event {
  string id = 1;
  int64 time = 2;
  string type = 3;
  string realm_id = 4;
  string realm_name = 5;
  string client_id = 6;
  string user_id = 7;
  string session_id = 8;
  string ip_address = 9;
  string error = 10;
  map<string, string> details = 11;
}
```

### Admin Event

```protobuf
syntax = "proto3";
package io.github.fortunen.kete.events;

message AdminEvent {
  string id = 1;
  int64 time = 2;
  string realm_id = 3;
  string realm_name = 4;
  AuthDetails auth_details = 5;
  string resource_type = 6;
  string operation_type = 7;
  string resource_path = 8;
  string representation = 9;
  string error = 10;
  map<string, string> details = 11;
}
```

### Auth Details

```protobuf
syntax = "proto3";
package io.github.fortunen.kete.events;

message AuthDetails {
  string realm_id = 1;
  string realm_name = 2;
  string client_id = 3;
  string user_id = 4;
  string ip_address = 5;
}
```

## Proto3 Defaults

Unset fields use proto3 defaults — empty string for `string`, `0` for `int64`, empty for `map`. There is no distinction between "not set" and "default value" in proto3.

## Decoding Example (Python)

```python
from google.protobuf.descriptor_pool import DescriptorPool
from google.protobuf.descriptor_pb2 import FileDescriptorSet
from google.protobuf.message_factory import MessageFactory

# Load the descriptor set shipped with KETE
with open("protobuf.desc", "rb") as f:
    fds = FileDescriptorSet()
    fds.ParseFromString(f.read())

pool = DescriptorPool()
for fd in fds.file:
    pool.Add(fd)

Event = MessageFactory(pool).GetPrototype(
    pool.FindMessageTypeByName("io.github.fortunen.kete.events.Event")
)

event = Event()
event.ParseFromString(raw_bytes)
print(event.type, event.realm_id, dict(event.details))
```

## Decoding Example (Java)

```java
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

// Obtain the Event descriptor (same approach KETE uses internally)
Descriptor eventDesc = /* load from protobuf.desc */;
DynamicMessage event = DynamicMessage.parseFrom(eventDesc, rawBytes);

String type = (String) event.getField(eventDesc.findFieldByName("type"));
String realmId = (String) event.getField(eventDesc.findFieldByName("realm_id"));
```
