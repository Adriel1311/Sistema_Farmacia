@echo off
REM ============================================================
REM  FarmaMed - Arranque simultaneo Backend + Frontend
REM  Abre dos ventanas de consola independientes.
REM ============================================================

cd /d "%~dp0"

echo [1/2] Backend (Spring Boot :8080) -> MedicPOS
start "FarmaMed Backend" cmd /k "cd /d MedicPOS && mvn spring-boot:run"

timeout /t 3 >nul

echo [2/2] Frontend (Angular :4200) -> MedicPOS/frontend
start "FarmaMed Frontend" cmd /k "cd /d MedicPOS/frontend && npm start"

echo.
echo ============================================================
echo Backend:  http://localhost:8080/api
echo Frontend: http://localhost:4200
echo Swagger:  http://localhost:8080/swagger-ui.html
echo.
echo Credenciales: jefe / FarmaMed2026  (tambien tecnico, logistica, admin)
echo ============================================================
echo Cierra las ventanas de consola para detener los servicios.
pause