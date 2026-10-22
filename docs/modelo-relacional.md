# FarmaMed – Modelo Relacional

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Base de Datos:** PostgreSQL  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

El Modelo Relacional representa la transformación del Modelo Entidad Relación (DER) en estructuras tabulares listas para implementarse en PostgreSQL.

Este documento define:

- Tablas.
- Claves primarias (PK).
- Claves foráneas (FK).
- Restricciones UNIQUE.
- Restricciones CHECK.
- Relaciones entre tablas.

---

# 2. Esquema General

```text
SEGURIDAD
├── ROL
├── USUARIO
└── AUDITORIA

CATÁLOGOS
├── PROVEEDOR
└── MEDICAMENTO

INVENTARIO
├── LOTE
├── MOVIMIENTO_INVENTARIO
└── BAJA_INVENTARIO

ATENCIÓN FARMACÉUTICA
├── PACIENTE
├── RECETA
├── DISPENSACION
└── DETALLE_DISPENSACION

ABASTECIMIENTO
├── ORDEN_COMPRA
└── DETALLE_ORDEN_COMPRA

CONFIGURACIÓN
└── CONFIGURACION_SISTEMA
```

---

# 3. Tablas Relacionales

## ROL

```text
ROL
(
    id PK,
    nombre UQ,
    descripcion,
    activo,

    created_at,

    updated_at
)
```

### Restricciones

```text
PK(id)

UNIQUE(nombre)
```

---

## USUARIO

```text
USUARIO
(
    id PK,

    rol_id FK,

    usuario UQ,
    nombres,
    apellidos,

    correo UQ,
    password_hash,

    activo,
    ultimo_acceso,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at
)
```

### Relaciones

```text
USUARIO N:1 ROL
```

### Restricciones

```text
PK(id)

FK(rol_id)

UNIQUE(usuario)

UNIQUE(correo)
```

---

## AUDITORIA

```text
AUDITORIA
(
    id PK,

    usuario_id FK,

    entidad,
    accion,

    valor_anterior,
    valor_nuevo,

    fecha_hora
)
```

### Relaciones

```text
AUDITORIA N:1 USUARIO
```

---

## PROVEEDOR

```text
PROVEEDOR
(
    id PK,

    ruc UQ,
    razon_social,

    telefono,
    correo,

    activo,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at,

    deleted_by FK,
    deleted_at
)
```

### Restricciones

```text
PK(id)

UNIQUE(ruc)
```

---

## MEDICAMENTO

```text
MEDICAMENTO
(
    id PK,

    proveedor_id FK,

    codigo UQ,
    nombre,
    descripcion,
    unidad_medida,

    stock_minimo,
    stock_maximo,

    activo,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at,

    deleted_by FK,
    deleted_at
)
```

### Relaciones

```text
MEDICAMENTO N:1 PROVEEDOR
```

### Restricciones

```text
UNIQUE(codigo)

CHECK(stock_minimo > 0)

CHECK(stock_maximo > stock_minimo)
```

---

## LOTE

```text
LOTE
(
    id PK,

    medicamento_id FK,

    numero_lote,

    fecha_ingreso,
    fecha_vencimiento,

    cantidad_inicial,
    cantidad_actual,

    estado,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at
)
```

### Relaciones

```text
LOTE N:1 MEDICAMENTO
```

### Restricciones

```text
UNIQUE(medicamento_id, numero_lote)

CHECK(cantidad_inicial > 0)

CHECK(cantidad_actual >= 0)

CHECK(fecha_vencimiento > fecha_ingreso)
```

---

## PACIENTE

```text
PACIENTE
(
    id PK,

    dni UQ,

    nombres,
    apellidos,

    activo,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at
)
```

### Restricciones

```text
UNIQUE(dni)
```

---

## RECETA

```text
RECETA
(
    id PK,

    paciente_id FK,

    numero_receta UQ,

    medico,

    tipo_receta,

    fecha_receta,

    created_by FK,
    created_at
)
```

### Relaciones

```text
RECETA N:1 PACIENTE
```

### Restricciones

```text
UNIQUE(numero_receta)

CHECK(tipo_receta IN
(
 'ASEGURADO',
 'PARTICULAR'
))
```

---

## DISPENSACION

```text
DISPENSACION
(
    id PK,

    receta_id FK,

    usuario_id FK,

    fecha_hora,

    estado,

    created_by FK,
    created_at
)
```

### Relaciones

```text
DISPENSACION N:1 RECETA

DISPENSACION N:1 USUARIO
```

### Restricciones

```text
CHECK
(
 estado IN
 (
   'COMPLETADA',
   'ANULADA'
 )
)

UNIQUE(receta_id)
-- 1 Receta = 1 Dispensación
```

---

## DETALLE_DISPENSACION

```text
DETALLE_DISPENSACION
(
    id PK,

    dispensacion_id FK,

    medicamento_id FK,

    lote_id FK,

    cantidad
)
```

### Relaciones

```text
DETALLE_DISPENSACION N:1 DISPENSACION

DETALLE_DISPENSACION N:1 MEDICAMENTO

DETALLE_DISPENSACION N:1 LOTE
```

### Restricciones

```text
CHECK(cantidad > 0)
```

---

## BAJA_INVENTARIO

```text
BAJA_INVENTARIO
(
    id PK,

    lote_id FK,

    usuario_id FK,

    motivo,

    cantidad,

    observacion,

    fecha_hora
)
```

### Relaciones

```text
BAJA_INVENTARIO N:1 LOTE

BAJA_INVENTARIO N:1 USUARIO
```

### Restricciones

```text
CHECK(cantidad > 0)

CHECK
(
 motivo IN
 (
   'VENCIDO_EN_ESTANTE',
   'MERMA',
   'DEVUELTO'
 )
)
```

---

## MOVIMIENTO_INVENTARIO

```text
MOVIMIENTO_INVENTARIO
(
    id PK,

    medicamento_id FK,

    lote_id FK,

    usuario_id FK,

    tipo_movimiento,

    cantidad,

    stock_anterior,
    stock_nuevo,

    referencia_tipo,
    referencia_id,

    fecha_hora
)
```

### Relaciones

```text
MOVIMIENTO_INVENTARIO N:1 MEDICAMENTO

MOVIMIENTO_INVENTARIO N:1 LOTE

MOVIMIENTO_INVENTARIO N:1 USUARIO
```

### Restricciones

```text
CHECK
(
 tipo_movimiento IN
 (
   'ENTRADA',
   'SALIDA',
   'BAJA',
   'AJUSTE'
 )
)

CHECK(cantidad > 0)

CHECK(stock_nuevo >= 0)
```

---

## ORDEN_COMPRA

```text
ORDEN_COMPRA
(
    id PK,

    proveedor_id FK,

    usuario_id FK,

    estado,

    observacion,

    fecha_hora,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at
)
```

### Relaciones

```text
ORDEN_COMPRA N:1 PROVEEDOR

ORDEN_COMPRA N:1 USUARIO
```

### Restricciones

```text
CHECK
(
 estado IN
 (
   'BORRADOR',
   'SUGERIDA',
   'APROBADA',
   'RECHAZADA'
 )
)
```

---

## DETALLE_ORDEN_COMPRA

```text
DETALLE_ORDEN_COMPRA
(
    id PK,

    orden_compra_id FK,

    medicamento_id FK,

    cantidad
)
```

### Relaciones

```text
DETALLE_ORDEN_COMPRA N:1 ORDEN_COMPRA

DETALLE_ORDEN_COMPRA N:1 MEDICAMENTO
```

### Restricciones

```text
CHECK(cantidad > 0)
```

---

## CONFIGURACION_SISTEMA

```text
CONFIGURACION_SISTEMA
(
    id PK,

    clave UQ,

    valor,

    descripcion,

    activo,

    created_by FK,
    created_at,

    updated_by FK,
    updated_at
)
```

### Restricciones

```text
UNIQUE(clave)
```

---

# 4. Relaciones Globales

```text
ROL
 1 ───── N USUARIO

USUARIO
 1 ───── N AUDITORIA

PROVEEDOR
 1 ───── N MEDICAMENTO

MEDICAMENTO
 1 ───── N LOTE

PACIENTE
 1 ───── N RECETA

RECETA
 1 ───── 1 DISPENSACION (1:1, receta_id UQ)

DISPENSACION
 1 ───── N DETALLE_DISPENSACION

LOTE
 1 ───── N DETALLE_DISPENSACION

MEDICAMENTO
 1 ───── N DETALLE_DISPENSACION

LOTE
 1 ───── N BAJA_INVENTARIO

PROVEEDOR
 1 ───── N ORDEN_COMPRA

ORDEN_COMPRA
 1 ───── N DETALLE_ORDEN_COMPRA

MEDICAMENTO
 1 ───── N DETALLE_ORDEN_COMPRA

MEDICAMENTO
 1 ───── N MOVIMIENTO_INVENTARIO

LOTE
 1 ───── N MOVIMIENTO_INVENTARIO

USUARIO
 1 ───── N MOVIMIENTO_INVENTARIO
```

---

# 5. Índices Recomendados

## FEFO

```text
IDX_LOTE_FEFO
(
    medicamento_id,
    fecha_vencimiento
)
```

---

## Recetas

```text
IDX_RECETA_NUMERO
(
    numero_receta
)
```

---

## Auditoría

```text
IDX_AUDITORIA_FECHA
(
    fecha_hora
)
```

---

## Movimientos

```text
IDX_MOVIMIENTO_FECHA
(
    fecha_hora
)
```

---

## Dashboard

```text
IDX_LOTE_VENCIMIENTO
(
    fecha_vencimiento
)
```

---

# 6. Vistas Recomendadas (nombres reales en 003_views.sql)

```text
vw_lotes_fefo

vw_stock_actual

vw_stock_critico

vw_proximos_vencer

vw_lotes_vencidos

vw_pedidos_sugeridos

vw_consumo_mensual

vw_bajas_vencimiento

vw_ordenes_pendientes

vw_kardex

vw_dashboard
```

---

# 7. Reglas de Integridad Finales

### RI-01

Todo usuario debe tener un rol válido.

### RI-02

No pueden existir lotes duplicados para un mismo medicamento.

### RI-03

No puede existir stock negativo.

### RI-04

Toda dispensación debe tener una receta.

### RI-05

Toda receta debe tener un paciente.

### RI-06

Toda orden de compra debe tener proveedor.

### RI-07

Toda baja debe asociarse a un lote válido.

### RI-08

Toda operación crítica debe generar auditoría.

### RI-09

La dispensación siempre debe respetar FEFO.

### RI-10

Los lotes vencidos no pueden participar en dispensaciones.

### RI-11

1 Receta = 1 Dispensación (receta_id UNIQUE en DISPENSACION). No se permiten dispensaciones vacías.

---

# 8. Criterio de Cierre

El Modelo Relacional se considera completo cuando:

- Todas las tablas tienen PK.
- Todas las relaciones poseen FK.
- Existen restricciones UNIQUE y CHECK.
- Las reglas de negocio están reflejadas en el modelo.
- El modelo puede implementarse directamente en PostgreSQL.

---
