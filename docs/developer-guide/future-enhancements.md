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

## Cloud Destinations (Native SDK Required)

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

Unary gRPC is implemented — see [gRPC Destination docs](../user-guide/destinations/grpc.md). Streaming modes (bidirectional, server-streaming) remain potential future enhancements.

**Priority:** Low (niche use case beyond unary)

---

## Uncovered Protocols & Systems

Systems that cannot be reached by any existing KETE destination because they use proprietary or unsupported protocols.

| System | Container | Protocol | Notes |
|--------|-----------|----------|-------|
| Beanstalkd | `schickling/beanstalkd` | Custom text protocol | Lightweight work queue |
| Pravega | `pravega/pravega` | Custom protocol | Dell streaming storage |

### Protocol Categories Not Covered

- **Server-Sent Events (SSE)** — one-way HTTP streaming
- **Database-as-queue** — PostgreSQL LISTEN/NOTIFY, Oracle AQ

---

## Cross-Cutting Enhancements

### Dynamic Header Values (Template Processing)

Custom headers are currently static `Map<String, String>` — values are set at configuration time and never change. Template processing would allow header values to reference event fields:

```properties
kete.routes.myroute.destination.headers.x-correlation-id=${event.id}
kete.routes.myroute.destination.headers.x-realm=${event.realmId}
kete.routes.myroute.destination.headers.x-event-type=${event.type}
```

**Scope:** All destinations that support custom headers (HTTP, SOAP, gRPC metadata, Kafka, MQTT user properties, AMQP properties, NATS, Pulsar, etc.)

**Considerations:**
- Template syntax: `${event.field}` or `{{event.field}}`?
- Which event fields are available? All top-level fields? Nested `details` map?
- Performance: resolve per-message vs. cache static headers and only resolve templated ones
- Applies to common KETE headers too, or custom headers only?

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
