# FarmaMed — Cómo abrirlo

## El error clásico

Abrir `frontend/src/index.html` directamente (doble clic, Live Server, extensión
Live Preview) muestra una **pantalla en blanco**.

Ese archivo es una plantilla: contiene `<app-root></app-root>` vacío y **ningún
`<script>`**. Angular los inyecta al compilar. Sin scripts no se ejecuta nada.

## Orden de arranque (primera vez)

1. **`INICIAR-BASE-DATOS.bat`** — crea la base `farmamed` y aplica la estructura:
   `001_schema.sql`, `002_indexes.sql`, `003_views.sql`, `004_triggers.sql` y
   `006_seguridad_bd.sql`. Los scripts son idempotentes: se pueden repetir sin
   romper nada.

2. **`INICIAR-BACKEND.bat`** — levanta Spring Boot en el puerto 8080. En el
   primer arranque la aplicación crea los 4 roles y los 4 usuarios
   (`FarmaMed2026`); el backend usa `ddl-auto=validate`, por lo que **nunca
   modifica el esquema**: si el modelo y la base no coinciden, falla al arrancar.

3. **Cierra el backend y vuelve a ejecutar `INICIAR-BASE-DATOS.bat`** — ahora
   `farmamed.usuario` ya tiene datos y el script aplica `005_seed_data.sql`
   (proveedores, medicamentos, lotes, pacientes, recetas, una dispensación de
   ejemplo, una baja por merma y órdenes de compra).

4. **`ABRIR.bat`** — levanta Angular en el puerto 4200.

5. Abre **http://localhost:4200** e ingresa con `jefe` / `FarmaMed2026`
   (también `tecnico`, `logistica` y `admin`).

> Atajo: si la base ya está lista, basta con `INICIAR-BACKEND.bat` + `ABRIR.bat`.

## Respaldo de la base

`database\007_respaldo.bat` genera un volcado con `pg_dump -Fc` en
`database\_respaldo\` y conserva los 7 respaldos más recientes.

## Cómo abrirlo bien (frontend)

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
- La API responde en `http://localhost:8080/api`; Swagger en
  `http://localhost:8080/swagger-ui.html`.
- El frontend llama directo a `http://localhost:8080/api` (sin proxy).
- La conexión del backend usa el rol de mínimo privilegio `farmamed_app`
  (`farmamed_dev_2026`), que no puede crear tablas ni modificar el esquema.

## Exportar y etiquetas

- En **Reportes** (Jefe, Logística o Admin TI) los botones **PDF** y **Excel**
  descargan el reporte de la pestaña activa, respetando el rango de fechas.
  Por API: `GET /api/reportes/{consumo|inventario|bajas|ordenes}/pdf` y
  `/xlsx` (params opcionales `desde` y `hasta`).
- Las **etiquetas** con código de barras están en el botón *Etiqueta* de cada
  medicamento y *Código* de cada lote (CODE 128), y el **QR del comprobante**
  aparece en el modal del ticket de dispensación. Por API:
  `GET /api/codigos-barras/medicamento/{id}`, `/lote/{id}` y `/dispensacion/{id}`
  (imagen PNG).
