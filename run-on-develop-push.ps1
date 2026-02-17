# =============================================================================
#
#   KETE │ Merge to Develop
#
#   Runs after merge to develop branch:
#     • All tests (unit, integration, end-to-end)
#     • Build and push Docker images with :develop tag
#     • Build documentation site (validation only - not deployed)
#
#   Usage: .\run-on-develop-push.ps1
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

Write-StepHeader 1 "Run All Tests (with Coverage)"

$stepStart = Get-Date
Write-Task "Executing test suites (unit, integration, end-to-end) with JaCoCo coverage..."
Write-Host ""

& .\run-all-tests.ps1
$testsPassed = $LASTEXITCODE -eq 0
$duration = Format-Duration((Get-Date) - $stepStart)

Write-Host ""
Write-TaskResult "Test execution complete" $testsPassed $duration
$script:Results["Tests"] = $testsPassed

if ($testsPassed) {
    Write-Task "Generating coverage badge..."
    $coveragePercent = 0
    $csvPath = "target/site/jacoco/jacoco.csv"
    if (Test-Path $csvPath) {
        $csv = Import-Csv $csvPath
        $totalMissed = ($csv | Measure-Object -Property LINE_MISSED -Sum).Sum
        $totalCovered = ($csv | Measure-Object -Property LINE_COVERED -Sum).Sum
        $total = $totalMissed + $totalCovered
        if ($total -gt 0) {
            $coveragePercent = [math]::Round(($totalCovered / $total) * 100, 1)
        }
    }
    $color = if ($coveragePercent -ge 80) { "brightgreen" } elseif ($coveragePercent -ge 60) { "green" } elseif ($coveragePercent -ge 40) { "yellow" } else { "red" }
    $badgeJson = @{ schemaVersion = 1; label = "Coverage"; message = "$coveragePercent%"; color = $color } | ConvertTo-Json
    Set-Content -Path "coverage-badge.json" -Value $badgeJson -Encoding UTF8
    Write-TaskResult "Coverage: $coveragePercent% (badge updated)" $true
}

# =============================================================================
# Step 2: Build Docker Images (validation only, no push)
# =============================================================================

Write-StepHeader 2 "Build Docker Images (validation only)"

$stepStart = Get-Date
$imageName = "$script:Registry/quick-start-keycloak:$script:Tag"

Write-Task "Building $imageName"

docker build -q -t $imageName -f "quick-starts/`$images/keycloak/Dockerfile" . 2>&1 | Out-Null
$buildSuccess = $LASTEXITCODE -eq 0

Write-TaskResult "quick-start-keycloak" $buildSuccess

$script:Results["Docker: quick-start-keycloak"] = $buildSuccess
$duration = Format-Duration((Get-Date) - $stepStart)

Write-Host ""
Write-Host "    Docker build completed in $duration" -ForegroundColor DarkGray

# =============================================================================
# Step 3: Build Documentation (Validation Only)
# =============================================================================

Write-StepHeader 3 "Build Documentation (Validation Only)"

$stepStart = Get-Date
Write-Task "Building MkDocs site with --strict validation..."

python -m mkdocs build --strict 2>&1
$docsSuccess = $LASTEXITCODE -eq 0
$duration = Format-Duration((Get-Date) - $stepStart)

Write-TaskResult "Documentation validation" $docsSuccess $duration
Write-TaskSkipped "Documentation deployment" "deploy on release branch only"
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
