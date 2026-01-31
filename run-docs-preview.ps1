# Stop any existing Python/MkDocs processes

Get-Process -Name python -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

# Change to project root

Set-Location $PSScriptRoot

# Start MkDocs server

& "C:\Users\fortu\AppData\Local\Programs\Python\Python312\python.exe" -m mkdocs serve
