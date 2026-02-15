# run-integration-tests.ps1

Runs integration tests only. These tests use Testcontainers to spin up real broker/service containers.

## Usage

```powershell
.\run-integration-tests.ps1
```

## What It Does

1. Sets working directory to project root
2. Runs `mvn clean test` with filter `-Dtest=io.github.fortunen.kete.integrationtests.**`
3. Uses `-Dsurefire.skipAfterFailureCount=1` to stop on first failure
4. Reports pass/fail with duration

## Maven Command

```
mvn clean test -Dtest=io.github.fortunen.kete.integrationtests.** -Dsurefire.skipAfterFailureCount=1 -q
```

## Exit Code

- `0` — all integration tests passed
- Non-zero — one or more tests failed

## Prerequisites

- Java 21
- Maven
- Docker Desktop (Testcontainers launches containers automatically)
