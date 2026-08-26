# Contributing to KETE

Thank you for considering a contribution. This page is the short version; the [developer guide](https://fortunen.github.io/kete/developer-guide/development/) has the details.

## Before you start

- Open an issue for anything larger than a typo fix so the approach can be agreed first.
- Read the [test patterns and conventions](https://fortunen.github.io/kete/developer-guide/test-patterns-and-conventions/) — pull requests that do not follow them are sent back.
- Work on a branch off `develop`; `release` only ever receives merges from `develop`.

## Building and testing

Java 21, Maven 3.9+, Docker (for integration and end-to-end tests) and PowerShell 7 are required.

```powershell
.\run-unit-tests.ps1          # ~2 minutes, no Docker
.\run-integration-tests.ps1   # broker and emulator containers
.\run-end-to-end-tests.ps1    # the shaded jar inside a real Keycloak
.\run-all-tests.ps1           # everything, as CI runs it
```

Documentation must build strictly (`python -m mkdocs build --strict`) and the shaded jar must pass `.\run-jar-check.ps1` after `mvn package -DskipTests`.

## Conventions that are enforced in review

- `var` for local variables, no Javadoc, no new INFO logs, no inline fully-qualified class names, no unused code or imports, warnings fixed.
- Destination unit tests perform zero I/O; each destination has exactly three integration send tests (plus `isHealthyTests` when it exposes a health probe) and one end-to-end test.
- Every runtime dependency is shaded and relocated (`run-jar-check.ps1` enforces it).
- Configuration is read from environment variables only; a new option needs a documentation row, a unit test for its default and for its explicit value, and — this is a hard rule — **must not change what an existing installation does**: new behaviour is opt-in and existing defaults keep their meaning.
- Commit before and after a change with a message that says what changed and why.

## Pull requests

- `develop` and `release` are protected: contributions arrive through pull requests with one approving review, and force-pushes to `release` are refused. Target `develop`; the `Build & Test` workflow (unit, integration and end-to-end suites, image build, strict docs build) runs on every pull request and must pass before the merge.
- Keep the change focused; update the documentation page of every option or behaviour you touch.
- Releases are cut by merging `develop` into `release` with a merge commit; the release workflow refuses to publish a tree that has not passed the Develop workflow.
