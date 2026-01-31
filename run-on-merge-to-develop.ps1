# =============================================================================
#
#   KETE │ Merge to Develop
#
#   Runs after merge to develop branch:
#     • All tests (unit, integration, end-to-end)
#     • Build and push Docker images with :develop tag
#     • Build documentation site (validation only - not deployed)
#
#   Usage: .\run-on-merge-to-develop.ps1
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
$script:Tag = "develop"
$script:Registry = "ghcr.io/fortunen/kete"

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

function Write-TaskSkipped {
    param([string]$Message, [string]$Reason = "")
    $suffix = if ($Reason) { " ($Reason)" } else { "" }
    Write-Host "    ○ " -NoNewline -ForegroundColor Yellow
    Write-Host "$Message" -NoNewline -ForegroundColor Gray
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

    Write-Host ""
    Write-Host "  ┌────────────────────────────────────────────┬──────────┐" -ForegroundColor DarkGray
    Write-Host "  │  Task                                      │  Status  │" -ForegroundColor DarkGray
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

Write-Banner "KETE │ Merge to Develop" "Building and validating develop branch" "Blue"

Write-Host ""
Write-Host "  Started:  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Branch:   develop" -ForegroundColor DarkGray
Write-Host "  Commit:   $(git rev-parse --short HEAD 2>$null)" -ForegroundColor DarkGray
Write-Host "  Tag:      :$script:Tag" -ForegroundColor DarkGray

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
$script:Results["Tests"] = $testsPassed

# =============================================================================
# Step 2: Build and Push Docker Images
# =============================================================================

Write-StepHeader 2 "Build and Push Docker Images (:$script:Tag)"

$stepStart = Get-Date

# quick-start-keycloak
$imageName = "$script:Registry/quick-start-keycloak:$script:Tag"
Write-Task "Building $imageName"
docker build -q -t $imageName -f quick-starts/quick-start-keycloak/Dockerfile . 2>&1 | Out-Null
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Task "Pushing $imageName"
    docker push $imageName 2>&1 | Out-Null
    $pushSuccess = $LASTEXITCODE -eq 0
    Write-TaskResult "quick-start-keycloak:$script:Tag" $pushSuccess
    $script:Results["Push: quick-start-keycloak"] = $pushSuccess
} else {
    Write-TaskResult "quick-start-keycloak build failed" $false
    $script:Results["Push: quick-start-keycloak"] = $false
}

# quick-start-curl
$imageName = "$script:Registry/quick-start-curl:$script:Tag"
Write-Task "Building $imageName"
docker build -q -t $imageName -f quick-starts/quick-start-curl/Dockerfile . 2>&1 | Out-Null
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Task "Pushing $imageName"
    docker push $imageName 2>&1 | Out-Null
    $pushSuccess = $LASTEXITCODE -eq 0
    Write-TaskResult "quick-start-curl:$script:Tag" $pushSuccess
    $script:Results["Push: quick-start-curl"] = $pushSuccess
} else {
    Write-TaskResult "quick-start-curl build failed" $false
    $script:Results["Push: quick-start-curl"] = $false
}

$duration = Format-Duration((Get-Date) - $stepStart)
Write-Host ""
Write-Host "    Docker operations completed in $duration" -ForegroundColor DarkGray

# =============================================================================
# Step 3: Build Documentation (Validation Only)
# =============================================================================

Write-StepHeader 3 "Build Documentation (Validation Only)"

$stepStart = Get-Date
Write-Task "Building MkDocs site with --strict validation..."

$docsOutput = python -m mkdocs build --strict 2>&1
$docsSuccess = $LASTEXITCODE -eq 0
$duration = Format-Duration((Get-Date) - $stepStart)

Write-TaskResult "Documentation validation" $docsSuccess $duration
Write-TaskSkipped "Documentation deployment" "deploy on main branch only"
$script:Results["Docs: Validation"] = $docsSuccess

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
Write-Host ("═" * 80) -ForegroundColor Blue
Write-Host ""
Write-Host "  DEVELOP BRANCH SUMMARY" -ForegroundColor Blue
Write-Host ""
Write-Host ("═" * 80) -ForegroundColor Blue

Write-SummaryTable $script:Results

Write-Host "  ─────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Published Artifacts:" -ForegroundColor White
Write-Host "    • $script:Registry/quick-start-keycloak:$script:Tag" -ForegroundColor Gray
Write-Host "    • $script:Registry/quick-start-curl:$script:Tag" -ForegroundColor Gray
Write-Host ""

if ($failedCount -eq 0) {
    Write-Host "  ╔══════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "  ║                                                          ║" -ForegroundColor Green
    Write-Host "  ║   ✓  DEVELOP BUILD SUCCESSFUL                            ║" -ForegroundColor Green
    Write-Host "  ║                                                          ║" -ForegroundColor Green
    Write-Host "  ╚══════════════════════════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "  ╔══════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "  ║                                                          ║" -ForegroundColor Red
    Write-Host "  ║   ✗  DEVELOP BUILD FAILED                                ║" -ForegroundColor Red
    Write-Host "  ║                                                          ║" -ForegroundColor Red
    Write-Host "  ╚══════════════════════════════════════════════════════════╝" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Duration:  $totalDuration" -ForegroundColor DarkGray
Write-Host ""

exit $(if ($failedCount -eq 0) { 0 } else { 1 })
