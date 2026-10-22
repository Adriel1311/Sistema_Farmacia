-- =====================================================
-- FARMAMED
-- 003_views.sql
-- Vistas de negocio
-- =====================================================

SET search_path TO farmamed;

-- =====================================================
-- VW_FEFO
-- =====================================================
-- Muestra lotes ordenados según FEFO
-- (First Expired First Out)

CREATE OR REPLACE VIEW vw_lotes_fefo AS
SELECT
    l.id,
    l.medicamento_id,
    m.codigo,
    m.nombre,
    l.numero_lote,
    l.fecha_vencimiento,
    l.cantidad_actual,
    l.estado
FROM lote l
INNER JOIN medicamento m
    ON m.id = l.medicamento_id
WHERE l.estado = 'ACTIVO'
  AND l.cantidad_actual > 0
  AND l.fecha_vencimiento >= CURRENT_DATE
ORDER BY
    m.nombre,
    l.fecha_vencimiento;

-- =====================================================
-- VW_STOCK_ACTUAL
-- =====================================================

CREATE OR REPLACE VIEW vw_stock_actual AS
SELECT
    m.id,
    m.codigo,
    m.nombre,
    m.stock_minimo,
    m.stock_maximo,

    COALESCE(
        SUM(l.cantidad_actual),
        0
    ) AS stock_actual

FROM medicamento m
LEFT JOIN lote l
    ON l.medicamento_id = m.id
    AND l.estado = 'ACTIVO'
    AND l.fecha_vencimiento >= CURRENT_DATE
    AND l.cantidad_actual > 0
GROUP BY
    m.id,
    m.codigo,
    m.nombre,
    m.stock_minimo,
    m.stock_maximo;

-- =====================================================
-- VW_STOCK_CRITICO
-- =====================================================

CREATE OR REPLACE VIEW vw_stock_critico AS
SELECT
    *
FROM vw_stock_actual
WHERE stock_actual <= stock_minimo
ORDER BY stock_actual ASC;

-- =====================================================
-- VW_PROXIMOS_VENCER
-- =====================================================

CREATE OR REPLACE VIEW vw_proximos_vencer AS
SELECT
    l.id,
    m.codigo,
    m.nombre,
    l.numero_lote,

    l.fecha_vencimiento,

    l.cantidad_actual,

    (
        l.fecha_vencimiento - CURRENT_DATE
    ) AS dias_restantes

FROM lote l
INNER JOIN medicamento m
    ON m.id = l.medicamento_id

WHERE
    l.estado = 'ACTIVO'
    AND l.cantidad_actual > 0
    AND l.fecha_vencimiento >= CURRENT_DATE
    AND (
        l.fecha_vencimiento - CURRENT_DATE
    ) <= 60

ORDER BY
    dias_restantes ASC;

-- =====================================================
-- VW_LOTES_VENCIDOS
-- =====================================================

CREATE OR REPLACE VIEW vw_lotes_vencidos AS
SELECT
    l.id,
    m.codigo,
    m.nombre,
    l.numero_lote,
    l.fecha_vencimiento,
    l.cantidad_actual
FROM lote l
INNER JOIN medicamento m
    ON m.id = l.medicamento_id
WHERE
    l.fecha_vencimiento < CURRENT_DATE
    AND l.estado <> 'BAJA'
ORDER BY
    l.fecha_vencimiento DESC;

-- =====================================================
-- VW_PEDIDOS_SUGERIDOS
-- =====================================================

CREATE OR REPLACE VIEW vw_pedidos_sugeridos AS
SELECT
    m.id,
    m.codigo,
    m.nombre,

    COALESCE(
        SUM(l.cantidad_actual),
        0
    ) AS stock_actual,

    m.stock_minimo,
    m.stock_maximo,

    (
        m.stock_maximo -
        COALESCE(
            SUM(l.cantidad_actual),
            0
        )
    ) AS cantidad_sugerida,

    p.id AS proveedor_id,
    p.razon_social

FROM medicamento m

LEFT JOIN lote l
    ON l.medicamento_id = m.id
    AND l.estado = 'ACTIVO'
    AND l.fecha_vencimiento >= CURRENT_DATE
    AND l.cantidad_actual > 0

INNER JOIN proveedor p
    ON p.id = m.proveedor_id

GROUP BY
    m.id,
    p.id

HAVING
    COALESCE(
        SUM(l.cantidad_actual),
        0
    ) <= m.stock_minimo;

-- =====================================================
-- VW_CONSUMO_MENSUAL
-- =====================================================

CREATE OR REPLACE VIEW vw_consumo_mensual AS
SELECT
    DATE_TRUNC(
        'month',
        d.fecha_hora
    ) AS periodo,

    m.id AS medicamento_id,
    m.codigo,
    m.nombre,

    SUM(dd.cantidad) AS cantidad_dispensada

FROM detalle_dispensacion dd

INNER JOIN dispensacion d
    ON d.id = dd.dispensacion_id

INNER JOIN medicamento m
    ON m.id = dd.medicamento_id

WHERE
    d.estado = 'COMPLETADA'

GROUP BY
    DATE_TRUNC(
        'month',
        d.fecha_hora
    ),
    m.id,
    m.codigo,
    m.nombre

ORDER BY
    periodo DESC,
    cantidad_dispensada DESC;

-- =====================================================
-- VW_BAJAS_VENCIMIENTO
-- =====================================================

CREATE OR REPLACE VIEW vw_bajas_vencimiento AS
SELECT
    b.id,
    b.fecha_hora,

    m.codigo,
    m.nombre,

    l.numero_lote,

    b.cantidad,

    b.motivo,

    u.usuario

FROM baja_inventario b

INNER JOIN lote l
    ON l.id = b.lote_id

INNER JOIN medicamento m
    ON m.id = l.medicamento_id

INNER JOIN usuario u
    ON u.id = b.usuario_id

WHERE
    b.motivo = 'VENCIDO_EN_ESTANTE'

ORDER BY
    b.fecha_hora DESC;

-- =====================================================
-- VW_ORDENES_PENDIENTES
-- =====================================================

CREATE OR REPLACE VIEW vw_ordenes_pendientes AS
SELECT
    oc.id,
    oc.fecha_hora,

    p.ruc,
    p.razon_social,

    u.usuario,

    oc.estado

FROM orden_compra oc

INNER JOIN proveedor p
    ON p.id = oc.proveedor_id

INNER JOIN usuario u
    ON u.id = oc.usuario_id

WHERE
    oc.estado IN (
        'BORRADOR',
        'SUGERIDA'
    )

ORDER BY
    oc.fecha_hora DESC;

-- =====================================================
-- VW_KARDEX
-- =====================================================

CREATE OR REPLACE VIEW vw_kardex AS
SELECT
    mi.id,

    mi.fecha_hora,

    m.codigo,
    m.nombre,

    l.numero_lote,

    mi.tipo_movimiento,

    mi.cantidad,

    mi.stock_anterior,
    mi.stock_nuevo,

    mi.referencia_tipo,
    mi.referencia_id,

    u.usuario

FROM movimiento_inventario mi

INNER JOIN medicamento m
    ON m.id = mi.medicamento_id

INNER JOIN lote l
    ON l.id = mi.lote_id

INNER JOIN usuario u
    ON u.id = mi.usuario_id

ORDER BY
    mi.fecha_hora DESC;

-- =====================================================
-- VW_DASHBOARD
-- =====================================================

CREATE OR REPLACE VIEW vw_dashboard AS
SELECT

    (
        SELECT COUNT(*)
        FROM lote
        WHERE estado = 'ACTIVO'
    ) AS lotes_activos,

    (
        SELECT COUNT(*)
        FROM vw_lotes_vencidos
    ) AS lotes_vencidos,

    (
        SELECT COUNT(*)
        FROM vw_proximos_vencer
    ) AS lotes_por_vencer,

    (
        SELECT COUNT(*)
        FROM vw_stock_critico
    ) AS productos_stock_critico,

    (
        SELECT COUNT(*)
        FROM vw_ordenes_pendientes
    ) AS ordenes_pendientes,

    (
        SELECT COUNT(*)
        FROM dispensacion
        WHERE DATE(fecha_hora) = CURRENT_DATE
    ) AS dispensaciones_hoy;

-- =====================================================
-- FIN
-- =====================================================