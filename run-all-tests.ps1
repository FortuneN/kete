# Run all tests in order: unit tests, destination tests, end-to-end tests
# Usage: .\run-all-tests.ps1

Set-Location $PSScriptRoot

$totalStart = Get-Date

mvn clean

# =============================================================================
# Unit Tests
# =============================================================================

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host "  UNIT TESTS" -ForegroundColor Cyan
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""

$unitStart = Get-Date
mvn test -Dtest="io.github.fortunen.kete.unittests.**" -Dsurefire.skipAfterFailureCount=1 -q
$unitExit = $LASTEXITCODE
$unitDuration = (Get-Date) - $unitStart

if ($unitExit -ne 0) {
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host "  ✗ Unit tests failed! Aborting. ($([math]::Round($unitDuration.TotalSeconds, 1))s)" -ForegroundColor Red
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host ""
    exit $unitExit
}

Write-Host ""
Write-Host "  ✓ Unit tests passed! ($([math]::Round($unitDuration.TotalSeconds, 1))s)" -ForegroundColor Green

# =============================================================================
# Integration Tests
# =============================================================================

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host "  INTEGRATION TESTS" -ForegroundColor Cyan
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""

$destStart = Get-Date
mvn test -Dtest="io.github.fortunen.kete.integrationtests.**" -Dsurefire.skipAfterFailureCount=1 -q
$destExit = $LASTEXITCODE
$destDuration = (Get-Date) - $destStart

if ($destExit -ne 0) {
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host "  ✗ Integration tests failed! Aborting. ($([math]::Round($destDuration.TotalSeconds, 1))s)" -ForegroundColor Red
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host ""
    exit $destExit
}

Write-Host ""
Write-Host "  ✓ Integration tests passed! ($([math]::Round($destDuration.TotalSeconds, 1))s)" -ForegroundColor Green

# =============================================================================
# End-to-End Tests
# =============================================================================

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host "  END-TO-END TESTS" -ForegroundColor Cyan
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""

$e2eStart = Get-Date
mvn test -Dtest="io.github.fortunen.kete.endtoendtests.**" -Dsurefire.skipAfterFailureCount=1 -q
$e2eExit = $LASTEXITCODE
$e2eDuration = (Get-Date) - $e2eStart

if ($e2eExit -ne 0) {
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host "  ✗ End-to-end tests failed! ($([math]::Round($e2eDuration.TotalSeconds, 1))s)" -ForegroundColor Red
    Write-Host ("=" * 80) -ForegroundColor DarkGray
    Write-Host ""
    exit $e2eExit
}

Write-Host ""
Write-Host "  ✓ End-to-end tests passed! ($([math]::Round($e2eDuration.TotalSeconds, 1))s)" -ForegroundColor Green

# =============================================================================
# Summary
# =============================================================================

$totalDuration = (Get-Date) - $totalStart

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host "  ✓ ALL TESTS PASSED!" -ForegroundColor Green
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Unit tests:        $([math]::Round($unitDuration.TotalSeconds, 1))s" -ForegroundColor White
Write-Host "  Integration tests: $([math]::Round($destDuration.TotalSeconds, 1))s" -ForegroundColor White
Write-Host "  End-to-end tests:  $([math]::Round($e2eDuration.TotalSeconds, 1))s" -ForegroundColor White
Write-Host "  ─────────────────────────────" -ForegroundColor DarkGray
Write-Host "  Total:             $([math]::Round($totalDuration.TotalSeconds, 1))s" -ForegroundColor Cyan
Write-Host ""
