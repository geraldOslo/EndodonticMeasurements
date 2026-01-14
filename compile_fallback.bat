@echo off
echo Compiling without Maven...

REM Check for ij.jar
if not exist "C:\ImageJ\ij.jar" (
    echo Error: C:\ImageJ\ij.jar not found. Please install ImageJ to C:\ImageJ or use Maven.
    pause
    exit /b 1
)

mkdir bin 2>nul

REM Compile
javac -encoding UTF-8 -cp "C:\ImageJ\ij.jar" -sourcepath src\main\java -d bin src\main\java\no\uio\odont\Endodontic_Measurements_2.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

REM Copy resources
echo Copying resources...
copy src\main\resources\plugins.config bin\plugins.config >nul
copy src\main\resources\Endodontic_Measurements.cfg bin\Endodontic_Measurements.cfg >nul

REM Package JAR
echo Packaging JAR...
jar cvf Endodontic_Measurements_2.jar -C bin .

REM Install
echo Installing to C:\ImageJ\plugins...
copy Endodontic_Measurements_2.jar "C:\ImageJ\plugins\"

echo Fallback installation complete.
pause
