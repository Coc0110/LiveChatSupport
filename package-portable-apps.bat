@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   LiveChat - Dong goi Customer App va Staff App Portable
echo ========================================================
echo.

if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
)

if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo [LOI] Khong tim thay jpackage.exe tai: %JAVA_HOME%
    echo Vui long kiem tra lai duong dan JAVA_HOME!
    pause
    exit /b 1
)

echo [1/4] Bien dich va dong goi cac fat JAR bang Maven...
call .\mvnw.cmd package -DskipTests
if errorlevel 1 (
    echo [LOI] Bien dich Maven that bai!
    pause
    exit /b 1
)

echo [2/4] Chuan bi thu muc dong goi...
if not exist "target\pkg-customer" mkdir "target\pkg-customer"
if not exist "target\pkg-staff" mkdir "target\pkg-staff"
if not exist "dist" mkdir "dist"

copy /y "target\CustomerApp.jar" "target\pkg-customer\" >nul
copy /y "target\StaffApp.jar" "target\pkg-staff\" >nul

if exist "dist\CustomerApp" rmdir /s /q "dist\CustomerApp"
if exist "dist\StaffApp" rmdir /s /q "dist\StaffApp"

echo [3/4] Tao ung dung Portable CustomerApp (CustomerApp.exe)...
"%JAVA_HOME%\bin\jpackage.exe" --type app-image --name CustomerApp --input target\pkg-customer --main-jar CustomerApp.jar --main-class com.cmp180.livechat.client.customer.Main --dest dist

echo [4/4] Tao ung dung Portable StaffApp (StaffApp.exe)...
"%JAVA_HOME%\bin\jpackage.exe" --type app-image --name StaffApp --input target\pkg-staff --main-jar StaffApp.jar --main-class com.cmp180.livechat.client.staff.Main --dest dist

echo.
echo ========================================================
echo   HOAN TAT DONG GOI!
echo   Thu muc ung dung nam tai:
echo     - dist\CustomerApp\CustomerApp.exe
echo     - dist\StaffApp\StaffApp.exe
echo ========================================================
echo.
pause
