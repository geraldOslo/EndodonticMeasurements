@echo off
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Maven\bin\mvn.cmd" (
        echo mvn not in PATH, using C:\Program Files\Maven\bin\mvn.cmd
        set "MAVEN_CMD=C:\Program Files\Maven\bin\mvn.cmd"
        goto :found_maven
    )
    echo Maven not found! Running fallback script...
    call compile_fallback.bat
    exit /b
)
set "MAVEN_CMD=mvn"

:found_maven
echo Building with Maven...
call "%MAVEN_CMD%" clean package
if %errorlevel% neq 0 (
    echo Maven build failed!
    pause
    exit /b
)

echo Installing to ImageJ...
if not exist "C:\ImageJ\plugins\" (
    echo C:\ImageJ\plugins not found. Please copy target\Endodontic_Measurements_2-2.0.0.jar manually.
    pause
    exit /b
)

copy target\Endodontic_Measurements_2-2.0.0.jar "C:\ImageJ\plugins\"
echo Installation complete.
pause
