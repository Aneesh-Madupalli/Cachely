@echo off
REM Cachely – local build script. Run: build   or   build.bat
cd /d "%~dp0"

if exist gradlew.bat (
    call gradlew.bat assembleDebug --no-daemon
) else if exist gradlew (
    call gradlew assembleDebug --no-daemon
) else (
    echo No Gradle wrapper found. Run "gradle wrapper" first or build from Android Studio.
    exit /b 1
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo Build succeeded. APK: app\build\outputs\apk\debug\app-debug.apk
)
exit /b %ERRORLEVEL%
