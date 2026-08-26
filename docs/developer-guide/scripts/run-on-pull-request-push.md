# run-on-pull-request-push.ps1

PR validation pipeline: runs all tests, packages and checks the jar, builds the quick-start image, and validates the documentation site. CI (`pull-request.yml`) runs the same work as four parallel jobs (unit, integration, end-to-end, validate) behind a final `Build & Test` job; this script is the sequential local equivalent.

## Usage

```powershell
.\run-on-pull-request-push.ps1
```

## Steps

| Step | Name | Description |
|------|------|-------------|
| 1 | Run All Tests | Calls `run-all-tests.ps1` (unit → integration → E2E) |
| 2 | Build Quick-Start Docker Images | Packages `target/kete.jar`, runs [`run-jar-check.ps1`](run-jar-check.md), then builds `ghcr.io/fortunen/kete/quick-start-keycloak` from that jar (validation only, no push) |
| 3 | Build Documentation Site | Runs `python -m mkdocs build --strict` to validate docs |

## Output

Displays a step-by-step progress table and a validation summary:

```
  ╔══════════════════════════════════════════════════════════╗
  ║   ✓  PR VALIDATION PASSED                                ║
  ║   Ready to merge!                                        ║
  ╚══════════════════════════════════════════════════════════╝
```

## Exit Code

- `0` — all validation steps passed
- `1` — one or more steps failed

## Prerequisites

- Java 21, Maven, Docker Desktop
- Python with MkDocs (`mkdocs-material` and `mkdocs-include-markdown-plugin`)
