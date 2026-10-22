@echo off
chcp 65001 > nul
title FarmaMed - Backend Spring Boot (puerto 8080)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "DATABASE_URL=jdbc:postgresql://localhost:5432/medical_store_pos"
set "DATABASE_USERNAME=postgres"
set "DATABASE_PASSWORD=admin"
set "DDL_AUTO=update"
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
"%JAVA_HOME%\bin\java.exe" -jar "target\pos-backend-1.0.0.jar"
pause
