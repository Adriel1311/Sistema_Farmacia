@echo off
chcp 65001 > nul
title FarmaMed - Respaldo de la base farmamed
setlocal enabledelayedexpansion

rem ============================================================
rem  FarmaMed - Respaldo de la base de datos.
rem
rem  Genera un volcado con formato custom (-Fc) en la carpeta
rem  database\_respaldo y conserva los ultimos RESPALDOS_A_MANTENER
rem  archivos, borrando los mas antiguos.
rem
rem  Uso:  database\007_respaldo.bat
rem ============================================================

set "PG_BIN=C:\Program Files\PostgreSQL\18\bin"
set "PGHOST=localhost"
set "PGPORT=5432"
set "PGUSER=postgres"
set "PGPASSWORD=admin"
set "DB_NAME=farmamed"
set "RESPALDOS_A_MANTENER=7"

cd /d "%~dp0"

if not exist "%PG_BIN%\pg_dump.exe" (
  echo [ERROR] No se encuentra pg_dump.exe en "%PG_BIN%"
  pause
  exit /b 1
)

if not exist "_respaldo" (
  mkdir "_respaldo"
)

set "STAMP=%DATE:~-4%%DATE:~3,2%%DATE:~0,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%"
set "STAMP=%STAMP: =0%"
set "ARCHIVO=_respaldo\%DB_NAME%_%STAMP%.dump"

echo ============================================
echo   FarmaMed - Respaldo de %DB_NAME%
echo ============================================
echo.

"%PG_BIN%\pg_dump.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -Fc -f "%ARCHIVO%"
if errorlevel 1 (
  echo [ERROR] Fallo el respaldo de %DB_NAME%
  pause
  exit /b 1
)

for %%A in ("%ARCHIVO%") do set "TAMANO=%%~zA"
echo [OK] Respaldo creado: %ARCHIVO% (%TAMANO% bytes)

rem ------------------------------------------------------------
rem  Limpieza: se conservan los respaldos mas recientes
rem ------------------------------------------------------------
set "CONTADOS=0"
for /f "tokens=*" %%A in ('dir /b /o-d "_respaldo\%DB_NAME%_*.dump" 2^>nul') do (
  set /a CONTADOS+=1
  if !CONTADOS! gtr %RESPALDOS_A_MANTENER% (
    echo       Borrando respaldo antiguo: %%A
    del /q "_respaldo\%%A"
  )
)

echo.
echo Respaldo terminado.
pause
