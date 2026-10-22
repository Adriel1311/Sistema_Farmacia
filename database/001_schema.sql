-- =====================================================
-- FARMAMED
-- 001_schema.sql
-- PostgreSQL
-- =====================================================

CREATE SCHEMA IF NOT EXISTS farmamed;

SET search_path TO farmamed;

-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE tipo_receta_enum AS ENUM (
    'ASEGURADO',
    'PARTICULAR'
);

CREATE TYPE estado_lote_enum AS ENUM (
    'ACTIVO',
    'VENCIDO',
    'BAJA'
);

CREATE TYPE estado_dispensacion_enum AS ENUM (
    'COMPLETADA',
    'ANULADA'
);

CREATE TYPE motivo_baja_enum AS ENUM (
    'VENCIDO_EN_ESTANTE',
    'MERMA',
    'DEVUELTO'
);

CREATE TYPE tipo_movimiento_enum AS ENUM (
    'ENTRADA',
    'SALIDA',
    'BAJA',
    'AJUSTE'
);

CREATE TYPE estado_orden_enum AS ENUM (
    'BORRADOR',
    'SUGERIDA',
    'APROBADA',
    'RECHAZADA'
);

-- =====================================================
-- TABLA ROL
-- =====================================================

CREATE TABLE rol (
    id BIGSERIAL PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL UNIQUE,

    descripcion VARCHAR(255),

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ
);

-- =====================================================
-- TABLA USUARIO
-- =====================================================

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,

    rol_id BIGINT NOT NULL,

    usuario VARCHAR(50) NOT NULL UNIQUE,

    nombres VARCHAR(100) NOT NULL,

    apellidos VARCHAR(100) NOT NULL,

    correo VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    ultimo_acceso TIMESTAMPTZ,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
        REFERENCES rol(id)
);

-- =====================================================
-- TABLA AUDITORIA
-- =====================================================

CREATE TABLE auditoria (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT NOT NULL,

    entidad VARCHAR(100) NOT NULL,

    accion VARCHAR(50) NOT NULL,

    valor_anterior TEXT,

    valor_nuevo TEXT,

    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

-- =====================================================
-- TABLA PROVEEDOR
-- =====================================================

CREATE TABLE proveedor (
    id BIGSERIAL PRIMARY KEY,

    ruc VARCHAR(11) NOT NULL UNIQUE,

    razon_social VARCHAR(200) NOT NULL,

    telefono VARCHAR(30),

    correo VARCHAR(150),

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    deleted_by BIGINT,

    deleted_at TIMESTAMPTZ,

    CONSTRAINT chk_proveedor_ruc
        CHECK (ruc ~ '^[0-9]{11}$')
);

-- =====================================================
-- TABLA MEDICAMENTO
-- =====================================================

CREATE TABLE medicamento (
    id BIGSERIAL PRIMARY KEY,

    proveedor_id BIGINT NOT NULL,

    codigo VARCHAR(30) NOT NULL UNIQUE,

    nombre VARCHAR(200) NOT NULL,

    descripcion TEXT,

    unidad_medida VARCHAR(30) NOT NULL,

    stock_minimo INTEGER NOT NULL,

    stock_maximo INTEGER NOT NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    deleted_by BIGINT,

    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_medicamento_proveedor
        FOREIGN KEY (proveedor_id)
        REFERENCES proveedor(id),

    CONSTRAINT chk_stock_minimo
        CHECK (stock_minimo > 0),

    CONSTRAINT chk_stock_maximo
        CHECK (stock_maximo > stock_minimo)
);

-- =====================================================
-- TABLA LOTE
-- =====================================================

CREATE TABLE lote (
    id BIGSERIAL PRIMARY KEY,

    medicamento_id BIGINT NOT NULL,

    numero_lote VARCHAR(50) NOT NULL,

    fecha_ingreso DATE NOT NULL,

    fecha_vencimiento DATE NOT NULL,

    cantidad_inicial INTEGER NOT NULL,

    cantidad_actual INTEGER NOT NULL,

    estado estado_lote_enum NOT NULL DEFAULT 'ACTIVO',

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_lote_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamento(id),

    CONSTRAINT uq_lote_medicamento
        UNIQUE (
            medicamento_id,
            numero_lote
        ),

    CONSTRAINT chk_lote_cantidad_inicial
        CHECK (cantidad_inicial > 0),

    CONSTRAINT chk_lote_cantidad_actual
        CHECK (cantidad_actual >= 0),

    CONSTRAINT chk_lote_limite
        CHECK (cantidad_actual <= cantidad_inicial),

    CONSTRAINT chk_lote_fecha
        CHECK (
            fecha_vencimiento > fecha_ingreso
        )
);

-- =====================================================
-- TABLA PACIENTE
-- =====================================================

CREATE TABLE paciente (
    id BIGSERIAL PRIMARY KEY,

    dni VARCHAR(8) NOT NULL UNIQUE,

    nombres VARCHAR(100) NOT NULL,

    apellidos VARCHAR(100) NOT NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    CONSTRAINT chk_paciente_dni
        CHECK (dni ~ '^[0-9]{8}$')
);

-- =====================================================
-- TABLA RECETA
-- =====================================================

CREATE TABLE receta (
    id BIGSERIAL PRIMARY KEY,

    paciente_id BIGINT NOT NULL,

    numero_receta VARCHAR(50) NOT NULL UNIQUE,

    medico VARCHAR(200) NOT NULL,

    tipo_receta tipo_receta_enum NOT NULL,

    fecha_receta DATE NOT NULL,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_receta_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES paciente(id)
);

-- =====================================================
-- TABLA DISPENSACION
-- =====================================================

CREATE TABLE dispensacion (
    id BIGSERIAL PRIMARY KEY,

    receta_id BIGINT NOT NULL,

    usuario_id BIGINT NOT NULL,

    estado estado_dispensacion_enum NOT NULL
        DEFAULT 'COMPLETADA',

    fecha_hora TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispensacion_receta
        FOREIGN KEY (receta_id)
        REFERENCES receta(id),

    CONSTRAINT fk_dispensacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT uq_dispensacion_receta
        UNIQUE (receta_id)
);

-- =====================================================
-- TABLA DETALLE DISPENSACION
-- =====================================================

CREATE TABLE detalle_dispensacion (
    id BIGSERIAL PRIMARY KEY,

    dispensacion_id BIGINT NOT NULL,

    medicamento_id BIGINT NOT NULL,

    lote_id BIGINT NOT NULL,

    cantidad INTEGER NOT NULL,

    CONSTRAINT fk_dd_dispensacion
        FOREIGN KEY (dispensacion_id)
        REFERENCES dispensacion(id),

    CONSTRAINT fk_dd_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamento(id),

    CONSTRAINT fk_dd_lote
        FOREIGN KEY (lote_id)
        REFERENCES lote(id),

    CONSTRAINT chk_dd_cantidad
        CHECK (cantidad > 0)
);

-- =====================================================
-- TABLA BAJA INVENTARIO
-- =====================================================

CREATE TABLE baja_inventario (
    id BIGSERIAL PRIMARY KEY,

    lote_id BIGINT NOT NULL,

    usuario_id BIGINT NOT NULL,

    motivo motivo_baja_enum NOT NULL,

    cantidad INTEGER NOT NULL,

    observacion VARCHAR(500),

    fecha_hora TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_baja_lote
        FOREIGN KEY (lote_id)
        REFERENCES lote(id),

    CONSTRAINT fk_baja_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT chk_baja_cantidad
        CHECK (cantidad > 0)
);

-- =====================================================
-- TABLA MOVIMIENTO INVENTARIO
-- =====================================================

CREATE TABLE movimiento_inventario (
    id BIGSERIAL PRIMARY KEY,

    medicamento_id BIGINT NOT NULL,

    lote_id BIGINT NOT NULL,

    usuario_id BIGINT NOT NULL,

    tipo_movimiento tipo_movimiento_enum NOT NULL,

    cantidad INTEGER NOT NULL,

    stock_anterior INTEGER NOT NULL,

    stock_nuevo INTEGER NOT NULL,

    referencia_tipo VARCHAR(50) NOT NULL,

    referencia_id BIGINT NOT NULL,

    fecha_hora TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mov_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamento(id),

    CONSTRAINT fk_mov_lote
        FOREIGN KEY (lote_id)
        REFERENCES lote(id),

    CONSTRAINT fk_mov_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT chk_mov_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_mov_stock_anterior
        CHECK (stock_anterior >= 0),

    CONSTRAINT chk_mov_stock_nuevo
        CHECK (stock_nuevo >= 0)
);

-- =====================================================
-- TABLA ORDEN COMPRA
-- =====================================================

CREATE TABLE orden_compra (
    id BIGSERIAL PRIMARY KEY,

    proveedor_id BIGINT NOT NULL,

    usuario_id BIGINT NOT NULL,

    estado estado_orden_enum NOT NULL,

    observacion TEXT,

    fecha_hora TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_orden_proveedor
        FOREIGN KEY (proveedor_id)
        REFERENCES proveedor(id),

    CONSTRAINT fk_orden_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

-- =====================================================
-- TABLA DETALLE ORDEN COMPRA
-- =====================================================

CREATE TABLE detalle_orden_compra (
    id BIGSERIAL PRIMARY KEY,

    orden_compra_id BIGINT NOT NULL,

    medicamento_id BIGINT NOT NULL,

    cantidad INTEGER NOT NULL,

    CONSTRAINT fk_doc_orden
        FOREIGN KEY (orden_compra_id)
        REFERENCES orden_compra(id),

    CONSTRAINT fk_doc_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamento(id),

    CONSTRAINT chk_doc_cantidad
        CHECK (cantidad > 0)
);

-- =====================================================
-- TABLA CONFIGURACION SISTEMA
-- =====================================================

CREATE TABLE configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,

    clave VARCHAR(100) NOT NULL UNIQUE,

    valor VARCHAR(255) NOT NULL,

    descripcion VARCHAR(255),

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,

    updated_at TIMESTAMPTZ
);

-- =====================================================
-- COMENTARIOS
-- =====================================================

COMMENT ON TABLE medicamento IS
'Catalogo institucional de medicamentos';

COMMENT ON TABLE lote IS
'Control de inventario por lotes y vencimientos';

COMMENT ON TABLE dispensacion IS
'Cabecera de atencion farmaceutica';

COMMENT ON TABLE movimiento_inventario IS
'Kardex centralizado de movimientos';

COMMENT ON COLUMN medicamento.stock_minimo IS
'Cantidad minima para generar alerta de stock';

COMMENT ON COLUMN medicamento.stock_maximo IS
'Cantidad maxima recomendada de abastecimiento';

COMMENT ON COLUMN lote.fecha_vencimiento IS
'Fecha utilizada por el algoritmo FEFO';

-- =====================================================
-- FIN DEL SCHEMA
-- =====================================================