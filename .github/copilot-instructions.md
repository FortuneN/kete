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
