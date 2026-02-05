#!/usr/bin/env pwsh
# Stress Test Monitor Script for KETE
# Monitors event generation rate and Redis connection pool statistics

param(
    [int]$StartupWaitSeconds = 45,
    [int]$CheckIntervalSeconds = 60
)

Write-Host "=== KETE Stress Test Monitor ===" -ForegroundColor Cyan
Write-Host "Waiting ${StartupWaitSeconds}s for Keycloak startup...`n" -ForegroundColor Yellow

Start-Sleep -Seconds $StartupWaitSeconds

# Verify route is active
$route = try {
    (Invoke-WebRequest -Uri http://localhost:9000/metrics -UseBasicParsing -TimeoutSec 5).Content |
        Select-String 'kete_routes_active\s+([\d.]+)' |
        ForEach-Object { $_.Matches.Groups[1].Value }
} catch {
    "N/A"
}

Write-Host "Route active: $route" -ForegroundColor $(if ($route -eq "1.0") { "Green" } else { "Red" })
Write-Host ""

if ($route -ne "1.0") {
    Write-Host "WARNING: Route is not active. Check Keycloak logs." -ForegroundColor Red
    Write-Host ""
}

# Monitoring loop
$global:eventCount = 0
$global:lastCount = 0

# Start Redis subscriber in background to count messages
$subscriberJob = Start-Job -ScriptBlock {
    param($CheckInterval)
    $count = 0
    $lineNum = 0
    docker exec stress-test-redis-1 redis-cli SUBSCRIBE keycloak-events-stress 2>$null | ForEach-Object {
        $lineNum++
        # Redis SUBSCRIBE output: line 1=type, line 2=channel, line 3=message (repeats)
        # Every 3rd line is the actual message
        if ($lineNum % 3 -eq 0) {
            $count++
            if ($count % 100 -eq 0) {
                Write-Output $count
            }
        }
    }
} -ArgumentList $CheckIntervalSeconds

while ($true) {
    $start = $global:lastCount
    Start-Sleep -Seconds $CheckIntervalSeconds

    # Get count from background job
    $jobOutput = Receive-Job -Job $subscriberJob -Keep | Select-Object -Last 1
    if ($jobOutput) {
        $global:eventCount = [int]$jobOutput
    }
    $end = $global:eventCount
    $global:lastCount = $end

    $ratePerSec = [math]::Round(($end - $start) / $CheckIntervalSeconds, 0)

    # Fetch pool metrics and convert to string
    $metrics = try {
        $response = Invoke-WebRequest -Uri http://localhost:9000/metrics -UseBasicParsing -TimeoutSec 5
        [System.Text.Encoding]::UTF8.GetString($response.Content)
    } catch {
        ""
    }

    $active = if ($metrics) {
        if ($metrics -match 'kete_pool_active\{[^\}]*\}\s+([\d.]+)') { $Matches[1] } else { "?" }
    } else {
        "?"
    }

    $idle = if ($metrics) {
        if ($metrics -match 'kete_pool_idle\{[^\}]*\}\s+([\d.]+)') { $Matches[1] } else { "?" }
    } else {
        "?"
    }

    $total = if ($metrics) {
        if ($metrics -match 'kete_pool_total\{[^\}]*\}\s+([\d.]+)') { $Matches[1] } else { "?" }
    } else {
        "?"
    }

    $forwardTimeMax = if ($metrics) {
        if ($metrics -match 'kete_forward_duration_seconds_max\{[^\}]*\}\s+([\d.]+)') {
            [math]::Round([double]$Matches[1] * 1000, 2)
        } else {
            "?"
        }
    } else {
        "?"
    }

    $timestamp = Get-Date -Format "HH:mm:ss"
    $color = if ($ratePerSec -lt 1000) { "Red" } elseif ($ratePerSec -lt 2000) { "Yellow" } else { "Green" }

    Write-Host ("[{0}] Events: {1:N0} | Rate: {2:N0}/sec | Pool: {3} active, {4} idle, {5} total | Forward: {6}ms" -f $timestamp, $end, $ratePerSec, $active, $idle, $total, $forwardTimeMax) -ForegroundColor $color
}

