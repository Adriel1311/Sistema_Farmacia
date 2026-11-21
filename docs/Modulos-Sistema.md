# FarmaMed – Módulos del Sistema

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Tecnología:** Java MVC + PostgreSQL + Angular Material  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Este documento define la estructura modular de FarmaMed.

Un módulo representa un conjunto de funcionalidades relacionadas que permiten ejecutar un proceso de negocio específico.

Los módulos aquí definidos constituyen la base para:

- Casos de Uso.
- Flujos de Usuario.
- Especificaciones OpenSpec.
- Diseño de Menús.
- Diseño de Pantallas.
- Arquitectura Frontend y Backend.
- Modelo de Datos.

---

# 2. Estructura General del Sistema

```text
FarmaMed
│
├── M01 Dashboard
├── M02 Medicamentos
├── M03 Inventario y Lotes
├── M04 Dispensación
├── M05 Alertas
├── M06 Proveedores
├── M07 Órdenes de Compra
├── M08 Reportes
├── M09 Usuarios
├── M10 Roles
└── M11 Configuración
```

---

# M01 – Dashboard

## Objetivo

Permitir visualizar el estado general de farmacia al ingresar al sistema.

## Funcionalidades

- Visualizar indicadores principales.
- Mostrar alertas críticas.
- Mostrar stock crítico.
- Mostrar medicamentos próximos a vencer.
- Mostrar órdenes sugeridas.
- Mostrar métricas resumidas.

## Indicadores

- Lotes vencidos.
- Lotes críticos.
- Productos con stock crítico.
- Pedidos sugeridos.
- Dispensaciones del día.

## Roles

- Jefe de Farmacia
- Técnico de Farmacia
- Logística
- Administrador TI

## RF Asociados

- RF-13

---

# M02 – Medicamentos

## Objetivo

Administrar el catálogo institucional de medicamentos.

## Funcionalidades

- Registrar medicamento.
- Consultar medicamento.
- Modificar medicamento.
- Desactivar medicamento.
- Configurar stock mínimo.
- Configurar stock máximo.
- Asociar proveedor habitual.

## Información principal

- Código.
- Nombre.
- Descripción.
- Unidad de medida.
- Estado.

## Roles

- Jefe de Farmacia

Consulta:

- Técnico de Farmacia
- Logística
- Administrador TI

## RF Asociados

- RF-01

---

# M03 – Inventario y Lotes

## Objetivo

Controlar la existencia física de medicamentos mediante lotes.

## Funcionalidades

- Registrar ingreso de lote.
- Consultar lotes.
- Consultar vencimientos.
- Consultar stock.
- Registrar bajas.
- Aplicar semáforo de vencimiento.

## Información principal

- Medicamento.
- Número de lote.
- Fecha de ingreso.
- Fecha de vencimiento.
- Cantidad inicial.
- Cantidad disponible.
- Estado.

## Roles

- Jefe de Farmacia
- Técnico de Farmacia

## RF Asociados

- RF-02
- RF-03
- RF-05
- RF-14

---

# M04 – Dispensación

## Objetivo

Gestionar la entrega de medicamentos a pacientes siguiendo la política FEFO.

## Funcionalidades

- Registrar receta.
- Buscar paciente.
- Buscar medicamento.
- Aplicar FEFO.
- Registrar dispensación.
- Emitir ticket interno.
- Actualizar inventario.

## Información principal

- Paciente.
- Receta.
- Medicamento.
- Cantidad.
- Usuario responsable.

## Roles

- Jefe de Farmacia
- Técnico de Farmacia

## RF Asociados

- RF-04
- RF-05A
- RF-05B
- RF-06
- RF-07

---

# M05 – Alertas

## Objetivo

Detectar y mostrar eventos que requieren atención.

## Funcionalidades

- Mostrar alertas críticas.
- Mostrar alertas de vencimiento.
- Mostrar alertas de stock crítico.
- Mostrar pedidos sugeridos.
- Marcar alertas atendidas.

## Tipos de Alerta

### Alta Prioridad

- Vencido bloqueado.
- Crítico 30 días.
- Stock crítico.

### Media Prioridad

- Próximo a vencer.
- Orden sugerida.

## Roles

- Jefe de Farmacia
- Técnico de Farmacia
- Logística

## RF Asociados

- RF-08
- RF-09

---

# M06 – Proveedores

## Objetivo

Gestionar la información de los proveedores de medicamentos.

## Funcionalidades

- Registrar proveedor.
- Consultar proveedor.
- Modificar proveedor.
- Desactivar proveedor.

## Información principal

- Razón social.
- RUC.
- Contacto.
- Teléfono.
- Correo.
- Estado.

## Roles

- Logística (crea/modifica/desactiva)
- Jefe de Farmacia (consulta)

## RF Asociados

- RF-10

---

# M07 – Órdenes de Compra

## Objetivo

Administrar el proceso de abastecimiento de medicamentos.

## Funcionalidades

- Generar orden sugerida.
- Crear orden.
- Revisar orden.
- Aprobar orden.
- Rechazar orden.
- Consultar historial.

## Estados

- Borrador.
- Sugerida.
- Aprobada.
- Rechazada.

## Roles

### Logística

- Crear.
- Modificar.
- Consultar.

### Jefe de Farmacia

- Aprobar.
- Rechazar.
- Consultar.

## RF Asociados

- RF-09
- RF-10A

---

# M08 – Reportes

## Objetivo

Proporcionar información para control y toma de decisiones.

## Funcionalidades

- Generar reportes.
- Consultar reportes.
- Exportar información.

## Reportes Mínimos

### Reporte de Consumo

Consumo mensual por medicamento.

### Reporte de Bajas

Medicamentos dados de baja por vencimiento o merma.

### Reporte de Inventario

Stock actual disponible.

### Reporte de Órdenes

Historial de órdenes de compra.

## Formatos

- PDF
- Excel

## Roles

- Jefe de Farmacia (genera)
- Logística (consulta)

Consulta:

- Técnico de Farmacia
- Administrador TI

## RF Asociados

- RF-12

---

# M09 – Usuarios

## Objetivo

Gestionar las cuentas de acceso al sistema.

## Funcionalidades

- Crear usuario.
- Modificar usuario.
- Desactivar usuario.
- Consultar usuario.

## Información principal

- Nombre.
- Usuario.
- Correo.
- Estado.
- Rol asignado.

## Roles

- Administrador TI

## RF Asociados

- RF-11

---

# M10 – Roles

## Objetivo

Administrar los roles definidos dentro del sistema.

## Funcionalidades

- Consultar roles.
- Asignar rol a usuario.
- Gestionar permisos autorizados.

## Roles del Sistema

- Jefe de Farmacia.
- Técnico de Farmacia.
- Logística.
- Administrador TI.

## Roles

- Administrador TI

## RF Asociados

- RF-11

---

# M11 – Configuración

## Objetivo

Gestionar parámetros globales del sistema.

## Funcionalidades

- Configurar semáforo de vencimiento.
- Configurar stock mínimo.
- Configurar stock máximo.
- Configurar parámetros operativos.

## Parámetros Iniciales

### Semáforo

- Crítico: 0–30 días.
- Próximo a vencer: 31–60 días.
- Vigente: más de 60 días.

### Operación

- Tiempo máximo de sesión.
- Parámetros de alertas.

## Roles

- Jefe de Farmacia

## RF Asociados

- RF-02
- RF-08

---

# 3. Menú Principal Propuesto

```text
Dashboard

Inventario
├── Medicamentos
└── Lotes

Dispensación

Abastecimiento
├── Alertas
├── Proveedores
└── Órdenes de Compra

Reportes

Administración
├── Usuarios
├── Roles
└── Configuración
```

---

# 4. Dependencias Entre Módulos

```text
Medicamentos
        │
        ▼
Inventario y Lotes
        │
        ▼
Dispensación
        │
        ▼
Alertas
        │
        ▼
Órdenes de Compra
        │
        ▼
Reportes
```

---

# 5. Trazabilidad Módulo → RF

| Módulo | RF Asociados |
|----------|----------|
| M01 Dashboard | RF-13 |
| M02 Medicamentos | RF-01 |
| M03 Inventario y Lotes | RF-02, RF-03, RF-05, RF-14 |
| M04 Dispensación | RF-04, RF-05A, RF-05B, RF-06, RF-07 |
| M05 Alertas | RF-08, RF-09 |
| M06 Proveedores | RF-10 |
| M07 Órdenes de Compra | RF-09, RF-10A |
| M08 Reportes | RF-12 |
| M09 Usuarios | RF-11 |
| M10 Roles | RF-11 |
| M11 Configuración | RF-02, RF-08 |

---

# 6. Criterio de Cierre

La estructura modular se considera aprobada cuando:

- Todos los RF están asociados a un módulo.
- Todos los roles tienen acceso a módulos definidos.
- Todos los procesos del negocio están representados.
- La navegación principal está definida.
- Existe trazabilidad entre RN, RF, módulos y futuros casos de uso.

Una vez aprobado este documento, el siguiente paso será:

1. User Flows (user-flows.md)
2. Casos de Uso (use-cases.md)
3. Especificaciones OpenSpec
4. Modelo Entidad-Relación (DER)
5. Diccionario de Datos
6. Modelo Relacional PostgreSQL