-- =====================================================
-- FARMAMED
-- 005_seed_data.sql
-- Datos Iniciales
-- =====================================================

SET search_path TO farmamed;

-- =====================================================
-- ROLES
-- =====================================================

INSERT INTO rol
(
    nombre,
    descripcion
)
VALUES
(
    'JEFE_FARMACIA',
    'Responsable general del area de farmacia'
),
(
    'TECNICO_FARMACIA',
    'Responsable de inventario y dispensacion'
),
(
    'LOGISTICA',
    'Responsable de compras y proveedores'
),
(
    'ADMIN_TI',
    'Administrador tecnico del sistema'
);

-- =====================================================
-- USUARIO ADMINISTRADOR INICIAL
-- =====================================================
-- Password sugerida:
-- Admin123*
--
-- Reemplazar por hash BCrypt generado
-- desde Spring Security
-- =====================================================

INSERT INTO usuario
(
    rol_id,
    usuario,
    nombres,
    apellidos,
    correo,
    password_hash,
    activo,
    created_by
)
VALUES
(
    (
        SELECT id
        FROM rol
        WHERE nombre = 'ADMIN_TI'
    ),
    'admin',
    'Administrador',
    'Sistema',
    'admin@farmamed.local',
    '$2a$10$REEMPLAZAR_CON_BCRYPT_REAL',
    TRUE,
    1
);

-- =====================================================
-- CONFIGURACIÓN DEL SISTEMA
-- =====================================================

INSERT INTO configuracion_sistema
(
    clave,
    valor,
    descripcion,
    activo
)
VALUES
(
    'DIAS_CRITICO',
    '30',
    'Dias para considerar vencimiento critico',
    TRUE
),
(
    'DIAS_PROXIMO_VENCER',
    '60',
    'Dias para considerar proximo vencimiento',
    TRUE
),
(
    'TIMEOUT_SESION',
    '30',
    'Tiempo de expiracion de sesion en minutos',
    TRUE
),
(
    'APLICAR_FEFO',
    'TRUE',
    'Habilita algoritmo FEFO',
    TRUE
),
(
    'AUDITORIA_ACTIVA',
    'TRUE',
    'Habilita auditoria del sistema',
    TRUE
);

-- =====================================================
-- PROVEEDOR DE DEMOSTRACIÓN
-- =====================================================

INSERT INTO proveedor
(
    ruc,
    razon_social,
    telefono,
    correo,
    activo,
    created_by
)
VALUES
(
    '20100070970',
    'DROGUERIA FARMAMED SAC',
    '056123456',
    'ventas@farmamed.pe',
    TRUE,
    1
);

-- =====================================================
-- MEDICAMENTO DE DEMOSTRACIÓN
-- =====================================================

INSERT INTO medicamento
(
    proveedor_id,
    codigo,
    nombre,
    descripcion,
    unidad_medida,
    stock_minimo,
    stock_maximo,
    activo,
    created_by
)
VALUES
(
    (
        SELECT id
        FROM proveedor
        WHERE ruc = '20100070970'
    ),
    'MED0001',
    'PARACETAMOL 500 MG',
    'Analgesico y antipiretico',
    'TABLETA',
    100,
    1000,
    TRUE,
    1
);

-- =====================================================
-- LOTE DEMO
-- =====================================================

INSERT INTO lote
(
    medicamento_id,
    numero_lote,
    fecha_ingreso,
    fecha_vencimiento,
    cantidad_inicial,
    cantidad_actual,
    estado,
    created_by
)
VALUES
(
    (
        SELECT id
        FROM medicamento
        WHERE codigo = 'MED0001'
    ),
    'LOT-2026-001',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '365 days',
    500,
    500,
    'ACTIVO',
    1
);

-- =====================================================
-- PACIENTE DEMO
-- =====================================================

INSERT INTO paciente
(
    dni,
    nombres,
    apellidos,
    activo,
    created_by
)
VALUES
(
    '12345678',
    'JUAN',
    'PEREZ',
    TRUE,
    1
);

-- =====================================================
-- RECETA DEMO
-- =====================================================

INSERT INTO receta
(
    paciente_id,
    numero_receta,
    medico,
    tipo_receta,
    fecha_receta,
    created_by
)
VALUES
(
    (
        SELECT id
        FROM paciente
        WHERE dni = '12345678'
    ),
    'REC-000001',
    'DR. CARLOS RAMIREZ',
    'ASEGURADO',
    CURRENT_DATE,
    1
);

-- =====================================================
-- ORDEN DE COMPRA DEMO
-- =====================================================

INSERT INTO orden_compra
(
    proveedor_id,
    usuario_id,
    estado,
    observacion,
    created_by
)
VALUES
(
    (
        SELECT id
        FROM proveedor
        WHERE ruc = '20100070970'
    ),
    1,
    'BORRADOR',
    'Orden inicial de prueba',
    1
);

-- =====================================================
-- DETALLE ORDEN DEMO
-- =====================================================

INSERT INTO detalle_orden_compra
(
    orden_compra_id,
    medicamento_id,
    cantidad
)
VALUES
(
    (
        SELECT MAX(id)
        FROM orden_compra
    ),
    (
        SELECT id
        FROM medicamento
        WHERE codigo = 'MED0001'
    ),
    200
);

-- =====================================================
-- MENSAJE DE VERIFICACIÓN
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '===========================================';
    RAISE NOTICE 'FARMAMED - SEED DATA INSTALADO';
    RAISE NOTICE 'Roles cargados';
    RAISE NOTICE 'Usuario admin creado';
    RAISE NOTICE 'Configuracion inicial cargada';
    RAISE NOTICE 'Proveedor demo cargado';
    RAISE NOTICE 'Medicamento demo cargado';
    RAISE NOTICE 'Lote demo cargado';
    RAISE NOTICE 'Paciente demo cargado';
    RAISE NOTICE '===========================================';
END;
$$;

-- =====================================================
-- FIN
-- =====================================================