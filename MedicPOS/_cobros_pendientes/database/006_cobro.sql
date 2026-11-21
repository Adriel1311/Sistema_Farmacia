-- =====================================================
-- MÓDULO COBRO Y PRECIOS - FARMAMED
-- Migración 006: Tablas de precios de venta y cobros
-- =====================================================

-- -----------------------------------------------------
-- TABLA MEDICAMENTO_PRECIO
-- Histórico de precios de venta por medicamento con vigencia
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.medicamento_precio (
    id BIGSERIAL PRIMARY KEY,

    medicamento_id BIGINT NOT NULL,
    CONSTRAINT fk_mp_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES farmamed.medicamento(id),

    precio_venta DECIMAL(12,2) NOT NULL CHECK (precio_venta >= 0),

    vigente_desde DATE NOT NULL DEFAULT CURRENT_DATE,
    vigente_hasta DATE,  -- NULL = vigente indefinido

    created_by BIGINT REFERENCES farmamed.usuario(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_precio_vigencia UNIQUE (medicamento_id, vigente_desde)
);

COMMENT ON TABLE farmamed.medicamento_precio IS
'Histórico de precios de venta por medicamento. Permite cambios de precio sin afectar cobros pasados.';
COMMENT ON COLUMN farmamed.medicamento_precio.precio_venta IS 'Precio de venta al paciente (no costo de adquisición).';
COMMENT ON COLUMN farmamed.medicamento_precio.vigente_desde IS 'Fecha desde la que aplica este precio.';
COMMENT ON COLUMN farmamed.medicamento_precio.vigente_hasta IS 'Fecha hasta la que aplica (NULL = sin fin).';

CREATE INDEX idx_mp_medicamento_vigencia ON farmamed.medicamento_precio(medicamento_id, vigente_desde, vigente_hasta);
CREATE INDEX idx_mp_vigente_hoy ON farmamed.medicamento_precio(medicamento_id, vigente_desde) WHERE vigente_hasta IS NULL OR vigente_hasta >= CURRENT_DATE;

-- -----------------------------------------------------
-- TABLA COBRO
-- Registro de cobro/pago vinculado a una dispensación
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS farmamed.cobro (
    id BIGSERIAL PRIMARY KEY,

    dispensacion_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_cobro_dispensacion
        FOREIGN KEY (dispensacion_id)
        REFERENCES farmamed.dispensacion(id),

    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    descuento_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,  -- 0, 50, 100
    descuento_monto DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,

    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE, PAGADO, ANULADO
    CONSTRAINT chk_cobro_estado CHECK (estado IN ('PENDIENTE', 'PAGADO', 'ANULADO')),

    metodo_pago VARCHAR(30),  -- EFECTIVO, TARJETA, TRANSFERENCIA, MIXTO
    CONSTRAINT chk_cobro_metodo CHECK (metodo_pago IS NULL OR metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'MIXTO')),

    observacion VARCHAR(500),

    pagado_en TIMESTAMP,
    pagado_por BIGINT REFERENCES farmamed.usuario(id),

    created_by BIGINT REFERENCES farmamed.usuario(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES farmamed.usuario(id),
    updated_at TIMESTAMP
);

COMMENT ON TABLE farmamed.cobro IS 'Registro de cobro generado automáticamente al dispensar. Un cobro por dispensación.';
COMMENT ON COLUMN farmamed.cobro.subtotal IS 'Suma de (cantidad × precio_venta) de todos los items.';
COMMENT ON COLUMN farmamed.cobro.descuento_porcentaje IS 'Porcentaje de descuento según cobertura: ASEGURADO=100 (no cobra), SIS=50, PARTICULAR=0.';
COMMENT ON COLUMN farmamed.cobro.total IS 'subtotal - (subtotal × descuento_porcentaje / 100).';
COMMENT ON COLUMN farmamed.cobro.estado IS 'PENDIENTE (generado), PAGADO (cobrado), ANULADO (dispensación anulada).';

CREATE INDEX idx_cobro_dispensacion ON farmamed.cobro(dispensacion_id);
CREATE INDEX idx_cobro_estado ON farmamed.cobro(estado);
CREATE INDEX idx_cobro_fecha ON farmamed.cobro(created_at DESC);

-- -----------------------------------------------------
-- CONFIGURACIÓN DEL SISTEMA - Parámetros de cobro
-- -----------------------------------------------------
INSERT INTO farmamed.configuracion_sistema (clave, valor, descripcion, activo) VALUES
    ('cobro.precio.obligatorio.particular', 'true', 'Requiere precio vigente para dispensar recetas PARTICULAR', true),
    ('cobro.precio.obligatorio.sis', 'true', 'Requiere precio vigente para dispensar recetas SIS', true),
    ('cobro.descuento.sis.porcentaje', '50', 'Porcentaje de descuento para recetas SIS', true),
    ('cobro.descuento.asegurado.porcentaje', '100', 'Porcentaje de descuento para recetas ASEGURADO (100 = no cobra)', true)
ON CONFLICT (clave) DO NOTHING;

-- -----------------------------------------------------
-- MIGRACIÓN DE DATOS: Insertar precio base para medicamentos activos
-- Precio inicial = costo_unitario * 1.3 (30% margen por defecto)
-- -----------------------------------------------------
INSERT INTO farmamed.medicamento_precio (medicamento_id, precio_venta, vigente_desde, created_by)
SELECT
    m.id,
    ROUND((m.costo_unitario * 1.3)::numeric, 2),
    CURRENT_DATE,
    1  -- usuario sistema
FROM farmamed.medicamento m
WHERE m.activo = true
  AND m.costo_unitario > 0
ON CONFLICT (medicamento_id, vigente_desde) DO NOTHING;

-- -----------------------------------------------------
-- GENERAR COBROS HISTÓRICOS para dispensaciones PARTICULAR existentes
-- (Solo para datos de prueba; en producción real se haría con script separado)
-- -----------------------------------------------------
-- NOTA: Esto requiere que existan dispensaciones con receta PARTICULAR
-- Se ejecuta automáticamente si hay datos; si no, no hace nada.