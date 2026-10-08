@echo off
chcp 65001 >nul
title LiveChat - Customer Client
set JAVA_HOME=C:\Program Files\Java\jdk-25
echo Dang khoi dong Customer Client (Maven)...
call mvnw.cmd compile exec:java@run-customer
pause
