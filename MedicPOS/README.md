# FarmaMed — README técnico

Sistema de gestión de farmacia hospitalaria: dispensing con control FEFO,
inventario por lotes con vencimiento configurable, recetas, abastecimiento,
reportes exportables y auditoría de acciones.

- **Backend:** Spring Boot 3.2 (Java 17) · Spring Security + JWT · Spring Data JPA
  · iText 7 (PDF) · Apache POI 5.2 (XLSX) · ZXing 3.5 (código de barras y QR)
- **Frontend:** Angular 17 (standalone, signals) · SCSS con tokens de diseño
- **Base de datos:** PostgreSQL 18 con esquema `farmamed`

---

## 1. Requisitos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 17 | `JAVA_HOME` debe apuntar a la instalación |
| Maven | 3.9+ | o usar el `mvnw` / IntelliJ |
| PostgreSQL | 18 | servidor local con el superusuario `postgres` |
| Node.js | 18+ | para el frontend |

## 2. Cómo ejecutar

Desde la raíz del proyecto (`Sistema_Farmacia1`):

1. **`INICIAR-BASE-DATOS.bat`** — crea la base `farmamed` y aplica, en orden,
   `001_schema.sql`, `002_indexes.sql`, `003_views.sql`, `004_triggers.sql` y
   `006_seguridad_bd.sql`. Todos los scripts son **idempotentes**.
2. **`INICIAR-BACKEND.bat`** — levanta Spring Boot en el puerto `8080`. En el
   primer arranque `DataInitializer` crea los 4 roles y los 4 usuarios.
3. Cerrar el backend y volver a ejecutar **`INICIAR-BASE-DATOS.bat`**: ahora
   `farmamed.usuario` tiene filas y el script aplica `005_seed_data.sql`
   (proveedores, medicamentos, lotes, pacientes, recetas, una dispensación de
   ejemplo, una baja por merma y órdenes de compra).
4. **`ABRIR.bat`** — levanta Angular en el puerto `4200`.
5. Entrar en <http://localhost:4200> con `jefe` / `FarmaMed2026`
   (también `tecnico`, `logistica` y `admin`).

Otros scripts:

- `database\007_respaldo.bat` — volcado `pg_dump -Fc` en `database\_respaldo\`,
  conserva los 7 respaldos más recientes.
- `mvn -o clean package` — genera `target\pos-backend-1.0.0.jar`.
- `mvn -o test` — ejecuta la suite de integración (11 pruebas).

Swagger: <http://localhost:8080/swagger-ui.html>.

## 3. Arquitectura

```
frontend (Angular 17 :4200)  ──HTTP/JSON + Bearer JWT──▶  backend (:8080 /api)
                                                            │
                                                            ▼
                                        PostgreSQL 18 · esquema "farmamed"
                                        15 tablas · 8 vistas · 4 triggers
```

- El frontend llama directo a `http://localhost:8080/api` (sin proxy); el
  `TokenInterceptor` adjunta `Authorization: Bearer <token>` en cada petición,
  incluidas las descargas binarias.
- El backend usa `ddl-auto=validate`: **nunca** modifica el esquema. Si el
  modelo y la base no coinciden, la aplicación falla al arrancar.
- La conexión del backend usa el rol de mínimo privilegio `farmamed_app`
  (`farmamed_dev_2026`): no puede crear tablas ni alterar el esquema.

## 4. Módulos

| Módulo | Descripción |
|---|---|
| M01 Autenticación | Login JWT, `/auth/me`, logout. BCrypt en contraseñas. |
| M02 Medicamentos | Alta/edición, proveedor, stock mínimo y máximo, búsqueda. |
| M03 Inventario | Ingreso por lote, vencimiento obligatorio (RN01), semáforo, bajas por merma con valorización, kardex. |
| M04 Dispensación | Dispensación por receta con selección FEFO de lote, excepción FEFO autorizada por el Jefe, ticket `TI-######`, anulación con reversión de stock. |
| M05 Alertas | Avisos de vencimiento, stock crítico y suggestions de pedido (solo en pantalla). |
| M06 Abastecimiento | Proveedores, órdenes de compra, sugerencias de reposición, aprobación del Jefe. |
| M07 Dashboard | Indicadores del día/mes, merma, valor de inventario. |
| M08 Reportes | Consumo, inventario, bajas y órdenes de compra; exportables a **PDF** y **XLSX**. |
| M09/M10 Usuarios y roles | Alta de usuarios y roles, reset de contraseña, activación. |
| M11 Configuración | Umbrales de vencimiento (crítico / próximo) y mínimos de stock; solo el Jefe. |
| M12 Pacientes y recetas | Registro de pacientes, recetas médicas, código externo. |
| M13 Auditoría | Registro de acciones por entidad, usuario y rango de fechas. |
| M14 Códigos de barras | Etiquetas CODE 128 (medicamento, lote) y QR (comprobante de entrega). |

## 5. Base de datos

Esquema `farmamed` con 15 tablas: `usuario`, `rol`, `proveedor`,
`medicamento`, `lote`, `movimiento_inventario`, `baja_inventario`, `receta`,
`paciente`, `dispensacion`, `detalle_dispensacion`, `orden_compra`,
`detalle_orden_compra`, `configuracion_sistema` y `auditoria`.

- IDs `BIGSERIAL`, `timestamp` sin zona horaria (`LocalDateTime`), `varchar`
  con `CHECK` para dominios acotados, claves foráneas con `ON DELETE SET NULL`
  en las columnas de auditoría.
- Secuencia `farmamed.seq_numero_ticket` para los tickets `TI-######`.
- 63 índices, incluido `idx_lote_fefo` (parcial, sobre lotes activos) y GIN
  trigram para la búsqueda de texto.
- 8 vistas de consulta (`vw_dashboard`, `vw_inventario_valorizado`,
  `vw_lotes_por_vencer`, `vw_reporte_bajas`, `vw_reporte_consumo`,
  `vw_reporte_ordenes`, `vw_semaforo_lote`, `vw_stock_critico`) que usan la
  función `farmamed.fn_umbral` para respetar los umbrales configurados.
- 4 triggers de **guarda**: impiden datos inconsistentes (no mutan datos).

Scripts en `database\`: `001_schema.sql`, `002_indexes.sql`, `003_views.sql`,
`004_triggers.sql`, `005_seed_data.sql`, `006_seguridad_bd.sql`,
`007_respaldo.bat`.

## 6. API

Base URL `http://localhost:8080/api`. Todas las rutas exigen `Authorization:
Bearer <token>` salvo `POST /api/auth/login` y la documentación de Swagger.

### Autenticación
| Método | Ruta | Rol |
|---|---|---|
| POST | `/auth/login` | público |
| GET | `/auth/me` | autenticado |
| POST | `/auth/logout` | autenticado |

### Catálogo e inventario
| Método | Ruta | Rol |
|---|---|---|
| GET/POST | `/medicamentos`, `/medicamentos/buscar`, `/medicamentos/proveedor/{id}`, `/medicamentos/stock-critico`, `/medicamentos/{id}` | lectura: Jefe, Técnico, Logística |
| PUT/PATCH | `/medicamentos/{id}`, `/medicamentos/{id}/toggle-activo` | Jefe |
| GET/POST | `/lotes`, `/lotes/vencidos`, `/lotes/proximos-vencer`, `/lotes/criticos`, `/lotes/medicamento/{id}`, `/lotes/{id}` | Jefe, Técnico |
| POST | `/lotes/sincronizar-vencidos` | Jefe, Técnico |
| GET/POST | `/bajas`, `/bajas/rango`, `/bajas/motivo/{motivo}`, `/bajas/pendientes` | Jefe, Técnico |
| GET | `/movimientos`, `/movimientos/lote/{loteId}`, `/movimientos/referencia` | Jefe, Técnico |

### Dispensación y atención
| Método | Ruta | Rol |
|---|---|---|
| GET/POST | `/dispensaciones`, `/dispensaciones/{id}`, `/dispensaciones/receta/{recetaId}` | Jefe, Técnico |
| PATCH | `/dispensaciones/{id}/anular` | Jefe, Técnico |
| GET/POST | `/recetas`, `/recetas/{id}` · `/pacientes`, `/pacientes/buscar`, `/pacientes/{id}` | Jefe, Técnico |

### Abastecimiento y alertas
| Método | Ruta | Rol |
|---|---|---|
| GET/POST/PUT/PATCH | `/proveedores/**` | escritura: Logística · lectura: Jefe, Técnico, Logística |
| GET | `/ordenes`, `/ordenes/sugerencias`, `/ordenes/{id}` | Jefe, Logística |
| POST | `/ordenes`, `/ordenes/desde-sugerencia` | Logística |
| PATCH/POST | `/ordenes/{id}/resolver` | Jefe |
| GET | `/alertas`, `/alertas/pendientes` | Jefe, Técnico, Logística |
| POST | `/alertas/atender` | Jefe, Logística |

### Reportes, dashboard y configuración
| Método | Ruta | Rol |
|---|---|---|
| GET | `/dashboard` | todos los roles |
| GET | `/reportes/consumo`, `/reportes/inventario`, `/reportes/bajas`, `/reportes/ordenes` | Jefe, Logística, Admin TI |
| GET | `/reportes/{tipo}/pdf` | Jefe, Logística, Admin TI |
| GET | `/reportes/{tipo}/xlsx` | Jefe, Logística, Admin TI |
| GET | `/codigos-barras/medicamento/{id}` | Jefe, Técnico, Logística |
| GET | `/codigos-barras/lote/{id}` | Jefe, Técnico, Logística |
| GET | `/codigos-barras/dispensacion/{id}` | Jefe, Técnico, Logística |
| GET | `/configuracion`, `/configuracion/semaforo` · PUT `/configuracion` | Jefe |

`{tipo}` admite `consumo`, `inventario`, `bajas` y `ordenes`. Las exportaciones
aceptan `desde` y `hasta` (ISO `yyyy-MM-dd`) y responden con
`Content-Disposition: attachment`.

### Administración
| Método | Ruta | Rol |
|---|---|---|
| GET/POST/PUT/PATCH | `/usuarios/**` | Admin TI |
| GET | `/roles`, `/roles/{id}` | Admin TI |
| GET | `/auditoria`, `/auditoria/entidad/{entidad}`, `/auditoria/usuario/{usuarioId}`, `/auditoria/rango` | Jefe, Admin TI |

## 7. Roles

| Rol | Alcance |
|---|---|
| `JEFE_FARMACIA` | Todo. Único que configura umbrales y mínimos, aprueba pedidos y autoriza excepciones FEFO. |
| `TECNICO_FARMACIA` | Registra inventario y atiende en caja; los avisos solo los consulta. |
| `LOGISTICA` | Proveedores y pedidos; atiende avisos de stock; no dispensa. |
| `ADMIN_TI` | Usuarios, roles y respaldos; no vende en caja ni toca inventario. |

## 8. Reglas de negocio highlights

- **RN01** — todo medicamento ingresa por lote con fecha de vencimiento.
- **RN04/RN06** — los umbrales de vencimiento y los mínimos de stock se
  configuran en `configuracion_sistema`; el semáforo los respeta (por defecto
  30 días crítico, 60 días próximo).
- **RN02** — la dispensación consume primero el lote que vence antes (FEFO);
  cualquier desvío exige autorización del Jefe y queda en auditoría.
- **RN08** — `ADMIN_TI` no attends caja ni modifica inventario.
- Sin cobros: `costo_unitario` y `valor_perdida` valúan inventario y merma;
  `numero_ticket` es el comprobante interno de entrega (para pacientes
  `ASEGURADO` y `PARTICULAR`).

## 9. Pruebas

`mvn -o test` ejecuta 11 pruebas de integración sobre la base real:

- `FarmamedIntegrationTest` (6): login de los 4 roles, 401 con clave
  incorrecta, dashboard por rol, prohibición de dispensar para Logística,
  configuración exclusiva del Jefe, dispensación FEFO con ticket persistido.
- `ReporteExportIntegrationTest` (5): los 4 reportes en PDF, inventario en
  PDF y XLSX, tipo desconocido → 400, Técnico sin acceso a reportes → 403,
  etiquetas de código de barras de medicamento, lote y comprobante.

## 10. Estructura

```
MedicPOS/
├── src/main/java/com/medicalstore/pos/
│   ├── config/      # application.yml, DataInitializer, OpenAPI
│   ├── controller/  # endpoints REST
│   ├── dto/         # request/response
│   ├── entity/      # 15 entidades JPA (schema = "farmamed")
│   ├── exception/   # GlobalExceptionHandler
│   ├── repository/  # Spring Data JPA
│   ├── security/    # JWT, SecurityConfig
│   └── service/     # lógica de negocio
├── src/main/resources/
├── src/test/java/   # pruebas de integración
└── frontend/src/app/
    ├── core/        # servicios HTTP, modelos, interceptor, constantes
    ├── modules/     # un módulo por pantalla (lazy load)
    └── shared/      # componentes y pipes reutilizables
```

## 11. Solución de problemas

| Síntoma | Causa habitual | Qué hacer |
|---|---|---|
| Pantalla en blanco al abrir `index.html` | se abrió el archivo sin compilar | usar `ABRIR.bat` o `npm start` |
| `Could not resolve placeholder 'spring.security.jwt.secret'` | JAR antiguo o corrupto (sincronización de OneDrive) | reconstruir `mvn -o clean package` y arrancar de nuevo |
| `relation "farmamed.xxx" does not exist` | falta `001_schema.sql` | ejecutar `INICIAR-BASE-DATOS.bat` |
| `ddl-auto` validation failed | el modelo y la base difieren | revisar `001_schema.sql` y el mapeo de la entidad |
| 403 al exportar reportes | el rol no tiene permiso | exportar con Jefe, Logística o Admin TI |
