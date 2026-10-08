@echo off
chcp 65001 >nul
title LiveChat - Staff Client
set JAVA_HOME=C:\Program Files\Java\jdk-25
echo Dang khoi dong Staff Client (Maven)...
call mvnw.cmd compile exec:java@run-staff
pause
