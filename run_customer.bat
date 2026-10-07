@echo off
chcp 65001 >nul
title LiveChat Customer App
color 0B
echo Dang khoi dong LiveChat Customer App...
java -cp bin com.cmp180.livechat.client.customer.CustomerClient
pause
