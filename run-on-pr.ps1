# =============================================================================
#
#   KETE │ Pull Request Validation
#
#   Validates that the PR is ready to merge by running:
#     • All tests (unit, integration, end-to-end)
#     • Quick-start Docker image builds
#     • Documentation site build
#
#   Usage: .\run-on-pr.ps1
#
# =============================================================================

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# -----------------------------------------------------------------------------
# Configuration
# -----------------------------------------------------------------------------

$script:TotalSteps = 3
$script:Results = @{}
$script:StartTime = Get-Date

# -----------------------------------------------------------------------------
# Helper Functions
# -----------------------------------------------------------------------------

function Write-Banner {
    param([string]$Title, [string]$Subtitle = "", [string]$Color = "Cyan")
    $width = 80
    Write-Host ""
    Write-Host ("═" * $width) -ForegroundColor $Color
    Write-Host ""
    Write-Host "  $Title" -ForegroundColor $Color
    if ($Subtitle) {
        Write-Host "  $Subtitle" -ForegroundColor DarkGray
    }
    Write-Host ""
    Write-Host ("═" * $width) -ForegroundColor $Color
}

function Write-StepHeader {
    param([int]$Number, [string]$Name)
    Write-Host ""
    Write-Host ""
    Write-Host "  ┌──────────────────────────────────────────────────────────────────────────┐" -ForegroundColor DarkCyan
    Write-Host "  │  STEP $Number of $script:TotalSteps │ $($Name.ToUpper().PadRight(59))" -NoNewline -ForegroundColor DarkCyan
    Write-Host " │" -ForegroundColor DarkCyan
    Write-Host "  └──────────────────────────────────────────────────────────────────────────┘" -ForegroundColor DarkCyan
    Write-Host ""
}

function Write-Task {
    param([string]$Message)
    Write-Host "    ► " -NoNewline -ForegroundColor DarkGray
    Write-Host $Message -ForegroundColor Gray
}

function Write-TaskResult {
    param([string]$Message, [bool]$Success, [string]$Duration = "")
    $icon = if ($Success) { "✓" } else { "✗" }
    $color = if ($Success) { "Green" } else { "Red" }
    $suffix = if ($Duration) { " [$Duration]" } else { "" }
    Write-Host "    $icon " -NoNewline -ForegroundColor $color
    Write-Host "$Message" -NoNewline -ForegroundColor White
    Write-Host $suffix -ForegroundColor DarkGray
}

function Format-Duration {
    param([TimeSpan]$Duration)
    if ($Duration.TotalMinutes -ge 1) {
        return "$([math]::Round($Duration.TotalMinutes, 1)) min"
    } else {
        return "$([math]::Round($Duration.TotalSeconds, 1)) sec"
    }
}

function Write-SummaryTable {
    param([hashtable]$Results)

    $passed = @($Results.Values | Where-Object { $_ -eq $true }).Count
    $failed = @($Results.Values | Where-Object { $_ -eq $false }).Count
    $total = $Results.Count

    Write-Host ""
    Write-Host "  ┌────────────────────────────────────────────┬──────────┐" -ForegroundColor DarkGray
    Write-Host "  │  Validation                                │  Status  │" -ForegroundColor DarkGray
    Write-Host "  ├────────────────────────────────────────────┼──────────┤" -ForegroundColor DarkGray

    foreach ($key in $Results.Keys | Sort-Object) {
        $success = $Results[$key]
        $icon = if ($success) { "  ✓  " } else { "  ✗  " }
        $color = if ($success) { "Green" } else { "Red" }
        $paddedKey = ("  " + $key).PadRight(44)
        Write-Host "$paddedKey│" -NoNewline -ForegroundColor DarkGray
        Write-Host $icon -NoNewline -ForegroundColor $color
        Write-Host "    │" -ForegroundColor DarkGray
    }

    Write-Host "  └────────────────────────────────────────────┴──────────┘" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "  Results: " -NoNewline -ForegroundColor Gray
    Write-Host "$passed passed" -NoNewline -ForegroundColor Green
    Write-Host ", " -NoNewline -ForegroundColor Gray
    if ($failed -gt 0) {
        Write-Host "$failed failed" -ForegroundColor Red
    } else {
        Write-Host "$failed failed" -ForegroundColor Green
    }
}

# -----------------------------------------------------------------------------
# Main Script
# -----------------------------------------------------------------------------

Write-Banner "KETE │ Pull Request Validation" "Validating PR readiness..." "Magenta"

Write-Host ""
Write-Host "  Started:  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Branch:   $(git rev-parse --abbrev-ref HEAD 2>$null)" -ForegroundColor DarkGray
Write-Host "  Commit:   $(git rev-parse --short HEAD 2>$null)" -ForegroundColor DarkGray

# =============================================================================
# Step 1: Run All Tests
# =============================================================================

Write-StepHeader 1 "Run All Tests"

$stepStart = Get-Date
Write-Task "Executing test suites (unit, integration, end-to-end)..."
Write-Host ""

& .\run-all-tests.ps1
$testsPassed = $LASTEXITCODE -eq 0
$duration = Format-Duration((Get-Date) - $stepStart)

Write-Host ""
Write-TaskResult "Test execution complete" $testsPassed $duration
$script:Results["Tests (Unit + Integration + E2E)"] = $testsPassed

# =============================================================================
# Step 2: Build Docker Images
# =============================================================================

Write-StepHeader 2 "Build Quick-Start Docker Images"

$stepStart = Get-Date

# quick-start-keycloak
Write-Task "Building image: ghcr.io/fortunen/kete/quick-start-keycloak"
$buildOutput = docker build -q -t ghcr.io/fortunen/kete/quick-start-keycloak -f quick-starts/quick-start-keycloak/Dockerfile . 2>&1
$keycloakSuccess = $LASTEXITCODE -eq 0
Write-TaskResult "quick-start-keycloak" $keycloakSuccess
$script:Results["Docker: quick-start-keycloak"] = $keycloakSuccess

# quick-start-curl
Write-Task "Building image: ghcr.io/fortunen/kete/quick-start-curl"
$buildOutput = docker build -q -t ghcr.io/fortunen/kete/quick-start-curl -f quick-starts/quick-start-curl/Dockerfile . 2>&1
$curlSuccess = $LASTEXITCODE -eq 0
Write-TaskResult "quick-start-curl" $curlSuccess
$script:Results["Docker: quick-start-curl"] = $curlSuccess

$duration = Format-Duration((Get-Date) - $stepStart)
Write-Host ""
Write-Host "    Docker builds completed in $duration" -ForegroundColor DarkGray

# =============================================================================
# Step 3: Build Documentation
# =============================================================================

Write-StepHeader 3 "Build Documentation Site"

$stepStart = Get-Date
Write-Task "Building MkDocs site with --strict validation..."

$docsOutput = python -m mkdocs build --strict 2>&1
$docsSuccess = $LASTEXITCODE -eq 0
$duration = Format-Duration((Get-Date) - $stepStart)

Write-TaskResult "Documentation build" $docsSuccess $duration
$script:Results["Documentation (MkDocs)"] = $docsSuccess

if ($docsSuccess -and (Test-Path "site")) {
    Remove-Item -Recurse -Force "site"
}

# =============================================================================
# Summary
# =============================================================================

$totalDuration = Format-Duration((Get-Date) - $script:StartTime)
$failedCount = @($script:Results.Values | Where-Object { $_ -eq $false }).Count

Write-Host ""
Write-Host ""
Write-Host ("═" * 80) -ForegroundColor Magenta
Write-Host ""
Write-Host "  VALIDATION SUMMARY" -ForegroundColor Magenta
Write-Host ""
Write-Host ("═" * 80) -ForegroundColor Magenta

Write-SummaryTable $script:Results

Write-Host "  ─────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
Write-Host ""

if ($failedCount -eq 0) {
    Write-Host "  ╔══════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "  ║                                                          ║" -ForegroundColor Green
    Write-Host "  ║   ✓  PR VALIDATION PASSED                                ║" -ForegroundColor Green
    Write-Host "  ║                                                          ║" -ForegroundColor Green
    Write-Host "  ║   Ready to merge!                                        ║" -ForegroundColor Green
    Write-Host "  ║                                                          ║" -ForegroundColor Green
    Write-Host "  ╚══════════════════════════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "  ╔══════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "  ║                                                          ║" -ForegroundColor Red
    Write-Host "  ║   ✗  PR VALIDATION FAILED                                ║" -ForegroundColor Red
    Write-Host "  ║                                                          ║" -ForegroundColor Red
    Write-Host "  ║   $failedCount check(s) failed. Please fix before merging.         ║" -ForegroundColor Red
    Write-Host "  ║                                                          ║" -ForegroundColor Red
    Write-Host "  ╚══════════════════════════════════════════════════════════╝" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Duration:  $totalDuration" -ForegroundColor DarkGray
Write-Host ""

exit $(if ($failedCount -eq 0) { 0 } else { 1 })
