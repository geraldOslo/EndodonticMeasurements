@echo off
set "JDK_BIN=C:\Program Files\Java\jdk-25\bin"
set "IJ_JAR=C:\ImageJ\ij.jar"
set "BIN_DIR=bin"

echo Running Automated Test...
"%JDK_BIN%\java.exe" -cp "%BIN_DIR%;%IJ_JAR%" no.uio.odont.AutomationTest
