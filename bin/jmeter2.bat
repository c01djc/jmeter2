@echo off
rem
rem JMeter2 launcher (Windows)
rem Based on Apache JMeter startup script; product entry is jmeter2.bat only.
rem

setlocal

rem Guess JMETER_HOME if not defined
set "CURRENT_DIR=%cd%"
if not "%JMETER_HOME%" == "" goto gotHome
set "JMETER_HOME=%CURRENT_DIR%"
if exist "%JMETER_HOME%\bin\jmeter2.bat" goto okHome
cd ..
set "JMETER_HOME=%cd%"
cd "%CURRENT_DIR%"
if exist "%JMETER_HOME%\bin\jmeter2.bat" goto okHome
set "JMETER_HOME=%~dp0\.."
:gotHome

if exist "%JMETER_HOME%\bin\jmeter2.bat" goto okHome
echo The JMETER_HOME environment variable is not defined correctly
echo This environment variable is needed to run JMeter2
goto end
:okHome

if exist "%JMETER_HOME%\bin\setenv.bat" call "%JMETER_HOME%\bin\setenv.bat"

if not defined JMETER_LANGUAGE (
    rem Use JVM / OS default locale (set language= in user.properties to pin a locale)
    set JMETER_LANGUAGE=
)

set MINIMAL_VERSION=1.8.0
set JAVA9_OPTS=

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)
if not defined JAVAVER (
    @echo Not able to find Java executable or version. Please check your Java installation.
    set ERRORLEVEL=2
    goto pause
)

IF "%JAVAVER:~1,2%"=="1." (
    set JAVAVER=%JAVAVER:"=%
    for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
        set current_minor=%%w
)
) else (
    set current_minor=9
    set JAVA9_OPTS=--add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.swing=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/sun.awt.shell=ALL-UNNAMED
)

for /f "delims=. tokens=1-3" %%v in ("%MINIMAL_VERSION%") do (
    set minimal_minor=%%w
)

if not defined current_minor (
    @echo Not able to find Java executable or version. Please check your Java installation.
    set ERRORLEVEL=2
    goto pause
)
if %current_minor% LSS %minimal_minor% (
    @echo Error: Java version -- %JAVAVER% -- is too low to run JMeter2. Needs Java ^>= %MINIMAL_VERSION%
    set ERRORLEVEL=3
    goto pause
)

if not defined JM_LAUNCH (
    set JM_LAUNCH=java.exe
)

if not defined JMETER_BIN (
    set JMETER_BIN=%~dp0
)

set JMETER_CMD_LINE_ARGS=%*

if not defined HEAP (
    set HEAP=-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m
)

if not defined GC_ALGO (
    set GC_ALGO=-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1ReservePercent=20
)

set SYSTEM_PROPS=-Djava.security.egd=file:/dev/urandom
set DUMP=-XX:+HeapDumpOnOutOfMemoryError

if not defined DDRAW (
    set DDRAW=
)

if not defined JMETER_COMPLETE_ARGS (
    set ARGS=%JAVA9_OPTS% %DUMP% %HEAP% %VERBOSE_GC% %GC_ALGO% %DDRAW% %SYSTEM_PROPS% %JMETER_LANGUAGE% %RUN_IN_DOCKER%
) else (
    set ARGS=
)

if "%JM_START%" == "start" (
    set JM_START=start "JMeter2"
)

%JM_START% "%JM_LAUNCH%" %ARGS% %JVM_ARGS% -jar "%JMETER_BIN%ApacheJMeter.jar" %JMETER_CMD_LINE_ARGS%

if NOT errorlevel 0 goto pause
if errorlevel 1 goto pause
goto end

:pause
echo errorlevel=%ERRORLEVEL%
pause

:end
