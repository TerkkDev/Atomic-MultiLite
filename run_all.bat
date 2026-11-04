@echo off
chcp 65001 > nul
title Atomic MultiLite - Launcher
cd /d "%~dp0"

echo ========================================
echo   KHỞI ĐỘNG ATOMIC MULTILITE SYSTEM
echo ========================================

:: 1. CHẠY SERVER Ở MỘT CỬA SỔ ẨN/RIÊNG
echo [1/2] Dang khoi dong Server...
start "Atomic Server" /min cmd /c "file_server.bat"

:: Chờ 2 giây cho Server khởi tạo xong socket/port
timeout /t 2 /nobreak > nul

:: 2. BIÊN DỊCH VÀ BẬT GUI CLIENT 1
echo [2/2] Dang bien dich va mo GUI Client 1...
javac -encoding UTF-8 --module-path "javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,java.desktop -cp "lib/*" -d bin src\Gui\*.java

if %errorlevel% equ 0 (
    java --module-path "javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,java.desktop -cp "bin;lib/*" Gui.main_gui
) else (
    echo [!] Loi bien dich GUI, vui long kiem tra lai code.
    pause
)