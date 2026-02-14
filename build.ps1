# Cachely – local build script
# Run: .\build   or   .\build.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleDebug --no-daemon
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    gradle assembleDebug --no-daemon
} else {
    Write-Host "No Gradle found. Either run 'gradle wrapper' first (if you have Gradle installed) or open the project in Android Studio and build from there."
    exit 1
}

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Build succeeded. APK: app\build\outputs\apk\debug\app-debug.apk"
}
