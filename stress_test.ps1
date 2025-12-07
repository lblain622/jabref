# stress_test.ps1
param(
    [int]$runs = 5,
    [string]$gradleTask = "run",   # JabRef launches with "gradlew run"
    [string]$projectDir = "."
)

# Change to project directory
Set-Location $projectDir

# Make sure Gradle wrapper exists
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "ERROR: gradlew.bat not found!" -ForegroundColor Red
    exit 1
}

# Log output location
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "stress_log_$timestamp.txt"

# Warm Gradle (optional)
Write-Host "Warming up Gradle..." -ForegroundColor Yellow
.\gradlew.bat --version | Out-Null

Write-Host "Starting stress test...`n" -ForegroundColor Cyan

for ($i = 1; $i -le $runs; $i++) {

    Write-Host "`n========== Run $i ==========" -ForegroundColor Green

    # Start timer
    $start = Get-Date

    # Launch JabRef using Gradle (runs JavaFX UI)
    $process = Start-Process `
        -FilePath ".\gradlew.bat" `
        -ArgumentList $gradleTask `
        -PassThru `
        -WindowStyle Hidden

    # Let it run for a bit (simulated user time)
    Start-Sleep -Seconds 5

    # Kill the process
    Stop-Process -Id $process.Id -Force

    # Capture metrics
    $duration = (Get-Date) - $start | Select-Object -ExpandProperty TotalSeconds
    $cpu = (Get-Counter '\Processor(_Total)\% Processor Time').CounterSamples.CookedValue
    $mem = (Get-Counter '\Memory\Available MBytes').CounterSamples.CookedValue

    # Log entry
    $entry = "Run ${i}: Duration=${duration}s CPU=${cpu}% AvailableMem=${mem}MB"
    Write-Host $entry -ForegroundColor Yellow
    Add-Content -Path $logFile -Value $entry
}

Write-Host "`nLogs saved to $logFile" -ForegroundColor Cyan
