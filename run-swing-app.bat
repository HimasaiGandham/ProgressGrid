@echo off
rem Launches the ProgressGrid Java Swing Desktop Suite
rem (Interactive Habit Tracking Matrix & Progress Grid + Visual Analytics Dashboard)
cd /d "%~dp0"

set "MVN=mvn"
if exist "apache-maven-3.9.5\bin\mvn.cmd" set "MVN=%CD%\apache-maven-3.9.5\bin\mvn.cmd"

rem If classes are not compiled yet, compile them
if not exist "Back-End\target\classes\com\progressgrid\swing\ProgressGridSwingLauncher.class" (
    echo Compiling ProgressGrid Swing modules...
    cd Back-End
    call "%MVN%" compile -DskipTests
    cd ..
)

echo Starting ProgressGrid Desktop Swing Application...
java -cp "Back-End\target\classes" com.progressgrid.swing.ProgressGridSwingLauncher
pause
