@echo off
echo Compiling without Maven...

REM Check for ij.jar
if not exist "C:\ImageJ\ij.jar" (
    echo Error: C:\ImageJ\ij.jar not found. Please install ImageJ to C:\ImageJ or use Maven.
    pause
    exit /b 1
)

REM Locate javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: javac not found. Please install a JDK and add it to PATH.
    pause
    exit /b 1
)

REM --------------------------------------------------------------------------
REM Locate jar.exe
REM  Strategy 1: same directory as javac (works for most JDK installs)
REM  Strategy 2: JAVA_HOME\bin  (set by many installers)
REM  Strategy 3: scan common install roots for the newest JDK bin
REM --------------------------------------------------------------------------
set "JAR_CMD="

for /f "delims=" %%i in ('where javac') do (
    if not defined JAR_CMD (
        if exist "%%~dpijar.exe" set "JAR_CMD=%%~dpijar.exe"
    )
)

if not defined JAR_CMD (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
    )
)

if not defined JAR_CMD (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if not defined JAR_CMD (
            if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
        )
    )
)

if not defined JAR_CMD (
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if not defined JAR_CMD (
            if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
        )
    )
)

if not defined JAR_CMD (
    echo Error: jar.exe not found. Is a full JDK (not just JRE) installed?
    echo Set JAVA_HOME to your JDK directory and retry.
    pause
    exit /b 1
)
echo Using jar: %JAR_CMD%

mkdir bin 2>nul

REM Compile targeting Java 8 so the plugin runs inside ImageJ's Java 8 JRE.
REM  --release 8  enforces source, target AND the Java 8 API surface in one flag.
javac --release 8 -encoding UTF-8 -cp "C:\ImageJ\ij.jar" -sourcepath src\main\java -d bin ^
    src\main\java\no\uio\odont\Endodontic_Measurements_2.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b 1
)

REM Copy resources into bin so they end up inside the JAR
echo Copying resources...
copy src\main\resources\plugins.config bin\plugins.config >nul
copy src\main\resources\Endodontic_Measurements.cfg bin\Endodontic_Measurements.cfg >nul

REM Package JAR
echo Packaging JAR...
"%JAR_CMD%" cvf Endodontic_Measurements_2.jar -C bin .
if %errorlevel% neq 0 (
    echo JAR creation failed.
    pause
    exit /b 1
)

REM Install
echo Installing to C:\ImageJ\plugins...
if not exist "C:\ImageJ\plugins\" (
    echo Warning: C:\ImageJ\plugins not found.
    echo Manually copy Endodontic_Measurements_2.jar to your ImageJ plugins folder.
    pause
    exit /b 1
)
copy Endodontic_Measurements_2.jar "C:\ImageJ\plugins\"
if %errorlevel% neq 0 (
    echo Copy to C:\ImageJ\plugins failed.
    pause
    exit /b 1
)

echo Fallback installation complete.
pause
