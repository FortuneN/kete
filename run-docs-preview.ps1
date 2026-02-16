# Stop any existing Python/MkDocs processes

Get-Process -Name python -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

# Change to project root

Set-Location $PSScriptRoot

# Start MkDocs server

& python -m mkdocs serve
