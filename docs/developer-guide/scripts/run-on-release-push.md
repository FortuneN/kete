# Release Push

Pipeline script executed when pushing to the `release` branch. Creates a full production release with versioned artifacts, Docker images, documentation, and a GitHub Release.

## Usage

```powershell
.\run-on-release-push.ps1
```

## Version Format

The version is generated automatically from the current timestamp:

```
yyyy.MM.dd.HH.mm
```

Example: `2026.01.31.18.45`

This version is used for the JAR manifest, Docker image tags, Git tag, and GitHub Release title.

## Pipeline Steps

The script executes 5 steps sequentially. Each step requires all previous steps to have passed — if any step fails, subsequent steps are skipped and the release is aborted.

| Step | Name | Description |
|------|------|-------------|
| 1 | Run All Tests (with Coverage) | Executes `run-all-tests.ps1` (unit → integration → E2E), then generates a coverage badge from JaCoCo results |
| 2 | Package Versioned JAR | Runs `mvn package -DskipTests -Drevision=<version>` to produce `target/kete.jar` with the version imprinted in `META-INF/MANIFEST.MF` |
| 3 | Build and Push Docker Images | Auto-discovers images from `quick-starts/$images/`, builds each with Docker, tags as `:<version>` and `:latest`, pushes both tags to `ghcr.io/fortunen/kete` |
| 4 | Build Documentation Site | Runs `python -m mkdocs build --strict` to validate and build the documentation site |
| 5 | Create Git Tag and GitHub Release | Creates an annotated Git tag, pushes it to origin, then creates a GitHub Release via `gh release create` with the JAR attached |

## Coverage Badge

After tests pass, the script generates a coverage badge:

1. Reads `target/site/jacoco/jacoco.csv`
2. Calculates line coverage percentage from `LINE_MISSED` and `LINE_COVERED` columns
3. Writes `coverage-badge.json` with color thresholds:

| Coverage | Color |
|----------|-------|
| ≥ 80% | `brightgreen` |
| ≥ 60% | `green` |
| ≥ 40% | `yellow` |
| < 40% | `red` |

## Docker Image Discovery

Images are discovered automatically from the `quick-starts/$images/` directory. Each subdirectory becomes an image named `quick-start-<subdirectory>`.

- Registry: `ghcr.io/fortunen/kete`
- Tags per image: `:<version>` and `:latest`
- The `keycloak` image uses the repository root as its build context (for multi-stage Maven builds); all other images use their own directory as context

## GitHub Release

The release is created using the GitHub CLI (`gh`):

- Tag name: the generated version (e.g., `2026.01.31.18.45`)
- Title: same as tag name
- Attachment: `target/kete.jar`

## Exit Codes

| Code | Meaning |
|------|---------|
| `0` | All 5 steps completed successfully |
| `1` | One or more steps failed |

## Prerequisites

- Java 21
- Maven
- Docker
- Python with MkDocs and plugins installed
- GitHub CLI (`gh`) authenticated
- Push access to `ghcr.io/fortunen/kete`
- Push access to the Git repository

## Sample Output

```
══════════════════════════════════════════════════════════════════════════════════

  KETE │ Release Push
  Creating production release 2026.01.31.18.45

══════════════════════════════════════════════════════════════════════════════════

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 1 of 5 │ RUN ALL TESTS (WITH COVERAGE)                           │
  └──────────────────────────────────────────────────────────────────────────┘

    ...test output...

    ✓ Test execution complete [4.2 min]
    ✓ Coverage: 85.3% (badge updated)

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 2 of 5 │ PACKAGE VERSIONED JAR                                   │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ kete.jar (12.45 MB)

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 3 of 5 │ BUILD AND PUSH DOCKER IMAGES                            │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ quick-start-keycloak [:2026.01.31.18.45 + :latest]

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 4 of 5 │ BUILD DOCUMENTATION SITE                                │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ Documentation site built successfully

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 5 of 5 │ CREATE GIT TAG AND GITHUB RELEASE                       │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ Git tag 2026.01.31.18.45
    ✓ GitHub Release 2026.01.31.18.45

══════════════════════════════════════════════════════════════════════════════════

  RELEASE SUMMARY │ 2026.01.31.18.45

══════════════════════════════════════════════════════════════════════════════════

  Release Artifact:
    kete.jar (2026.01.31.18.45)

  Docker Images Published:
    ghcr.io/fortunen/kete/quick-start-keycloak:2026.01.31.18.45

  GitHub Release:
    https://github.com/FortuneN/kete/releases/tag/2026.01.31.18.45

  ╔══════════════════════════════════════════════════════════════════════╗
  ║                                                                      ║
  ║   ✓  RELEASE 2026.01.31.18.45 PUBLISHED SUCCESSFULLY               ║
  ║                                                                      ║
  ╚══════════════════════════════════════════════════════════════════════╝
```
