# run-on-develop-push.ps1

Develop branch pipeline. Runs all tests with coverage, builds Docker images, and validates the documentation site.

## Usage

```powershell
.\run-on-develop-push.ps1
```

## Steps

| Step | Name | Description |
|------|------|-------------|
| 1 | Run All Tests (with Coverage) | Calls `run-all-tests.ps1`, then generates coverage badge from JaCoCo CSV |
| 2 | Build Docker Images | Builds `ghcr.io/fortunen/kete/quick-start-keycloak:develop` (validation only, no push) |
| 3 | Build Documentation | Runs `python -m mkdocs build --strict` (validation only, deploy is release-only) |

## Coverage Badge

After tests pass, the script reads `target/site/jacoco/jacoco.csv`, calculates line coverage percentage, and writes `coverage-badge.json` with appropriate color:

| Coverage | Color |
|----------|-------|
| ≥ 80% | brightgreen |
| ≥ 60% | green |
| ≥ 40% | yellow |
| < 40% | red |

## Output

Displays step progress, a summary table, and published artifact names.

## Exit Code

- `0` — all steps passed
- `1` — one or more steps failed

## Prerequisites

- Java 21, Maven, Docker Desktop
- Python with MkDocs (`mkdocs` and `mkdocs-material`)
