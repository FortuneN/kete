# Matchers

Filter which events flow through a route.

## Overview

Matchers decide whether an event should be processed by a route. They're **optional** — without matchers, a route accepts all events.

**Two types:**

- **Realm matchers** — filter by realm name
- **Event matchers** — filter by event type

## Available Matchers

| Matcher | Config |
|---------|--------|
| **[List](list.md)** | `list:...` |
| **[Glob](glob.md)** | `glob:...` |
| **[Regex](regex.md)** | `regex:...` |
| **[SQL](sql.md)** | `sql:...` |

## Quick Examples

**Include specific events:**
```bash
kete.routes.audit.event-matchers.filter=list:LOGIN,LOGOUT,REGISTER
```

**Match patterns:**
```bash
kete.routes.errors.event-matchers.filter=glob:*_ERROR
```

**Filter by realm:**
```bash
kete.routes.prod.realm-matchers.filter=list:production
```

## Match Modes

When using multiple matchers, control how they combine:

| Mode | Behavior |
|------|----------|
| `any` (default) | Event passes if **any** matcher matches |
| `all` | Event passes only if **all** matchers match |

```bash
kete.routes.myroute.event-match-mode=all
```
