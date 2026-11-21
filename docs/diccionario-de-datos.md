# FarmaMed – Diccionario de Datos

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Base de Datos:** PostgreSQL  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

El presente documento define la estructura lógica de los datos utilizados por FarmaMed.

Su objetivo es servir como puente entre el Modelo Entidad Relación (DER) y la implementación física en PostgreSQL.

Cada entidad describe:

- Propósito.
- Campos.
- Restricciones.
- Reglas de integridad.
- Relaciones.

---

# 2. Catálogo de Roles

## Tabla: ROL

### Descripción

Almacena los roles utilizados para controlar el acceso al sistema.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| nombre | VARCHAR(50) | No | UNIQUE |
| descripcion | VARCHAR(255) | Sí | |
| activo | BOOLEAN | No | DEFAULT TRUE |
| created_by | BIGINT | Sí | FK USUARIO |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK USUARIO |
| updated_at | TIMESTAMP | Sí | |

### Registros Iniciales

```text
JEFE_FARMACIA
TECNICO_FARMACIA
LOGISTICA
ADMIN_TI
```

---

# 3. Usuarios

## Tabla: USUARIO

### Descripción

Representa a los usuarios autenticados del sistema.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| rol_id | BIGINT | No | FK |
| usuario | VARCHAR(50) | No | UNIQUE |
| nombres | VARCHAR(100) | No | |
| apellidos | VARCHAR(100) | No | |
| correo | VARCHAR(150) | No | UNIQUE |
| password_hash | VARCHAR(255) | No | |
| activo | BOOLEAN | No | DEFAULT TRUE |
| ultimo_acceso | TIMESTAMP | Sí | |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |

### Relaciones

```text
USUARIO N:1 ROL
```

---

# 4. Auditoría

## Tabla: AUDITORIA

### Descripción

Registro histórico de todas las operaciones críticas.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| usuario_id | BIGINT | No | FK |
| entidad | VARCHAR(100) | No | |
| accion | VARCHAR(50) | No | |
| valor_anterior | TEXT | Sí | |
| valor_nuevo | TEXT | Sí | |
| fecha_hora | TIMESTAMP | No | DEFAULT NOW() |

### Acciones típicas

```text
CREATE
UPDATE
DELETE_LOGICO
DISPENSACION
BAJA
APROBACION
```

---

# 5. Proveedores

## Tabla: PROVEEDOR

### Descripción

Catálogo de proveedores de medicamentos.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| ruc | VARCHAR(11) | No | UNIQUE |
| razon_social | VARCHAR(200) | No | |
| telefono | VARCHAR(30) | Sí | |
| correo | VARCHAR(150) | Sí | |
| activo | BOOLEAN | No | DEFAULT TRUE |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |
| deleted_by | BIGINT | Sí | FK |
| deleted_at | TIMESTAMP | Sí | |

---

# 6. Medicamentos

## Tabla: MEDICAMENTO

### Descripción

Catálogo institucional de medicamentos.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| proveedor_id | BIGINT | No | FK |
| codigo | VARCHAR(30) | No | UNIQUE |
| nombre | VARCHAR(200) | No | |
| descripcion | TEXT | Sí | |
| unidad_medida | VARCHAR(30) | No | |
| stock_minimo | INTEGER | No | CHECK > 0 |
| stock_maximo | INTEGER | No | CHECK > stock_minimo |
| activo | BOOLEAN | No | DEFAULT TRUE |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |
| deleted_by | BIGINT | Sí | FK |
| deleted_at | TIMESTAMP | Sí | |

### Relaciones

```text
MEDICAMENTO N:1 PROVEEDOR
MEDICAMENTO 1:N LOTE
```

---

# 7. Lotes

## Tabla: LOTE

### Descripción

Controla el inventario físico por lote.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| medicamento_id | BIGINT | No | FK |
| numero_lote | VARCHAR(50) | No | |
| fecha_ingreso | DATE | No | |
| fecha_vencimiento | DATE | No | |
| cantidad_inicial | INTEGER | No | CHECK > 0 |
| cantidad_actual | INTEGER | No | CHECK >= 0 |
| estado | VARCHAR(20) | No | |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |

### Restricción de negocio

```text
UNIQUE (medicamento_id, numero_lote)
```

### Estados

```text
ACTIVO
VENCIDO
BAJA
```

---

# 8. Pacientes

## Tabla: PACIENTE

### Descripción

Almacena pacientes atendidos por farmacia.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| dni | VARCHAR(8) | No | UNIQUE |
| nombres | VARCHAR(100) | No | |
| apellidos | VARCHAR(100) | No | |
| activo | BOOLEAN | No | DEFAULT TRUE |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |

---

# 9. Recetas

## Tabla: RECETA

### Descripción

Representa la receta médica utilizada para dispensar medicamentos.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| paciente_id | BIGINT | No | FK |
| numero_receta | VARCHAR(50) | No | UNIQUE |
| medico | VARCHAR(200) | No | |
| tipo_receta | VARCHAR(20) | No | |
| fecha_receta | DATE | No | |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |

### Valores permitidos

```text
ASEGURADO
PARTICULAR
```

---

# 10. Dispensaciones

## Tabla: DISPENSACION

### Descripción

Cabecera de una atención farmacéutica. Relación 1:1 con RECETA (receta_id UNIQUE). No se permiten dispensaciones vacías.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| receta_id | BIGINT | No | FK, UNIQUE (1:1 con RECETA) |
| usuario_id | BIGINT | No | FK |
| fecha_hora | TIMESTAMP | No | DEFAULT NOW() |
| estado | VARCHAR(20) | No | |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |

### Estados

```text
COMPLETADA
ANULADA
```

---

# 11. Detalle de Dispensación

## Tabla: DETALLE_DISPENSACION

### Descripción

Medicamentos entregados durante una dispensación.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| dispensacion_id | BIGINT | No | FK |
| medicamento_id | BIGINT | No | FK |
| lote_id | BIGINT | No | FK |
| cantidad | INTEGER | No | CHECK > 0 |

### Relaciones

```text
DETALLE_DISPENSACION N:1 DISPENSACION
DETALLE_DISPENSACION N:1 MEDICAMENTO
DETALLE_DISPENSACION N:1 LOTE
```

---

# 12. Bajas de Inventario

## Tabla: BAJA_INVENTARIO

### Descripción

Registra pérdidas por vencimiento, merma o devolución.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| lote_id | BIGINT | No | FK |
| usuario_id | BIGINT | No | FK |
| motivo | VARCHAR(50) | No | |
| cantidad | INTEGER | No | CHECK > 0 |
| observacion | VARCHAR(500) | Sí | |
| fecha_hora | TIMESTAMP | No | DEFAULT NOW() |

### Motivos permitidos

```text
VENCIDO_EN_ESTANTE
MERMA
DEVUELTO
```

---

# 13. Órdenes de Compra

## Tabla: ORDEN_COMPRA

### Descripción

Cabecera de pedidos de abastecimiento.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| proveedor_id | BIGINT | No | FK |
| usuario_id | BIGINT | No | FK |
| estado | VARCHAR(20) | No | |
| observacion | TEXT | Sí | |
| fecha_hora | TIMESTAMP | No | DEFAULT NOW() |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |

### Estados

```text
BORRADOR
SUGERIDA
APROBADA
RECHAZADA
```

---

# 14. Detalle de Orden de Compra

## Tabla: DETALLE_ORDEN_COMPRA

### Descripción

Medicamentos incluidos en una orden.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| orden_compra_id | BIGINT | No | FK |
| medicamento_id | BIGINT | No | FK |
| cantidad | INTEGER | No | CHECK > 0 |

---

# 15. Configuración del Sistema

## Tabla: CONFIGURACION_SISTEMA

### Descripción

Almacena parámetros globales del sistema.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| clave | VARCHAR(100) | No | UNIQUE |
| valor | VARCHAR(255) | No | |
| descripcion | VARCHAR(255) | Sí | |
| activo | BOOLEAN | No | DEFAULT TRUE |
| created_by | BIGINT | Sí | FK |
| created_at | TIMESTAMP | No | DEFAULT NOW() |
| updated_by | BIGINT | Sí | FK |
| updated_at | TIMESTAMP | Sí | |

### Parámetros iniciales

```text
DIAS_CRITICO = 30
DIAS_PROXIMO_VENCER = 60
TIMEOUT_SESION = 30
APLICAR_FEFO = TRUE
AUDITORIA_ACTIVA = TRUE
```

---

# 16. Movimiento de Inventario (Recomendado)

## Tabla: MOVIMIENTO_INVENTARIO

### Descripción

Kardex centralizado para registrar todas las entradas, salidas y bajas.

### Campos

| Campo | Tipo PostgreSQL | Nulo | Restricción |
|---------|---------|---------|---------|
| id | BIGSERIAL | No | PK |
| medicamento_id | BIGINT | No | FK |
| lote_id | BIGINT | No | FK |
| usuario_id | BIGINT | No | FK |
| tipo_movimiento | VARCHAR(20) | No | |
| cantidad | INTEGER | No | CHECK > 0 |
| stock_anterior | INTEGER | No | |
| stock_nuevo | INTEGER | No | |
| referencia_tipo | VARCHAR(50) | No | |
| referencia_id | BIGINT | No | |
| fecha_hora | TIMESTAMP | No | DEFAULT NOW() |

### Tipos de movimiento

```text
ENTRADA
SALIDA
BAJA
AJUSTE
```

### Referencias posibles

```text
LOTE
DISPENSACION
BAJA_INVENTARIO
AJUSTE_MANUAL
```

---

# 17. Reglas Globales de Integridad

## RI-01

Todo usuario debe tener un rol válido.

---

## RI-02

No se permiten lotes duplicados para el mismo medicamento.

```text
UNIQUE (medicamento_id, numero_lote)
```

---

## RI-03

No se permite stock negativo.

---

## RI-04

No se permite dispensar desde lotes vencidos.

---

## RI-05

No se permite dispensar más stock del disponible.

---

## RI-06

Toda dispensación debe estar asociada a una receta (1:1, receta_id UNIQUE). No se permiten dispensaciones vacías.

---

## RI-07

Toda receta debe estar asociada a un paciente.

---

## RI-08

Toda operación crítica debe quedar auditada.

---

## RI-09

No se permite dispensar desde lotes con estado <> 'ACTIVO' (BAJA/VENCIDO).

---

# 18. Criterio de Cierre

El Diccionario de Datos se considera completo cuando:

- Todas las entidades del DER están documentadas.
- Todos los campos tienen tipo PostgreSQL definido.
- Se especifican PK y FK.
- Se documentan restricciones de negocio.
- Se documentan relaciones e integridad referencial.

Una vez aprobado este documento, el siguiente paso será:

1. Modelo Relacional.
2. Script PostgreSQL (`schema-postgresql.sql`).
3. Índices y optimizaciones FEFO.
4. Procedimientos y transacciones.
5. Desarrollo Backend Spring Boot.