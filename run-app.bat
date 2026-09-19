@echo off
rem Starts ProgressGrid: the backend on http://localhost:8080 and the site on http://localhost:3000.
rem Needs Java 17+, Python 3, and Maven either on PATH or unzipped into apache-maven-3.9.5 here.
cd /d "%~dp0"

set "MVN=mvn"
if exist "apache-maven-3.9.5\bin\mvn.cmd" set "MVN=%CD%\apache-maven-3.9.5\bin\mvn.cmd"

start "ProgressGrid site" cmd /k python dev_server.py
start "" "http://localhost:3000"

cd Back-End
call "%MVN%" spring-boot:run
pause
