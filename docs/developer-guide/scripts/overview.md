# Scripts

PowerShell automation scripts in the repository root for testing, CI/CD, and development tasks.

## Available Scripts

| Script | Purpose | Guide |
|--------|---------|-------|
| `run-unit-tests.ps1` | Run unit tests | [Guide →](run-unit-tests.md) |
| `run-integration-tests.ps1` | Run integration tests (Testcontainers) | [Guide →](run-integration-tests.md) |
| `run-end-to-end-tests.ps1` | Run end-to-end tests (Testcontainers) | [Guide →](run-end-to-end-tests.md) |
| `run-all-tests.ps1` | Run all test suites sequentially | [Guide →](run-all-tests.md) |
| `run-quick-starts.ps1` | Test quick-start Docker Compose stacks | [Guide →](run-quick-starts.md) |
| `run-on-pull-request-push.ps1` | PR validation pipeline | [Guide →](run-on-pull-request-push.md) |
| `run-on-develop-push.ps1` | Develop branch pipeline | [Guide →](run-on-develop-push.md) |
| `run-on-release-push.ps1` | Release pipeline | [Guide →](run-on-release-push.md) |
| `run-jar-check.ps1` | Verify every class in the shaded JAR is relocated | [Guide →](run-jar-check.md) |
| `run-push.ps1` | Squash commits and push | [Guide →](run-push.md) |
| `run-docs-preview.ps1` | Preview documentation site locally | [Guide →](run-docs-preview.md) |

## Script Categories

### Testing

Scripts that execute Maven test suites against the codebase.

| Script | Scope | Maven Filter |
|--------|-------|-------------|
| `run-unit-tests.ps1` | Unit tests only | `io.github.fortunen.kete.unittests.**` |
| `run-integration-tests.ps1` | Integration tests only | `io.github.fortunen.kete.integrationtests.**` |
| `run-end-to-end-tests.ps1` | End-to-end tests only | `io.github.fortunen.kete.endtoendtests.**` |
| `run-all-tests.ps1` | All three suites | Runs each sequentially, aborts on failure |
| `run-quick-starts.ps1` | Quick-start stacks | Docker Compose up → login → verify metrics → verify reception → down |

### CI/CD Pipeline

Scripts that simulate CI/CD pipeline stages locally.

| Script | Trigger Context | Steps |
|--------|----------------|-------|
| `run-on-pull-request-push.ps1` | Pull request | Tests → Docker image build → MkDocs build |
| `run-on-develop-push.ps1` | Merge to develop | Tests + coverage → Docker image build → MkDocs build |
| `run-on-release-push.ps1` | Merge to release | Versioned JAR → versioned Docker push → MkDocs build → GitHub Release → `:latest` tags (tests are not re-run; `release.yml` first verifies that the tree passed the Develop workflow) |

### Utility

| Script | Purpose |
|--------|---------|
| `run-push.ps1` | Squashes all commits since merge-base with `origin/develop` into one, prompts for message, pushes |
| `run-docs-preview.ps1` | Starts local MkDocs dev server for documentation preview |

## Prerequisites

- PowerShell 7 or later (`pwsh`)
- Java 21
- Maven
- Docker Desktop (for integration tests, E2E tests, quick-starts, and CI/CD scripts)
- Python with `mkdocs-material` and `mkdocs-include-markdown-plugin` (for documentation scripts)
- GitHub CLI `gh` (for release script)
