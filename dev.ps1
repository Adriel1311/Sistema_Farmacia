# FarmaMed - Arranque simultaneo Backend + Frontend (PowerShell)
# Uso: .\dev.ps1

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
$medicPos = Join-Path $root "MedicPOS"
$frontend = Join-Path $medicPos "frontend"

Write-Host "=== FarmaMed: arranque desarrollo ===" -ForegroundColor Cyan
Write-Host "Backend  -> $medicPos" -ForegroundColor Gray
Write-Host "Frontend -> $frontend" -ForegroundColor Gray

# Backend en ventana nueva
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$medicPos'; mvn spring-boot:run" -WindowStyle Normal

# Pequeña pausa para que Maven arranque
Start-Sleep -Seconds 3

# Frontend en otra ventana
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontend'; npm start" -WindowStyle Normal

Write-Host ""
Write-Host "Backend:  http://localhost:8080/api" -ForegroundColor Green
Write-Host "Frontend: http://localhost:4200"   -ForegroundColor Green
Write-Host "Swagger:  http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host ""
Write-Host "Credenciales: jefe / FarmaMed2026 (tambien tecnico, logistica, admin)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Cierra las ventanas de PowerShell para detener los servicios." -ForegroundColor Gray
Read-Host "Presiona Enter para cerrar esta ventana"