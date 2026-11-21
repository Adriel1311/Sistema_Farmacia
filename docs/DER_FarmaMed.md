# FarmaMed – Modelo Entidad Relación (DER)

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación
**Motor:** PostgreSQL
**Autor:** Francisco Gabriel Berrocal Ramos

> Nota: `Farmamed-EDR-mermaid.png` está pendiente de regenerar con mermaid-cli (falla puppeteer/chrome en este equipo). Fuente de verdad es el bloque mermaid de este .md (ya incluye MOVIMIENTO_INVENTARIO y 1:1 Receta-Dispensación).

```mermaid
erDiagram
    PACIENTE {
        bigint id PK
        varchar dni
        varchar nombres
        varchar apellidos
        boolean activo
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
    }
    ROL {
        bigint id PK
        varchar nombre
        varchar descripcion
        boolean activo
        timestamp created_at
        timestamp updated_at
    }
    USUARIO {
        bigint id PK
        bigint rol_id FK
        varchar usuario
        varchar nombres
        varchar apellidos
        varchar correo
        varchar password_hash
        boolean activo
        timestamp ultimo_acceso
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
    }
    PROVEEDOR {
        bigint id PK
        varchar ruc
        varchar razon_social
        varchar telefono
        varchar correo
        boolean activo
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
        bigint deleted_by FK
        timestamp deleted_at
    }
    CONFIGURACION_SISTEMA {
        bigint id PK
        varchar clave
        varchar valor
        varchar descripcion
        boolean activo
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
    }
    RECETA {
        bigint id PK
        bigint paciente_id FK
        varchar numero_receta
        varchar medico
        varchar tipo_receta
        date fecha_receta
        bigint created_by FK
        timestamp created_at
    }
    MEDICAMENTO {
        bigint id PK
        bigint proveedor_id FK
        varchar codigo
        varchar nombre
        text descripcion
        varchar unidad_medida
        integer stock_minimo
        integer stock_maximo
        boolean activo
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
        bigint deleted_by FK
        timestamp deleted_at
    }
    LOTE {
        bigint id PK
        bigint medicamento_id FK
        varchar numero_lote
        date fecha_ingreso
        date fecha_vencimiento
        integer cantidad_inicial
        integer cantidad_actual
        varchar estado
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
    }
    DISPENSACION {
        bigint id PK
        bigint receta_id FK
        bigint usuario_id FK
        timestamp fecha_hora
        varchar estado
        bigint created_by FK
        timestamp created_at
    }
    DETALLE_DISPENSACION {
        bigint id PK
        bigint dispensacion_id FK
        bigint medicamento_id FK
        bigint lote_id FK
        integer cantidad
    }
    AUDITORIA {
        bigint id PK
        bigint usuario_id FK
        varchar entidad
        varchar accion
        text valor_anterior
        text valor_nuevo
        timestamp fecha_hora
    }
    BAJA_INVENTARIO {
        bigint id PK
        bigint lote_id FK
        bigint usuario_id FK
        varchar motivo
        integer cantidad
        varchar observacion
        timestamp fecha_hora
    }
    ORDEN_COMPRA {
        bigint id PK
        bigint proveedor_id FK
        bigint usuario_id FK
        varchar estado
        text observacion
        timestamp fecha_hora
        bigint created_by FK
        timestamp created_at
        bigint updated_by FK
        timestamp updated_at
    }
    DETALLE_ORDEN_COMPRA {
        bigint id PK
        bigint orden_compra_id FK
        bigint medicamento_id FK
        integer cantidad
    }
    MOVIMIENTO_INVENTARIO {
        bigint id PK
        bigint medicamento_id FK
        bigint lote_id FK
        bigint usuario_id FK
        varchar tipo_movimiento
        integer cantidad
        integer stock_anterior
        integer stock_nuevo
        varchar referencia_tipo
        bigint referencia_id
        timestamp fecha_hora
    }

    PACIENTE ||--o{ RECETA : "tiene"
    ROL ||--o{ USUARIO : "tiene"
    PROVEEDOR ||--o{ MEDICAMENTO : "suministra"
    PROVEEDOR ||--o{ ORDEN_COMPRA : "recibe"
    USUARIO ||--o{ RECETA : "registra"
    RECETA ||--|| DISPENSACION : "origina (1:1)"
    USUARIO ||--o{ DISPENSACION : "realiza"
    USUARIO ||--o{ AUDITORIA : "genera"
    USUARIO ||--o{ MEDICAMENTO : "registra"
    MEDICAMENTO ||--o{ LOTE : "posee"
    USUARIO ||--o{ LOTE : "registra"
    DISPENSACION ||--o{ DETALLE_DISPENSACION : "contiene"
    MEDICAMENTO ||--o{ DETALLE_DISPENSACION : "dispensado"
    LOTE ||--o{ DETALLE_DISPENSACION : "utilizado"
    LOTE ||--o{ BAJA_INVENTARIO : "baja"
    USUARIO ||--o{ BAJA_INVENTARIO : "registra"
    USUARIO ||--o{ ORDEN_COMPRA : "genera"
    ORDEN_COMPRA ||--o{ DETALLE_ORDEN_COMPRA : "contiene"
    MEDICAMENTO ||--o{ DETALLE_ORDEN_COMPRA : "solicitado"
    MEDICAMENTO ||--o{ MOVIMIENTO_INVENTARIO : "kardex"
    LOTE ||--o{ MOVIMIENTO_INVENTARIO : "kardex"
    USUARIO ||--o{ MOVIMIENTO_INVENTARIO : "registra"
```
