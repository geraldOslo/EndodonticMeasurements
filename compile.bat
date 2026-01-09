@echo off
"C:\Program Files\Java\jdk-25\bin\javac.exe" -cp "C:\ImageJ\ij.jar" -d . plugin\Endodontic_Measurements\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)
echo Compilation successful.
