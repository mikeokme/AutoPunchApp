@echo off
echo Cleaning up processes and directories...

REM Stop Gradle daemons
gradlew --stop

REM Kill Java processes
taskkill /F /IM java.exe 2>nul

REM Kill Android Studio if running
taskkill /F /IM studio64.exe 2>nul

REM Remove build directories
if exist "app\build" rmdir /s /q "app\build"
if exist "build" rmdir /s /q "build"
if exist ".gradle" rmdir /s /q ".gradle"

echo Building project...
gradlew assembleDebug --no-daemon

echo Build completed!
pause 