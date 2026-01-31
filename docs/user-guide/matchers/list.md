# List Matcher

Exact-match filtering using a comma-separated list of event names.

## Syntax

```
list:<event1>,<event2>,<event3>
```

- Separate multiple events with comma `,`
- Event names are case-insensitive
- Exact match only (no wildcards)
- Use `not:` prefix to exclude instead of include

## Examples

### Include Events

```bash
# Single event
kete.routes.audit.event-matchers.login=list:LOGIN

# Multiple events
kete.routes.audit.event-matchers.auth=list:LOGIN,LOGOUT,LOGIN_ERROR

# User management events
kete.routes.audit.event-matchers.users=list:REGISTER,UPDATE_EMAIL,UPDATE_PASSWORD,DELETE_ACCOUNT

# Admin events (format: RESOURCETYPE_OPERATIONTYPE)
kete.routes.audit.event-matchers.admin=list:USER_CREATE,USER_UPDATE,USER_DELETE
```

### Exclude Events

```bash
# Exclude high-frequency events (use with ALL mode)
kete.routes.kafka.event-match-mode=all
kete.routes.kafka.event-matchers.all=glob:*
kete.routes.kafka.event-matchers.no-tokens=list:not:REFRESH_TOKEN,CODE_TO_TOKEN,INTROSPECT_TOKEN
```

## Configuration Examples

### Multiple Destinations

```bash
# Kafka gets login/logout only
kete.routes.kafka.destination.kind=kafka
kete.routes.kafka.destination.bootstrap.servers=kafka:9092
kete.routes.kafka.destination.topic=login-events
kete.routes.kafka.event-matchers.list=list:LOGIN,LOGOUT

# RabbitMQ gets all events (no event-matchers = accept all)
kete.routes.rabbitmq.destination.kind=amqp-0.9.1
kete.routes.rabbitmq.destination.host=rabbitmq
kete.routes.rabbitmq.destination.exchange=keycloak
```

### Combining with Other Matchers

```bash
# Match login OR wildcard pattern (ANY mode - default)
kete.routes.kafka.event-matchers.login=list:LOGIN
kete.routes.kafka.event-matchers.user=glob:USER_*
```

## Case Sensitivity

All matchers in KETE are **case-insensitive**:

- `LOGIN` matches `LOGIN`, `login`, `Login`
- `login` matches `LOGIN`, `login`, `Login`


