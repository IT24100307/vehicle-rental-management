@echo off
rem Maven build script for project1

echo Building project1...

rem Check if Maven is installed and in PATH
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Maven not found in PATH. Checking for Maven installation...
    
    rem Try some common Maven installation locations
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        echo Found Maven at %%MAVEN_HOME%%
        set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd
    ) else if exist "C:\Program Files\Apache Maven\bin\mvn.cmd" (
        echo Found Maven at C:\Program Files\Apache Maven
        set MVN_CMD=C:\Program Files\Apache Maven\bin\mvn.cmd
    ) else if exist "%USERPROFILE%\apache-maven\bin\mvn.cmd" (
        echo Found Maven at %%USERPROFILE%%\apache-maven
        set MVN_CMD=%USERPROFILE%\apache-maven\bin\mvn.cmd
    ) else (
        echo Maven not found. Please install Maven or add it to your PATH.
        exit /b 1
    )
) else (
    set MVN_CMD=mvn
)

echo Cleaning previous build...
%MVN_CMD% clean

echo Compiling application...
%MVN_CMD% compile

if %ERRORLEVEL% neq 0 (
    echo Build failed. See above for details.
    exit /b 1
)

echo Packaging application...
%MVN_CMD% package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Packaging failed. See above for details.
    exit /b 1
)

echo Build successful!
echo You can now run the application with: java -jar target\project1-0.0.1-SNAPSHOT.jar
