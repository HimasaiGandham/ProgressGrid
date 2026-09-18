@echo off
echo =======================================================
echo     Starting ProgressGrid Habit Tracker ...
echo =======================================================
echo.
echo [1] Checking for Maven...
if not exist "apache-maven-3.9.5" (
    echo Maven not found locally. Please ensure you have an IDE or Maven installed,
    echo or run the setup script to download it.
) else (
	set "PATH=%CD%\apache-maven-3.9.5\bin;%PATH%"
)

echo [2] Launching Frontend in your default browser...
start "" "http://localhost:3000/login.html"

echo [3] Starting Spring Boot Backend...
cd Back-End
call ..\apache-maven-3.9.5\bin\mvn.cmd spring-boot:run

pause
