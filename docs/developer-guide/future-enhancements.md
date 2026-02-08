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

### Google Cloud Tasks (`gcp-cloudtasks`)

Enqueue Keycloak events as Cloud Tasks for reliable, rate-controlled asynchronous processing.

**Priority:** 🥉 Low (niche, task-queue pattern)

**Potential Configuration:**

```properties
kete.routes.gcp.destination.kind=gcp-cloudtasks
kete.routes.gcp.destination.project-id=my-gcp-project
kete.routes.gcp.destination.location=us-central1
kete.routes.gcp.destination.queue=keycloak-events
kete.routes.gcp.destination.target-url=https://my-service.run.app/handle-event
kete.routes.gcp.destination.credentials-file=/path/to/service-account.json
# Or use GOOGLE_APPLICATION_CREDENTIALS environment variable
```

**Dependencies Required:**
- `com.google.cloud:google-cloud-tasks`

**Authentication Options:**
- Service account JSON key file
- Workload Identity (GKE)
- Application Default Credentials

**Use Cases:**
- Rate-limited event processing with automatic retries
- Deferred/scheduled event handling
- Offloading event processing to Cloud Run or Cloud Functions

---

### Google Cloud Workflows (`gcp-workflows`)

Trigger Google Cloud Workflows executions from Keycloak events for orchestrated multi-step processing.

**Priority:** 🥉 Low (niche, orchestration pattern)

**Potential Configuration:**

```properties
kete.routes.gcp.destination.kind=gcp-workflows
kete.routes.gcp.destination.project-id=my-gcp-project
kete.routes.gcp.destination.location=us-central1
kete.routes.gcp.destination.workflow=process-keycloak-event
kete.routes.gcp.destination.credentials-file=/path/to/service-account.json
# Or use GOOGLE_APPLICATION_CREDENTIALS environment variable
```

**Dependencies Required:**
- `com.google.cloud:google-cloud-workflows-executions`

**Authentication Options:**
- Service account JSON key file
- Workload Identity (GKE)
- Application Default Credentials

**Use Cases:**
- Multi-step event processing workflows (e.g., notify → provision → audit)
- Orchestrating responses across multiple GCP services
- Complex event-driven automation with built-in error handling

---

### Google Eventarc Advanced (`gcp-eventarc-advanced`)

Publish Keycloak events to Google Eventarc for advanced event routing across GCP services and third-party destinations.

**Priority:** 🥉 Low (niche, GCP-native event routing)

**Potential Configuration:**

```properties
kete.routes.gcp.destination.kind=gcp-eventarc-advanced
kete.routes.gcp.destination.project-id=my-gcp-project
kete.routes.gcp.destination.location=us-central1
kete.routes.gcp.destination.channel=keycloak-events
kete.routes.gcp.destination.credentials-file=/path/to/service-account.json
# Or use GOOGLE_APPLICATION_CREDENTIALS environment variable
# Events published in CloudEvents format
```

**Dependencies Required:**
- `com.google.cloud:google-cloud-eventarc-publishing`

**Authentication Options:**
- Service account JSON key file
- Workload Identity (GKE)
- Application Default Credentials

**Use Cases:**
- Fan-out Keycloak events to multiple GCP services via Eventarc triggers
- Routing events to Cloud Run, Cloud Functions, GKE, or Workflows
- Cross-project event distribution using Eventarc channels

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

### AWS EventBridge (`aws-eventbridge`)

Publish Keycloak events to Amazon EventBridge for event-driven routing across AWS services and SaaS integrations.

**Priority:** 🥈 Medium (AWS serverless ecosystem, event routing)

**Potential Configuration:**

```properties
kete.routes.aws.destination.kind=aws-eventbridge
kete.routes.aws.destination.event-bus-name=keycloak-events
kete.routes.aws.destination.region=us-east-1
kete.routes.aws.destination.source=keycloak
kete.routes.aws.destination.detail-type=${event-type}
# Authentication via environment variables or IAM role
```

**Dependencies Required:**
- `software.amazon.awssdk:eventbridge`

**Authentication Options:**
- Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
- IAM instance profile (EC2/ECS)
- IRSA (EKS)

**Use Cases:**
- Fan-out Keycloak events to multiple AWS services via EventBridge rules
- Routing events to Lambda, Step Functions, SQS, SNS, or API destinations
- Cross-account event distribution
- SaaS integration via EventBridge partner event sources

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

## Logging & Observability Destinations

### Syslog (`syslog`)

Stream events to Syslog-compatible log aggregators using RFC 5424.

**Priority:** 🥇 High (wide enterprise adoption, low implementation effort)

**Protocol:** Syslog RFC 5424 over UDP/TCP/TLS

**Compatible Systems:**

| System | Notes |
|--------|-------|
| **rsyslog** | Linux default, high-performance |
| **syslog-ng** | Enterprise syslog with advanced routing |
| **Graylog** | Log management with syslog input |
| **Splunk** | Via syslog input or HEC |
| **Elastic/Logstash** | Via syslog input plugin |
| **Papertrail** | Managed cloud logging |
| **Datadog** | Log ingestion via syslog |
| **Sumo Logic** | Cloud SIEM with syslog |
| **Loggly** | Cloud log management |
| **Fluentd/Fluent Bit** | Via syslog input |

**Potential Configuration:**

```properties
kete.routes.syslog.destination.kind=syslog
kete.routes.syslog.destination.host=syslog.example.com
kete.routes.syslog.destination.port=514
kete.routes.syslog.destination.protocol=udp  # udp, tcp, or tls
kete.routes.syslog.destination.facility=local0
kete.routes.syslog.destination.severity=info
kete.routes.syslog.destination.app-name=keycloak
```

**Dependencies Required:**
- `com.cloudbees:syslog-java-client` or custom RFC 5424 implementation

**Why This Matters:**
- Nearly universal enterprise adoption
- Simple protocol (text-based, like STOMP)
- Integrates with existing log infrastructure
- Low implementation effort

---

## Real-Time Streaming Destinations

### Server-Sent Events (`sse`)

Stream events to clients via HTTP Server-Sent Events.

**Priority:** 🥈 Medium (browser-friendly, simple implementation)

**Protocol:** SSE (HTTP streaming, text/event-stream)

**Compatible Systems:**

| System | Notes |
|--------|-------|
| **Browsers** | Native EventSource API |
| **curl** | `curl -N` for streaming |
| **Real-time dashboards** | React, Vue, Angular apps |
| **API Gateways** | Kong, AWS API Gateway |
| **Mobile apps** | iOS/Android SSE clients |

**Potential Configuration:**

```properties
kete.routes.sse.destination.kind=sse
kete.routes.sse.destination.url=http://dashboard.example.com/events
kete.routes.sse.destination.event-type=keycloak-event
kete.routes.sse.destination.retry=3000
```

**Implementation Notes:**
- KETE acts as SSE publisher to an endpoint
- Can be used with an SSE relay/fanout server
- Simpler than WebSocket (unidirectional)

**Use Cases:**
- Live admin dashboards
- Real-time audit displays
- Browser-based monitoring

---

### gRPC Streaming (`grpc`)

Stream events to gRPC servers using bidirectional or server streaming.

**Priority:** 🥉 Low (high effort, niche use case)

**Protocol:** gRPC over HTTP/2

**Compatible Systems:**

| System | Notes |
|--------|-------|
| **Custom gRPC servers** | Any language with gRPC support |
| **Envoy Proxy** | gRPC routing and load balancing |
| **Google Cloud Run** | Serverless gRPC |
| **Kubernetes services** | Native gRPC support |
| **Istio** | Service mesh with gRPC |

**Potential Configuration:**

```properties
kete.routes.grpc.destination.kind=grpc
kete.routes.grpc.destination.target=grpc-server.example.com:9090
kete.routes.grpc.destination.service=keycloak.EventService
kete.routes.grpc.destination.method=StreamEvents
kete.routes.grpc.destination.tls.enabled=true
```

**Dependencies Required:**
- `io.grpc:grpc-netty-shaded`
- `io.grpc:grpc-protobuf`
- `io.grpc:grpc-stub`
- Protobuf schema for events

**Implementation Notes:**
- Requires defining a `.proto` schema for Keycloak events
- Higher complexity than HTTP/WebSocket
- Best for microservices architectures already using gRPC

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
