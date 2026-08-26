# run-coverage-badge.ps1

Merges one or more JaCoCo execution files, writes the coverage report to `target/site/jacoco/` and produces `coverage-badge.json` (the [shields.io endpoint](https://shields.io/badges/endpoint-badge) format the README badge reads).

## Usage

```powershell
.\run-coverage-badge.ps1                                                        # target/jacoco.exec
.\run-coverage-badge.ps1 -ExecFiles coverage/unit/jacoco.exec,coverage/integration/jacoco.exec,coverage/e2e/jacoco.exec
.\run-coverage-badge.ps1 -BadgePath site/coverage-badge.json
```

## What It Does

1. Downloads the JaCoCo command-line tool once (`org.jacoco:org.jacoco.cli:nodeps` into `target/`) and compiles the sources if `target/classes` is missing.
2. Merges the execution files (`jacoco merge`) when more than one is given — this is how the CI shards (unit, integration, end-to-end) are combined into one number.
3. Generates the CSV, HTML and XML report (`jacoco report`).
4. Sums `LINE_COVERED` / `LINE_MISSED` from the CSV and writes the badge JSON with the colour thresholds below.

| Coverage | Color |
|----------|-------|
| ≥ 80% | `brightgreen` |
| ≥ 60% | `green` |
| ≥ 40% | `yellow` |
| < 40% | `red` |

## Where It Runs

- `run-on-develop-push.ps1` calls it after the sequential local run (single `target/jacoco.exec`).
- The Develop workflow's final `Build & Test` job calls it with the three shard files downloaded as artifacts, then uploads `coverage-badge.json` as the `coverage-badge` artifact that the release publishes with the documentation site.

## Exit Code

- `0` — badge written
- `1` — no execution file found, or the JaCoCo CLI, compile, merge or report step failed

## Prerequisites

- PowerShell 7+, Java 21, Maven
