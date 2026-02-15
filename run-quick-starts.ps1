#!/usr/bin/env pwsh

<#
.SYNOPSIS
    KETE Quick-Start Test Runner - Professional test automation for all quickstarts.

.DESCRIPTION
    Tests all quick-starts by:
    1. Starting containers
    2. Waiting for containers to be healthy
    3. Triggering a login event
    4. Verifying event was sent (via metrics)
    5. Checking event was received (via per-quickstart check script)

.PARAMETER Filter
    Wildcard filter for quickstart names (default: "*")

.PARAMETER TimeoutSeconds
    Timeout for event reception check (default: 60)

.EXAMPLE
    ./run-quick-starts.ps1
    ./run-quick-starts.ps1 -Filter "kafka-*"
#>

param(
    [string]$Filter = "*",
    [int]$TimeoutSeconds = 60
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# ----------------------------------------------------------------=============
# Configuration
# ----------------------------------------------------------------=============

$script:KeycloakUrl = "http://localhost:8080"
$script:MetricsEndpoint = "http://localhost:9000/metrics"
$script:TokenEndpoint = "$script:KeycloakUrl/realms/master/protocol/openid-connect/token"

# Animation frames
$script:SpinnerFrames = @('⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏')
$script:SpinnerIndex = 0

# Colors
$script:Colors = @{
    Primary   = "Cyan"
    Success   = "Green"
    Error     = "Red"
    Warning   = "Yellow"
    Muted     = "DarkGray"
    Info      = "White"
    Highlight = "Magenta"
}

# Test results tracking
$script:Results = @{
    Passed  = [System.Collections.ArrayList]::new()
    Failed  = [System.Collections.ArrayList]::new()
    Skipped = [System.Collections.ArrayList]::new()
}

# ----------------------------------------------------------------=============
# UI Components
# ----------------------------------------------------------------=============

function Format-Duration {
    param([int]$Seconds)
    if ($Seconds -ge 60) {
        $m = [math]::Floor($Seconds / 60)
        $s = $Seconds % 60
        return "${m}m ${s}s"
    }
    return "${Seconds}s"
}

function Clear-CurrentLine {
    Write-Host "`r$(' ' * 80)`r" -NoNewline
}

function Write-Spinner {
    param([string]$Message)
    $frame = $script:SpinnerFrames[$script:SpinnerIndex % $script:SpinnerFrames.Length]
    $script:SpinnerIndex++
    Write-Host "`r    $frame " -NoNewline -ForegroundColor $script:Colors.Primary
    Write-Host "$Message" -NoNewline -ForegroundColor $script:Colors.Muted
}

function Write-Logo {
    $logo = @"

    ██╗  ██╗███████╗████████╗███████╗
    ██║ ██╔╝██╔════╝╚══██╔══╝██╔════╝
    █████╔╝ █████╗     ██║   █████╗
    ██╔═██╗ ██╔══╝     ██║   ██╔══╝
    ██║  ██╗███████╗   ██║   ███████╗
    ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝

"@
    Write-Host $logo -ForegroundColor $script:Colors.Primary
    Write-Host "    Quick-Start Test Runner" -ForegroundColor $script:Colors.Muted
    Write-Host "    ════════════════════════════════════════════════════════════" -ForegroundColor $script:Colors.Primary
    Write-Host ""
}

function Write-SectionHeader {
    param([string]$Title)
    Write-Host ""
    Write-Host "  ----------------------------------------------------------------" -ForegroundColor $script:Colors.Primary
    Write-Host "  $Title" -ForegroundColor $script:Colors.Info
    Write-Host "  ----------------------------------------------------------------" -ForegroundColor $script:Colors.Primary
}

function Write-TestHeader {
    param(
        [string]$Name,
        [int]$Current,
        [int]$Total
    )

    Write-Host ""
    Write-Host "  ----------------------------------------------------------------" -ForegroundColor $script:Colors.Primary
    Write-Host "  " -NoNewline
    Write-Host "$Current" -ForegroundColor $script:Colors.Highlight -NoNewline
    Write-Host " of " -ForegroundColor $script:Colors.Muted -NoNewline
    Write-Host "$Total" -ForegroundColor $script:Colors.Highlight -NoNewline
    Write-Host " - " -ForegroundColor $script:Colors.Muted -NoNewline
    Write-Host "$Name" -ForegroundColor $script:Colors.Info
    Write-Host "  ----------------------------------------------------------------" -ForegroundColor $script:Colors.Primary
    Write-Host ""
}

function Write-Step {
    param(
        [string]$Message,
        [ValidateSet("pending", "running", "success", "failed", "skipped", "info")]
        [string]$Status = "pending"
    )

    $icon = switch ($Status) {
        "pending"  { "○"; $color = $script:Colors.Muted }
        "running"  { "◐"; $color = $script:Colors.Primary }
        "success"  { "✓"; $color = $script:Colors.Success }
        "failed"   { "✗"; $color = $script:Colors.Error }
        "skipped"  { "◌"; $color = $script:Colors.Warning }
        "info"     { "ℹ"; $color = $script:Colors.Info }
    }

    if ($Status -eq "running") {
        Write-Host "    $icon " -NoNewline -ForegroundColor $color
        Write-Host $Message -ForegroundColor $script:Colors.Muted
    } else {
        Write-Host "    $icon " -NoNewline -ForegroundColor $color
        Write-Host $Message -ForegroundColor $(if ($Status -eq "success") { $script:Colors.Info } elseif ($Status -eq "failed") { $script:Colors.Error } else { $script:Colors.Muted })
    }
}

function Write-ProgressBar {
    param(
        [int]$Current,
        [int]$Total,
        [string]$Label = ""
    )

    $percent = if ($Total -gt 0) { [math]::Round(($Current / $Total) * 100) } else { 0 }
    $barWidth = 40
    $filled = [math]::Round(($percent / 100) * $barWidth)
    $empty = $barWidth - $filled

    $bar = "█" * $filled + "░" * $empty

    Write-Host "`r    [$bar] $percent% $Label" -NoNewline -ForegroundColor $script:Colors.Primary
}



# ----------------------------------------------------------------=============
# Core Functions
# ----------------------------------------------------------------=============

function Get-EventsSentCount {
    try {
        $response = Invoke-RestMethod -Uri $script:MetricsEndpoint -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response -match 'kete_events_forwarded_total\{[^}]*\}\s+([\d.]+)') {
            return [int]$Matches[1]
        }
        return 0
    } catch {
        return 0
    }
}

function Invoke-Login {
    try {
        $body = @{
            grant_type = "password"
            client_id  = "admin-cli"
            username   = "admin"
            password   = "admin"
        }
        Invoke-RestMethod -Uri $script:TokenEndpoint -Method Post -Body $body -TimeoutSec 10 -ErrorAction SilentlyContinue | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Wait-ForHealthy {
    param([string]$QuickstartPath, [int]$TimeoutSeconds = 60)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $spinnerIdx = 0

    while ((Get-Date) -lt $deadline) {
        $output = docker compose -f "$QuickstartPath/docker-compose.yml" ps --format json 2>$null | ConvertFrom-Json

        if ($output) {
            $services = @($output)
            $allHealthy = $true
            $healthyCount = 0
            $totalCount = 0

            foreach ($svc in $services) {
                $health = $svc.Health
                $state = $svc.State

                # Skip init containers
                if ($state -eq "exited" -and $svc.ExitCode -eq 0) {
                    continue
                }

                $totalCount++

                if ($state -ne "running") {
                    $allHealthy = $false
                } elseif ($health -and $health -ne "healthy" -and $health -ne "") {
                    $allHealthy = $false
                } else {
                    $healthyCount++
                }
            }

            # Show progress
            $frame = $script:SpinnerFrames[$spinnerIdx % $script:SpinnerFrames.Length]
            $spinnerIdx++
            $remaining = [math]::Ceiling(($deadline - (Get-Date)).TotalSeconds)
            $remainingStr = Format-Duration -Seconds $remaining
            $message = "    $frame Waiting for containers... ($healthyCount/$totalCount healthy, $remainingStr left)".PadRight(80)
            Write-Host "`r$message" -NoNewline -ForegroundColor $script:Colors.Muted

            if ($allHealthy -and $services.Count -gt 0) {
                Clear-CurrentLine
                return $true
            }
        }

        Start-Sleep -Milliseconds 500
    }

    Clear-CurrentLine
    return $false
}

function Wait-ForKeycloak {
    param([int]$TimeoutSeconds = 60)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $spinnerIdx = 0

    while ((Get-Date) -lt $deadline) {
        $frame = $script:SpinnerFrames[$spinnerIdx % $script:SpinnerFrames.Length]
        $spinnerIdx++
        $remaining = [math]::Ceiling(($deadline - (Get-Date)).TotalSeconds)
        $remainingStr = Format-Duration -Seconds $remaining
        $message = "    $frame Waiting for Keycloak... ($remainingStr left)".PadRight(80)
        Write-Host "`r$message" -NoNewline -ForegroundColor $script:Colors.Muted

        try {
            # Check if realm endpoint is ready (more reliable than /health/ready)
            $response = Invoke-WebRequest -Uri "$script:KeycloakUrl/realms/master" -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Clear-CurrentLine
                return $true
            }
        } catch { }

        Start-Sleep -Milliseconds 500
    }

    Clear-CurrentLine
    return $false
}

function Wait-ForEventSent {
    param([int]$BaselineCount, [int]$TimeoutSeconds = 60)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $spinnerIdx = 0

    while ((Get-Date) -lt $deadline) {
        $frame = $script:SpinnerFrames[$spinnerIdx % $script:SpinnerFrames.Length]
        $spinnerIdx++
        $remaining = [math]::Ceiling(($deadline - (Get-Date)).TotalSeconds)
        $remainingStr = Format-Duration -Seconds $remaining
        $message = "    $frame Checking metrics... ($remainingStr left)".PadRight(80)
        Write-Host "`r$message" -NoNewline -ForegroundColor $script:Colors.Muted

        $afterCount = Get-EventsSentCount
        if ($afterCount -gt $BaselineCount) {
            Clear-CurrentLine
            return $afterCount
        }

        Start-Sleep -Milliseconds 500
    }

    Clear-CurrentLine
    return $null
}

function Test-QuickStart {
    param(
        [string]$Name,
        [string]$Path,
        [int]$TimeoutSeconds
    )

    $startTime = Get-Date
    $success = $false
    $checkScript = "$Path/check-event-reception.ps1"

    try {
        # Step 1: Start containers
        Write-Step "Starting containers..." "running"
        $output = docker compose -f "$Path/docker-compose.yml" up -d 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "`r" -NoNewline
            Write-Step "Failed to start containers" "failed"
            Write-Host ""
            Write-Host "  Error output:" -ForegroundColor $script:Colors.Error
            $output | ForEach-Object { Write-Host "    $_" -ForegroundColor $script:Colors.Muted }
            return $false
        }
        Write-Host "`r" -NoNewline
        Write-Step "Containers started" "success"

        # Step 2: Wait for healthy
        Write-Step "Waiting for containers to be healthy..." "running"
        if (-not (Wait-ForHealthy -QuickstartPath $Path -TimeoutSeconds 60)) {
            Write-Step "Containers not healthy (timeout)" "failed"
            return $false
        }
        Write-Step "All containers healthy" "success"

        # Step 3: Wait for Keycloak
        Write-Step "Waiting for Keycloak readiness..." "running"
        if (-not (Wait-ForKeycloak -TimeoutSeconds 180)) {
            Write-Step "Keycloak not ready (timeout)" "failed"
            return $false
        }
        Write-Step "Keycloak ready" "success"

        # Step 4: Trigger login event
        Write-Step "Triggering login event..." "running"
        Start-Sleep -Milliseconds 1000  # Small delay for stability

        $beforeCount = Get-EventsSentCount

        if (-not (Invoke-Login)) {
            Write-Step "Login failed" "failed"
            return $false
        }
        Write-Host "`r" -NoNewline
        Write-Step "Login event triggered" "success"

        # Step 4a: Verify event was sent via metrics (retry loop for SDK init overhead)
        Write-Step "Verifying event was sent..." "running"
        $metricsDeadline = (Get-Date).AddSeconds(15)
        $eventsSent = 0
        $spinnerIdx = 0

        while ((Get-Date) -lt $metricsDeadline) {
            Start-Sleep -Seconds 2
            $afterCount = Get-EventsSentCount
            $eventsSent = $afterCount - $beforeCount
            if ($eventsSent -gt 0) { break }

            $frame = $script:SpinnerFrames[$spinnerIdx % $script:SpinnerFrames.Length]
            $spinnerIdx++
            $remaining = [math]::Ceiling(($metricsDeadline - (Get-Date)).TotalSeconds)
            $remainingStr = Format-Duration -Seconds $remaining
            $message = "    $frame Waiting for metrics... ($remainingStr left)".PadRight(80)
            Write-Host "`r$message" -NoNewline -ForegroundColor $script:Colors.Muted
        }

        if ($eventsSent -gt 0) {
            Write-Host "`r" -NoNewline
            Write-Step "Event sent confirmed" "success"
        } else {
            Write-Host "`r" -NoNewline
            Write-Step "Event send NOT confirmed (metrics unavailable or no increase)" "failed"
            return $false
        }

        # Step 5: Check event reception
        if (Test-Path $checkScript) {
            Write-Step "Verifying event reception..." "running"

            $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
            $received = $false
            $skipCheck = $false
            $spinnerIdx = 0
            $lastError = $null
            $retryDelay = 2  # seconds between retries

            while ((Get-Date) -lt $deadline) {
                $frame = $script:SpinnerFrames[$spinnerIdx % $script:SpinnerFrames.Length]
                $spinnerIdx++
                $remaining = [math]::Ceiling(($deadline - (Get-Date)).TotalSeconds)
                $remainingStr = Format-Duration -Seconds $remaining
                $message = "    $frame Checking reception... ($remainingStr left)".PadRight(80)
                Write-Host "`r$message" -NoNewline -ForegroundColor $script:Colors.Muted

                try {
                    $result = & $checkScript
                    $lastError = $null  # Clear error on successful execution

                    if ($null -eq $result) {
                        $skipCheck = $true
                        break
                    }
                    if ($result -eq $true) {
                        $received = $true
                        break
                    }
                    # Result is false, continue retrying
                } catch {
                    # Capture error for reporting if we timeout
                    $lastError = $_
                    # Continue retrying - script may fail due to API not ready yet
                }

                Start-Sleep -Seconds $retryDelay
            }

            Clear-CurrentLine

            if ($skipCheck) {
                Write-Step "Reception check skipped (pub/sub or cloud-only)" "skipped"
            } elseif (-not $received) {
                Write-Step "Event not received (timeout)" "failed"
                if ($lastError) {
                    Write-Host ""
                    Write-Host "  Last error from check script:" -ForegroundColor $script:Colors.Error
                    Write-Host "    $lastError" -ForegroundColor $script:Colors.Muted
                }
                return $false
            } else {
                Write-Step "Event reception confirmed" "success"
            }
        } else {
            Write-Step "No reception check script" "skipped"
        }

        $success = $true

    } finally {
        # Cleanup with spinner
        Write-Step "Cleaning up containers..." "running"
        docker compose -f "$Path/docker-compose.yml" down -v --remove-orphans 2>&1 | Out-Null
        Start-Sleep -Seconds 5
        Write-Host "`r" -NoNewline
        Write-Step "Cleanup complete" "info"
    }

    # Final result
    $duration = (Get-Date) - $startTime
    $durationStr = "{0:mm\:ss}" -f $duration

    Write-Host ""
    if ($success) {
        Write-Host "    " -NoNewline
        Write-Host "Success" -ForegroundColor $script:Colors.Success -NoNewline
        Write-Host " - Duration: $durationStr" -ForegroundColor $script:Colors.Muted
    } else {
        Write-Host "    " -NoNewline
        Write-Host "Failure" -ForegroundColor $script:Colors.Error -NoNewline
        Write-Host " - Duration: $durationStr" -ForegroundColor $script:Colors.Muted
    }

    return $success
}

# ----------------------------------------------------------------=============
# Main Execution
# ----------------------------------------------------------------=============

$totalStartTime = Get-Date

# Get list of quickstarts
$quickstarts = Get-ChildItem "quick-starts" -Directory |
    Where-Object { $_.Name -ne "`$images" } |
    Where-Object { $_.Name -notlike "quick-start-*" } |
    Where-Object { $_.Name -like $Filter } |
    Where-Object { -not (Test-Path "$($_.FullName)/dont-run-this-quickstart") } |
    Sort-Object Name

$total = $quickstarts.Count

if ($total -eq 0) {
    Write-Host "  No quickstarts match the filter." -ForegroundColor $script:Colors.Warning
    exit 0
}

# Run tests
$current = 0
foreach ($qs in $quickstarts) {
    $current++
    $name = $qs.Name
    $path = $qs.FullName

    Write-TestHeader -Name $name -Current $current -Total $total

    $success = Test-QuickStart -Name $name -Path $path -TimeoutSeconds $TimeoutSeconds

    if ($success) {
        [void]$script:Results.Passed.Add($name)
    } else {
        [void]$script:Results.Failed.Add($name)
        break
    }
}

# Summary and total duration
$totalDuration = (Get-Date) - $totalStartTime
$totalDurationStr = "{0:mm\:ss}" -f $totalDuration

Write-SectionHeader "Test Summary"
Write-Host "  Passed:  $($script:Results.Passed.Count)" -ForegroundColor $script:Colors.Success
Write-Host "  Failed:  $($script:Results.Failed.Count)" -ForegroundColor $(if ($script:Results.Failed.Count -gt 0) { $script:Colors.Error } else { $script:Colors.Success })
Write-Host "  Skipped: $($script:Results.Skipped.Count)" -ForegroundColor $script:Colors.Warning
Write-Host "  Total Duration: $totalDurationStr" -ForegroundColor $script:Colors.Muted
Write-Host ""

# Exit code
if ($script:Results.Failed.Count -gt 0) {
    exit 1
}
exit 0
