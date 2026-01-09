@echo off
set "JDK_BIN=C:\Program Files\Java\jdk-25\bin"
set "BIN_DIR=bin"
set "JAR_NAME=Endodontic_Measurements_2.0.jar"

if not exist "%BIN_DIR%" (
    echo bin directory not found. Please run compile_v2.bat first.
    exit /b 1
)

echo Packaging into %JAR_NAME%...

rem Copy plugins.config to bin for packaging
copy src\plugins.config %BIN_DIR%\ > nul

cd %BIN_DIR%
"%JDK_BIN%\jar.exe" cvf ..\%JAR_NAME% .
cd ..

echo Packaging successful: %JAR_NAME%
