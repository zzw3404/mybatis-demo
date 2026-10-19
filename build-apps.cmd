@echo off
setlocal
chcp 65001 >nul
call "%~dp0mvnw.cmd" -f "%~dp0apps\pom.xml" package %*
exit /b %errorlevel%
