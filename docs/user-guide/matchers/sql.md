# SQL Matcher

SQL LIKE-style pattern matching.

## Syntax

```
sql:<pattern>
```

| Wildcard | Matches | Example | Matches |
|----------|---------|---------|---------|
| `%` | Any number of characters | `LOGIN%` | `LOGIN`, `LOGIN_ERROR` |
| `_` | Exactly one character | `CODE_` | `CODEA`, `CODE1` |

## Examples

### Prefix Matching

```bash
# All login-related events
kete.routes.kafka.event-matchers.login=sql:LOGIN%
# Matches: LOGIN, LOGIN_ERROR, LOGIN_FAILED

# All user admin events
kete.routes.kafka.event-matchers.user=sql:USER%
# Matches: USER_CREATE, USER_UPDATE, USER_DELETE
```

### Suffix Matching

```bash
# All error events
kete.routes.kafka.event-matchers.errors=sql:%_ERROR
# Matches: LOGIN_ERROR, REGISTER_ERROR, UPDATE_ERROR

# All delete operations
kete.routes.kafka.event-matchers.deletes=sql:%_DELETE
# Matches: USER_DELETE, CLIENT_DELETE, REALM_DELETE
```

### Single Character Matching

```bash
# Events with single character at end
kete.routes.kafka.event-matchers.match=sql:CODE_
# Matches: CODEA, CODE1 (not CODEAB)

# USER followed by exactly 7 characters
kete.routes.kafka.event-matchers.match=sql:USER_______
# Matches: USER_CREATE, USER_UPDATE, USER_DELETE
```

### Exclude Patterns

```bash
# Exclude refresh events (use with ALL mode)
kete.routes.kafka.event-match-mode=all
kete.routes.kafka.event-matchers.all=glob:*
kete.routes.kafka.event-matchers.no-refresh=sql:not:REFRESH%
```

## Configuration Examples

### Multiple Patterns

```bash
# Match login OR user events
kete.routes.kafka.event-matchers.login=sql:LOGIN%
kete.routes.kafka.event-matchers.user=sql:USER%
```

### Combining with Other Matchers

```bash
# Exact match for LOGIN, SQL pattern for USER events
kete.routes.kafka.event-matchers.login=list:LOGIN
kete.routes.kafka.event-matchers.user=sql:USER%
```

### Realm-Specific Filtering

```bash
# Production realm - only errors
kete.routes.prod.realm-matchers.realm=list:production
kete.routes.prod.event-matchers.errors=sql:%ERROR

# Dev realm - events with UPDATE
kete.routes.dev.realm-matchers.realm=list:development
kete.routes.dev.event-matchers.updates=sql:%UPDATE%
```

## Case Sensitivity

All matchers in KETE are **case-insensitive**:

- `sql:LOGIN%` matches `LOGIN`, `login`, `Login`
- `sql:login%` matches `LOGIN`, `login`

## Escaping Wildcards

To match literal `%` or `_` characters:

```bash
# Match "USER_100%" literally
sql:USER\_100\%

# Match "CODE_1_A" with literal underscores
sql:CODE\_1\_A
```

## Underscore Behavior

The `_` wildcard behavior differs between matchers:

| Matcher | `LOGIN_ERROR` pattern | Matches |
|---------|----------------------|---------|
| **sql** | `_` is wildcard | `LOGIN1ERROR`, `LOGINAERROR` |
| **glob** | `_` is literal | `LOGIN_ERROR` exactly |

For event types with underscores, use **glob** to treat underscores as literals.
