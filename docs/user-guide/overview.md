# Overview

Keycloak handles user logins, registrations, password resets, and admin operations like creating realms and clients. Every action emits an event — but those events stay inside Keycloak.

**KETE gets them out.**

It taps into Keycloak events and forwards them wherever you need: Kafka for your data pipeline, RabbitMQ for your microservices, MQTT for your IoT platform, or a simple HTTP webhook. You choose the format — JSON, YAML, XML, or others. You filter what matters — only logins, only specific realms, only admin actions.

No custom code. No plugins to write. Just configuration.

---

## Use Cases

- Sync new users to your CRM or database
- Trigger workflows when permissions change
- Audit login activity
- Update caches when clients are modified
- Alert on failed login attempts

## How It Works

Drop in one JAR, add some environment variables, restart. Events start flowing.

**One route, one destination:**

```bash
kete.routes.audit.destination.kind=kafka
kete.routes.audit.destination.bootstrap.servers=kafka:9092
kete.routes.audit.destination.topic=keycloak-events
```
