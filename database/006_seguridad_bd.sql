-- =====================================================
-- FARMAMED
-- 006_seguridad_bd.sql  -  Usuario de aplicacion
-- PostgreSQL 18
-- -----------------------------------------------------
-- La aplicacion NO debe conectarse como superusuario.
-- Este script crea el rol con el que corre el backend:
--
--   * NO es superusuario
--   * NO crea bases de datos ni roles
--   * NO tiene CREATE sobre el schema farmamed
--   * Solo puede leer y escribir datos, y usar las
--     secuencias (necesario para el numero de ticket
--     interno y para los identificadores)
--
-- Ademas se le cierra a todo el mundo la creacion de
-- objetos en el schema public, que en PostgreSQL venia
-- habilitada por defecto.
--
-- La clave por defecto es la de desarrollo. En el
-- despliegue real se cambia por variable de entorno:
--   setx DATABASE_PASSWORD "una_clave_robusta"
--
-- Es idempotente: el rol se crea si no existe y la clave
-- se actualiza en cada ejecucion.
-- =====================================================

SET search_path TO farmamed, public;

-- =====================================================
-- 1. ROL DE APLICACION
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'farmamed_app') THEN
        CREATE ROLE farmamed_app
            LOGIN
            PASSWORD 'farmamed_dev_2026'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            NOINHERIT
            NOREPLICATION
            CONNECTION LIMIT 20;
        RAISE NOTICE 'Rol farmamed_app creado';
    ELSE
        RAISE NOTICE 'El rol farmamed_app ya existia';
    END IF;
END;
$$;

-- La clave se sincroniza en cada ejecucion para que el
-- script sea la fuente unica de verdad en desarrollo.
ALTER ROLE farmamed_app WITH
    LOGIN
    PASSWORD 'farmamed_dev_2026'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOINHERIT
    NOREPLICATION
    CONNECTION LIMIT 20;

-- =====================================================
-- 2. SCHEMA PUBLIC CERRADO A PUBLIC
-- Por defecto cualquier usuario puede crear objetos en
-- public. Eso se revoca.
-- =====================================================

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- =====================================================
-- 3. PERMISOS SOBRE EL SCHEMA FARMAMED
-- =====================================================

REVOKE ALL ON SCHEMA farmamed FROM PUBLIC;
GRANT USAGE ON SCHEMA farmamed TO farmamed_app;

-- =====================================================
-- 4. PERMISOS SOBRE LOS OBJETOS
-- =====================================================

GRANT SELECT, INSERT, UPDATE, DELETE
    ON ALL TABLES IN SCHEMA farmamed TO farmamed_app;

GRANT USAGE, SELECT
    ON ALL SEQUENCES IN SCHEMA farmamed TO farmamed_app;

GRANT EXECUTE
    ON ALL FUNCTIONS IN SCHEMA farmamed TO farmamed_app;

-- Para que los objetos que se creen mas adelante
-- (vistas, indices, funciones) hereden los mismos
-- permisos sin tener que repetir el GRANT.
ALTER DEFAULT PRIVILEGES IN SCHEMA farmamed
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO farmamed_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA farmamed
    GRANT USAGE, SELECT ON SEQUENCES TO farmamed_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA farmamed
    GRANT EXECUTE ON FUNCTIONS TO farmamed_app;

-- =====================================================
-- 5. COMPROBACION
-- =====================================================

DO $$
DECLARE
    v_es_super BOOLEAN;
BEGIN
    SELECT rolsuper INTO v_es_super
    FROM pg_roles WHERE rolname = 'farmamed_app';

    IF v_es_super THEN
        RAISE EXCEPTION 'farmamed_app no debe ser superusuario';
    END IF;

    RAISE NOTICE 'farmamed_app: rol verificado, sin superusuario';
END;
$$;

-- =====================================================
-- FIN DEL SCRIPT DE SEGURIDAD
-- =====================================================
