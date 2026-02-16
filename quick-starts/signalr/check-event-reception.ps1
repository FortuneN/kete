# Check if event was received via SignalR echo server messages endpoint
# Returns $true if LOGIN event found, $false otherwise

$result = Invoke-RestMethod -Uri "http://localhost:5000/messages" -TimeoutSec 5 2>&1 | Out-String
return $result -match "LOGIN"
