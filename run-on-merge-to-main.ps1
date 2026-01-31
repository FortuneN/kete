# =============================================================================
#
#   KETE │ Release to Main
#
#   Creates a production release:
#     • All tests (unit, integration, end-to-end)
#     • Package JAR with version imprinted in manifest
#     • Push Docker images with version tag + :latest
#     • Deploy documentation site to GitHub Pages
#     • Create Git tag and GitHub Release
#
#   Version format: yyyy.MM.dd.HH.mm (e.g., 2026.01.31.18.45)
#
#   Usage: .\run-on-merge-to-main.ps1
#
# =============================================================================

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# -----------------------------------------------------------------------------
# Configuration
# -----------------------------------------------------------------------------

$script:TotalSteps = 5
$script:Results = @{}
$script:StartTime = Get-Date
$script:Version = (Get-Date).ToString("yyyy.MM.dd.HH.mm")
$script:Registry = "ghcr.io/fortunen/kete"
$script:JarName = "kete.jar"

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

function Test-PreviousStepsPassed {
    return @($script:Results.Values | Where-Object { $_ -eq $false }).Count -eq 0
}

# -----------------------------------------------------------------------------
# Main Script
# -----------------------------------------------------------------------------

Write-Banner "KETE │ Release to Main" "Creating production release v$($script:Version)" "Green"

Write-Host ""
Write-Host "  ┌─────────────────────────────────────────────────────────────────────────┐" -ForegroundColor DarkGreen
Write-Host "  │                                                                         │" -ForegroundColor DarkGreen
Write-Host "  │   VERSION:  $($script:Version)                                              │" -ForegroundColor DarkGreen
Write-Host "  │                                                                         │" -ForegroundColor DarkGreen
Write-Host "  └─────────────────────────────────────────────────────────────────────────┘" -ForegroundColor DarkGreen
Write-Host ""
Write-Host "  Started:  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Branch:   main" -ForegroundColor DarkGray
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
$script:Results["1. Tests"] = $testsPassed

if (-not $testsPassed) {
    Write-Host ""
    Write-Host "  ⚠  RELEASE ABORTED: Tests must pass before releasing" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# =============================================================================
# Step 2: Package Versioned JAR
# =============================================================================

Write-StepHeader 2 "Package Versioned JAR"

$stepStart = Get-Date
Write-Task "Building JAR with version $($script:Version) imprinted..."

# Build JAR with version number baked into MANIFEST.MF
mvn package -DskipTests "-Drevision=$($script:Version)" -q 2>&1 | Out-Null
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess -and (Test-Path "target/kete.jar")) {
    $jarSuccess = $true
    $jarSize = [math]::Round((Get-Item "target/kete.jar").Length / 1MB, 2)
    Write-TaskResult "kete.jar ($jarSize MB)" $jarSuccess
    Write-Task "Version $($script:Version) imprinted in META-INF/MANIFEST.MF"
} else {
    $jarSuccess = $false
    Write-TaskResult "Failed to create JAR" $false
}

$duration = Format-Duration((Get-Date) - $stepStart)
$script:Results["2. Package JAR"] = $jarSuccess

# =============================================================================
# Step 3: Build and Push Docker Images
# =============================================================================

Write-StepHeader 3 "Build and Push Docker Images"

if (-not (Test-PreviousStepsPassed)) {
    Write-TaskSkipped "Docker operations" "previous step failed"
    $script:Results["3. Push: quick-start-keycloak"] = $false
    $script:Results["3. Push: quick-start-curl"] = $false
} else {
    $stepStart = Get-Date

    # quick-start-keycloak
    $versionedImage = "$script:Registry/quick-start-keycloak:$($script:Version)"
    $latestImage = "$script:Registry/quick-start-keycloak:latest"

    Write-Task "Building $versionedImage"
    docker build -q -t $versionedImage -t $latestImage -f quick-starts/quick-start-keycloak/Dockerfile . 2>&1 | Out-Null
    $buildSuccess = $LASTEXITCODE -eq 0

    if ($buildSuccess) {
        Write-Task "Pushing $versionedImage"
        docker push $versionedImage 2>&1 | Out-Null
        $push1 = $LASTEXITCODE -eq 0

        Write-Task "Pushing $latestImage"
        docker push $latestImage 2>&1 | Out-Null
        $push2 = $LASTEXITCODE -eq 0

        $keycloakSuccess = $push1 -and $push2
        Write-TaskResult "quick-start-keycloak [:$($script:Version) + :latest]" $keycloakSuccess
    } else {
        $keycloakSuccess = $false
        Write-TaskResult "quick-start-keycloak build failed" $false
    }
    $script:Results["3. Push: quick-start-keycloak"] = $keycloakSuccess

    # quick-start-curl
    $versionedImage = "$script:Registry/quick-start-curl:$($script:Version)"
    $latestImage = "$script:Registry/quick-start-curl:latest"

    Write-Task "Building $versionedImage"
    docker build -q -t $versionedImage -t $latestImage -f quick-starts/quick-start-curl/Dockerfile . 2>&1 | Out-Null
    $buildSuccess = $LASTEXITCODE -eq 0

    if ($buildSuccess) {
        Write-Task "Pushing $versionedImage"
        docker push $versionedImage 2>&1 | Out-Null
        $push1 = $LASTEXITCODE -eq 0

        Write-Task "Pushing $latestImage"
        docker push $latestImage 2>&1 | Out-Null
        $push2 = $LASTEXITCODE -eq 0

        $curlSuccess = $push1 -and $push2
        Write-TaskResult "quick-start-curl [:$($script:Version) + :latest]" $curlSuccess
    } else {
        $curlSuccess = $false
        Write-TaskResult "quick-start-curl build failed" $false
    }
    $script:Results["3. Push: quick-start-curl"] = $curlSuccess

    $duration = Format-Duration((Get-Date) - $stepStart)
    Write-Host ""
    Write-Host "    Docker operations completed in $duration" -ForegroundColor DarkGray
}

# =============================================================================
# Step 4: Deploy Documentation Site
# =============================================================================

Write-StepHeader 4 "Deploy Documentation Site"

if (-not (Test-PreviousStepsPassed)) {
    Write-TaskSkipped "Documentation deployment" "previous step failed"
    $script:Results["4. Deploy Docs"] = $false
} else {
    $stepStart = Get-Date

    Write-Task "Building documentation site..."
    python -m mkdocs build --strict 2>&1 | Out-Null
    $buildSuccess = $LASTEXITCODE -eq 0

    if ($buildSuccess) {
        Write-Task "Deploying to GitHub Pages..."
        python -m mkdocs gh-deploy --force 2>&1 | Out-Null
        $deploySuccess = $LASTEXITCODE -eq 0
        Write-TaskResult "Documentation deployed to GitHub Pages" $deploySuccess
        $script:Results["4. Deploy Docs"] = $deploySuccess
    } else {
        Write-TaskResult "Documentation build failed" $false
        $script:Results["4. Deploy Docs"] = $false
    }

    if (Test-Path "site") { Remove-Item -Recurse -Force "site" }

    $duration = Format-Duration((Get-Date) - $stepStart)
    Write-Host ""
    Write-Host "    Documentation completed in $duration" -ForegroundColor DarkGray
}

# =============================================================================
# Step 5: Create Git Tag and GitHub Release
# =============================================================================

Write-StepHeader 5 "Create Git Tag and GitHub Release"

if (-not (Test-PreviousStepsPassed)) {
    Write-TaskSkipped "Release creation" "previous step failed"
    $script:Results["5. Git Tag"] = $false
    $script:Results["5. GitHub Release"] = $false
} else {
    $stepStart = Get-Date
    $tagName = "v$($script:Version)"

    # Create and push tag
    Write-Task "Creating Git tag $tagName..."
    git tag -a $tagName -m "Release $tagName" 2>&1 | Out-Null
    $tagCreated = $LASTEXITCODE -eq 0

    if ($tagCreated) {
        Write-Task "Pushing tag to origin..."
        git push origin $tagName 2>&1 | Out-Null
        $tagPushed = $LASTEXITCODE -eq 0
        Write-TaskResult "Git tag $tagName" $tagPushed
        $script:Results["5. Git Tag"] = $tagPushed
    } else {
        Write-TaskResult "Failed to create Git tag" $false
        $script:Results["5. Git Tag"] = $false
    }

    # Create GitHub Release
    if ($script:Results["5. Git Tag"]) {
        Write-Task "Creating GitHub Release..."

        $releaseNotes = @"
## KETE Release $tagName

### 📦 Artifact

``kete.jar`` - Keycloak Event Transmitter Extension

> Version $($script:Version) is imprinted in META-INF/MANIFEST.MF

### 🚀 Quick Start

``````bash
curl -sSL https://raw.githubusercontent.com/FortuneN/kete/main/quick-starts/http-webhook/docker-compose.yml | docker compose -f - up
``````

### 📚 Documentation

https://fortunen.github.io/kete/
"@

        gh release create $tagName --title "$tagName" --notes $releaseNotes "target/$script:JarName" 2>&1 | Out-Null
        $releaseCreated = $LASTEXITCODE -eq 0
        Write-TaskResult "GitHub Release $tagName" $releaseCreated
        $script:Results["5. GitHub Release"] = $releaseCreated
    } else {
        Write-TaskSkipped "GitHub Release" "tag creation failed"
        $script:Results["5. GitHub Release"] = $false
    }

    $duration = Format-Duration((Get-Date) - $stepStart)
    Write-Host ""
    Write-Host "    Release operations completed in $duration" -ForegroundColor DarkGray
}

# =============================================================================
# Summary
# =============================================================================

$totalDuration = Format-Duration((Get-Date) - $script:StartTime)
$failedCount = @($script:Results.Values | Where-Object { $_ -eq $false }).Count

Write-Host ""
Write-Host ""
Write-Host ("═" * 80) -ForegroundColor Green
Write-Host ""
Write-Host "  RELEASE SUMMARY │ v$($script:Version)" -ForegroundColor Green
Write-Host ""
Write-Host ("═" * 80) -ForegroundColor Green

Write-SummaryTable $script:Results

Write-Host "  ─────────────────────────────────────────────────────────────" -ForegroundColor DarkGray
Write-Host ""

if ($failedCount -eq 0) {
    Write-Host "  Release Artifact:" -ForegroundColor White
    Write-Host "    kete.jar (v$($script:Version))" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Also Published:" -ForegroundColor DarkGray
    Write-Host "    Docker:  $script:Registry/quick-start-keycloak:$($script:Version)" -ForegroundColor DarkGray
    Write-Host "    Docker:  $script:Registry/quick-start-curl:$($script:Version)" -ForegroundColor DarkGray
    Write-Host "    Docs:    https://fortunen.github.io/kete/" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "  GitHub Release:" -ForegroundColor White
    Write-Host "    https://github.com/FortuneN/kete/releases/tag/v$($script:Version)" -ForegroundColor Gray
    Write-Host ""
    Write-Host ""
    Write-Host "  ╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "  ║                                                                      ║" -ForegroundColor Green
    Write-Host "  ║   ✓  RELEASE v$($script:Version) PUBLISHED SUCCESSFULLY                   ║" -ForegroundColor Green
    Write-Host "  ║                                                                      ║" -ForegroundColor Green
    Write-Host "  ╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "  ╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "  ║                                                                      ║" -ForegroundColor Red
    Write-Host "  ║   ✗  RELEASE FAILED                                                  ║" -ForegroundColor Red
    Write-Host "  ║                                                                      ║" -ForegroundColor Red
    Write-Host "  ║   $failedCount step(s) failed. Release was not completed.                   ║" -ForegroundColor Red
    Write-Host "  ║                                                                      ║" -ForegroundColor Red
    Write-Host "  ╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Completed: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor DarkGray
Write-Host "  Duration:  $totalDuration" -ForegroundColor DarkGray
Write-Host ""

exit $(if ($failedCount -eq 0) { 0 } else { 1 })
