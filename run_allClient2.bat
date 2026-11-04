@echo off
chcp 65001 > nul
title Atomic MultiLite - Client 2
cd /d "%~dp0"

echo Dang mo Cua so Game thu 2...
java --module-path "javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,java.desktop -cp "bin;lib/*" Gui.main_gui