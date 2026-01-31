Set-Location $PSScriptRoot

$start = Get-Date

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host "  UNIT TESTS" -ForegroundColor Cyan
Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""

mvn clean test -Dtest="io.github.fortunen.kete.unittests.**" -Dsurefire.skipAfterFailureCount=1 -q
$exitCode = $LASTEXITCODE

$duration = (Get-Date) - $start

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor DarkGray

if ($exitCode -eq 0) {
    Write-Host "  ✓ Unit tests passed! ($([math]::Round($duration.TotalSeconds, 1))s)" -ForegroundColor Green
} else {
    Write-Host "  ✗ Unit tests failed! ($([math]::Round($duration.TotalSeconds, 1))s)" -ForegroundColor Red
}

Write-Host ("=" * 80) -ForegroundColor DarkGray
Write-Host ""

exit $exitCode
