@echo off
set "PROJECT_DIR=C:\Users\ibrag\Documents\GitHub\RavenBS-Plus-Plus"
set "MODS_DIR=C:\Users\ibrag\AppData\Roaming\.sectlauncher\instances\c5ac291e-5272-4d8e-ac57-30e47d011a2a\mods"

cd /d "%PROJECT_DIR%"
echo Building project...
rem Adding clean to ensure we only have the latest jar
call gradlew clean build

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Build successful!

if not exist "%MODS_DIR%" (
    mkdir "%MODS_DIR%"
)

echo Deleting old RavenBS versions...
if exist "%MODS_DIR%\raven-bs-fabric-*.jar" (
    del "%MODS_DIR%\raven-bs-fabric-*.jar"
)

echo Copying new mod...
rem Copy all non-sources/dev jars (simple wildcard copy, usually fine)
copy "%PROJECT_DIR%\build\libs\raven-bs-fabric-*.jar" "%MODS_DIR%"

echo Done!
pause
