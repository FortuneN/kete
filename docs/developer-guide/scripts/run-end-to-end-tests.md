# run-end-to-end-tests.ps1

Runs end-to-end tests only. These tests use Testcontainers to spin up Keycloak with the extension installed alongside a destination container, then trigger real events.

## Usage

```powershell
.\run-end-to-end-tests.ps1
```

## What It Does

1. Sets working directory to project root
2. Runs `mvn clean test` with filter `-Dtest=io.github.fortunen.kete.endtoendtests.**`
3. Uses `-Dsurefire.skipAfterFailureCount=1` to stop on first failure
4. Reports pass/fail with duration

## Maven Command

```
mvn clean test -Dtest=io.github.fortunen.kete.endtoendtests.** -Dsurefire.skipAfterFailureCount=1 -q
```

## Exit Code

- `0` — all end-to-end tests passed
- Non-zero — one or more tests failed

## Prerequisites

- Java 21
- Maven
- Docker Desktop (Testcontainers launches containers automatically)
