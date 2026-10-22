# FarmaMed — Cómo abrirlo

## El error clásico

Abrir `frontend/src/index.html` directamente (doble clic, Live Server, extensión
Live Preview) muestra una **pantalla en blanco**.

Ese archivo es una plantilla: contiene `<app-root></app-root>` vacío y **ningún
`<script>`**. Angular los inyecta al compilar. Sin scripts no se ejecuta nada.

## Cómo abrirlo bien

1. Doble clic en **`ABRIR.bat`** (en la raíz del proyecto).
   O manualmente:
   ```powershell
   cd MedicPOS\frontend
   npm start
   ```
2. Espera el mensaje `compiled successfully` (la primera vez tarda ~40 s).
3. Abre **http://localhost:4200**

Debe aparecer la pantalla de ingreso de FarmaMed.

## Notas

- El servidor escucha en `0.0.0.0`, así que también sirve en `http://127.0.0.1:4200`.
- Para detenerlo: `Ctrl+C` en la ventana del servidor.
- El backend (Spring Boot, puerto 8080) es aparte. Sin él la pantalla de login
  aparece igual, pero el login fallará porque no hay API.
