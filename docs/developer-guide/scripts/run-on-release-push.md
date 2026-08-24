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

The script executes 5 steps sequentially. Step 1 is currently disabled in the script (the full suite runs on every push to `develop`, so a release is cut from an already-tested commit) and is recorded as passed. Steps 3–5 are skipped if any previous step failed.

| Step | Name | Description |
|------|------|-------------|
| 1 | Run All Tests (with Coverage) | **Disabled** — kept in the script for reference; marked as passed |
| 2 | Package Versioned JAR | Runs `mvn package -DskipTests -Drevision=<version>` to produce `target/kete.jar` with the version imprinted in `META-INF/MANIFEST.MF` |
| 3 | Build and Push Docker Images | Auto-discovers images from `quick-starts/$images/`, builds each with Docker, tags as `:<version>` and `:latest`, pushes both tags to `ghcr.io/fortunen/kete` |
| 4 | Build Documentation Site | Runs `python -m mkdocs build --strict` to validate and build the documentation site |
| 5 | Create Git Tag and GitHub Release | Creates an annotated Git tag, pushes it to origin, then creates a GitHub Release via `gh release create` with the JAR attached |

## Coverage Badge

The coverage-badge generation is part of the disabled step 1; the badge is produced by `run-on-develop-push.ps1` instead. For reference, it:

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

- PowerShell 7+
- Java 21
- Maven
- Docker
- Python with `mkdocs-material` and `mkdocs-include-markdown-plugin`
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
  │  STEP 2 of 5 │ PACKAGE VERSIONED JAR                                   │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ kete.jar (12.45 MB)

  ┌──────────────────────────────────────────────────────────────────────────┐
  │  STEP 3 of 5 │ BUILD AND PUSH DOCKER IMAGES                            │
  └──────────────────────────────────────────────────────────────────────────┘

    ✓ quick-start-keycloak [:2026.01.31.18.45 + :latest]
    ✓ quick-start-activemq-artemis [:2026.01.31.18.45 + :latest]
    ✓ quick-start-mosquitto [:2026.01.31.18.45 + :latest]
    ...48 more images...
    ✓ quick-start-zeromq-subscriber [:2026.01.31.18.45 + :latest]

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
    ghcr.io/fortunen/kete/quick-start-activemq-artemis:2026.01.31.18.45
    ...and 49 more images (51 in total)

  GitHub Release:
    https://github.com/FortuneN/kete/releases/tag/2026.01.31.18.45

  ╔══════════════════════════════════════════════════════════════════════╗
  ║                                                                      ║
  ║   ✓  RELEASE 2026.01.31.18.45 PUBLISHED SUCCESSFULLY               ║
  ║                                                                      ║
  ╚══════════════════════════════════════════════════════════════════════╝
```
