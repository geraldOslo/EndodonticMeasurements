@echo off
set "DEST_DIR=C:\ImageJ\plugins\Endodontic_Measurements"
set "JAR_NAME=Endodontic_Measurements_2.0.jar"
set "CONFIG_NAME=Endodontic_Measurements.cfg"

echo Starting installation...

echo [1/4] Compiling...
call compile.bat
if %errorlevel% neq 0 (
    echo Compilation failed. Installation aborted.
    exit /b %errorlevel%
)

echo [2/4] Packaging...
call package.bat
if %errorlevel% neq 0 (
    echo Packaging failed. Installation aborted.
    exit /b %errorlevel%
)

echo [3/4] creating destination directory...
if not exist "%DEST_DIR%" (
    mkdir "%DEST_DIR%"
    if %errorlevel% neq 0 (
        echo Failed to create directory: %DEST_DIR%
        exit /b %errorlevel%
    )
    echo Created %DEST_DIR%
)

echo [4/4] Copying files...
copy "dist\%JAR_NAME%" "%DEST_DIR%\" > nul
if %errorlevel% neq 0 (
    echo Failed to copy JAR file.
    exit /b %errorlevel%
)
echo Copied %JAR_NAME%

copy "dist\%CONFIG_NAME%" "%DEST_DIR%\" > nul
if %errorlevel% neq 0 (
    echo Failed to copy config file.
    exit /b %errorlevel%
)
echo Copied %CONFIG_NAME%

echo Installation successful!
echo Plugin installed to: %DEST_DIR%
pause
