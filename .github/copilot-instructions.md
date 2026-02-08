# Copilot Instructions for KETE

## Before Starting Work

1. **Review the codebase** - Familiarize yourself with both the core source code (`src/main/java`) and test code (`src/tests/java`) to understand the project structure, patterns, and conventions.

2. **Read the test patterns document** - Always read [docs/developer-guide/test-patterns-and-conventions.md](../docs/developer-guide/test-patterns-and-conventions.md) before writing or modifying tests to ensure consistency.

3. **Follow existing conventions** - All code updates must follow conventions already prevalent in the codebase. Study existing patterns before making changes.

4. **Discussion mode** - When the user says "let's discuss", engage in discussion only until they say "let's apply", "execute", "do", or similar action words. Do not make changes during discussion.

5. **Ask questions, don't assume** - When requirements are unclear or ambiguous, ask clarifying questions rather than making assumptions.

6. **Use internet resources, don't assume** - When required actions are unclear or ambiguous, ask consult relevant product/plugin/tool/platform/e.t.c documentation or other internet resources rather than making assumptions.

7. use var as much as possible

8. Validation tools as much as possible

9. ALWAYS commit before and after making changes [COMPILE AND RUN BEFORE COMMIT], with clear commit messages describing the change and its purpose. This ensures a clear history of changes and allows for easy rollback if needed and no destruction for progress.

10. Do not use Git for any other reason other than for (9). Strictly no other exceptions

11. Always remove unused imports and code, and fix any warnings before committing. This ensures a clean codebase and reduces technical debt.

12. No javadocs - remove all you find and generate any

13. No new info logs - dont remove exusting, dont generate any (low nopise philosphy, only log the unexpected, not the expected, and that excludes info logs for expected operations)

## During Long-Running Operations

### Real-Time Progress Reporting for Tests

When running tests or any long-running terminal operations:

- **Poll terminal output regularly** while tests execute
- **Report results per test class** as they complete (e.g., "✅ MyTests: 15 tests, 0 failures")
- **Track pass/fail status progressively** - don't wait until the end
- **Highlight any failures immediately** when they occur
- **Provide a final summary** with total test count, failures, errors, and duration

This applies to:
- `mvn test` runs
- `mvn verify` runs
- Any test execution via IDE or command line
- Build operations that take more than a few seconds

## Project Context

- **Language**: Java 21
- **Build Tool**: Maven
- **Testing**: JUnit 5 with Testcontainers
- **Purpose**: Keycloak event listener that forwards events to various destinations (Kafka, MQTT, AMQP, HTTP, etc.)

### Dependency Shading (CRITICAL)

**MANDATORY RULE**: All runtime dependencies (non-`provided`, non-`test` scope) MUST be shade-relocated in `pom.xml`.

**WHY**: 
- Keycloak bundles many libraries internally (Guava, Jackson, Netty, Apache Commons)
- Different Keycloak versions use different library versions (e.g., 25.0.6 vs 26.0.0)
- Without shading: `NoSuchMethodError`, `ClassNotFoundException`, version conflicts
- With shading: Extension works across Keycloak 25.x → 26.x+ without recompilation

**WHAT TO SHADE**: Every dependency without `<scope>provided</scope>` or `<scope>test</scope>`

**NEVER**: Remove relocations from the Maven Shade Plugin configuration without understanding multi-version Keycloak compatibility impact.

### Integration & E2E Test Policy (CRITICAL)

**MANDATORY RULE**: Tests requiring containers or external services are **expensive**. Strict limits apply per destination:

- **Integration tests**: Exactly **3 send tests** per destination:
  1. `shouldSend_NonTls` — plain text connection
  2. `shouldSend_Tls` — TLS (server auth only, client trusts server)
  3. `shouldSend_mTls` — mutual TLS (both client and server authenticate)

- **E2E tests**: Exactly **1 test** per destination

- **No other integration test files**: No `initializeTests`, `closeTests`, or extra send tests. Only `TestBase.java` + `sendTests.java`.

- **Nginx proxy fallback**: If the destination broker/server does NOT natively support TLS/mTLS, use an nginx reverse proxy container to terminate TLS and still run all 3 tests.

**REFERENCE PATTERN**: See `integrationtests/httpdestination/` for the canonical implementation of this policy.

### Destination Documentation (CRITICAL)

**MANDATORY RULE**: Every destination MUST have a documentation page at `docs/user-guide/destinations/<kind>.md` that follows the **exact section order** below. Study existing pages (e.g., `kafka.md`, `http.md`, `pulsar.md`) before writing.

**Required sections in strict order:**

1. **Title** — `# <Name> Destination`
2. **One-liner** — "Stream Keycloak events to <system>."
3. **Kind/Protocol table** — 2-row table: `destination.kind` value + Protocol name
4. **`## Compatible Systems`** — Table of compatible brokers/services with notes
5. **`## Example Configurations`** — Tabbed examples using `=== "Tab Name"` syntax (minimum 2 tabs)
6. **`## Features`** — Bullet list of capabilities
7. **`## Configuration Properties`** — Subsections in order:
   - `### Required Properties` — table
   - `### Optional Properties` — table with defaults
   - `### Dynamic <thing> (Templating)` — if destination supports templating (topic, subject, URL, etc.)
   - `### Custom Headers` — if applicable
   - `### Authentication` — if destination has auth (sub-sections per auth method)
   - `### TLS Properties` — reference to `overview.md#tls-mtls` plus destination-specific notes
8. **`## Configuration Examples`** — Numbered examples (`### Example 1: <Title>`, `### Example 2: <Title>`, etc.)
9. **`## Quick Starts`** — Table linking to quickstart folders
10. **`## See Also`** — Links to Serializers, Matchers, Event Types, Certificate Loaders

**When adding a new destination, also update:**

- `mkdocs.yml` nav → add entry under `Destinations:` (alphabetical among peers, after existing entries)
- `docs/user-guide/destinations/overview.md` → Available Destinations table, Cloud Services Compatibility table (if cloud), Message Headers table, Quick Examples section
- `docs/user-guide/destinations/support-matrix.md` → Quick Reference Matrix (add column + row), "By Protocol" section, Decision Guide tree, Performance Considerations table, Available Quickstarts table

**REFERENCE PAGES**: `kafka.md` (complex, many examples), `http.md` (OAuth, headers), `pulsar.md` (auth methods, batching), `zeromq.md` (limitations section pattern)
