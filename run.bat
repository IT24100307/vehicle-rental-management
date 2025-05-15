@echo off
rem Script to run the Spring Boot application

echo Attempting to run the Spring Boot application...

rem Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Java not found. Please install Java to run this application.
    exit /b 1
)

rem Try to find the JAR file
set JAR_FILE=target\project1-0.0.1-SNAPSHOT.jar
if not exist "%JAR_FILE%" (
    echo JAR file not found: %JAR_FILE%
    echo Please build the application first.
    exit /b 1
)

rem Run the application
echo Starting Spring Boot application...
java -jar %JAR_FILE%
