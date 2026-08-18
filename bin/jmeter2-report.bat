@echo off
rem
rem JMeter2 — non-GUI run + HTML dashboard report
rem Usage:
rem   jmeter2-report.bat plan.jmx
rem   jmeter2-report.bat plan.jmx results\out
rem
setlocal EnableExtensions

set "BIN=%~dp0"
set "PLAN=%~1"
set "OUTDIR=%~2"

if "%PLAN%"=="" (
  echo Usage: %~nx0 ^<testplan.jmx^> [output-dir]
  echo Example: %~nx0 examples\HTTPAssertionTest.jmx reports\run1
  exit /b 2
)

if not exist "%PLAN%" (
  echo Test plan not found: %PLAN%
  exit /b 3
)

if "%OUTDIR%"=="" (
  set "OUTDIR=%BIN%..\reports\%~n1-%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%-%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%"
  set "OUTDIR=%OUTDIR: =0%"
)

if not exist "%OUTDIR%" mkdir "%OUTDIR%"
set "JTL=%OUTDIR%\results.jtl"
set "HTML=%OUTDIR%\html"

echo Running non-GUI test...
call "%BIN%jmeter2.bat" -n -t "%PLAN%" -l "%JTL%" -e -o "%HTML%"
set "RC=%ERRORLEVEL%"

if not "%RC%"=="0" (
  echo JMeter2 finished with exit code %RC%
  exit /b %RC%
)

echo.
echo JTL : %JTL%
echo HTML: %HTML%\index.html
exit /b 0
