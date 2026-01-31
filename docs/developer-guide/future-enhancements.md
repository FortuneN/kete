# Future Enhancements

Potential future enhancements that have been considered but not yet implemented. These are ideas for future development—not commitments or guarantees.

---

## JMS-Compatible Systems (Low Effort)

These systems provide JMS client libraries that can be integrated with minimal effort since KETE already has a fully functional JMS-based destination (`Amqp1Destination`). The existing code uses standard `jakarta.jms.*` interfaces—only the `ConnectionFactory` implementation differs per system.

!!! tip "Why These Are Easy Wins"
    The current `Amqp1Destination` code is pure JMS. Adding these would only require:
    
    1. A new `*DestinationConfig` class with the system-specific `ConnectionFactory`
    2. Adding the client library dependency to `pom.xml` (with shade relocation)
    3. Documentation and quick-start examples

### Amazon SQS via JMS (`sqs-jms`)

Amazon provides a JMS 2.0 client library for SQS, making integration straightforward.

**Priority:** 🥇 High (AWS market dominance)

**Potential Configuration:**

```properties
kete.routes.sqs.destination.kind=sqs-jms
kete.routes.sqs.destination.region=us-east-1
kete.routes.sqs.destination.queue=keycloak-events
# Optional for FIFO queues
kete.routes.sqs.destination.message-group-id=${realm}
# Authentication via environment variables or IAM role
```

**ConnectionFactory:**
```java
import com.amazon.sqs.javamessaging.SQSConnectionFactory;

connectionFactory = new SQSConnectionFactory(
    new ProviderConfiguration(),
    SqsClient.builder().region(Region.of(region)).build()
);
```

**Dependencies Required:**
- `com.amazonaws:amazon-sqs-java-messaging-lib`
- `software.amazon.awssdk:sqs`

**Authentication Options:**
- Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
- IAM instance profile (EC2/ECS)
- IRSA (EKS)
- Web Identity Token

---

### Apache Pulsar via JMS (`pulsar-jms`)

DataStax provides a JMS 2.0 client for Apache Pulsar.

**Priority:** 🥈 Medium (growing adoption)

**Potential Configuration:**

```properties
kete.routes.pulsar.destination.kind=pulsar-jms
kete.routes.pulsar.destination.service-url=pulsar://localhost:6650
kete.routes.pulsar.destination.topic=keycloak-events
kete.routes.pulsar.destination.namespace=public/default
```

**ConnectionFactory:**
```java
import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;

Map<String, Object> config = Map.of("brokerServiceUrl", serviceUrl);
connectionFactory = new PulsarConnectionFactory(config);
```

**Dependencies Required:**
- `com.datastax.oss:pulsar-jms`

---

### IBM MQ (`ibm-mq-jms`)

IBM MQ has native JMS support—it's one of the original JMS implementations.

**Priority:** 🥉 Medium (enterprise legacy systems)

**Potential Configuration:**

```properties
kete.routes.ibm.destination.kind=ibm-mq-jms
kete.routes.ibm.destination.host=mq.example.com
kete.routes.ibm.destination.port=1414
kete.routes.ibm.destination.queue-manager=QM1
kete.routes.ibm.destination.channel=DEV.APP.SVRCONN
kete.routes.ibm.destination.queue=KEYCLOAK.EVENTS
kete.routes.ibm.destination.username=app
kete.routes.ibm.destination.password=secret
```

**ConnectionFactory:**
```java
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

var factory = new MQConnectionFactory();
factory.setHostName(host);
factory.setPort(port);
factory.setQueueManager(queueManager);
factory.setChannel(channel);
factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
connectionFactory = factory;
```

**Dependencies Required:**
- `com.ibm.mq:com.ibm.mq.allclient`

---

### ActiveMQ Classic (`activemq-classic-jms`)

Apache ActiveMQ "Classic" (5.x) uses OpenWire protocol with native JMS.

**Priority:** Low (most users should migrate to Artemis/AMQP 1.0)

**Potential Configuration:**

```properties
kete.routes.amq.destination.kind=activemq-classic-jms
kete.routes.amq.destination.broker-url=tcp://localhost:61616
kete.routes.amq.destination.queue=keycloak-events
kete.routes.amq.destination.username=admin
kete.routes.amq.destination.password=admin
```

**ConnectionFactory:**
```java
import org.apache.activemq.ActiveMQConnectionFactory;

connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
```

**Dependencies Required:**
- `org.apache.activemq:activemq-client`

!!! note
    ActiveMQ Artemis already works via AMQP 1.0 using the existing `amqp-1` destination with Qpid JMS. This would only be needed for legacy OpenWire-only deployments.

---

### Solace PubSub+ (`solace-jms`)

Solace provides a JMS API for their PubSub+ platform.

**Priority:** Low (niche enterprise)

**Potential Configuration:**

```properties
kete.routes.solace.destination.kind=solace-jms
kete.routes.solace.destination.host=solace.example.com
kete.routes.solace.destination.vpn=default
kete.routes.solace.destination.queue=keycloak-events
kete.routes.solace.destination.username=admin
kete.routes.solace.destination.password=admin
```

**Dependencies Required:**
- `com.solacesystems:sol-jms`

---

### TIBCO EMS (`tibco-ems-jms`)

TIBCO Enterprise Message Service with native JMS.

**Priority:** Low (legacy enterprise)

**Potential Configuration:**

```properties
kete.routes.tibco.destination.kind=tibco-ems-jms
kete.routes.tibco.destination.server-url=tcp://localhost:7222
kete.routes.tibco.destination.queue=keycloak.events
kete.routes.tibco.destination.username=admin
kete.routes.tibco.destination.password=admin
```

**Dependencies Required:**
- `com.tibco:tibjms` (proprietary, not in Maven Central)

---

## Protocol-Based Destinations (One Client, Many Brokers)

These destinations use open standard protocols, providing wide broker compatibility with a single client library—similar to how KETE's `amqp-1` destination works with any AMQP 1.0 broker.

### STOMP (`stomp`)

Stream events to any STOMP-compatible message broker.

**Priority:** 🥇 High (unlocks ActiveMQ Classic and many enterprise systems)

**Protocol:** STOMP 1.2 (Simple Text Oriented Messaging Protocol)

**Compatible Systems:**

| System | STOMP Support | Already Reachable Via KETE? |
|--------|:-------------:|:---------------------------:|
| **ActiveMQ Classic** | ✅ Port 61613 | ❌ None (main gap!) |
| **ActiveMQ Artemis** | ✅ Port 61613 | AMQP 1.0 |
| **RabbitMQ** | ✅ Plugin | AMQP 0-9-1, AMQP 1.0 |
| **Amazon MQ (ActiveMQ)** | ✅ Port 61614 | ❌ None |
| **Apache Apollo** | ✅ Native | ❌ None |
| **HornetQ** | ✅ Native | ❌ None (legacy) |
| **Spring WebSocket STOMP** | ✅ Native | ❌ None |
| **Solace PubSub+** | ✅ Native | AMQP 1.0 |
| **TIBCO EMS** | ✅ Native | ❌ None |
| **OpenMQ** | ✅ Native | ❌ None |
| **SwiftMQ** | ✅ Native | ❌ None |
| **LavinMQ** | ✅ Native | AMQP 0-9-1 |
| **WildFly (embedded)** | ✅ Native | ❌ None |
| **Payara (embedded)** | ✅ Native | ❌ None |
| **Kaazing Gateway** | ✅ WebSocket | ❌ None |
| **CoilMQ** | ✅ Native | ❌ None |

**Potential Configuration:**

```properties
kete.routes.stomp.destination.kind=stomp
kete.routes.stomp.destination.host=activemq.example.com
kete.routes.stomp.destination.port=61613
kete.routes.stomp.destination.destination=/queue/keycloak-events
kete.routes.stomp.destination.username=admin
kete.routes.stomp.destination.password=admin
```

**Implementation Notes:**

STOMP is a simple text protocol (like HTTP):

```
SEND
destination:/queue/keycloak-events
content-type:application/json

{"type":"LOGIN","userId":"123"}
^@
```

**Dependencies Required:**
- Lightweight option: `io.github.stomp-js:stompjava` or custom TCP client
- Full option: `org.springframework:spring-messaging` + WebSocket support

**Why This Matters:**

ActiveMQ Classic is still widely deployed in enterprises. It only speaks:
- OpenWire (proprietary, no good standalone Java client)
- STOMP ✅

It does NOT natively support AMQP 1.0, AMQP 0-9-1, Kafka, or MQTT.

---

### SignalR (`signalr`)

Stream events to Microsoft SignalR hubs for real-time web applications.

**Priority:** Medium (Microsoft ecosystem, ASP.NET applications)

**Protocol:** SignalR (WebSocket-based with fallbacks)

**Compatible Systems:**

| System | Notes |
|--------|-------|
| **Azure SignalR Service** | Managed SignalR in Azure |
| **Self-hosted SignalR** | ASP.NET Core apps |
| **Blazor Server** | Real-time Blazor applications |

**Potential Configuration:**

```properties
kete.routes.signalr.destination.kind=signalr
kete.routes.signalr.destination.hub-url=https://app.example.com/eventshub
kete.routes.signalr.destination.method=ReceiveKeycloakEvent
kete.routes.signalr.destination.access-token=...
```

**Dependencies Required:**
- `com.microsoft.signalr:signalr` (official Java client)

**Use Cases:**
- Real-time dashboards showing login activity
- Live user session monitoring
- Browser-based admin consoles

---

## Cloud Destinations (Native SDK Required)

### Google Cloud Pub/Sub (`gcp-pubsub`)

Publish events to Google Cloud Pub/Sub topics.

**Potential Configuration:**

```properties
kete.routes.gcp.destination.kind=gcp-pubsub
kete.routes.gcp.destination.project-id=my-gcp-project
kete.routes.gcp.destination.topic=keycloak-events
kete.routes.gcp.destination.credentials-file=/path/to/service-account.json
# Or use GOOGLE_APPLICATION_CREDENTIALS environment variable
```

**Dependencies Required:**
- `com.google.cloud:google-cloud-pubsub`

**Authentication Options:**
- Service account JSON key file
- Workload Identity (GKE)
- Application Default Credentials

---

### AWS SNS (`aws-sns`)

Publish events to Amazon Simple Notification Service topics.

**Potential Configuration:**

```properties
kete.routes.aws.destination.kind=aws-sns
kete.routes.aws.destination.topic-arn=arn:aws:sns:us-east-1:123456789:keycloak-events
kete.routes.aws.destination.region=us-east-1
# Authentication via environment variables or IAM role
# AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
```

**Dependencies Required:**
- `software.amazon.awssdk:sns`

**Authentication Options:**
- Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
- IAM instance profile (EC2/ECS)
- IRSA (EKS)

---

### AWS SQS (`aws-sqs`)

Send events directly to Amazon Simple Queue Service queues.

**Potential Configuration:**

```properties
kete.routes.aws.destination.kind=aws-sqs
kete.routes.aws.destination.queue-url=https://sqs.us-east-1.amazonaws.com/123456789/keycloak-events
kete.routes.aws.destination.region=us-east-1
kete.routes.aws.destination.message-group-id=${realm}  # For FIFO queues
```

**Dependencies Required:**
- `software.amazon.awssdk:sqs`

---

### AWS Kinesis (`aws-kinesis`)

Stream events to Amazon Kinesis Data Streams for real-time analytics.

**Potential Configuration:**

```properties
kete.routes.aws.destination.kind=aws-kinesis
kete.routes.aws.destination.stream-name=keycloak-events
kete.routes.aws.destination.region=us-east-1
kete.routes.aws.destination.partition-key=${realm}
```

**Dependencies Required:**
- `software.amazon.awssdk:kinesis`

---

## Database Destinations

### JDBC Tables (`jdbc-tables`)

Persist events to database tables. Useful for audit logging and compliance.

**Potential Configuration:**

```properties
kete.routes.db.destination.kind=jdbc-tables
kete.routes.db.destination.jdbc.url=jdbc:postgresql://localhost:5432/events
kete.routes.db.destination.jdbc.username=events_user
kete.routes.db.destination.jdbc.password=secret
kete.routes.db.destination.jdbc.driver=org.postgresql.Driver
kete.routes.db.destination.table=keycloak_events
```

**Supported Databases:**
- PostgreSQL
- MySQL/MariaDB
- SQL Server
- Oracle
- H2 (testing)

---

### JDBC Stored Procedures (`jdbc-callable`)

Invoke stored procedures for custom event processing.

**Potential Configuration:**

```properties
kete.routes.sproc.destination.kind=jdbc-callable
kete.routes.sproc.destination.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=events
kete.routes.sproc.destination.jdbc.username=events_user
kete.routes.sproc.destination.jdbc.password=secret
kete.routes.sproc.destination.jdbc.procedure=CALL usp_ProcessKeycloakEvent(?, ?, ?, ?)
# Parameters: event_id, event_type, realm, payload
```

---

### Redis Pub/Sub (`redis-pubsub`)

Publish events to Redis channels.

**Potential Configuration:**

```properties
kete.routes.redis.destination.kind=redis-pubsub
kete.routes.redis.destination.host=redis.example.com
kete.routes.redis.destination.port=6379
kete.routes.redis.destination.channel=keycloak-events
kete.routes.redis.destination.password=secret
```

**Dependencies Required:**
- `io.lettuce:lettuce-core` or `redis.clients:jedis`

---

### Redis Streams (`redis-streams`)

Append events to Redis Streams for persistent, ordered messaging.

**Potential Configuration:**

```properties
kete.routes.redis.destination.kind=redis-streams
kete.routes.redis.destination.host=redis.example.com
kete.routes.redis.destination.port=6379
kete.routes.redis.destination.stream=keycloak-events
kete.routes.redis.destination.max-len=100000  # Trim to max entries
```

---

### NATS (`nats`)

Publish events to NATS subjects.

**Potential Configuration:**

```properties
kete.routes.nats.destination.kind=nats
kete.routes.nats.destination.servers=nats://localhost:4222
kete.routes.nats.destination.subject=keycloak.events
kete.routes.nats.destination.username=user
kete.routes.nats.destination.password=secret
```

**Dependencies Required:**
- `io.nats:jnats`

---

### NATS JetStream (`nats-jetstream`)

Publish events to NATS JetStream for persistent messaging.

**Potential Configuration:**

```properties
kete.routes.nats.destination.kind=nats-jetstream
kete.routes.nats.destination.servers=nats://localhost:4222
kete.routes.nats.destination.stream=KEYCLOAK
kete.routes.nats.destination.subject=keycloak.events
```

---

### Apache Pulsar Native (`pulsar-native`)

Publish events to Apache Pulsar topics using the native Pulsar client (alternative to JMS wrapper).

**Potential Configuration:**

```properties
kete.routes.pulsar.destination.kind=pulsar-native
kete.routes.pulsar.destination.service-url=pulsar://localhost:6650
kete.routes.pulsar.destination.topic=persistent://public/default/keycloak-events
kete.routes.pulsar.destination.auth-plugin=org.apache.pulsar.client.impl.auth.AuthenticationToken
kete.routes.pulsar.destination.auth-params=token:xxx
```

**Dependencies Required:**
- `org.apache.pulsar:pulsar-client`

!!! note
    Consider using `pulsar-jms` instead (see JMS-Compatible Systems above) for simpler integration with existing KETE JMS infrastructure.

---

## Additional Serializers

### Avro (`avro`)

Serialize events using Apache Avro with Schema Registry support.

**Potential Configuration:**

```properties
kete.routes.avro.serializer.kind=avro
kete.routes.avro.serializer.schema-registry-url=http://schema-registry:8081
kete.routes.avro.serializer.auto-register-schemas=true
```

**Dependencies Required:**
- `org.apache.avro:avro`
- `io.confluent:kafka-avro-serializer`

**Use Cases:**
- Kafka consumers requiring Avro format
- Schema evolution and compatibility

---

### Protocol Buffers (`protobuf`)

Serialize events using Google Protocol Buffers.

**Potential Configuration:**

```properties
kete.routes.proto.serializer.kind=protobuf
kete.routes.proto.serializer.schema-file=/path/to/event.proto
```

**Dependencies Required:**
- `com.google.protobuf:protobuf-java`

**Use Cases:**
- gRPC-based consumers
- Size-efficient binary format

---

## Notes

These enhancements are ideas for future development based on common use cases. Implementation priority depends on:

1. Community demand
2. Contributor availability
3. Complexity vs. value
4. Existing workarounds (e.g., Azure Service Bus works via AMQP 1.0)

### Implementation Strategy

**JMS-Compatible Systems** are the easiest to add because:

- KETE's `Amqp1Destination` already uses pure JMS code (`jakarta.jms.*`)
- Only the `ConnectionFactory` instantiation differs per system
- All JMS message handling, session management, and resource cleanup is reusable
- Could be refactored into a base `JmsDestination` class if multiple JMS systems are added

**Native SDK Destinations** require more effort because:

- Each cloud provider has unique SDK patterns
- Authentication mechanisms vary significantly
- No code reuse between implementations

Some destinations may never be implemented if the existing destinations provide adequate coverage through protocol compatibility (e.g., ActiveMQ Artemis via AMQP 1.0).
