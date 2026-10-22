# FarmaMed – Casos de Uso

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Tecnología:** Java MVC + PostgreSQL + Angular Material  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Este documento describe los Casos de Uso (CU) de FarmaMed.

Los casos de uso representan las funcionalidades que los actores pueden ejecutar dentro del sistema para cumplir los procesos de negocio definidos en las Reglas de Negocio (RN), Requerimientos Funcionales (RF) y Flujos de Usuario.

Este documento sirve como base para:

- Especificaciones OpenSpec.
- Diseño de pantallas.
- Diseño de APIs.
- Modelo Entidad Relación (DER).
- Planificación del desarrollo.

---

# 2. Actores

## ACT01 – Jefe de Farmacia

Responsable de supervisión, configuración y control operativo.

---

## ACT02 – Técnico de Farmacia

Responsable de la gestión de inventario y dispensación.

---

## ACT03 – Logística / Compras

Responsable de abastecimiento y proveedores.

---

## ACT04 – Administrador TI

Responsable de usuarios, roles y soporte técnico.

---

# 3. Inventario

---

# CU01 – Gestionar Medicamentos

## Actor Principal

Jefe de Farmacia

## Objetivo

Administrar el catálogo institucional de medicamentos.

## Precondiciones

- Usuario autenticado.
- Permiso para gestionar medicamentos.

## Flujo Principal

1. Acceder al módulo Medicamentos.
2. Registrar, editar o desactivar medicamento.
3. Validar información.
4. Guardar cambios.
5. Registrar auditoría.

## Resultado

Medicamento actualizado correctamente.

## RF Relacionados

- RF-01

---

# CU02 – Consultar Medicamentos

## Actor Principal

Jefe de Farmacia
Técnico de Farmacia
Logística
Administrador TI

## Objetivo

Consultar información de medicamentos registrados.

## Resultado

Listado de medicamentos disponible.

## RF Relacionados

- RF-01

---

# CU03 – Registrar Lote

## Actor Principal

Técnico de Farmacia
Jefe de Farmacia

## Objetivo

Registrar el ingreso de un lote al inventario.

## Precondiciones

- Medicamento existente.
- Usuario autorizado.

## Flujo Principal

1. Seleccionar medicamento.
2. Registrar lote.
3. Registrar cantidad.
4. Registrar fechas.
5. Validar datos.
6. Guardar lote.
7. Actualizar stock.
8. Registrar auditoría.

## Resultado

Lote registrado exitosamente.

## RF Relacionados

- RF-05

---

# CU04 – Consultar Inventario

## Actor Principal

Jefe de Farmacia
Técnico de Farmacia

## Objetivo

Consultar stock y vencimientos de medicamentos.

## Resultado

Inventario actualizado mostrado en pantalla.

## RF Relacionados

- RF-02
- RF-03

---

# CU05 – Registrar Baja de Inventario

## Actor Principal

Jefe de Farmacia

## Objetivo

Registrar bajas por vencimiento, merma o devolución.

## Precondiciones

- Existencia de lote.

## Flujo Principal

1. Seleccionar lote.
2. Seleccionar motivo.
3. Confirmar.
4. Actualizar stock.
5. Registrar auditoría.

## Resultado

Baja registrada.

## RF Relacionados

- RF-14

---

# 4. Dispensación

---

# CU06 – Registrar Receta

## Actor Principal

Técnico de Farmacia

## Objetivo

Registrar datos de una receta médica (una sola vez, luego CU07 la referencia 1:1).

## Resultado

Receta asociada a la atención (1 Receta = 1 Dispensación).

## RF Relacionados

- RF-05A
- RF-05B

---

# CU07 – Realizar Dispensación

## Actor Principal

Técnico de Farmacia
Jefe de Farmacia (único que puede autorizar excepción FEFO)

## Objetivo

Entregar medicamentos siguiendo la política FEFO.

## Precondiciones

- Receta válida y no usada antes (1:1).
- Stock disponible.
- Lote vigente.

## Flujo Principal

1. Referenciar receta existente (CU06, no duplicar).
2. Seleccionar medicamento.
3. Registrar cantidad.
4. Aplicar FEFO.
5. Validar stock.
6. Confirmar entrega.
7. Actualizar stock.
8. Generar ticket.
9. Registrar auditoría.

## Flujos Alternativos

### Stock insuficiente

- Operación cancelada.

### Lote vencido

- Operación bloqueada.

### Excepción FEFO

- Solo Jefe de Farmacia con motivo obligatorio y auditado.

## Resultado

Dispensación registrada exitosamente.

## RF Relacionados

- RF-04
- RF-06
- RF-07

---

# CU08 – Consultar Historial de Dispensaciones

## Actor Principal

Jefe de Farmacia

## Objetivo

Consultar dispensaciones realizadas.

## Resultado

Historial disponible para consulta.

## RF Relacionados

- RF-06

---

# 5. Alertas

---

# CU09 – Consultar Alertas

## Actor Principal

Jefe de Farmacia
Técnico de Farmacia
Logística

## Objetivo

Visualizar alertas activas.

## Resultado

Listado de alertas mostrado.

## RF Relacionados

- RF-08

---

# CU10 – Atender Alerta

## Actor Principal

Jefe de Farmacia
Logística

## Objetivo

Gestionar alertas críticas.

## Flujo Principal

1. Seleccionar alerta.
2. Revisar detalle.
3. Ejecutar acción.
4. Marcar atendida.
5. Registrar auditoría.

## Resultado

Alerta gestionada.

## RF Relacionados

- RF-08

---

# CU11 – Generar Pedido Sugerido

## Actor Principal

Sistema

## Objetivo

Generar sugerencias de compra cuando exista stock crítico.

## Resultado

Pedido sugerido generado.

## RF Relacionados

- RF-09

---

# 6. Compras y Abastecimiento

---

# CU12 – Gestionar Proveedores

## Actor Principal

Logística (crea/modifica/desactiva)
Jefe de Farmacia (consulta)

## Objetivo

Administrar proveedores registrados.

## Flujo Principal

1. Crear proveedor.
2. Editar proveedor.
3. Consultar proveedor.
4. Desactivar proveedor.

## Resultado

Proveedor actualizado.

## RF Relacionados

- RF-10

---

# CU13 – Generar Orden de Compra

## Actor Principal

Logística

## Objetivo

Crear órdenes de compra.

## Resultado

Orden generada.

## RF Relacionados

- RF-10A

---

# CU14 – Aprobar Orden de Compra

## Actor Principal

Jefe de Farmacia

## Objetivo

Aprobar o rechazar órdenes de compra.

## Flujo Principal

1. Revisar orden.
2. Aprobar o rechazar.
3. Registrar decisión.
4. Registrar auditoría.

## Resultado

Orden actualizada.

## RF Relacionados

- RF-10A

---

# CU15 – Consultar Órdenes de Compra

## Actor Principal

Jefe de Farmacia
Logística

## Objetivo

Consultar órdenes de compra históricas y activas.

## Resultado

Listado de órdenes disponible.

## RF Relacionados

- RF-10A

---

# 7. Reportes y Dashboard

---

# CU16 – Generar Reporte de Consumo

## Actor Principal

Jefe de Farmacia (genera)
Logística (consulta)

## Objetivo

Analizar consumo de medicamentos.

## Resultado

Reporte generado.

## RF Relacionados

- RF-12

---

# CU17 – Generar Reporte de Bajas

## Actor Principal

Jefe de Farmacia (genera)
Logística (consulta)

## Objetivo

Consultar bajas por vencimiento y merma.

## Resultado

Reporte generado.

## RF Relacionados

- RF-12

---

# CU18 – Consultar Dashboard

## Actor Principal

Jefe de Farmacia
Técnico de Farmacia
Logística
Administrador TI

## Objetivo

Visualizar indicadores principales.

## Resultado

Dashboard mostrado.

## RF Relacionados

- RF-13

---

# 8. Administración

---

# CU19 – Gestionar Usuarios

## Actor Principal

Administrador TI

## Objetivo

Administrar cuentas de acceso.

## Flujo Principal

1. Crear usuario.
2. Modificar usuario.
3. Desactivar usuario.
4. Consultar usuario.

## Resultado

Usuario actualizado.

## RF Relacionados

- RF-11

---

# CU20 – Gestionar Roles

## Actor Principal

Administrador TI

## Objetivo

Administrar la asignación de roles.

## Flujo Principal

1. Consultar roles.
2. Asignar rol.
3. Actualizar permisos autorizados.

## Resultado

Rol asignado correctamente.

## RF Relacionados

- RF-11

---

# CU21 – Configurar Parámetros del Sistema

## Actor Principal

Jefe de Farmacia

## Objetivo

Configurar parámetros operativos del sistema.

## Alcance

- Umbrales del semáforo.
- Stock mínimo.
- Stock máximo.
- Parámetros de alertas.

## Resultado

Configuración aplicada.

## RF Relacionados

- RF-02
- RF-08

---

# 4. Matriz Actor → Caso de Uso

| Caso de Uso | JF | TF | LOG | ATI |
|------------|----|----|----|----|
| CU01 Gestionar Medicamentos | ✅ | ❌ | ❌ | ❌ |
| CU02 Consultar Medicamentos | ✅ | ✅ | ✅ | ✅ |
| CU03 Registrar Lote | ✅ | ✅ | ❌ | ❌ |
| CU04 Consultar Inventario | ✅ | ✅ | ❌ | ❌ |
| CU05 Registrar Baja | ✅ | ❌ | ❌ | ❌ |
| CU06 Registrar Receta | ❌ | ✅ | ❌ | ❌ |
| CU07 Realizar Dispensación | ✅ | ✅ | ❌ | ❌ |
| CU08 Consultar Historial | ✅ | ❌ | ❌ | ❌ |
| CU09 Consultar Alertas | ✅ | ✅ | ✅ | ❌ |
| CU10 Atender Alerta | ✅ | ❌ | ✅ | ❌ |
| CU11 Generar Pedido Sugerido | SYS | SYS | SYS | SYS |
| CU12 Gestionar Proveedores | 👁️ | ❌ | ✅ | ❌ |
| CU13 Generar Orden Compra | ❌ | ❌ | ✅ | ❌ |
| CU14 Aprobar Orden Compra | ✅ | ❌ | ❌ | ❌ |
| CU15 Consultar Órdenes | ✅ | ❌ | ✅ | ❌ |
| CU16 Reporte Consumo | ✅ | ❌ | 👁️ | ❌ |
| CU17 Reporte Bajas | ✅ | ❌ | 👁️ | ❌ |
| CU18 Dashboard | ✅ | ✅ | ✅ | ✅ |
| CU19 Gestionar Usuarios | ❌ | ❌ | ❌ | ✅ |
| CU20 Gestionar Roles | ❌ | ❌ | ❌ | ✅ |
| CU21 Configurar Sistema | ✅ | ❌ | ❌ | ❌ |

---

# 5. Trazabilidad CU → RF

| Caso de Uso | RF |
|------------|----|
| CU01 | RF-01 |
| CU02 | RF-01 |
| CU03 | RF-05 |
| CU04 | RF-02, RF-03 |
| CU05 | RF-14 |
| CU06 | RF-05A, RF-05B |
| CU07 | RF-04, RF-06, RF-07 |
| CU08 | RF-06 |
| CU09 | RF-08 |
| CU10 | RF-08 |
| CU11 | RF-09 |
| CU12 | RF-10 |
| CU13 | RF-10A |
| CU14 | RF-10A |
| CU15 | RF-10A |
| CU16 | RF-12 |
| CU17 | RF-12 |
| CU18 | RF-13 |
| CU19 | RF-11 |
| CU20 | RF-11 |
| CU21 | RF-02, RF-08 |

---

# 6. Criterio de Cierre

Los Casos de Uso se consideran completos cuando:

- Todos los actores tienen casos de uso asignados.
- Todos los RF están relacionados con uno o más CU.
- Todos los módulos poseen casos de uso asociados.
- Existe trazabilidad RN → RF → CU.
- Los procesos principales del negocio están representados.

Una vez aprobado este documento, el siguiente paso será:

1. Especificaciones OpenSpec (Specs).
2. Modelo Entidad-Relación (DER).
3. Diccionario de Datos.
4. Modelo Relacional PostgreSQL.
5. Diseño de API.
6. Implementación del Sistema FarmaMed.