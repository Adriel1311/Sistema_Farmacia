-- =====================================================
-- FARMAMED
-- 001_schema.sql  -  Esquema fisico de la base de datos
-- PostgreSQL 18
-- -----------------------------------------------------
-- Reglas de este archivo:
--   * Es idempotente: se puede aplicar mas de una vez.
--   * Los tipos coinciden exactamente con el mapeo JPA de
--     las entidades, para que ddl-auto=validate no falle.
--   * LocalDateTime -> timestamp (sin zona horaria).
--     La hora es local del hospital (America/Lima, UTC-5).
--   * LocalDate     -> date
--   * @Enumerated(STRING) -> varchar(n) + CHECK de dominio.
--     No se usan ENUM nativos de PostgreSQL porque el
--     mapeo JPA los persiste como cadenas.
--   * id -> bigserial (IDENTITY de Hibernate = nextval).
--   * created_by / updated_by / deleted_by / aprobado_por
--     guardan el id del usuario sin relacion JPA, pero con
--     integridad referencial real agregada al final.
--
-- Orden de aplicacion: 001 -> 002 -> 003 -> 004 -> 006
-- (005_seed_data.sql se aplica al final, con la app ya
--  arrancada, porque los roles y usuarios los crea
--  DataInitializer).
-- =====================================================

CREATE SCHEMA IF NOT EXISTS farmamed;

SET search_path TO farmamed, public;

-- =====================================================
-- SECUENCIA DEL TICKET INTERNO DE ENTREGA
-- RN05.0: el ticket no es una boleta SUNAT, es un
-- comprobante de entrega de farmacia. El sistema no cobra.
-- =====================================================

CREATE SEQUENCE IF NOT EXISTS farmamed.seq_numero_ticket START 1 INCREMENT 1;

-- =====================================================
-- TABLA ROL
-- M10 Roles. Cuatro roles: JEFE_FARMACIA, TECNICO_FARMACIA,
-- LOGISTICA, ADMIN_TI (RN08).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.rol (
    id BIGSERIAL PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
);

COMMENT ON TABLE farmamed.rol IS
'Roles del sistema. La aplicacion expone 4 roles: JEFE_FARMACIA, TECNICO_FARMACIA, LOGISTICA, ADMIN_TI.';

-- =====================================================
-- TABLA USUARIO
-- M09 Usuarios. El hash de contrasena es BCrypt (RN08).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.usuario (
    id BIGSERIAL PRIMARY KEY,

    rol_id BIGINT NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso TIMESTAMP,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
        REFERENCES farmamed.rol(id),

    CONSTRAINT uk_usuario_username UNIQUE (usuario),
    CONSTRAINT uk_usuario_correo UNIQUE (correo)
);

COMMENT ON TABLE farmamed.usuario IS
'Usuarios del sistema. password_hash almacena el hash BCrypt del contrasena.';

-- =====================================================
-- TABLA PROVEEDOR
-- M06 Abastecimiento. El unico actor que la gestiona es
-- Logistica; el Jefe la consulta (RN07).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.proveedor (
    id BIGSERIAL PRIMARY KEY,

    ruc VARCHAR(11) NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(150),
    contacto VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT uk_proveedor_ruc UNIQUE (ruc),

    CONSTRAINT chk_proveedor_ruc
        CHECK (ruc ~ '^[0-9]{11}$')
);

COMMENT ON TABLE farmamed.proveedor IS
'Proveedores de medicamentos. La baja es logica: activo = FALSE y deleted_at con marca de tiempo.';

-- =====================================================
-- TABLA PACIENTE
-- M04 Dispensacion. codigo_externo queda disponible para
-- una futura integracion con el sistema de historia clinica
-- del hospital (Fase 2, RNF-29). Es opcional y no se usa
-- en esta entrega.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.paciente (
    id BIGSERIAL PRIMARY KEY,

    dni VARCHAR(8) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    codigo_externo VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT uk_paciente_dni UNIQUE (dni),

    CONSTRAINT chk_paciente_dni
        CHECK (dni ~ '^[0-9]{8}$')
);

COMMENT ON TABLE farmamed.paciente IS
'Pacientes atendidos en farmacia. codigo_externo se reserva para la integracion con el HIS en Fase 2.';

-- =====================================================
-- TABLA MEDICAMENTO
-- M02 Catalogo institucional.
-- costo_unitario NO es un precio de venta al paciente:
-- el sistema no cobra (proyecto universitario, sin fines
-- de lucro). Sirve para cuantificar el valor de la merma
-- y el ahorro por rotacion FEFO.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.medicamento (
    id BIGSERIAL PRIMARY KEY,

    proveedor_id BIGINT NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    unidad_medida VARCHAR(30) NOT NULL,
    stock_minimo INTEGER NOT NULL,
    stock_maximo INTEGER NOT NULL,
    costo_unitario NUMERIC(12,2) NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_medicamento_proveedor
        FOREIGN KEY (proveedor_id)
        REFERENCES farmamed.proveedor(id),

    CONSTRAINT uk_medicamento_codigo UNIQUE (codigo),

    CONSTRAINT chk_medicamento_stock_minimo
        CHECK (stock_minimo > 0),

    CONSTRAINT chk_medicamento_stock_maximo
        CHECK (stock_maximo > stock_minimo),

    CONSTRAINT chk_medicamento_costo
        CHECK (costo_unitario >= 0)
);

COMMENT ON TABLE farmamed.medicamento IS
'Catalogo institucional de medicamentos. costo_unitario es costo de adquisicion, no precio de venta: el sistema no cobra.';
COMMENT ON COLUMN farmamed.medicamento.stock_minimo IS
'Cantidad minima para generar alerta de stock (RN06).';
COMMENT ON COLUMN farmamed.medicamento.stock_maximo IS
'Cantidad maxima recomendada de abastecimiento (RN06).';
COMMENT ON COLUMN farmamed.medicamento.costo_unitario IS
'Costo de adquisicion unitario en soles. Permite valuing la merma y el ahorro por rotacion FEFO.';

-- =====================================================
-- TABLA LOTE
-- M03 Inventario por lotes. El vencimiento se controla con
-- fecha_vencimiento; el algoritmo FEFO usa esta fecha (RN02).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.lote (
    id BIGSERIAL PRIMARY KEY,

    medicamento_id BIGINT NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial INTEGER NOT NULL,
    cantidad_actual INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_lote_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES farmamed.medicamento(id),

    CONSTRAINT uq_lote_medicamento
        UNIQUE (medicamento_id, numero_lote),

    CONSTRAINT chk_lote_estado
        CHECK (estado IN ('ACTIVO', 'VENCIDO', 'BAJA')),

    CONSTRAINT chk_lote_cantidad_inicial
        CHECK (cantidad_inicial > 0),

    CONSTRAINT chk_lote_cantidad_actual
        CHECK (cantidad_actual >= 0),

    CONSTRAINT chk_lote_limite
        CHECK (cantidad_actual <= cantidad_inicial),

    CONSTRAINT chk_lote_fecha
        CHECK (fecha_vencimiento > fecha_ingreso)
);

COMMENT ON TABLE farmamed.lote IS
'Control de inventario por lotes y vencimientos. Un lote vencido no se puede dispensar (RN03).';
COMMENT ON COLUMN farmamed.lote.fecha_vencimiento IS
'Fecha utilizada por el algoritmo FEFO (RN02).';

-- =====================================================
-- TABLA RECETA
-- M04 Dispensacion. RN05.0: sin receta no hay atencion.
-- Una receta genera como maximo una dispensacion; esa
-- relacion 1:1 se garantiza con UNIQUE en dispensacion.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.receta (
    id BIGSERIAL PRIMARY KEY,

    paciente_id BIGINT NOT NULL,
    numero_receta VARCHAR(50) NOT NULL,
    medico VARCHAR(200) NOT NULL,
    tipo_receta VARCHAR(20) NOT NULL,
    fecha_receta DATE NOT NULL,
    codigo_externo VARCHAR(50),

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_receta_paciente
        FOREIGN KEY (paciente_id)
        REFERENCES farmamed.paciente(id),

    CONSTRAINT uk_receta_numero UNIQUE (numero_receta),

    CONSTRAINT chk_receta_tipo
        CHECK (tipo_receta IN ('ASEGURADO', 'PARTICULAR'))
);

COMMENT ON TABLE farmamed.receta IS
'Recetas medicas. tipo_receta distingue el regimen de cobertura; el sistema no realiza cobros.';
COMMENT ON COLUMN farmamed.receta.codigo_externo IS
'Identificador de un sistema externo (HIS). Se reserva para la Fase 2 (RNF-29).';

-- =====================================================
-- TABLA DISPENSACION
-- M04 Dispensacion. Cabecera de la atencion en farmacia.
-- numero_ticket es el comprobante interno de entrega.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.dispensacion (
    id BIGSERIAL PRIMARY KEY,

    receta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_ticket VARCHAR(20) NOT NULL
        DEFAULT 'TI-' || lpad(nextval('farmamed.seq_numero_ticket')::text, 6, '0'),

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispensacion_receta
        FOREIGN KEY (receta_id)
        REFERENCES farmamed.receta(id),

    CONSTRAINT fk_dispensacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES farmamed.usuario(id),

    CONSTRAINT uq_dispensacion_receta UNIQUE (receta_id),

    CONSTRAINT uk_dispensacion_numero_ticket UNIQUE (numero_ticket),

    CONSTRAINT chk_dispensacion_estado
        CHECK (estado IN ('COMPLETADA', 'ANULADA'))
);

COMMENT ON TABLE farmamed.dispensacion IS
'Cabecera de atencion farmaceutica. Una receta no se puede atender dos veces (UNIQUE receta_id).';
COMMENT ON COLUMN farmamed.dispensacion.numero_ticket IS
'Ticket interno de entrega. No es una boleta SUNAT: el sistema no cobra (RN05.0).';

-- =====================================================
-- TABLA DETALLE DISPENSACION
-- Un renglon por lote entregado. motivo_excepcion_fefo
-- guarda el motivo cuando el Jefe de Farmacia autoriza
--_no_ seguir el FEFO (RN02).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.detalle_dispensacion (
    id BIGSERIAL PRIMARY KEY,

    dispensacion_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    motivo_excepcion_fefo VARCHAR(500),

    CONSTRAINT fk_dd_dispensacion
        FOREIGN KEY (dispensacion_id)
        REFERENCES farmamed.dispensacion(id),

    CONSTRAINT fk_dd_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES farmamed.medicamento(id),

    CONSTRAINT fk_dd_lote
        FOREIGN KEY (lote_id)
        REFERENCES farmamed.lote(id),

    CONSTRAINT chk_dd_cantidad
        CHECK (cantidad > 0)
);

COMMENT ON COLUMN farmamed.detalle_dispensacion.motivo_excepcion_fefo IS
'Justificacion de la excepcion al FEFO. Solo el Jefe de Farmacia puede autorizarla (RN02).';

-- =====================================================
-- TABLA BAJA INVENTARIO
-- M03 Bajas por caducidad, merma o devolucion (RN06b).
-- valor_perdida cuantifica el costo perdido; no es un
-- cobro, es la valorizacion de la perdida (RN09).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.baja_inventario (
    id BIGSERIAL PRIMARY KEY,

    lote_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    cantidad INTEGER NOT NULL,
    observacion VARCHAR(500),
    valor_perdida NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_baja_lote
        FOREIGN KEY (lote_id)
        REFERENCES farmamed.lote(id),

    CONSTRAINT fk_baja_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES farmamed.usuario(id),

    CONSTRAINT chk_baja_motivo
        CHECK (motivo IN ('VENCIDO_EN_ESTANTE', 'MERMA', 'DEVUELTO')),

    CONSTRAINT chk_baja_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_baja_valor
        CHECK (valor_perdida >= 0)
);

COMMENT ON TABLE farmamed.baja_inventario IS
'Bajas de stock. El motivo determina el tratamiento posterior del lote.';
COMMENT ON COLUMN farmamed.baja_inventario.valor_perdida IS
'Valorizacion en soles de la perdida (cantidad x costo unitario). No es un cobro al paciente.';

-- =====================================================
-- TABLA MOVIMIENTO INVENTARIO
-- M03 Kardex centralizado. Toda entrada, salida, baja o
-- ajuste queda registrado con el stock antes y despues.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.movimiento_inventario (
    id BIGSERIAL PRIMARY KEY,

    medicamento_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    referencia_tipo VARCHAR(50) NOT NULL,
    referencia_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mov_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES farmamed.medicamento(id),

    CONSTRAINT fk_mov_lote
        FOREIGN KEY (lote_id)
        REFERENCES farmamed.lote(id),

    CONSTRAINT fk_mov_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES farmamed.usuario(id),

    CONSTRAINT chk_mov_tipo
        CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'BAJA', 'AJUSTE')),

    CONSTRAINT chk_mov_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_mov_stock_anterior
        CHECK (stock_anterior >= 0),

    CONSTRAINT chk_mov_stock_nuevo
        CHECK (stock_nuevo >= 0)
);

COMMENT ON TABLE farmamed.movimiento_inventario IS
'Kardex centralizado de movimientos de inventario.';
COMMENT ON COLUMN farmamed.movimiento_inventario.referencia_tipo IS
'Tipo de entidad que origino el movimiento: DISPENSACION, BAJA_INVENTARIO, AJUSTE.';
COMMENT ON COLUMN farmamed.movimiento_inventario.referencia_id IS
'Id de la entidad de referencia. Los ajustes manuales usan 0 por no tener entidad origen.';

-- =====================================================
-- TABLA ORDEN COMPRA
-- M07 Ordenes de compra. El sistema sugiere, Logistica
-- decide y el Jefe aprueba o rechaza (RN07).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.orden_compra (
    id BIGSERIAL PRIMARY KEY,

    proveedor_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    observacion TEXT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    aprobado_por BIGINT,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_orden_proveedor
        FOREIGN KEY (proveedor_id)
        REFERENCES farmamed.proveedor(id),

    CONSTRAINT fk_orden_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES farmamed.usuario(id),

    CONSTRAINT chk_orden_estado
        CHECK (estado IN ('BORRADOR', 'SUGERIDA', 'APROBADA', 'RECHAZADA'))
);

COMMENT ON TABLE farmamed.orden_compra IS
'Ordenes de compra. El sistema propone y el Jefe de Farmacia aprueba (RN07).';
COMMENT ON COLUMN farmamed.orden_compra.aprobado_por IS
'Id del Jefe de Farmacia que aprobo o rechazo la orden.';

-- =====================================================
-- TABLA DETALLE ORDEN COMPRA
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.detalle_orden_compra (
    id BIGSERIAL PRIMARY KEY,

    orden_compra_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    sugerido BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_doc_orden
        FOREIGN KEY (orden_compra_id)
        REFERENCES farmamed.orden_compra(id),

    CONSTRAINT fk_doc_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES farmamed.medicamento(id),

    CONSTRAINT chk_doc_cantidad
        CHECK (cantidad > 0)
);

COMMENT ON COLUMN farmamed.detalle_orden_compra.sugerido IS
'TRUE cuando el renglon fue propuesto por el sistema y no agregado manualmente (RN07).';

-- =====================================================
-- TABLA CONFIGURACION SISTEMA
-- M11 Configuracion. Solo el Jefe de Farmacia la modifica
-- y todo cambio queda auditado (RN04, RN06).
-- Los umbrales del semaforo viven aqui, no en el codigo.
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,

    clave VARCHAR(100) NOT NULL,
    CONSTRAINT uq_configuracion_clave UNIQUE (clave),
    valor VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP
);

COMMENT ON TABLE farmamed.configuracion_sistema IS
'Parametros globales. Claves usadas: vencimiento.dias.critico, vencimiento.dias.proximo, sesion.timeout.minutos, alertas.hora.evaluacion, orden.sugerida.automatica, fefo.permitir.excepcion.';
COMMENT ON COLUMN farmamed.configuracion_sistema.valor IS
'Valor del parametro, guardado como texto. Los umbrales del semaforo se leen de esta tabla.';

-- =====================================================
-- TABLA AUDITORIA
-- M15 Auditoria. No hay tabla de alertas: una alerta
-- atendida se registra aqui con la clave de la alerta
-- (RN05.3).
-- =====================================================

CREATE TABLE IF NOT EXISTS farmamed.auditoria (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT NOT NULL,
    entidad VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    descripcion VARCHAR(255),
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES farmamed.usuario(id)
);

COMMENT ON TABLE farmamed.auditoria IS
'Rastro de auditoria. Las alertas atendidas se registran con accion ALERTA_ATENDIDA y la clave de la alerta en descripcion (RN05.3).';

-- =====================================================
-- INTEGRIDAD REFERENCIAL DE LAS TRAZAS DE USUARIO
-- created_by, updated_by, deleted_by y aprobado_por se
-- guardan como Long plano en las entidades, pero aqui se
-- les agrega FK real para que no apunten a usuarios
-- inexistentes. Se agregan al final porque usuario y rol
-- se referencian entre si.
-- =====================================================

DO $$
DECLARE
    ref RECORD;
BEGIN
    FOR ref IN
        SELECT * FROM (VALUES
            ('usuario',            'created_by'),
            ('usuario',            'updated_by'),
            ('proveedor',          'created_by'),
            ('proveedor',          'updated_by'),
            ('proveedor',          'deleted_by'),
            ('paciente',           'created_by'),
            ('paciente',           'updated_by'),
            ('medicamento',        'created_by'),
            ('medicamento',        'updated_by'),
            ('medicamento',        'deleted_by'),
            ('lote',               'created_by'),
            ('lote',               'updated_by'),
            ('receta',             'created_by'),
            ('dispensacion',       'created_by'),
            ('orden_compra',       'created_by'),
            ('orden_compra',       'updated_by'),
            ('orden_compra',       'aprobado_por'),
            ('configuracion_sistema', 'created_by'),
            ('configuracion_sistema', 'updated_by')
        ) AS t(tabla, columna)
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_' || ref.tabla || '_' || ref.columna
        ) THEN
            EXECUTE format(
                'ALTER TABLE farmamed.%I ADD CONSTRAINT fk_%I_%I FOREIGN KEY (%I) REFERENCES farmamed.usuario(id) ON DELETE SET NULL',
                ref.tabla, ref.tabla, ref.columna, ref.columna
            );
        END IF;
    END LOOP;
END
$$;

-- =====================================================
-- FIN DEL ESQUEMA
-- =====================================================
