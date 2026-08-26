# =============================================================================
#
#   KETE │ Coverage Badge
#
#   Merges one or more JaCoCo execution files, generates the report under
#   target/site/jacoco and writes coverage-badge.json (shields.io endpoint
#   format) with the overall line coverage.
#
#   Usage: .\run-coverage-badge.ps1 [-ExecFiles target/jacoco.exec[,...]] [-BadgePath coverage-badge.json]
#
# =============================================================================

param(
    [string[]]$ExecFiles = @("target/jacoco.exec"),
    [string]$BadgePath = "coverage-badge.json",
    [string]$JacocoVersion = "0.8.13"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# `pwsh -File` hands "a,b,c" over as one string
$ExecFiles = @($ExecFiles | ForEach-Object { $_ -split "," } | ForEach-Object { $_.Trim() } | Where-Object { $_ })

$existing = @($ExecFiles | Where-Object { Test-Path $_ })

if ($existing.Count -eq 0) {
    Write-Host "  ✗ No JaCoCo execution file found ($($ExecFiles -join ', '))" -ForegroundColor Red
    exit 1
}

# JaCoCo command-line tool (merge + report without a POM execution)

$cliDirectory = "target/jacoco-cli"
$cli = Get-ChildItem -Path $cliDirectory -Filter "org.jacoco.cli*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($null -eq $cli) {
    New-Item -ItemType Directory -Force $cliDirectory | Out-Null
    $copyOutput = mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy "-Dartifact=org.jacoco:org.jacoco.cli:${JacocoVersion}:jar:nodeps" "-DoutputDirectory=$cliDirectory" 2>&1
    $cli = Get-ChildItem -Path $cliDirectory -Filter "org.jacoco.cli*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($LASTEXITCODE -ne 0 -or $null -eq $cli) {
        $copyOutput | Out-Host
        Write-Host "  ✗ Could not download the JaCoCo CLI" -ForegroundColor Red
        exit 1
    }
}

$cli = $cli.FullName

# the report needs the compiled classes

if (-not (Test-Path "target/classes/io/github/fortunen/kete")) {
    mvn -q compile 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ✗ mvn compile failed" -ForegroundColor Red
        exit 1
    }
}

# merge

$merged = $existing[0]

if ($existing.Count -gt 1) {
    $merged = "target/jacoco-merged.exec"
    java -jar $cli merge @existing --destfile $merged 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ✗ JaCoCo merge failed" -ForegroundColor Red
        exit 1
    }
}

# report

New-Item -ItemType Directory -Force "target/site/jacoco" | Out-Null

java -jar $cli report $merged --classfiles target/classes --sourcefiles src/main/java --csv target/site/jacoco/jacoco.csv --html target/site/jacoco --xml target/site/jacoco/jacoco.xml 2>&1 | Out-Null

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ JaCoCo report failed" -ForegroundColor Red
    exit 1
}

# badge

$csv = Import-Csv "target/site/jacoco/jacoco.csv"
$totalMissed = ($csv | Measure-Object -Property LINE_MISSED -Sum).Sum
$totalCovered = ($csv | Measure-Object -Property LINE_COVERED -Sum).Sum
$total = $totalMissed + $totalCovered
$coveragePercent = if ($total -gt 0) { [math]::Round(($totalCovered / $total) * 100, 1) } else { 0 }
$color = if ($coveragePercent -ge 80) { "brightgreen" } elseif ($coveragePercent -ge 60) { "green" } elseif ($coveragePercent -ge 40) { "yellow" } else { "red" }
$badgeJson = @{ schemaVersion = 1; label = "Coverage"; message = "$coveragePercent%"; color = $color } | ConvertTo-Json

Set-Content -Path $BadgePath -Value $badgeJson -Encoding UTF8

Write-Host "  ✓ Coverage: $coveragePercent% from $($existing.Count) execution file(s) → $BadgePath" -ForegroundColor Green
exit 0
