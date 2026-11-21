-- =====================================================
-- FARMAMED
-- 002_indexes.sql  -  Indices de la base de datos
-- PostgreSQL 18
-- -----------------------------------------------------
-- Indices para:
--   * sostener las consultas del sistema (RNF-01, RNF-02)
--   * sostener las consultas de los reportes (M08)
--   * proteger el FEFO bajo concurrencia (RN09)
--
-- Los indices UNIQUE que ya son restricciones (uk_*,
-- uq_*) estan cubiertos por el indice que PostgreSQL crea
-- junto con la restriccion, por eso no se repiten aqui.
--
-- Es idempotente: CREATE INDEX IF NOT EXISTS.
-- =====================================================

SET search_path TO farmamed, public;

-- =====================================================
-- PRERREQUISITOS
-- pg_trgm habilita la busqueda de medicamentos por
-- coincidencia parcial sin acentos ni mayusculas.
-- =====================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm SCHEMA public;

-- =====================================================
-- AUDITORIA (M15)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_auditoria_fecha
    ON farmamed.auditoria (fecha_hora DESC);

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario
    ON farmamed.auditoria (usuario_id);

-- Búsqueda por entidad y acción, que es como el usuario
-- filtra el historial (RN08).
CREATE INDEX IF NOT EXISTS idx_auditoria_entidad_accion
    ON farmamed.auditoria (entidad, accion, fecha_hora DESC);

-- =====================================================
-- BAJA INVENTARIO (M03)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_baja_fecha
    ON farmamed.baja_inventario (fecha_hora DESC);

CREATE INDEX IF NOT EXISTS idx_baja_lote
    ON farmamed.baja_inventario (lote_id);

CREATE INDEX IF NOT EXISTS idx_baja_usuario
    ON farmamed.baja_inventario (usuario_id);

-- Reporte de bajas por motivo (M08).
CREATE INDEX IF NOT EXISTS idx_baja_motivo_fecha
    ON farmamed.baja_inventario (motivo, fecha_hora DESC);

-- =====================================================
-- CONFIGURACION SISTEMA (M11)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_configuracion_created_by
    ON farmamed.configuracion_sistema (created_by);

-- =====================================================
-- DETALLE DISPENSACION (M04)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_dd_dispensacion
    ON farmamed.detalle_dispensacion (dispensacion_id);

CREATE INDEX IF NOT EXISTS idx_dd_medicamento
    ON farmamed.detalle_dispensacion (medicamento_id);

CREATE INDEX IF NOT EXISTS idx_dd_lote
    ON farmamed.detalle_dispensacion (lote_id);

-- =====================================================
-- DETALLE ORDEN COMPRA (M07)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_doc_orden
    ON farmamed.detalle_orden_compra (orden_compra_id);

CREATE INDEX IF NOT EXISTS idx_doc_medicamento
    ON farmamed.detalle_orden_compra (medicamento_id);

-- =====================================================
-- DISPENSACION (M04)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_dispensacion_fecha
    ON farmamed.dispensacion (fecha_hora DESC);

CREATE INDEX IF NOT EXISTS idx_dispensacion_usuario
    ON farmamed.dispensacion (usuario_id);

-- =====================================================
-- LOTE (M03)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_lote_vencimiento
    ON farmamed.lote (fecha_vencimiento);

CREATE INDEX IF NOT EXISTS idx_lote_medicamento_fecha
    ON farmamed.lote (medicamento_id, fecha_vencimiento);

-- Indice parcial para el FEFO: solo interesan los lotes
-- vigentes con existencias. Es el indice que sostiene la
-- seleccion de lote en la dispensacion (RN02) y mantiene
-- el indice pequeno cuando hay muchos lotes dados de baja.
CREATE INDEX IF NOT EXISTS idx_lote_fefo
    ON farmamed.lote (medicamento_id, fecha_vencimiento, id)
    WHERE estado = 'ACTIVO' AND cantidad_actual > 0;

-- =====================================================
-- MEDICAMENTO (M02)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_medicamento_nombre
    ON farmamed.medicamento (nombre);

CREATE INDEX IF NOT EXISTS idx_medicamento_proveedor
    ON farmamed.medicamento (proveedor_id);

-- Busqueda parcial por nombre, la que usa el buscador de
-- medicamentos (ILIKE '%texto%').
CREATE INDEX IF NOT EXISTS idx_medicamento_nombre_trgm
    ON farmamed.medicamento USING gin (nombre public.gin_trgm_ops);

-- =====================================================
-- MOVIMIENTO INVENTARIO (M03 kardex)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_mov_fecha
    ON farmamed.movimiento_inventario (fecha_hora DESC);

CREATE INDEX IF NOT EXISTS idx_mov_lote
    ON farmamed.movimiento_inventario (lote_id);

CREATE INDEX IF NOT EXISTS idx_mov_medicamento
    ON farmamed.movimiento_inventario (medicamento_id);

CREATE INDEX IF NOT EXISTS idx_mov_usuario
    ON farmamed.movimiento_inventario (usuario_id);

-- El kardex referencia la entidad que origino el
-- movimiento (DISPENSACION, BAJA_INVENTARIO).
CREATE INDEX IF NOT EXISTS idx_mov_referencia
    ON farmamed.movimiento_inventario (referencia_tipo, referencia_id);

-- =====================================================
-- ORDEN COMPRA (M07)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_orden_estado
    ON farmamed.orden_compra (estado);

CREATE INDEX IF NOT EXISTS idx_orden_proveedor
    ON farmamed.orden_compra (proveedor_id);

CREATE INDEX IF NOT EXISTS idx_orden_usuario
    ON farmamed.orden_compra (usuario_id);

-- =====================================================
-- PACIENTE (M04)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_paciente_created_by
    ON farmamed.paciente (created_by);

-- Historial de recetas por paciente.
CREATE INDEX IF NOT EXISTS idx_receta_paciente_fecha
    ON farmamed.receta (paciente_id, fecha_receta DESC);

-- El codigo externo es la clave de integracion futura
-- con el sistema de historia clinica (Fase 2, RNF-29).
-- Unico solo entre los que lo tienen.
CREATE UNIQUE INDEX IF NOT EXISTS ux_paciente_codigo_externo
    ON farmamed.paciente (codigo_externo)
    WHERE codigo_externo IS NOT NULL;

-- =====================================================
-- PROVEEDOR (M06)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_proveedor_created_by
    ON farmamed.proveedor (created_by);

-- =====================================================
-- RECETA (M04)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_receta_paciente
    ON farmamed.receta (paciente_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_receta_codigo_externo
    ON farmamed.receta (codigo_externo)
    WHERE codigo_externo IS NOT NULL;

-- =====================================================
-- ROL (M10) y USUARIO (M09)
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_usuario_rol
    ON farmamed.usuario (rol_id);

CREATE INDEX IF NOT EXISTS idx_usuario_created_by
    ON farmamed.usuario (created_by);

-- =====================================================
-- RESTRICCIONES UNIQUE PENDIENTES
-- La clave de configuracion es unica: una sola fila por
-- parametro. En una base recien creada 001_schema.sql ya
-- la trae; este bloque cubre las bases que se actualizan.
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_configuracion_clave'
          AND conrelid = 'farmamed.configuracion_sistema'::regclass
    ) THEN
        ALTER TABLE farmamed.configuracion_sistema
            ADD CONSTRAINT uq_configuracion_clave UNIQUE (clave);
    END IF;
END $$;

-- =====================================================
-- FIN DE LOS INDICES
-- =====================================================
