-- =====================================================
-- FARMAMED
-- 008_caja.sql  -  Modulo de Caja (M11)
-- PostgreSQL 18
-- -----------------------------------------------------
-- Script ADITIVO: solo crea tablas nuevas. No modifica ni una
-- sola tabla, columna, indice, vista ni trigger de 001..007.
--
--   precio_venta          precio al publico de cada medicamento
--   caja                  turno de caja (apertura / cierre con arqueo)
--   movimiento_caja       libro de la caja (apertura, venta, salida, devolucion)
--   movimiento_caja_item  detalle de cada venta (medicamento, cantidad, precio)
--
-- Es idempotente: se puede ejecutar varias veces.
-- Ejecutar con INICIAR-CAJA.bat despues de INICIAR-BASE-DATOS.bat.
-- =====================================================

SET search_path TO farmamed, public;

-- -----------------------------------------------------
-- PRECIO DE VENTA
-- Solo se cobra en recetas PARTICULAR; ASEGURADO no cobra.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.precio_venta (
    medicamento_id BIGINT PRIMARY KEY,
    precio NUMERIC(12,2) NOT NULL DEFAULT 0,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pv_medicamento
        FOREIGN KEY (medicamento_id) REFERENCES farmamed.medicamento(id),
    CONSTRAINT fk_pv_usuario
        FOREIGN KEY (updated_by) REFERENCES farmamed.usuario(id),
    CONSTRAINT chk_pv_precio CHECK (precio >= 0)
);

COMMENT ON TABLE farmamed.precio_venta IS
'Precio de venta al publico por medicamento (soles). El costo de adquisicion sigue en medicamento.costo_unitario.';

-- -----------------------------------------------------
-- CAJA
-- Un turno de cobro. Solo puede haber UNA caja abierta a la vez.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.caja (
    id BIGSERIAL PRIMARY KEY,

    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    monto_inicial NUMERIC(12,2) NOT NULL,
    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_apertura_id BIGINT NOT NULL,
    observacion_apertura VARCHAR(255),

    fecha_cierre TIMESTAMP,
    usuario_cierre_id BIGINT,
    monto_esperado NUMERIC(12,2),
    monto_contado NUMERIC(12,2),
    diferencia NUMERIC(12,2),
    observacion_cierre VARCHAR(255),

    CONSTRAINT fk_caja_usuario_apertura
        FOREIGN KEY (usuario_apertura_id) REFERENCES farmamed.usuario(id),
    CONSTRAINT fk_caja_usuario_cierre
        FOREIGN KEY (usuario_cierre_id) REFERENCES farmamed.usuario(id),
    CONSTRAINT chk_caja_estado CHECK (estado IN ('ABIERTA', 'CERRADA')),
    CONSTRAINT chk_caja_monto_inicial CHECK (monto_inicial >= 0),
    CONSTRAINT chk_caja_monto_contado CHECK (monto_contado IS NULL OR monto_contado >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_caja_unica_abierta
    ON farmamed.caja ((1)) WHERE estado = 'ABIERTA';
CREATE INDEX IF NOT EXISTS idx_caja_fecha_apertura
    ON farmamed.caja (fecha_apertura DESC);

COMMENT ON TABLE farmamed.caja IS
'Turno de caja. monto_esperado, monto_contado y diferencia se llenan al cerrar (arqueo).';

-- -----------------------------------------------------
-- MOVIMIENTO DE CAJA
-- monto siempre positivo: APERTURA y VENTA suman; SALIDA y DEVOLUCION restan.
-- VENTA y DEVOLUCION se vinculan con la dispensacion (boleta).
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.movimiento_caja (
    id BIGSERIAL PRIMARY KEY,

    caja_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    dispensacion_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mc_caja
        FOREIGN KEY (caja_id) REFERENCES farmamed.caja(id),
    CONSTRAINT fk_mc_dispensacion
        FOREIGN KEY (dispensacion_id) REFERENCES farmamed.dispensacion(id),
    CONSTRAINT fk_mc_usuario
        FOREIGN KEY (usuario_id) REFERENCES farmamed.usuario(id),
    CONSTRAINT chk_mc_tipo CHECK (tipo IN ('APERTURA', 'VENTA', 'SALIDA', 'DEVOLUCION')),
    CONSTRAINT chk_mc_monto CHECK (monto > 0 OR (tipo = 'APERTURA' AND monto >= 0)),
    CONSTRAINT chk_mc_venta_dispensacion
        CHECK (tipo NOT IN ('VENTA', 'DEVOLUCION') OR dispensacion_id IS NOT NULL)
);

-- Una boleta se cobra una sola vez y se devuelve una sola vez.
CREATE UNIQUE INDEX IF NOT EXISTS uq_mc_venta_por_dispensacion
    ON farmamed.movimiento_caja (dispensacion_id) WHERE tipo = 'VENTA';
CREATE UNIQUE INDEX IF NOT EXISTS uq_mc_devolucion_por_dispensacion
    ON farmamed.movimiento_caja (dispensacion_id) WHERE tipo = 'DEVOLUCION';
CREATE INDEX IF NOT EXISTS idx_mc_caja_fecha
    ON farmamed.movimiento_caja (caja_id, fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_mc_dispensacion
    ON farmamed.movimiento_caja (dispensacion_id);

COMMENT ON TABLE farmamed.movimiento_caja IS
'Libro de la caja: apertura, ventas por boleta, salidas (retiros) y devoluciones por anulacion.';

-- -----------------------------------------------------
-- DETALLE DE LA VENTA
-- Precio, nombre y lote congelados al cobrar: la boleta no cambia
-- aunque despues cambie el precio o el catalogo.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.movimiento_caja_item (
    id BIGSERIAL PRIMARY KEY,

    movimiento_id BIGINT NOT NULL,
    detalle_dispensacion_id BIGINT,
    medicamento_id BIGINT NOT NULL,
    medicamento_nombre VARCHAR(200) NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_mci_movimiento
        FOREIGN KEY (movimiento_id) REFERENCES farmamed.movimiento_caja(id),
    CONSTRAINT fk_mci_detalle
        FOREIGN KEY (detalle_dispensacion_id) REFERENCES farmamed.detalle_dispensacion(id),
    CONSTRAINT fk_mci_medicamento
        FOREIGN KEY (medicamento_id) REFERENCES farmamed.medicamento(id),
    CONSTRAINT chk_mci_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_mci_precio CHECK (precio_unitario >= 0)
);

CREATE INDEX IF NOT EXISTS idx_mci_movimiento
    ON farmamed.movimiento_caja_item (movimiento_id);

-- -----------------------------------------------------
-- PRECIOS DE EJEMPLO
-- Medicamentos activos con costo: costo x 1.30 (30% de margen).
-- No pisa precios ya registrados. Si aun no hay medicamentos
-- (seed sin cargar) no inserta nada: se puede volver a ejecutar.
-- -----------------------------------------------------
INSERT INTO farmamed.precio_venta (medicamento_id, precio)
SELECT m.id, ROUND((m.costo_unitario * 1.30)::numeric, 2)
FROM farmamed.medicamento m
WHERE m.activo = true
  AND m.costo_unitario > 0
ON CONFLICT (medicamento_id) DO NOTHING;

-- -----------------------------------------------------
-- PERMISOS del rol de la aplicacion (minimo privilegio)
-- -----------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmamed_app') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON
            farmamed.precio_venta,
            farmamed.caja,
            farmamed.movimiento_caja,
            farmamed.movimiento_caja_item
        TO farmamed_app;
        GRANT USAGE, SELECT ON
            farmamed.caja_id_seq,
            farmamed.movimiento_caja_id_seq,
            farmamed.movimiento_caja_item_id_seq
        TO farmamed_app;
    END IF;
END
$$;
