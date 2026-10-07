@echo off
chcp 65001 >nul
echo Dang bien dich JavaFX...
javac -encoding UTF-8 -cp "lib/*" src\com\cmp180\livechat\client\staff\Main.java src\com\cmp180\livechat\client\staff\StaffApp.java src\com\cmp180\livechat\client\staff\ui\*.java src\com\cmp180\livechat\client\staff\network\*.java src\com\cmp180\livechat\client\staff\context\*.java src\com\cmp180\livechat\client\staff\model\*.java src\com\cmp180\livechat\common\*.java

if %ERRORLEVEL% equ 0 (
    echo Dang khoi dong Staff LiveChat...
    java -cp "src;lib/*" com.cmp180.livechat.client.staff.Main
) else (
    echo Loi bien dich! Vui long kiem tra lai.
)
pause
