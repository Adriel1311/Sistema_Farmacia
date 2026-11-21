@echo off
chcp 65001 > nul
title FarmaMed - Servidor de desarrollo
cd /d "%~dp0MedicPOS\frontend"

echo.
echo ============================================
echo   FarmaMed - Iniciando servidor Angular
echo ============================================
echo.
echo   NO abras frontend\src\index.html
echo   Espera a que diga: "compiled successfully"
echo.
echo   Luego abre:  http://localhost:4200
echo   (Para cerrar esta ventana: Ctrl+C)
echo.

start "" cmd /c "timeout /t 3 > nul & start http://localhost:4200"
call "C:\Program Files\nodejs\npm.cmd" start
