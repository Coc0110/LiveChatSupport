@echo off
chcp 65001 >nul
echo Dang bien dich JavaFX (Customer)...
javac -encoding UTF-8 -cp "lib/*" src\com\cmp180\livechat\client\customer\Main.java src\com\cmp180\livechat\client\customer\CustomerApp.java src\com\cmp180\livechat\client\customer\ui\*.java src\com\cmp180\livechat\client\customer\network\*.java src\com\cmp180\livechat\client\customer\context\*.java src\com\cmp180\livechat\client\customer\model\*.java src\com\cmp180\livechat\common\*.java

if %ERRORLEVEL% equ 0 (
    echo Dang khoi dong Customer LiveChat...
    java -cp "src;lib/*" com.cmp180.livechat.client.customer.Main
) else (
    echo Loi bien dich! Vui long kiem tra lai.
)
pause
