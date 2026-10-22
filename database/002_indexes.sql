-- =====================================================
-- FARMAMED
-- 002_indexes.sql
-- Índices de optimización
-- PostgreSQL
-- =====================================================

SET search_path TO farmamed;

-- =====================================================
-- USUARIOS
-- =====================================================

CREATE INDEX idx_usuario_rol
ON usuario(rol_id);

CREATE INDEX idx_usuario_activo
ON usuario(activo);

-- =====================================================
-- AUDITORIA
-- =====================================================

CREATE INDEX idx_auditoria_usuario
ON auditoria(usuario_id);

CREATE INDEX idx_auditoria_fecha
ON auditoria(fecha_hora DESC);

CREATE INDEX idx_auditoria_entidad
ON auditoria(entidad);

CREATE INDEX idx_auditoria_accion
ON auditoria(accion);

-- =====================================================
-- PROVEEDORES
-- =====================================================

CREATE INDEX idx_proveedor_razon_social
ON proveedor(razon_social);

CREATE INDEX idx_proveedor_activo
ON proveedor(activo);

-- =====================================================
-- MEDICAMENTOS
-- =====================================================

CREATE INDEX idx_medicamento_nombre
ON medicamento(nombre);

CREATE INDEX idx_medicamento_proveedor
ON medicamento(proveedor_id);

CREATE INDEX idx_medicamento_activo
ON medicamento(activo);

-- =====================================================
-- LOTES
-- =====================================================

CREATE INDEX idx_lote_medicamento
ON lote(medicamento_id);

CREATE INDEX idx_lote_vencimiento
ON lote(fecha_vencimiento);

CREATE INDEX idx_lote_estado
ON lote(estado);

-- =====================================================
-- FEFO
-- =====================================================
-- Índice principal para el algoritmo FEFO

CREATE INDEX idx_lote_fefo
ON lote(
    medicamento_id,
    fecha_vencimiento,
    cantidad_actual
);

-- =====================================================
-- ALERTAS DE VENCIMIENTO
-- =====================================================

CREATE INDEX idx_lote_dashboard_vencimiento
ON lote(
    estado,
    fecha_vencimiento
);

-- =====================================================
-- PACIENTES
-- =====================================================

CREATE INDEX idx_paciente_apellidos
ON paciente(apellidos);

CREATE INDEX idx_paciente_nombres
ON paciente(nombres);

-- =====================================================
-- RECETAS
-- =====================================================

CREATE INDEX idx_receta_paciente
ON receta(paciente_id);

CREATE INDEX idx_receta_numero
ON receta(numero_receta);

CREATE INDEX idx_receta_fecha
ON receta(fecha_receta);

-- =====================================================
-- DISPENSACIONES
-- =====================================================

CREATE INDEX idx_dispensacion_receta
ON dispensacion(receta_id);

CREATE INDEX idx_dispensacion_usuario
ON dispensacion(usuario_id);

CREATE INDEX idx_dispensacion_fecha
ON dispensacion(fecha_hora DESC);

CREATE INDEX idx_dispensacion_estado
ON dispensacion(estado);

-- =====================================================
-- DETALLE DISPENSACION
-- =====================================================

CREATE INDEX idx_dd_dispensacion
ON detalle_dispensacion(dispensacion_id);

CREATE INDEX idx_dd_medicamento
ON detalle_dispensacion(medicamento_id);

CREATE INDEX idx_dd_lote
ON detalle_dispensacion(lote_id);

-- =====================================================
-- BAJAS
-- =====================================================

CREATE INDEX idx_baja_lote
ON baja_inventario(lote_id);

CREATE INDEX idx_baja_usuario
ON baja_inventario(usuario_id);

CREATE INDEX idx_baja_fecha
ON baja_inventario(fecha_hora DESC);

CREATE INDEX idx_baja_motivo
ON baja_inventario(motivo);

-- =====================================================
-- MOVIMIENTO INVENTARIO
-- =====================================================

CREATE INDEX idx_movimiento_medicamento
ON movimiento_inventario(medicamento_id);

CREATE INDEX idx_movimiento_lote
ON movimiento_inventario(lote_id);

CREATE INDEX idx_movimiento_usuario
ON movimiento_inventario(usuario_id);

CREATE INDEX idx_movimiento_tipo
ON movimiento_inventario(tipo_movimiento);

CREATE INDEX idx_movimiento_fecha
ON movimiento_inventario(fecha_hora DESC);

-- =====================================================
-- KARDEX
-- =====================================================

CREATE INDEX idx_movimiento_kardex
ON movimiento_inventario(
    medicamento_id,
    lote_id,
    fecha_hora DESC
);

-- =====================================================
-- ORDENES DE COMPRA
-- =====================================================

CREATE INDEX idx_orden_proveedor
ON orden_compra(proveedor_id);

CREATE INDEX idx_orden_usuario
ON orden_compra(usuario_id);

CREATE INDEX idx_orden_estado
ON orden_compra(estado);

CREATE INDEX idx_orden_fecha
ON orden_compra(fecha_hora DESC);

-- =====================================================
-- DETALLE ORDEN
-- =====================================================

CREATE INDEX idx_doc_orden
ON detalle_orden_compra(orden_compra_id);

CREATE INDEX idx_doc_medicamento
ON detalle_orden_compra(medicamento_id);

-- =====================================================
-- CONFIGURACION
-- =====================================================

CREATE INDEX idx_configuracion_activo
ON configuracion_sistema(activo);

-- =====================================================
-- DASHBOARD
-- =====================================================

CREATE INDEX idx_dashboard_stock
ON medicamento(
    stock_minimo,
    stock_maximo
);

CREATE INDEX idx_dashboard_lote
ON lote(
    fecha_vencimiento,
    estado
);

-- =====================================================
-- REPORTES
-- =====================================================

CREATE INDEX idx_reporte_dispensacion_fecha
ON dispensacion(fecha_hora);

CREATE INDEX idx_reporte_movimiento_fecha
ON movimiento_inventario(fecha_hora);

CREATE INDEX idx_reporte_baja_fecha
ON baja_inventario(fecha_hora);

-- =====================================================
-- FIN
-- =====================================================