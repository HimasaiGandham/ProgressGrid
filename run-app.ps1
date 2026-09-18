Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "    Starting ProgressGrid Habit Tracker ..." -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1] Checking for Maven..." -ForegroundColor Yellow
if (-not (Test-Path "apache-maven-3.9.5")) {
    Write-Host "Maven not found locally. Please ensure you have an IDE or Maven installed." -ForegroundColor Red
} else {
    $env:PATH = "$PWD\apache-maven-3.9.5\bin;$env:PATH"
}

Write-Host "[2] Launching Frontend in your default browser..." -ForegroundColor Yellow
Start-Process "http://localhost:3000/login.html"

Write-Host "[3] Starting Spring Boot Backend..." -ForegroundColor Yellow
Set-Location "Back-End"
& "..\apache-maven-3.9.5\bin\mvn.cmd" spring-boot:run
