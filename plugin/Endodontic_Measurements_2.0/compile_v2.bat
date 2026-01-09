@echo off
set "JDK_BIN=C:\Program Files\Java\jdk-25\bin"
set "IJ_JAR=C:\ImageJ\ij.jar"
set "BIN_DIR=bin"

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo Finding source files...
dir /s /b src\*.java > sources.txt

echo Compiling version 2.0 and test automation...
"%JDK_BIN%\javac.exe" --release 8 -cp "%IJ_JAR%" -d "%BIN_DIR%" @sources.txt

if %errorlevel% neq 0 (
    echo Compilation failed!
    del sources.txt
    exit /b %errorlevel%
)
echo Compilation successful. Result in %BIN_DIR%
del sources.txt
