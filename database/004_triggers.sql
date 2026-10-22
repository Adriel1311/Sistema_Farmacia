-- =====================================================
-- FARMAMED
-- 004_triggers.sql
-- Triggers y Funciones
-- =====================================================

SET search_path TO farmamed;

-- =====================================================
-- UPDATED_AT AUTOMÁTICO
-- =====================================================

CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_usuario_updated_at
BEFORE UPDATE ON usuario
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_proveedor_updated_at
BEFORE UPDATE ON proveedor
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_medicamento_updated_at
BEFORE UPDATE ON medicamento
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_lote_updated_at
BEFORE UPDATE ON lote
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_orden_compra_updated_at
BEFORE UPDATE ON orden_compra
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_configuracion_updated_at
BEFORE UPDATE ON configuracion_sistema
FOR EACH ROW
EXECUTE FUNCTION fn_update_timestamp();

-- =====================================================
-- AUDITORÍA GENÉRICA
-- =====================================================

CREATE OR REPLACE FUNCTION fn_auditoria()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_usuario_id BIGINT;
BEGIN

    v_usuario_id :=
        COALESCE(
            NEW.updated_by,
            NEW.created_by,
            OLD.updated_by,
            OLD.created_by,
            1
        );

    IF TG_OP = 'INSERT' THEN

        INSERT INTO auditoria (
            usuario_id,
            entidad,
            accion,
            valor_nuevo,
            fecha_hora
        )
        VALUES (
            v_usuario_id,
            TG_TABLE_NAME,
            'INSERT',
            row_to_json(NEW)::TEXT,
            CURRENT_TIMESTAMP
        );

        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN

        INSERT INTO auditoria (
            usuario_id,
            entidad,
            accion,
            valor_anterior,
            valor_nuevo,
            fecha_hora
        )
        VALUES (
            v_usuario_id,
            TG_TABLE_NAME,
            'UPDATE',
            row_to_json(OLD)::TEXT,
            row_to_json(NEW)::TEXT,
            CURRENT_TIMESTAMP
        );

        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN

        INSERT INTO auditoria (
            usuario_id,
            entidad,
            accion,
            valor_anterior,
            fecha_hora
        )
        VALUES (
            COALESCE(OLD.created_by,1),
            TG_TABLE_NAME,
            'DELETE',
            row_to_json(OLD)::TEXT,
            CURRENT_TIMESTAMP
        );

        RETURN OLD;

    END IF;

    RETURN NULL;
END;
$$;

-- =====================================================
-- AUDITORÍA DE TABLAS MAESTRAS
-- =====================================================

CREATE TRIGGER trg_medicamento_audit
AFTER INSERT OR UPDATE OR DELETE
ON medicamento
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria();

CREATE TRIGGER trg_proveedor_audit
AFTER INSERT OR UPDATE OR DELETE
ON proveedor
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria();

CREATE TRIGGER trg_usuario_audit
AFTER INSERT OR UPDATE OR DELETE
ON usuario
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria();

CREATE TRIGGER trg_lote_audit
AFTER INSERT OR UPDATE OR DELETE
ON lote
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria();

CREATE TRIGGER trg_orden_compra_audit
AFTER INSERT OR UPDATE OR DELETE
ON orden_compra
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria();

-- =====================================================
-- BLOQUEAR STOCK NEGATIVO EN DISPENSACIÓN
-- =====================================================

CREATE OR REPLACE FUNCTION fn_validar_stock_dispensacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_stock INTEGER;
BEGIN

    SELECT cantidad_actual
    INTO v_stock
    FROM lote
    WHERE id = NEW.lote_id
    FOR UPDATE;

    IF v_stock IS NULL THEN
        RAISE EXCEPTION 'Lote inexistente';
    END IF;

    IF NEW.cantidad > v_stock THEN
        RAISE EXCEPTION
        'Stock insuficiente. Disponible: %, solicitado: %',
        v_stock,
        NEW.cantidad;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_stock_dispensacion
BEFORE INSERT
ON detalle_dispensacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_stock_dispensacion();

-- =====================================================
-- BLOQUEAR LOTES VENCIDOS
-- =====================================================

CREATE OR REPLACE FUNCTION fn_validar_lote_vigente()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_fecha DATE;
    v_estado estado_lote_enum;
BEGIN

    SELECT fecha_vencimiento, estado
    INTO v_fecha, v_estado
    FROM lote
    WHERE id = NEW.lote_id
    FOR UPDATE;

    IF v_fecha IS NULL THEN
        RAISE EXCEPTION 'Lote inexistente';
    END IF;

    IF v_fecha < CURRENT_DATE THEN

        RAISE EXCEPTION
        'No se puede dispensar desde lotes vencidos';

    END IF;

    IF v_estado <> 'ACTIVO' THEN

        RAISE EXCEPTION
        'No se puede dispensar desde lotes no vigentes (estado=%)', v_estado;

    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_lote_vigente
BEFORE INSERT
ON detalle_dispensacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_lote_vigente();

-- =====================================================
-- DESCONTAR STOCK AL DISPENSAR
-- Nota orden: trg_descontar_stock (d) dispara antes que
-- trg_movimiento_salida (m) por orden alfabético PG.
-- fn_movimiento_salida asume descuento ya aplicado:
-- stock_nuevo = cantidad_actual post-descuento,
-- stock_anterior = stock_nuevo + NEW.cantidad.
-- No renombrar triggers sin revisar orden.
-- =====================================================

CREATE OR REPLACE FUNCTION fn_descontar_stock()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

    UPDATE lote
       SET cantidad_actual =
           cantidad_actual - NEW.cantidad
     WHERE id = NEW.lote_id;

    RETURN NEW;

END;
$$;

CREATE TRIGGER trg_descontar_stock
AFTER INSERT
ON detalle_dispensacion
FOR EACH ROW
EXECUTE FUNCTION fn_descontar_stock();

-- =====================================================
-- VALIDACIÓN DE BAJA INVENTARIO
-- =====================================================

CREATE OR REPLACE FUNCTION fn_validar_baja()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_stock INTEGER;
BEGIN

    SELECT cantidad_actual
    INTO v_stock
    FROM lote
    WHERE id = NEW.lote_id
    FOR UPDATE;

    IF v_stock IS NULL THEN
        RAISE EXCEPTION 'Lote inexistente';
    END IF;

    IF NEW.cantidad > v_stock THEN

        RAISE EXCEPTION
        'La baja supera el stock disponible';

    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_baja
BEFORE INSERT
ON baja_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_validar_baja();

-- =====================================================
-- DESCONTAR STOCK POR BAJA
-- Nota orden: trg_descontar_baja (d) antes que
-- trg_movimiento_baja (m). Misma convención que dispensación.
-- =====================================================

CREATE OR REPLACE FUNCTION fn_descontar_baja()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

    UPDATE lote
       SET cantidad_actual =
           cantidad_actual - NEW.cantidad
     WHERE id = NEW.lote_id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_descontar_baja
AFTER INSERT
ON baja_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_descontar_baja();

-- =====================================================
-- MOVIMIENTO AUTOMÁTICO DE INGRESO
-- =====================================================

CREATE OR REPLACE FUNCTION fn_movimiento_ingreso()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT INTO movimiento_inventario
    (
        medicamento_id,
        lote_id,
        usuario_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        referencia_tipo,
        referencia_id
    )
    VALUES
    (
        NEW.medicamento_id,
        NEW.id,
        COALESCE(NEW.created_by,1),
        'ENTRADA',
        NEW.cantidad_inicial,
        0,
        NEW.cantidad_inicial,
        'LOTE',
        NEW.id
    );

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_movimiento_ingreso
AFTER INSERT
ON lote
FOR EACH ROW
EXECUTE FUNCTION fn_movimiento_ingreso();

-- =====================================================
-- MOVIMIENTO AUTOMÁTICO DE SALIDA
-- =====================================================

CREATE OR REPLACE FUNCTION fn_movimiento_salida()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_usuario BIGINT;
    v_stock INTEGER;
BEGIN

    SELECT usuario_id
      INTO v_usuario
      FROM dispensacion
     WHERE id = NEW.dispensacion_id;

    SELECT cantidad_actual
      INTO v_stock
      FROM lote
     WHERE id = NEW.lote_id;

    INSERT INTO movimiento_inventario
    (
        medicamento_id,
        lote_id,
        usuario_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        referencia_tipo,
        referencia_id
    )
    VALUES
    (
        NEW.medicamento_id,
        NEW.lote_id,
        v_usuario,
        'SALIDA',
        NEW.cantidad,
        v_stock + NEW.cantidad,
        v_stock,
        'DISPENSACION',
        NEW.dispensacion_id
    );

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_movimiento_salida
AFTER INSERT
ON detalle_dispensacion
FOR EACH ROW
EXECUTE FUNCTION fn_movimiento_salida();

-- =====================================================
-- MOVIMIENTO AUTOMÁTICO DE BAJA
-- =====================================================

CREATE OR REPLACE FUNCTION fn_movimiento_baja()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_medicamento BIGINT;
    v_stock INTEGER;
BEGIN

    SELECT medicamento_id,
           cantidad_actual
      INTO v_medicamento,
           v_stock
      FROM lote
     WHERE id = NEW.lote_id;

    INSERT INTO movimiento_inventario
    (
        medicamento_id,
        lote_id,
        usuario_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        referencia_tipo,
        referencia_id
    )
    VALUES
    (
        v_medicamento,
        NEW.lote_id,
        NEW.usuario_id,
        'BAJA',
        NEW.cantidad,
        v_stock + NEW.cantidad,
        v_stock,
        'BAJA_INVENTARIO',
        NEW.id
    );

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_movimiento_baja
AFTER INSERT
ON baja_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_movimiento_baja();

-- =====================================================
-- ACTUALIZACIÓN AUTOMÁTICA DEL ESTADO DEL LOTE
-- =====================================================

CREATE OR REPLACE FUNCTION fn_estado_lote()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN

    IF NEW.fecha_vencimiento < CURRENT_DATE THEN
        NEW.estado := 'VENCIDO';
    ELSIF NEW.cantidad_actual = 0 THEN
        NEW.estado := 'BAJA';
    ELSE
        NEW.estado := 'ACTIVO';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_estado_lote
BEFORE INSERT OR UPDATE
ON lote
FOR EACH ROW
EXECUTE FUNCTION fn_estado_lote();

-- =====================================================
-- FIN
-- =====================================================