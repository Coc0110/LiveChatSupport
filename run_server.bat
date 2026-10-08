@echo off
chcp 65001 >nul
title LiveChat - Server
set JAVA_HOME=C:\Program Files\Java\jdk-25
echo Dang khoi dong Server (Maven)...
call mvnw.cmd compile exec:java@run-server
pause
