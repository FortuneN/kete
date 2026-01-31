# Regex Matcher

Full regular expression pattern matching.

## Syntax

```
regex:<pattern>
```

## Examples

### Basic Patterns

```bash
# Match LOGIN or LOGOUT exactly
kete.routes.kafka.event-matchers.auth=regex:^(LOGIN|LOGOUT)$

# Events starting with LOGIN
kete.routes.kafka.event-matchers.login=regex:^LOGIN

# Events ending with ERROR
kete.routes.kafka.event-matchers.errors=regex:ERROR$
```

### Character Classes

```bash
# USER followed by uppercase letter and digits
kete.routes.kafka.event-matchers.pattern=regex:^USER_[A-Z][0-9]+$

# Events containing numbers
kete.routes.kafka.event-matchers.pattern=regex:.*[0-9].*
```

### Optional Patterns

```bash
# LOGIN with optional _ERROR suffix
kete.routes.kafka.event-matchers.pattern=regex:^LOGIN(_ERROR)?$

# USER with optional operation
kete.routes.kafka.event-matchers.pattern=regex:^USER(_CREATE|_UPDATE|_DELETE)?$
```

### Exclude Patterns

```bash
# Exclude refresh events (use with ALL mode)
kete.routes.kafka.event-match-mode=all
kete.routes.kafka.event-matchers.all=glob:*
kete.routes.kafka.event-matchers.no-refresh=regex:not:.*REFRESH.*
```

## Configuration Examples

### Multiple Patterns

```bash
# Match login events OR user create events
kete.routes.kafka.event-matchers.login=regex:^LOGIN
kete.routes.kafka.event-matchers.user-create=regex:^USER_CREATE$
```

### Combining with Other Matchers

```bash
# Exact match for LOGIN, regex for USER events
kete.routes.kafka.event-matchers.login=list:LOGIN
kete.routes.kafka.event-matchers.user-ops=regex:^USER_(CREATE|UPDATE)$
```

### Realm-Specific Filtering

```bash
# Production - strict pattern matching
kete.routes.prod.realm-matchers.realm=list:production
kete.routes.prod.event-matchers.pattern=regex:^(LOGIN|LOGOUT|REGISTER)$
```

## Case Sensitivity

All matchers in KETE are **case-insensitive**:

- `regex:^LOGIN$` matches `LOGIN`, `login`, `Login`
- `regex:^login$` matches `LOGIN`, `login`

## Regex Reference

### Metacharacters

| Character | Meaning | Example |
|-----------|---------|---------|
| `.` | Any character | `L.GIN` matches `LOGIN`, `LAGIN` |
| `^` | Start of string | `^LOGIN` matches `LOGIN_ERROR` |
| `$` | End of string | `ERROR$` matches `LOGIN_ERROR` |
| `*` | Zero or more | `LOGIN.*` matches `LOGIN`, `LOGIN_ERROR` |
| `+` | One or more | `LOGIN.+` matches `LOGIN_ERROR` (not `LOGIN`) |
| `?` | Zero or one | `LOGIN_?ERROR` matches `LOGINERROR`, `LOGIN_ERROR` |
| `\|` | Alternation | `LOGIN\|LOGOUT` matches either |
| `[]` | Character class | `[A-Z]+` matches uppercase letters |
| `()` | Grouping | `(LOGIN\|LOGOUT)_ERROR` |

### Quantifiers

| Quantifier | Meaning | Example |
|------------|---------|---------|
| `{3}` | Exactly 3 | `^.{3}$` matches 3-character strings |
| `{2,5}` | 2 to 5 | `^.{2,5}$` matches 2-5 characters |
| `{4,}` | 4 or more | `^.{4,}$` matches 4+ characters |
