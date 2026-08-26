# run-all-tests.ps1

Runs all three test suites sequentially: unit tests → integration tests → end-to-end tests. Aborts immediately if any suite fails.

## Usage

```powershell
.\run-all-tests.ps1
```

## What It Does

1. Runs `mvn clean` once
2. Runs unit tests (`io.github.fortunen.kete.unittests.**`) — aborts on failure
3. Runs integration tests (`io.github.fortunen.kete.integrationtests.**`) — aborts on failure
4. Runs end-to-end tests (`io.github.fortunen.kete.endtoendtests.**`) — aborts on failure
5. Prints summary with per-suite and total durations

## Execution Order

```
Unit Tests → Integration Tests → End-to-End Tests
```

Each suite must pass before the next one starts. All suites use `-Dsurefire.skipAfterFailureCount=1`.

## Output

```
  ✓ ALL TESTS PASSED!

  Unit tests:        101s
  Integration tests: 2123.7s
  End-to-end tests:  1503.7s
  ─────────────────────────────
  Total:             3728.4s
```

## Exit Code

- `0` — all three suites passed
- Non-zero — the first failing suite's exit code

## Prerequisites

- Java 21
- Maven
- Docker Desktop (for integration and end-to-end tests)
