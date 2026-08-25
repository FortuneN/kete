# run-on-develop-push.ps1

Develop branch pipeline: runs all tests with coverage, packages and checks the jar, builds the quick-start image, and validates the documentation site. CI (`develop.yml`) runs the same work as four parallel jobs (unit, integration, end-to-end, validate) and merges their coverage in a final `Build & Test` job; this script is the sequential local equivalent.

## Usage

```powershell
.\run-on-develop-push.ps1
```

## Steps

| Step | Name | Description |
|------|------|-------------|
| 1 | Run All Tests (with Coverage) | Calls `run-all-tests.ps1`, then [`run-coverage-badge.ps1`](run-coverage-badge.md) writes the badge |
| 2 | Build Docker Images | Packages `target/kete.jar`, runs [`run-jar-check.ps1`](run-jar-check.md), then builds `ghcr.io/fortunen/kete/quick-start-keycloak:develop` from that jar (validation only, no push) |
| 3 | Build Documentation | Runs `python -m mkdocs build --strict` (validation only, deploy is release-only) |

## Coverage Badge

After tests pass, [`run-coverage-badge.ps1`](run-coverage-badge.md) reads the JaCoCo execution data, calculates line coverage percentage, and writes `coverage-badge.json` with appropriate color:

| Coverage | Color |
|----------|-------|
| ≥ 80% | brightgreen |
| ≥ 60% | green |
| ≥ 40% | yellow |
| < 40% | red |

The Develop workflow (`develop.yml`) produces the same file from the merged shard data and uploads it as the `coverage-badge` workflow artifact; the release pipeline publishes it with the documentation site (see [Release Push](run-on-release-push.md#coverage-badge)). CI never commits the file.

## Output

Displays step progress, a summary table, and published artifact names.

## Exit Code

- `0` — all steps passed
- `1` — one or more steps failed

## Prerequisites

- Java 21, Maven, Docker Desktop
- Python with MkDocs (`mkdocs-material` and `mkdocs-include-markdown-plugin`)
