-- =====================================================
-- FARMAMED
-- 004_triggers.sql  -  Guardas de integridad de la base
-- PostgreSQL 18
-- -----------------------------------------------------
-- IMPORTANTE: estos triggers NO mueven stock ni escriben
-- el kardex. El descuento de existencias y el registro de
-- movimientos los hace la aplicacion, dentro de una
-- transaccion y con bloqueo pesimista de escritura sobre
-- el lote (RNF-09). Si estos triggers tambien descontaran,
-- el stock se descontaria dos veces.
--
-- Lo que si hacen es servir de segunda barrera: aunque una
-- operacion llegue al banco con datos inconsistentes, la
-- base de datos la rechaza con un mensaje entendible.
--
-- NO hay un trigger generico de auditoria a proposito. La
-- auditoria la escribe AuditoriaService, que conoce el
-- usuario que esta operando y el valor anterior y el nuevo.
-- Un trigger de base de datos tendria que adivinar el
-- usuario y generaria registros duplicados.
--
-- Es idempotente: DROP TRIGGER IF EXISTS + CREATE TRIGGER.
-- =====================================================

SET search_path TO farmamed, public;

-- =====================================================
-- GUARDA 1: INTEGRIDAD DEL LOTE
-- El stock nunca es negativo, nunca supera la cantidad
-- inicial y el vencimiento siempre es posterior al ingreso
-- (RN03, RN09).
-- =====================================================

CREATE OR REPLACE FUNCTION farmamed.fn_lote_integridad()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.cantidad_actual < 0 THEN
        RAISE EXCEPTION
            'El lote % no puede quedar con stock negativo (actual: %)',
            NEW.numero_lote, NEW.cantidad_actual
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.cantidad_actual > NEW.cantidad_inicial THEN
        RAISE EXCEPTION
            'El stock del lote % (%), no puede superar la cantidad inicial (%)',
            NEW.numero_lote, NEW.cantidad_actual, NEW.cantidad_inicial
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.fecha_vencimiento <= NEW.fecha_ingreso THEN
        RAISE EXCEPTION
            'El lote % debe vencer despues de la fecha de ingreso (%)',
            NEW.numero_lote, NEW.fecha_ingreso
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_lote_integridad ON farmamed.lote;
CREATE TRIGGER trg_lote_integridad
    BEFORE INSERT OR UPDATE ON farmamed.lote
    FOR EACH ROW
    EXECUTE FUNCTION farmamed.fn_lote_integridad();

-- =====================================================
-- GUARDA 2: LA DISPENSACION NO SE REESCRIBE
-- Una receta se atiende una sola vez (RN05.0). No se puede
-- cambiar la receta de una dispensacion ya registrada ni
-- cambiar su ticket interno.
-- =====================================================

CREATE OR REPLACE FUNCTION farmamed.fn_dispensacion_inmutable()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.receta_id <> OLD.receta_id THEN
        RAISE EXCEPTION
            'La receta de una dispensacion no se puede cambiar: una receta genera una sola dispensacion (RN05.0)'
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.numero_ticket IS DISTINCT FROM OLD.numero_ticket THEN
        RAISE EXCEPTION
            'El numero de ticket interno no se puede modificar'
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_dispensacion_inmutable ON farmamed.dispensacion;
CREATE TRIGGER trg_dispensacion_inmutable
    BEFORE UPDATE ON farmamed.dispensacion
    FOR EACH ROW
    EXECUTE FUNCTION farmamed.fn_dispensacion_inmutable();

-- =====================================================
-- GUARDA 3: EL KARDEX NO REGISTRA STOCK NEGATIVO
-- =====================================================

CREATE OR REPLACE FUNCTION farmamed.fn_movimiento_stock_no_negativo()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.stock_nuevo < 0 THEN
        RAISE EXCEPTION
            'El movimiento de inventario no puede dejar el stock en negativo (stock nuevo: %)',
            NEW.stock_nuevo
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.stock_anterior < 0 THEN
        RAISE EXCEPTION
            'El movimiento de inventario no puede partir de un stock negativo (stock anterior: %)',
            NEW.stock_anterior
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_movimiento_stock_no_negativo ON farmamed.movimiento_inventario;
CREATE TRIGGER trg_movimiento_stock_no_negativo
    BEFORE INSERT ON farmamed.movimiento_inventario
    FOR EACH ROW
    EXECUTE FUNCTION farmamed.fn_movimiento_stock_no_negativo();

-- =====================================================
-- GUARDA 4: APROBACION DE ORDEN COHERENTE
-- Una orden aprobada tiene que decir quien la approves y
-- cuando. Una orden en borrador o sugerida no la tiene
-- todavia, y una orden rechazada no lleva aprobacion
-- (RN07).
-- =====================================================

CREATE OR REPLACE FUNCTION farmamed.fn_orden_aprobacion_coherente()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.estado = 'APROBADA' THEN
        IF NEW.aprobado_por IS NULL THEN
            RAISE EXCEPTION
                'Una orden aprobada debe registrar quien la aprueba'
                USING ERRCODE = 'check_violation';
        END IF;

        IF NEW.fecha_aprobacion IS NULL THEN
            RAISE EXCEPTION
                'Una orden aprobada debe registrar la fecha de aprobacion'
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_orden_aprobacion_coherente ON farmamed.orden_compra;
CREATE TRIGGER trg_orden_aprobacion_coherente
    BEFORE UPDATE ON farmamed.orden_compra
    FOR EACH ROW
    EXECUTE FUNCTION farmamed.fn_orden_aprobacion_coherente();

-- =====================================================
-- FIN DE LOS TRIGGERS
-- =====================================================
