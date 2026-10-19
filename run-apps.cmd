@echo off
setlocal
chcp 65001 >nul
set "taskJavaExe=java"
if defined JAVA_HOME set "taskJavaExe=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_HOME if exist "C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot\bin\java.exe" set "taskJavaExe=C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot\bin\java.exe"
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-17\bin\java.exe" set "taskJavaExe=C:\Program Files\Java\jdk-17\bin\java.exe"
if not exist "%~dp0apps\permission-console\target\permission-console-1.0-SNAPSHOT.jar" goto build
if not exist "%~dp0apps\user-api\target\user-api-1.0-SNAPSHOT.jar" goto build
goto launch
:build
call "%~dp0build-apps.cmd"
if errorlevel 1 exit /b 1
:launch
if /i "%~1"=="user-api" goto users
"%taskJavaExe%" -Dfile.encoding=UTF-8 "-Dapp.project.db=%~dp0src\main\resources\common\db.properties" -jar "%~dp0apps\permission-console\target\permission-console-1.0-SNAPSHOT.jar" %*
exit /b %errorlevel%
:users
"%taskJavaExe%" -Dfile.encoding=UTF-8 "-Dapp.project.db=%~dp0src\main\resources\common\db.properties" -jar "%~dp0apps\user-api\target\user-api-1.0-SNAPSHOT.jar"
exit /b %errorlevel%
