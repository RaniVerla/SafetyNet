#!/usr/bin/env pwsh

# Kill existing processes
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start the application
$proc = Start-Process -FilePath ".\gradlew.bat" -ArgumentList "bootRun" -WorkingDirectory "C:\Users\raniv\safetynet" -NoNewWindow -PassThru

# Wait for server to start
Write-Host "Waiting for server to start..."
Start-Sleep -Seconds 20

# Make the request
Write-Host "Calling endpoint..."
$response = Invoke-RestMethod -Uri "http://localhost:9011/firestation?stationNumber=3" -ErrorAction SilentlyContinue
Write-Host ($response | ConvertTo-Json -Depth 10)

# Cleanup
Stop-Process -InputObject $proc -Force -ErrorAction SilentlyContinue

