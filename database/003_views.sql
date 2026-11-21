-- =====================================================
-- FARMAMED
-- 003_views.sql  -  Vistas de consulta y reporte
-- PostgreSQL 18
-- -----------------------------------------------------
-- Las vistas concentran la logica de negocio que de otro
-- modo se repetiria en cada consulta:
--
--   * El semaforo de vencimiento (RN04) se calcula con los
--     umbrales guardados en configuracion_sistema, no con
--     numeros fijos. Cambiar el parametro en M11 cambia el
--     semaforo de todo el sistema.
--   * El stock vigente (RN06) solo cuenta lotes no vencidos,
--     no dados de baja y con existencias.
--
-- Es idempotente: CREATE OR REPLACE.
-- =====================================================

SET search_path TO farmamed, public;

-- =====================================================
-- FUNCION DE LECTURA DE PARAMETROS
-- Lee un parametro numerico de configuracion_sistema.
-- Si no existe, no esta activo o no es numerico, devuelve
-- el valor por defecto. Nunca lanza error: una tabla de
-- configuracion mal llenada no debe tumbar el sistema.
-- =====================================================

CREATE OR REPLACE FUNCTION farmamed.fn_umbral(p_clave TEXT, p_defecto INTEGER)
RETURNS INTEGER
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_valor TEXT;
BEGIN
    SELECT valor INTO v_valor
    FROM farmamed.configuracion_sistema
    WHERE clave = p_clave AND activo
    LIMIT 1;

    IF v_valor IS NULL THEN
        RETURN p_defecto;
    END IF;

    BEGIN
        RETURN v_valor::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        RETURN p_defecto;
    END;
END;
$$;

COMMENT ON FUNCTION farmamed.fn_umbral(TEXT, INTEGER) IS
'Lee un parametro numerico de configuracion_sistema con valor por defecto seguro.';

-- =====================================================
-- VISTA 1: SEMAFORO DE VENCIMIENTO (RN04)
-- Una fila por lote con su color: VERDE, AMBAR, ROJO o NEGRO.
-- El color depende de los umbrales vigentes.
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_semaforo_lote AS
SELECT
    l.id AS lote_id,
    l.numero_lote,
    m.id AS medicamento_id,
    m.codigo AS medicamento_codigo,
    m.nombre AS medicamento,
    m.unidad_medida,
    l.cantidad_inicial,
    l.cantidad_actual,
    l.fecha_ingreso,
    l.fecha_vencimiento,
    (l.fecha_vencimiento - CURRENT_DATE) AS dias_restantes,
    l.estado,
    CASE
        WHEN l.estado = 'BAJA' THEN 'NEGRO'
        WHEN l.fecha_vencimiento < CURRENT_DATE THEN 'NEGRO'
        WHEN (l.fecha_vencimiento - CURRENT_DATE)
             <= farmamed.fn_umbral('vencimiento.dias.critico', 30) THEN 'ROJO'
        WHEN (l.fecha_vencimiento - CURRENT_DATE)
             <= farmamed.fn_umbral('vencimiento.dias.proximo', 60) THEN 'AMBAR'
        ELSE 'VERDE'
    END AS estado_semaforo,
    farmamed.fn_umbral('vencimiento.dias.critico', 30) AS dias_critico,
    farmamed.fn_umbral('vencimiento.dias.proximo', 60) AS dias_proximo
FROM farmamed.lote l
JOIN farmamed.medicamento m ON m.id = l.medicamento_id;

COMMENT ON VIEW farmamed.vw_semaforo_lote IS
'Semaforo de vencimiento por lote (RN04). Los umbrales salen de configuracion_sistema.';

-- =====================================================
-- VISTA 2: LOTES POR VENCER
-- Lotes vigentes que vencen dentro de la ventana proxima.
-- Es la base de las alertas VENCIMIENTO_CRITICO y
-- VENCIMIENTO_PROXIMO (RN05.1).
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_lotes_por_vencer AS
SELECT
    s.lote_id,
    s.numero_lote,
    s.medicamento_id,
    s.medicamento_codigo,
    s.medicamento,
    s.unidad_medida,
    s.cantidad_actual,
    s.fecha_vencimiento,
    s.dias_restantes,
    s.estado_semaforo,
    s.dias_critico,
    s.dias_proximo
FROM farmamed.vw_semaforo_lote s
WHERE s.estado = 'ACTIVO'
  AND s.cantidad_actual > 0
  AND s.fecha_vencimiento >= CURRENT_DATE
  AND s.fecha_vencimiento
      <= CURRENT_DATE + farmamed.fn_umbral('vencimiento.dias.proximo', 60);

COMMENT ON VIEW farmamed.vw_lotes_por_vencer IS
'Lotes vigentes que vencen dentro de la ventana de alerta (RN05.1).';

-- =====================================================
-- VISTA 3: STOCK CRITICO (RN06)
-- Los medicamentos que igualaron o quedaron por debajo de
-- su stock minimo y por lo tanto disparan alerta.
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_stock_critico AS
SELECT
    m.id AS medicamento_id,
    m.codigo,
    m.nombre,
    m.unidad_medida,
    m.stock_minimo,
    m.stock_maximo,
    COALESCE(st.total, 0) AS stock_vigente,
    m.stock_minimo - COALESCE(st.total, 0) AS faltante,
    m.stock_maximo - COALESCE(st.total, 0) AS cantidad_sugerida,
    p.razon_social AS proveedor
FROM farmamed.medicamento m
JOIN farmamed.proveedor p ON p.id = m.proveedor_id
LEFT JOIN (
    SELECT l.medicamento_id, SUM(l.cantidad_actual) AS total
    FROM farmamed.lote l
    WHERE l.estado = 'ACTIVO'
      AND l.cantidad_actual > 0
      AND l.fecha_vencimiento >= CURRENT_DATE
    GROUP BY l.medicamento_id
) st ON st.medicamento_id = m.id
WHERE m.activo
  AND COALESCE(st.total, 0) <= m.stock_minimo;

COMMENT ON VIEW farmamed.vw_stock_critico IS
'Medicamentos con stock vigente igual o menor al minimo (RN06). Alimenta la alerta STOCK_CRITICO y las sugerencias de compra (RN07).';

-- =====================================================
-- VISTA 4: INVENTARIO VALORIZADO
-- Stock actual por medicamento con su valorizacion a costo
-- de adquisicion. No es un precio de venta: el sistema no
-- cobra. Sirve para medir la merma y el ahorro por FEFO.
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_inventario_valorizado AS
SELECT
    m.id AS medicamento_id,
    m.codigo,
    m.nombre,
    m.unidad_medida,
    m.costo_unitario,
    COUNT(l.id) AS lotes_activos,
    COALESCE(SUM(l.cantidad_actual), 0) AS unidades,
    COALESCE(SUM(l.cantidad_actual * m.costo_unitario), 0) AS valor_inventario
FROM farmamed.medicamento m
LEFT JOIN farmamed.lote l
       ON l.medicamento_id = m.id
      AND l.estado = 'ACTIVO'
      AND l.cantidad_actual > 0
      AND l.fecha_vencimiento >= CURRENT_DATE
WHERE m.activo
GROUP BY m.id, m.codigo, m.nombre, m.unidad_medida, m.costo_unitario;

COMMENT ON VIEW farmamed.vw_inventario_valorizado IS
'Inventario vigente valorado a costo de adquisicion. Base del reporte de inventario (M08).';

-- =====================================================
-- VISTA 5: REPORTE DE CONSUMO
-- Unidades dispensadas por medicamento y por mes.
-- Solo cuenta dispensaciones completadas (RN05.0).
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_reporte_consumo AS
SELECT
    date_trunc('month', d.fecha_hora)::date AS periodo,
    m.id AS medicamento_id,
    m.codigo,
    m.nombre,
    m.unidad_medida,
    COUNT(DISTINCT d.id) AS dispensaciones,
    SUM(dd.cantidad) AS unidades_consumidas,
    SUM(dd.cantidad * m.costo_unitario) AS valor_consumido
FROM farmamed.detalle_dispensacion dd
JOIN farmamed.dispensacion d ON d.id = dd.dispensacion_id
JOIN farmamed.medicamento m ON m.id = dd.medicamento_id
WHERE d.estado = 'COMPLETADA'
GROUP BY 1, m.id, m.codigo, m.nombre, m.unidad_medida;

COMMENT ON VIEW farmamed.vw_reporte_consumo IS
'Consumo de medicamentos por periodo. Solo dispensaciones completadas (M08).';

-- =====================================================
-- VISTA 6: REPORTE DE BAJAS (MERMA)
-- Perdidas por motivo y por mes, con su valorizacion.
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_reporte_bajas AS
SELECT
    date_trunc('month', b.fecha_hora)::date AS periodo,
    b.motivo,
    COUNT(*) AS numero_bajas,
    SUM(b.cantidad) AS unidades,
    SUM(b.valor_perdida) AS valor_perdida
FROM farmamed.baja_inventario b
GROUP BY 1, b.motivo;

COMMENT ON VIEW farmamed.vw_reporte_bajas IS
'Merma por motivo y periodo, valorizada (RN06b). Base del reporte de bajas (M08).';

-- =====================================================
-- VISTA 7: REPORTE DE ORDENES DE COMPRA
-- Detalle de cada orden con su valor estimado.
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_reporte_ordenes AS
SELECT
    o.id AS orden_id,
    o.estado,
    o.fecha_hora,
    o.fecha_aprobacion,
    p.razon_social AS proveedor,
    u.usuario AS solicitante,
    m.codigo AS medicamento_codigo,
    m.nombre AS medicamento,
    m.unidad_medida,
    doc.cantidad,
    doc.sugerido,
    m.costo_unitario,
    doc.cantidad * m.costo_unitario AS valor_estimado
FROM farmamed.orden_compra o
JOIN farmamed.proveedor p ON p.id = o.proveedor_id
JOIN farmamed.usuario u ON u.id = o.usuario_id
JOIN farmamed.detalle_orden_compra doc ON doc.orden_compra_id = o.id
JOIN farmamed.medicamento m ON m.id = doc.medicamento_id;

COMMENT ON VIEW farmamed.vw_reporte_ordenes IS
'Ordenes de compra con su detalle y valor estimado (M08).';

-- =====================================================
-- VISTA 8: INDICADORES DEL PANEL (M01)
-- Cifras del dashboard. Se corrigieron los tres desvios que
-- tenia la version anterior del script:
--   * las dispensaciones contaban tambien las anuladas
--   * el stock se calculaba sobre lotes ya vencidos
--   * no se filtraba por medicamento activo
-- =====================================================

CREATE OR REPLACE VIEW farmamed.vw_dashboard AS
SELECT
    (SELECT COUNT(*)
       FROM farmamed.dispensacion
      WHERE estado = 'COMPLETADA'
        AND fecha_hora >= date_trunc('month', CURRENT_DATE))
        AS dispensaciones_mes,

    (SELECT COUNT(*)
       FROM farmamed.dispensacion
      WHERE estado = 'COMPLETADA'
        AND fecha_hora >= CURRENT_DATE)
        AS dispensaciones_hoy,

    (SELECT COUNT(*)
       FROM farmamed.lote
      WHERE estado = 'ACTIVO'
        AND cantidad_actual > 0
        AND fecha_vencimiento < CURRENT_DATE)
        AS lotes_vencidos_con_stock,

    (SELECT COUNT(*)
       FROM farmamed.lote
      WHERE estado = 'ACTIVO'
        AND cantidad_actual > 0
        AND fecha_vencimiento >= CURRENT_DATE
        AND fecha_vencimiento
            <= CURRENT_DATE + farmamed.fn_umbral('vencimiento.dias.critico', 30))
        AS lotes_por_vencer,

    (SELECT COUNT(*)
       FROM farmamed.vw_stock_critico)
        AS medicamentos_stock_critico,

    (SELECT COUNT(*)
       FROM farmamed.orden_compra
      WHERE estado = 'SUGERIDA')
        AS ordenes_sugeridas,

    (SELECT COUNT(*)
       FROM farmamed.orden_compra
      WHERE estado = 'BORRADOR')
        AS ordenes_borrador,

    (SELECT COUNT(*)
       FROM farmamed.orden_compra
      WHERE estado = 'APROBADA')
        AS ordenes_aprobadas,

    (SELECT COALESCE(SUM(b.valor_perdida), 0)
       FROM farmamed.baja_inventario b
      WHERE b.fecha_hora >= date_trunc('month', CURRENT_DATE))
        AS merma_valor_mes,

    (SELECT COALESCE(SUM(l.cantidad_actual * m.costo_unitario), 0)
       FROM farmamed.lote l
       JOIN farmamed.medicamento m ON m.id = l.medicamento_id
      WHERE l.estado = 'ACTIVO'
        AND l.cantidad_actual > 0
        AND l.fecha_vencimiento >= CURRENT_DATE
        AND m.activo)
        AS valor_inventario_vigente,

    farmamed.fn_umbral('vencimiento.dias.critico', 30) AS dias_critico,
    farmamed.fn_umbral('vencimiento.dias.proximo', 60) AS dias_proximo;

COMMENT ON VIEW farmamed.vw_dashboard IS
'Indicadores del panel de control (M01). Los umbrales del semaforo se leen de configuracion_sistema.';

-- =====================================================
-- FIN DE LAS VISTAS
-- =====================================================
