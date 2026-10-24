@echo off
chcp 65001 > nul
title FarmaMed - Backend Spring Boot (puerto 8080)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem Base de datos reconstruida: farmamed (esquema farmamed, con public para las funciones).
rem ddl-auto=validate: el backend verifica el modelo contra el esquema y nunca lo modifica.
set "DATABASE_URL=jdbc:postgresql://localhost:5432/farmamed?currentSchema=farmamed,public"
set "DATABASE_USERNAME=farmamed_app"
set "DATABASE_PASSWORD=farmamed_dev_2026"
set "DDL_AUTO=validate"
set "SHOW_SQL=false"

cd /d "%~dp0MedicPOS"
if not exist "target\pos-backend-1.0.0.jar" (
  echo [ERROR] No existe el JAR. Compila con:  mvn clean package -DskipTests
  pause
  exit /b 1
)

echo Iniciando backend en http://localhost:8080 ...
echo Swagger: http://localhost:8080/swagger-ui.html
echo.
echo Si es la primera vez, al arrancar se crean los 4 roles y usuarios.
echo Despues ejecuta INICIAR-BASE-DATOS.bat para cargar los datos de negocio.
echo.
"%JAVA_HOME%\bin\java.exe" -jar "target\pos-backend-1.0.0.jar"
pause
