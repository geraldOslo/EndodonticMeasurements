@echo off
set "DEST_DIR=C:\ImageJ\plugins\Endodontic_Measurements_2.0"
set "JAR_NAME=Endodontic_Measurements_2.0.jar"
set "CONFIG_NAME=Endodontic_Measurements.cfg"

echo Installing Endodontic Measurements 2.0...

if not exist "%DEST_DIR%" (
    mkdir "%DEST_DIR%"
    if %errorlevel% neq 0 (
        echo Failed to create directory: %DEST_DIR%
        pause
        exit /b %errorlevel%
    )
    echo Created plugin directory.
)

copy "%JAR_NAME%" "%DEST_DIR%\" > nul
if %errorlevel% neq 0 (
    echo Failed to copy JAR file.
    pause
    exit /b %errorlevel%
)
echo Copied JAR file.

copy "%CONFIG_NAME%" "%DEST_DIR%\" > nul
if %errorlevel% neq 0 (
    echo Failed to copy config file.
    pause
    exit /b %errorlevel%
)
echo Copied config file.

echo.
echo Installation successful!
echo Please restart ImageJ.
pause
