# Glob Matcher

Unix-style wildcard pattern matching.

## Syntax

```
glob:<pattern>
```

| Wildcard | Matches | Example | Matches |
|----------|---------|---------|---------|
| `*` | Any number of characters | `LOGIN*` | `LOGIN`, `LOGIN_ERROR` |
| `?` | Exactly one character | `USER_?` | `USER_A`, `USER_1` |

Underscores (`_`) are treated as literal characters, not wildcards.

## Escaping Wildcards

To match a literal `*` or `?`, escape it with a backslash:

```bash
# Match "USER_*" literally
glob:USER_\*
```

## Examples

### Prefix Matching

```bash
# All login-related events
kete.routes.kafka.event-matchers.login=glob:LOGIN*
# Matches: LOGIN, LOGIN_ERROR, LOGIN_FAILED

# All user admin events (format: RESOURCETYPE_OPERATIONTYPE)
kete.routes.kafka.event-matchers.user=glob:USER_*
# Matches: USER_CREATE, USER_UPDATE, USER_DELETE
```

### Suffix Matching

```bash
# All error events
kete.routes.kafka.event-matchers.errors=glob:*_ERROR
# Matches: LOGIN_ERROR, REGISTER_ERROR, UPDATE_ERROR

# All delete operations
kete.routes.kafka.event-matchers.deletes=glob:*_DELETE
# Matches: USER_DELETE, CLIENT_DELETE, REALM_DELETE
```

### Single Character Matching

```bash
# Events with single character suffix
kete.routes.kafka.event-matchers.match=glob:USER_?
# Matches: USER_A, USER_1 (not USER_AB)
```

### Exclude Patterns

```bash
# Exclude refresh events (use with ALL mode)
kete.routes.kafka.event-match-mode=all
kete.routes.kafka.event-matchers.all=glob:*
kete.routes.kafka.event-matchers.no-refresh=glob:not:REFRESH*
```

## Configuration Examples

### Multiple Patterns

```bash
# Match login OR user events
kete.routes.kafka.event-matchers.login=glob:LOGIN*
kete.routes.kafka.event-matchers.user=glob:USER_*
```

### Combining with Other Matchers

```bash
# Exact match for LOGIN, glob for USER events
kete.routes.kafka.event-matchers.login=list:LOGIN
kete.routes.kafka.event-matchers.user=glob:USER_*
```

### Realm-Specific Filtering

```bash
# Production realm - only errors
kete.routes.prod.realm-matchers.realm=list:production
kete.routes.prod.event-matchers.errors=glob:*_ERROR

# Dev realm - all login events
kete.routes.dev.realm-matchers.realm=list:development
kete.routes.dev.event-matchers.login=glob:LOGIN*
```

## Case Sensitivity

All matchers in KETE are **case-insensitive**:

- `glob:LOGIN*` matches `LOGIN`, `login`, `Login`
- `glob:login*` matches `LOGIN`, `login`

