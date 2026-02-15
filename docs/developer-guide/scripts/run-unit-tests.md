# run-unit-tests.ps1

Runs unit tests only.

## Usage

```powershell
.\run-unit-tests.ps1
```

## What It Does

1. Sets working directory to project root
2. Runs `mvn clean test` with filter `-Dtest=io.github.fortunen.kete.unittests.**`
3. Uses `-Dsurefire.skipAfterFailureCount=1` to stop on first failure
4. Reports pass/fail with duration

## Maven Command

```
mvn clean test -Dtest=io.github.fortunen.kete.unittests.** -Dsurefire.skipAfterFailureCount=1 -q
```

## Exit Code

- `0` — all unit tests passed
- Non-zero — one or more tests failed

## Prerequisites

- Java 21
- Maven
