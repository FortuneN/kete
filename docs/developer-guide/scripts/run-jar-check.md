# run-jar-check.ps1

Verifies that the shaded `target/kete.jar` only ships classes under the relocation roots `kete/` and `io/github/fortunen/`, so that no bundled library can collide with a class on Keycloak's own classpath.

## Usage

```powershell
mvn package -DskipTests
.\run-jar-check.ps1
.\run-jar-check.ps1 -JarPath target/kete.jar
```

## What It Checks

Every `.class` entry in the JAR must start with `kete/` (a relocated dependency) or `io/github/fortunen/` (KETE itself). Any other class root means a dependency package is missing from the `maven-shade-plugin` relocation list in `pom.xml`; the script prints each offending root with its class count.

## Where It Runs

`run-on-release-push.ps1` runs it right after packaging the versioned JAR (step 2) and fails the release when it reports offenders.

## Exit Code

- `0` — every class is under a relocation root
- `1` — the JAR is missing or ships unrelocated classes

## Prerequisites

- PowerShell 7+
- A built `target/kete.jar`
