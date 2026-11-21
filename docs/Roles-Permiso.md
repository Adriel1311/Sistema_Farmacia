# FarmaMed – Roles y Permisos

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Este documento define los roles del sistema FarmaMed y los permisos asociados a cada uno.

El objetivo es garantizar que cada usuario acceda únicamente a las funcionalidades necesarias para cumplir sus responsabilidades dentro del proceso de gestión farmacéutica.

Los permisos aquí definidos son la fuente de verdad para:

- Seguridad del sistema.
- Casos de uso.
- Especificaciones OpenSpec.
- Diseño de base de datos.
- Implementación de control de acceso (RBAC).

---

# 2. Modelo de Control de Acceso

FarmaMed utiliza un modelo RBAC (Role-Based Access Control).

Cada usuario:

- Tiene un único rol asignado.
- Hereda los permisos de dicho rol.
- Puede ejecutar únicamente las acciones autorizadas.

---

# 3. Roles del Sistema

## ROL-01 – Jefe de Farmacia

### Descripción

Responsable de la supervisión general del área de farmacia.

Tiene control total sobre las operaciones de negocio y toma decisiones relacionadas con abastecimiento, configuración y seguimiento.

### Responsabilidades

- Supervisar inventario.
- Gestionar medicamentos.
- Configurar parámetros críticos.
- Atender alertas.
- Aprobar pedidos.
- Revisar reportes.
- Monitorear indicadores.

### Permisos

#### Dashboard

- Ver dashboard.
- Consultar indicadores.
- Consultar estadísticas.

#### Medicamentos

- Registrar.
- Modificar.
- Consultar.
- Desactivar.

#### Lotes

- Registrar.
- Modificar.
- Consultar.
- Dar de baja.

#### Dispensación

- Consultar dispensaciones.
- Realizar dispensaciones.
- Autorizar excepciones FEFO.

#### Alertas

- Ver alertas.
- Marcar alertas atendidas.
- Gestionar alertas.

#### Stock

- Configurar stock mínimo.
- Configurar stock máximo.

#### Semáforo

- Modificar umbrales de vencimiento.

#### Compras

- Aprobar órdenes.
- Rechazar órdenes.
- Consultar órdenes.

#### Reportes

- Generar reportes.
- Exportar reportes.

#### Seguridad

- Consultar auditoría.

### Restricciones

Ninguna funcional dentro del alcance APF1.

---

## ROL-02 – Técnico de Farmacia

### Descripción

Responsable de la operación diaria de la farmacia.

### Responsabilidades

- Registrar ingresos.
- Registrar lotes.
- Atender dispensaciones.
- Consultar inventario.

### Permisos

#### Dashboard

- Ver dashboard operativo.

#### Medicamentos

- Consultar medicamentos.

#### Lotes

- Registrar lotes.
- Consultar lotes.

#### Dispensación

- Registrar dispensaciones.
- Consultar historial propio.

#### Inventario

- Consultar inventario.

#### Alertas

- Ver alertas.

### Restricciones

No puede:

- Modificar configuración.
- Aprobar órdenes de compra.
- Modificar parámetros FEFO.
- Modificar mínimos y máximos.
- Gestionar usuarios.
- Consultar auditoría completa.
- Eliminar registros.

---

## ROL-03 – Logística / Compras

### Descripción

Responsable de abastecimiento y relación con proveedores.

### Responsabilidades

- Gestionar proveedores.
- Revisar sugerencias de compra.
- Gestionar órdenes de compra.

### Permisos

#### Proveedores

- Registrar proveedor.
- Modificar proveedor.
- Consultar proveedor.
- Desactivar proveedor.

#### Órdenes de Compra

- Crear orden.
- Modificar orden.
- Revisar orden.
- Consultar orden.

#### Alertas

- Ver alertas de stock crítico.
- Atender alertas de abastecimiento.

#### Reportes

- Consultar reportes de consumo.
- Consultar reportes de abastecimiento.

### Restricciones

No puede:

- Realizar dispensaciones.
- Modificar medicamentos.
- Configurar sistema.
- Gestionar usuarios.
- Modificar FEFO.

---

## ROL-04 – Administrador TI

### Descripción

Responsable de la administración técnica del sistema.

### Responsabilidades

- Gestionar usuarios.
- Gestionar roles.
- Gestionar respaldos.
- Administrar la plataforma.

### Permisos

#### Usuarios

- Crear usuario.
- Modificar usuario.
- Desactivar usuario.
- Consultar usuario.

#### Roles

- Asignar rol.
- Consultar roles.

#### Auditoría

- Consultar auditoría.

#### Sistema

- Ejecutar respaldos.
- Restaurar respaldos.
- Consultar logs técnicos.

### Restricciones

No puede:

- Dispensar medicamentos.
- Registrar lotes.
- Aprobar órdenes.
- Modificar inventario.
- Modificar parámetros farmacéuticos.

---

# 4. Matriz de Permisos

| Módulo | Jefe | Técnico | Logística | Admin TI |
|----------|----------|----------|----------|----------|
| Dashboard | ✅ | ✅ | ✅ | ✅ |
| Medicamentos | ✅ | 👁️ | 👁️ | 👁️ |
| Lotes | ✅ | ✅ | ❌ | ❌ |
| Dispensación | ✅ | ✅ | ❌ | ❌ |
| Alertas | ✅ | ✅ | ✅ | 👁️ |
| Proveedores | ✅ | ❌ | ✅ | ❌ |
| Órdenes Compra | ✅ | ❌ | ✅ | ❌ |
| Reportes | ✅ | 👁️ | 👁️ | 👁️ |
| Usuarios | ❌ | ❌ | ❌ | ✅ |
| Roles | ❌ | ❌ | ❌ | ✅ |
| Auditoría | ✅ | ❌ | ❌ | ✅ |
| Configuración | ✅ | ❌ | ❌ | ❌ |

### Leyenda

- ✅ Gestión completa
- 👁️ Solo consulta
- ❌ Sin acceso

---

# 5. Reglas de Seguridad

## RSP-01

Todo usuario debe tener exactamente un rol asignado.

---

## RSP-02

Ningún usuario puede acceder a funcionalidades fuera de los permisos de su rol.

---

## RSP-03

Toda acción crítica debe registrarse en auditoría.

---

## RSP-04

Las acciones de configuración solo pueden ser realizadas por el Jefe de Farmacia.

---

## RSP-05

La gestión de usuarios y roles solo puede ser realizada por el Administrador TI.

---

## RSP-06

La aprobación de órdenes de compra solo puede ser realizada por el Jefe de Farmacia.

---

## RSP-07

Las excepciones al algoritmo FEFO requieren autorización del Jefe de Farmacia y registro obligatorio del motivo.

---

# 6. Relación con Casos de Uso (CU01-CU21 vigentes)

## Jefe de Farmacia

- CU01 Gestionar Medicamentos
- CU02 Consultar Medicamentos
- CU03 Registrar Lote (junto a TF)
- CU04 Consultar Inventario
- CU05 Registrar Baja de Inventario
- CU07 Realizar Dispensación (incluye excepción FEFO con motivo)
- CU08 Consultar Historial de Dispensaciones
- CU09 Consultar Alertas
- CU10 Atender Alerta
- CU14 Aprobar Orden de Compra
- CU15 Consultar Órdenes de Compra
- CU16 Generar Reporte de Consumo
- CU17 Generar Reporte de Bajas
- CU18 Consultar Dashboard
- CU21 Configurar Parámetros del Sistema

---

## Técnico de Farmacia

- CU02 Consultar Medicamentos
- CU03 Registrar Lote
- CU04 Consultar Inventario
- CU06 Registrar Receta (una vez, luego CU07 la referencia 1:1)
- CU07 Realizar Dispensación (sin excepción FEFO)
- CU09 Consultar Alertas
- CU18 Consultar Dashboard

---

## Logística / Compras

- CU02 Consultar Medicamentos
- CU09 Consultar Alertas (stock)
- CU10 Atender Alerta (abastecimiento)
- CU12 Gestionar Proveedores
- CU13 Generar Orden de Compra
- CU15 Consultar Órdenes de Compra
- CU16/CU17 Consultar Reportes (solo lectura)
- CU18 Consultar Dashboard

---

## Administrador TI

- CU02 Consultar Medicamentos (solo lectura)
- CU18 Consultar Dashboard
- CU19 Gestionar Usuarios
- CU20 Gestionar Roles

---

# 7. Criterio de Cierre

El documento de Roles y Permisos se considera completo cuando:

- Todos los actores tienen responsabilidades definidas.
- Todos los módulos poseen permisos explícitos.
- No existen permisos ambiguos.
- Las restricciones están documentadas.
- Existe trazabilidad con RN, RF y Casos de Uso.

Una vez aprobado este documento, el siguiente paso será:

1. Módulos del Sistema (modules.md)
2. Flujos de Usuario (user-flows.md)
3. Casos de Uso (use-cases.md)
4. Especificaciones OpenSpec
5. Modelo Entidad-Relación (DER)
6. Diccionario de Datos
7. Modelo Relacional PostgreSQL
8. Implementación del Sistema FarmaMed

---

# 8. Historial

- v1.0: Creación inicial del modelo de roles y permisos.
- v1.1: Eliminación del rol "Docente / Evaluador" por no representar un actor real del negocio hospitalario.
- v1.2: Consolidación final de permisos y restricciones para APF1.