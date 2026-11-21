@echo off
chcp 65001 > nul
title FarmaMed - Base de datos farmamed
setlocal enabledelayedexpansion

rem ============================================================
rem  FarmaMed - Crea y prepara la base de datos "farmamed".
rem
rem  Orden de ejecucion:
rem    1. crea la base si no existe
rem    2. aplica estructura, indices, vistas, guardas y seguridad
rem    3. aplica los datos de negocio cuando ya existen los usuarios
rem    4. muestra un resumen
rem
rem  Los scripts son idempotentes: se pueden volver a ejecutar.
rem ============================================================

set "PG_BIN=C:\Program Files\PostgreSQL\18\bin"
set "PGHOST=localhost"
set "PGPORT=5432"
set "PGUSER=postgres"
set "PGPASSWORD=admin"
set "DB_NAME=farmamed"
set "TMP_OUT=%TEMP%\farmamed_sql_out.txt"

cd /d "%~dp0database"

if not exist "%PG_BIN%\psql.exe" (
  echo [ERROR] No se encuentra psql.exe en "%PG_BIN%"
  echo         Instala PostgreSQL 18 o ajusta PG_BIN en este archivo.
  pause
  exit /b 1
)

echo ============================================
echo   FarmaMed - Base de datos %DB_NAME%
echo ============================================
echo.

rem ------------------------------------------------------------
rem  1. Crear la base de datos
rem ------------------------------------------------------------
"%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d postgres -tAc "SELECT count(*) FROM pg_database WHERE datname = $$%DB_NAME%$$" > "%TMP_OUT%" 2>nul
set "DB_EXISTE="
set /p DB_EXISTE=<"%TMP_OUT%"

if "%DB_EXISTE%"=="1" (
  echo [1/4] La base %DB_NAME% ya existe.
) else (
  echo [1/4] Creando la base %DB_NAME% ...
  "%PG_BIN%\createdb.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -E UTF8 -T template0 %DB_NAME%
  if errorlevel 1 (
    echo [ERROR] No se pudo crear la base.
    pause
    exit /b 1
  )
)

rem ------------------------------------------------------------
rem  2. Estructura, indices, vistas, guardas y seguridad
rem ------------------------------------------------------------
echo [2/4] Aplicando estructura, indices, vistas, guardas y seguridad ...
for %%S in (001_schema.sql 002_indexes.sql 003_views.sql 004_triggers.sql 006_seguridad_bd.sql) do (
  echo       - %%~nxS
  "%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f "%%~nxS"
  if errorlevel 1 (
    echo [ERROR] Fallo al aplicar %%~nxS
    pause
    exit /b 1
  )
)

rem ------------------------------------------------------------
rem  3. Datos de negocio
rem    El seed usa los 4 roles y usuarios que crea la aplicacion
rem    al arrancar por primera vez, asi que necesita que el
rem    backend se haya iniciado al menos una vez.
rem ------------------------------------------------------------
"%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -tAc "SELECT count(*) FROM farmamed.usuario" > "%TMP_OUT%" 2>nul
set "USUARIOS="
set /p USUARIOS=<"%TMP_OUT%"

if "%USUARIOS%"=="0" (
  echo [3/4] Datos de negocio pendientes: la tabla farmamed.usuario esta vacia.
  echo.
  echo       El seed necesita los 4 roles y usuarios que crea la aplicacion.
  echo         1. Inicia el backend una vez:  INICIAR-BACKEND.bat
  echo         2. Cierra el backend.
  echo         3. Vuelve a ejecutar este archivo.
) else (
  echo [3/4] Aplicando datos de negocio ...
  "%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f "005_seed_data.sql"
  if errorlevel 1 (
    echo [ERROR] Fallo al aplicar 005_seed_data.sql
    pause
    exit /b 1
  )
)

rem ------------------------------------------------------------
rem  4. Verificacion final
rem ------------------------------------------------------------
echo.
echo [4/4] Verificando ...
"%PG_BIN%\psql.exe" -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %DB_NAME% -tAc "SELECT (SELECT count(*) FROM farmamed.configuracion_sistema) || ' configuraciones, ' || (SELECT count(*) FROM farmamed.usuario) || ' usuarios, ' || (SELECT count(*) FROM farmamed.medicamento) || ' medicamentos, ' || (SELECT count(*) FROM farmamed.lote) || ' lotes, ' || (SELECT count(*) FROM farmamed.dispensacion) || ' dispensaciones'" > "%TMP_OUT%" 2>nul
set "RESUMEN="
set /p RESUMEN=<"%TMP_OUT%"
if not defined RESUMEN set "RESUMEN=(sin datos)"
echo       %RESUMEN%

echo.
echo ============================================
echo   Base de datos %DB_NAME% lista
echo ============================================
echo   Usuario de la aplicacion : farmamed_app
echo   Conexion desde la app    : jdbc:postgresql://localhost:5432/%DB_NAME%?currentSchema=farmamed,public
echo.
pause
