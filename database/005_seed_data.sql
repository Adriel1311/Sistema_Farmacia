-- =====================================================
-- FARMAMED
-- 005_seed_data.sql  -  Datos de negocio de demostracion
-- PostgreSQL 18
-- -----------------------------------------------------
-- Este script NO crea roles ni usuarios. Eso lo hace
-- DataInitializer al arrancar el backend, con el hash
-- BCrypt real. Aqui solo van proveedores, medicamentos,
-- pacientes, recetas, lotes, movimientos de kardex, una
-- dispensacion de ejemplo, una baja por merma, ordenes de
-- compra y los parametros del sistema.
--
-- ORDEN DE EJECUCION:
--   1. 001_schema.sql
--   2. 002_indexes.sql
--   3. 003_views.sql
--   4. 004_triggers.sql
--   5. 006_seguridad_bd.sql
--   6. Arrancar el backend (crea roles y usuarios)
--   7. Este script
--
-- Los datos son idempotentes: se vuelven a aplicar sin
-- duplicar nada.
-- =====================================================

SET search_path TO farmamed, public;

-- =====================================================
-- 1. PARAMETROS DEL SISTEMA
-- Las claves son las que lee ConfiguracionService.
-- Los valores 30 y 60 son los umbrales validados.
-- =====================================================

INSERT INTO farmamed.configuracion_sistema (clave, valor, descripcion, activo, created_by, created_at)
VALUES
    ('vencimiento.dias.critico', '30',
     'Dias de anticipacion con los que un lote pasa a estado critico (semaforo rojo).', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP),

    ('vencimiento.dias.proximo', '60',
     'Dias de anticipacion con los que un lote pasa a estado proximo a vencer (semaforo ambar).', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP),

    ('sesion.timeout.minutos', '30',
     'Minutos de inactividad antes de que la sesion del usuario se cierre.', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP),

    ('alertas.hora.evaluacion', '07:00',
     'Hora del dia en que el sistema evalua y muestra las alertas de vencimiento y stock.', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP),

    ('orden.sugerida.automatica', 'true',
     'Si es true, el sistema genera ordenes de compra sugeridas cuando un medicamento queda en stock critico.', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP),

    ('fefo.permitir.excepcion', 'true',
     'Si es true, el Jefe de Farmacia puede elegir un lote distinto al FEFO dejando el motivo.', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'), CURRENT_TIMESTAMP)
ON CONFLICT (clave) DO UPDATE
SET valor = EXCLUDED.valor,
    descripcion = EXCLUDED.descripcion,
    updated_by = (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'),
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- 2. PROVEEDORES
-- =====================================================

INSERT INTO farmamed.proveedor (ruc, razon_social, telefono, correo, contacto, activo, created_by, created_at)
VALUES
    ('20123456789', 'Distribuidora Farmaceutica del Norte S.A.C.', '954-112233', 'compras@dfn.pe', 'Juan Vargas', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ('20445566778', 'Laboratorios Medicos del Sur S.A.C.', '952-334455', 'ventas@lmsur.pe', 'Rosa Quispe', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ('20778899001', 'Insumos Medicos Ica E.I.R.L.', '953-556677', 'contacto@insumosica.pe', 'Carlos Arroyo', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ('20990011222', 'Farmaceutica Andina S.A.C.', '951-778899', 'pedidos@andina.pe', 'Lucia Ramos', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP)
ON CONFLICT (ruc) DO NOTHING;

-- =====================================================
-- 3. MEDICAMENTOS
-- costo_unitario es el costo de adquisicion: sirve para
-- valuing el inventario y medir la merma. El sistema no
-- cobra, no hay precio de venta.
-- =====================================================

INSERT INTO farmamed.medicamento
    (proveedor_id, codigo, nombre, descripcion, unidad_medida, stock_minimo, stock_maximo, costo_unitario, activo, created_by, created_at)
VALUES
    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20123456789'), 'MED-001',
     'Paracetamol 500 mg', 'Tableta para dolor y fiebre', 'UNIDAD', 20, 200, 3.50, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20445566778'), 'MED-002',
     'Amoxicilina 500 mg', 'Capsula antibiotico', 'UNIDAD', 15, 150, 12.80, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20123456789'), 'MED-003',
     'Ibuprofeno 400 mg', 'Tableta antiinflamatoria', 'UNIDAD', 20, 200, 5.20, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20445566778'), 'MED-004',
     'Omeprazol 20 mg', 'Capsula antiulcerosa', 'UNIDAD', 15, 120, 9.90, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20778899001'), 'MED-005',
     'Losartan 50 mg', 'Tableta antihipertensiva', 'UNIDAD', 10, 100, 8.40, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20445566778'), 'MED-006',
     'Metformina 850 mg', 'Tableta para diabetes tipo 2', 'UNIDAD', 12, 120, 7.60, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20123456789'), 'MED-007',
     'Salbutamol inhalador 100 mcg', 'Inhalador broncodilatador', 'UNIDAD', 8, 80, 18.00, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20778899001'), 'MED-008',
     'Clotrimazol crema 1%', 'Crema antifungal topica', 'UNIDAD', 10, 90, 11.30, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20445566778'), 'MED-009',
     'Azitromicina 500 mg', 'Tableta antibiotico', 'UNIDAD', 10, 100, 15.75, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20990011222'), 'MED-010',
     'Ondansetrona 8 mg/4 ml', 'Ampolleta antiemetico', 'AMPOLLA', 6, 60, 22.50, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20123456789'), 'MED-011',
     'Loratadina 10 mg', 'Tableta antihistaminica', 'UNIDAD', 15, 150, 6.40, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.proveedor WHERE ruc = '20778899001'), 'MED-012',
     'Sulfato ferroso 200 mg', 'Tableta para anemia', 'UNIDAD', 10, 100, 9.10, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP)
ON CONFLICT (codigo) DO NOTHING;

-- =====================================================
-- 4. PACIENTES
-- codigo_externo queda informado para las fichas que ya
-- existen en el HIS. Es nullable porque la integracion con
-- el HIS es de la Fase 2.
-- =====================================================

INSERT INTO farmamed.paciente (dni, nombres, apellidos, codigo_externo, activo, created_by, created_at)
VALUES
    ('45678912', 'Maria Fernanda', 'Quispe Mamani', 'HIS-0001', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),
    ('40125678', 'Juan Carlos', 'Huaman Vega', NULL, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),
    ('70981234', 'Rosa Elena', 'Paredes Tito', NULL, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),
    ('10293847', 'Pedro Pablo', 'Chambers Ruiz', NULL, true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),
    ('87654321', 'Luis Alberto', 'Neyra Campos', 'HIS-0007', true,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP)
ON CONFLICT (dni) DO NOTHING;

-- =====================================================
-- 5. RECETAS
--tipo ASEGURADO y PARTICULAR, segun la decision de que el
-- ticket interno sirve para las dos.
-- =====================================================

INSERT INTO farmamed.receta (paciente_id, numero_receta, medico, tipo_receta, fecha_receta, codigo_externo, created_by, created_at)
VALUES
    ((SELECT id FROM farmamed.paciente WHERE dni = '45678912'), 'REC-2026-0001', 'Dra. Elena Vargas Rios',
     'ASEGURADO', CURRENT_DATE, 'HIS-R-0001',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.paciente WHERE dni = '40125678'), 'REC-2026-0002', 'Dr. Miguel Torres Saenz',
     'PARTICULAR', CURRENT_DATE, NULL,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.paciente WHERE dni = '70981234'), 'REC-2026-0003', 'Dra. Elena Vargas Rios',
     'ASEGURADO', CURRENT_DATE - 1, NULL,
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.paciente WHERE dni = '87654321'), 'REC-2026-0004', 'Dr. Andres Cueva Medina',
     'PARTICULAR', CURRENT_DATE - 2, 'HIS-R-0009',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'), CURRENT_TIMESTAMP)
ON CONFLICT (numero_receta) DO NOTHING;

-- =====================================================
-- 6. LOTES
-- Las fechas se calculan desde la fecha en que se aplica
-- el script, para que el semaforo siga teniendo sentido:
--   VERDE  mas de 60 dias
--   AMBAR  entre 31 y 60 dias
--   ROJO   menos de 30 dias
--   VENCIDO ya paso la fecha
-- cantidad_actual ya viene descontada: refleja el estado
-- real del inventario despues de la dispensacion y la merma
-- de ejemplo.
-- =====================================================

INSERT INTO farmamed.lote
    (medicamento_id, numero_lote, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad_actual, estado, created_by, created_at)
VALUES
    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-001'), 'L-2026-0001',
     CURRENT_DATE - 200, CURRENT_DATE + 400, 100, 100, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-001'), 'L-2026-0002',
     CURRENT_DATE - 120, CURRENT_DATE + 45, 60, 60, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-001'), 'L-2026-0003',
     CURRENT_DATE - 40, CURRENT_DATE + 12, 40, 30, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-002'), 'L-2026-0004',
     CURRENT_DATE - 300, CURRENT_DATE - 5, 30, 0, 'VENCIDO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-002'), 'L-2026-0005',
     CURRENT_DATE - 90, CURRENT_DATE + 25, 50, 45, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-002'), 'L-2026-0006',
     CURRENT_DATE - 150, CURRENT_DATE + 70, 30, 30, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-003'), 'L-2026-0007',
     CURRENT_DATE - 170, CURRENT_DATE + 200, 80, 80, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-004'), 'L-2026-0008',
     CURRENT_DATE - 100, CURRENT_DATE + 25, 25, 25, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-005'), 'L-2026-0009',
     CURRENT_DATE - 140, CURRENT_DATE + 50, 45, 45, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-006'), 'L-2026-0010',
     CURRENT_DATE - 180, CURRENT_DATE + 300, 60, 60, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-007'), 'L-2026-0011',
     CURRENT_DATE - 60, CURRENT_DATE + 15, 12, 12, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP),

    ((SELECT id FROM farmamed.medicamento WHERE codigo = 'MED-008'), 'L-2026-0012',
     CURRENT_DATE - 110, CURRENT_DATE + 120, 35, 32, 'ACTIVO',
     (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'), CURRENT_TIMESTAMP)
ON CONFLICT (medicamento_id, numero_lote) DO NOTHING;

-- =====================================================
-- 7. KARDEX: ENTRADAS
-- Una entrada por lote, con la misma convencion que usa la
-- aplicacion (referencia_tipo INGRESO_LOTE).
-- =====================================================

INSERT INTO farmamed.movimiento_inventario
    (medicamento_id, lote_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, referencia_tipo, referencia_id, fecha_hora)
SELECT
    l.medicamento_id,
    l.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    'ENTRADA',
    l.cantidad_inicial,
    0,
    l.cantidad_inicial,
    'INGRESO_LOTE',
    l.id,
    CURRENT_TIMESTAMP
FROM farmamed.lote l
WHERE NOT EXISTS (
    SELECT 1 FROM farmamed.movimiento_inventario m
    WHERE m.tipo_movimiento = 'ENTRADA'
      AND m.lote_id = l.id
      AND m.referencia_tipo = 'INGRESO_LOTE'
);

-- =====================================================
-- 8. DISPENSACION DE EJEMPLO
-- Una receta atendida con dos medicamentos, cada uno
-- tomado del lote que le corresponde por FEFO.
-- numero_ticket lo genera la base de datos con la
-- secuencia, igual que en la aplicacion.
-- =====================================================

INSERT INTO farmamed.dispensacion (receta_id, usuario_id, estado, fecha_hora, created_by, created_at)
SELECT
    r.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'),
    'COMPLETADA',
    CURRENT_TIMESTAMP,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'),
    CURRENT_TIMESTAMP
FROM farmamed.receta r
WHERE r.numero_receta = 'REC-2026-0001'
  AND NOT EXISTS (
      SELECT 1 FROM farmamed.dispensacion d WHERE d.receta_id = r.id
  );

INSERT INTO farmamed.detalle_dispensacion (dispensacion_id, medicamento_id, lote_id, cantidad, motivo_excepcion_fefo)
SELECT
    d.id,
    l.medicamento_id,
    l.id,
    v.cantidad,
    NULL
FROM farmamed.dispensacion d
JOIN farmamed.receta r ON r.id = d.receta_id
CROSS JOIN (VALUES
    ('L-2026-0003', 10),
    ('L-2026-0005', 5)
) AS v(numero_lote, cantidad)
JOIN farmamed.lote l ON l.numero_lote = v.numero_lote
WHERE r.numero_receta = 'REC-2026-0001'
  AND NOT EXISTS (
      SELECT 1 FROM farmamed.detalle_dispensacion dd
      WHERE dd.dispensacion_id = d.id
        AND dd.lote_id = l.id
        AND dd.cantidad = v.cantidad
  );

-- =====================================================
-- 9. KARDEX: SALIDAS POR DISPENSACION
-- =====================================================

INSERT INTO farmamed.movimiento_inventario
    (medicamento_id, lote_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, referencia_tipo, referencia_id, fecha_hora)
SELECT
    dd.medicamento_id,
    dd.lote_id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'tecnico'),
    'SALIDA',
    dd.cantidad,
    dd.cantidad + l.cantidad_actual,
    l.cantidad_actual,
    'DISPENSACION',
    d.id,
    d.fecha_hora
FROM farmamed.detalle_dispensacion dd
JOIN farmamed.dispensacion d ON d.id = dd.dispensacion_id
JOIN farmamed.lote l ON l.id = dd.lote_id
WHERE NOT EXISTS (
    SELECT 1 FROM farmamed.movimiento_inventario m
    WHERE m.tipo_movimiento = 'SALIDA'
      AND m.lote_id = dd.lote_id
      AND m.referencia_tipo = 'DISPENSACION'
      AND m.referencia_id = d.id
      AND m.cantidad = dd.cantidad
);

-- =====================================================
-- 10. BAJA POR MERMA
-- Tres unidades de clotrimazol con unidad de empaque
-- danada. Se descuentan del lote y se registra la perdida
-- valorizada.
-- =====================================================

INSERT INTO farmamed.baja_inventario (lote_id, usuario_id, motivo, cantidad, observacion, valor_perdida, fecha_hora)
SELECT
    l.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    'MERMA',
    3,
    'Empaque danado por humedad en el almacen',
    3 * m.costo_unitario,
    CURRENT_TIMESTAMP
FROM farmamed.lote l
JOIN farmamed.medicamento m ON m.id = l.medicamento_id
WHERE l.numero_lote = 'L-2026-0012'
  AND NOT EXISTS (
      SELECT 1 FROM farmamed.baja_inventario b
      WHERE b.lote_id = l.id
        AND b.motivo = 'MERMA'
        AND b.cantidad = 3
  );

-- =====================================================
-- 11. KARDEX: BAJA
-- =====================================================

INSERT INTO farmamed.movimiento_inventario
    (medicamento_id, lote_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, referencia_tipo, referencia_id, fecha_hora)
SELECT
    l.medicamento_id,
    l.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    'BAJA',
    b.cantidad,
    l.cantidad_actual + b.cantidad,
    l.cantidad_actual,
    'BAJA_INVENTARIO',
    b.id,
    b.fecha_hora
FROM farmamed.baja_inventario b
JOIN farmamed.lote l ON l.id = b.lote_id
WHERE NOT EXISTS (
    SELECT 1 FROM farmamed.movimiento_inventario m
    WHERE m.tipo_movimiento = 'BAJA'
      AND m.referencia_tipo = 'BAJA_INVENTARIO'
      AND m.referencia_id = b.id
);

-- =====================================================
-- 12. ORDENES DE COMPRA
-- Una sugerida (para que el Jefe de Farmacia la apruebe)
-- y una en borrador.
-- =====================================================

INSERT INTO farmamed.orden_compra (proveedor_id, usuario_id, estado, observacion, fecha_hora, created_by, created_at)
SELECT
    p.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    'SUGERIDA',
    'Reposicion sugerida por stock critico de ibuprofeno y loratadina',
    CURRENT_TIMESTAMP,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    CURRENT_TIMESTAMP
FROM farmamed.proveedor p
WHERE p.ruc = '20123456789'
  AND NOT EXISTS (
      SELECT 1 FROM farmamed.orden_compra o
      WHERE o.proveedor_id = p.id
        AND o.estado = 'SUGERIDA'
        AND o.observacion = 'Reposicion sugerida por stock critico de ibuprofeno y loratadina'
  );

INSERT INTO farmamed.orden_compra (proveedor_id, usuario_id, estado, observacion, fecha_hora, created_by, created_at)
SELECT
    p.id,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    'BORRADOR',
    'Pedido pendiente de completar',
    CURRENT_TIMESTAMP,
    (SELECT id FROM farmamed.usuario WHERE usuario = 'logistica'),
    CURRENT_TIMESTAMP
FROM farmamed.proveedor p
WHERE p.ruc = '20445566778'
  AND NOT EXISTS (
      SELECT 1 FROM farmamed.orden_compra o
      WHERE o.proveedor_id = p.id
        AND o.estado = 'BORRADOR'
        AND o.observacion = 'Pedido pendiente de completar'
  );

INSERT INTO farmamed.detalle_orden_compra (orden_compra_id, medicamento_id, cantidad, sugerido)
SELECT
    o.id,
    m.id,
    v.cantidad,
    v.sugerido
FROM farmamed.orden_compra o
CROSS JOIN (VALUES
    ('20123456789', 'MED-003', 100, true),
    ('20123456789', 'MED-011', 60, true),
    ('20445566778', 'MED-009', 40, false)
) AS v(ruc, codigo, cantidad, sugerido)
JOIN farmamed.proveedor p ON p.ruc = v.ruc AND p.id = o.proveedor_id
JOIN farmamed.medicamento m ON m.codigo = v.codigo
WHERE NOT EXISTS (
    SELECT 1 FROM farmamed.detalle_orden_compra doc
    WHERE doc.orden_compra_id = o.id
      AND doc.medicamento_id = m.id
);

-- =====================================================
-- 13. AUDITORIA DE ARRANQUE
-- =====================================================

INSERT INTO farmamed.auditoria (usuario_id, entidad, accion, valor_anterior, valor_nuevo, descripcion, fecha_hora)
SELECT
    (SELECT id FROM farmamed.usuario WHERE usuario = 'admin'),
    'Sistema',
    'CONFIGURACION_CARGADA',
    NULL,
    '6 parametros',
    'Parametros del sistema cargados con los valores validados de 30 y 60 dias',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM farmamed.auditoria a
    WHERE a.entidad = 'Sistema'
      AND a.accion = 'CONFIGURACION_CARGADA'
);

-- =====================================================
-- FIN DEL SEED
-- =====================================================
