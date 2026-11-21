@echo off
chcp 65001 > nul
title FarmaMed - Modulo de Caja (base de datos)
setlocal enabledelayedexpansion

rem ============================================================
rem  FarmaMed - Aplica SOLO el script de Caja (008_caja.sql).
rem
rem  Es aditivo: crea las tablas precio_venta, caja, movimiento_caja
rem  y movimiento_caja_item. No modifica ningun script ni tabla
rem  del resto de la base. Se puede ejecutar varias veces.
rem
rem  Ejecutalo DESPUES de INICIAR-BASE-DATOS.bat y cada vez que
rem  vuelvas a cargar los datos de ejemplo (para crear los precios).
rem ============================================================

set "PG_BIN=C:\Program Files\PostgreSQL\18\bin"
set "PGHOST=localhost"
set "PGPORT=5432"
set "PGUSER=postgres"
set "PGPASSWORD=admin"
set "DB_NAME=farmamed"

cd /d "%~dp0database"

if not exist "%PG_BIN%\psql.exe" (
  echo [ERROR] No se encuentra psql.exe en "%PG_BIN%"
  echo         Instala PostgreSQL 18 o ajusta PG_BIN en este archivo.
  pause
  exit /b 1
)

echo ============================================
echo   FarmaMed - Modulo de Caja
echo ============================================
echo.
echo Aplicando 008_caja.sql ...
"%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f "008_caja.sql"
if errorlevel 1 (
  echo.
  echo [ERROR] Fallo al aplicar 008_caja.sql
  echo         Ejecuta primero INICIAR-BASE-DATOS.bat.
  pause
  exit /b 1
)

echo.
echo [OK] Modulo de Caja listo.
pause
