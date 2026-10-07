@echo off
chcp 65001 >nul
title LiveChat Server
color 0A

echo Dang bien dich Server...
javac -encoding UTF-8 src\com\cmp180\livechat\server\*.java src\com\cmp180\livechat\common\*.java

if %ERRORLEVEL% equ 0 (
    echo Dang khoi dong LiveChat Server...
    java -cp "src" com.cmp180.livechat.server.ServerMain
) else (
    echo Loi bien dich Server!
)
pause
