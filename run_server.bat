@echo off
chcp 65001 >nul
title LiveChat Server
color 0A
echo Dang khoi dong LiveChat Server...
java -cp bin com.cmp180.livechat.server.ServerMain
pause
