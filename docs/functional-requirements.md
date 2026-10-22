# FarmaMed – Requerimientos Funcionales (RF)

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Este documento define los Requerimientos Funcionales (RF) de FarmaMed.

Los requerimientos funcionales describen las capacidades que debe proporcionar el sistema para cumplir las Reglas de Negocio (RN) definidas previamente.

Cada RF mantiene trazabilidad directa con una o más RN.

---

# 2. Gestión de Medicamentos

## RF-01 – Gestionar Medicamentos

### Descripción

El sistema debe permitir registrar, consultar, modificar y desactivar medicamentos.

### Entradas

- Código interno
- Nombre
- Descripción
- Unidad de medida
- Stock mínimo
- Stock máximo
- Proveedor habitual

### Salidas

- Medicamento registrado
- Medicamento actualizado
- Catálogo de medicamentos

### Reglas Asociadas

- RN01
- RN06
- RN07

### Prioridad

Alta

---

# 3. Control de Vencimiento

## RF-02 – Calcular Estado de Vencimiento

### Descripción

El sistema debe calcular automáticamente el estado de vencimiento de cada lote según los parámetros configurados.

### Estados

- Verde
- Ámbar
- Rojo
- Negro

### Reglas Asociadas

- RN04

### Prioridad

Alta

---

## RF-03 – Mostrar Semáforo de Vencimiento

### Descripción

El sistema debe visualizar el estado de vencimiento mediante colores en todas las consultas relacionadas con inventario.

### Reglas Asociadas

- RN04

### Prioridad

Alta

---

# 4. Gestión de Lotes

## RF-04 – Aplicar FEFO

### Descripción

El sistema debe seleccionar automáticamente el lote vigente cuya fecha de vencimiento sea la más próxima.

### Restricciones

- El Técnico de Farmacia no puede elegir otro lote.
- El Jefe de Farmacia puede realizar una excepción registrando el motivo correspondiente.
- Toda excepción debe auditarse.

### Reglas Asociadas

- RN02

### Prioridad

Alta

---

## RF-05 – Registrar Ingreso de Lotes

### Descripción

El sistema debe permitir registrar ingresos de medicamentos mediante lotes.

### Datos mínimos

- Medicamento
- Número de lote
- Fecha de ingreso
- Fecha de vencimiento
- Cantidad
- Usuario responsable

### Validaciones

- Cantidad mayor a cero.
- Fecha de vencimiento posterior a fecha de ingreso.
- Lote único.

### Reglas Asociadas

- RN01
- RN01b
- RN01c

### Prioridad

Alta

---

# 5. Dispensación

## RF-05A – Registrar Receta de Paciente Asegurado

### Descripción

El sistema debe registrar recetas médicas correspondientes a pacientes asegurados.

### Datos mínimos

- Número de receta
- DNI del paciente
- Médico responsable
- Fecha de receta

### Reglas Asociadas

- RN05.0

### Prioridad

Alta

---

## RF-05B – Registrar Receta de Paciente Particular

### Descripción

El sistema debe registrar recetas médicas correspondientes a pacientes particulares.

### Salida adicional

- Ticket interno de atención.

### Reglas Asociadas

- RN05.0

### Prioridad

Alta

---

## RF-06 – Registrar Dispensación

### Descripción

El sistema debe registrar la entrega de medicamentos asociada a una receta médica.

### Datos mínimos

- Receta
- Paciente
- Medicamentos
- Cantidades
- Usuario responsable

### Acciones automáticas

- Aplicar FEFO.
- Actualizar stock.
- Evaluar alertas.

### Reglas Asociadas

- RN05.0
- RN08
- RN09

### Prioridad

Alta

---

## RF-07 – Bloquear Venta de Lotes Vencidos

### Descripción

El sistema debe impedir la dispensación de medicamentos provenientes de lotes vencidos.

### Comportamiento

- Bloquear operación.
- Mostrar mensaje informativo.
- Generar alerta correspondiente.

### Reglas Asociadas

- RN03

### Prioridad

Alta

---

# 6. Gestión de Alertas

## RF-08 – Generar Alertas Operativas

### Descripción

El sistema debe generar alertas dinámicas relacionadas con vencimientos y stock.

### Alertas

- Crítico 30 días.
- Próximo a vencer 60 días.
- Stock crítico.

### Reglas Asociadas

- RN05
- RN06

### Prioridad

Alta

---

## RF-09 – Generar Pedido Sugerido

### Descripción

El sistema debe generar automáticamente sugerencias de abastecimiento cuando se detecte stock crítico.

### Información generada

- Medicamento.
- Stock actual.
- Stock máximo.
- Cantidad sugerida.
- Proveedor habitual.

### Reglas Asociadas

- RN05
- RN06
- RN07

### Prioridad

Alta

---

# 7. Compras y Proveedores

## RF-10 – Gestionar Proveedores

### Descripción

El sistema debe permitir registrar, consultar, modificar y desactivar proveedores.

### Datos mínimos

- Razón social.
- RUC.
- Teléfono.
- Correo.
- Estado.

### Reglas Asociadas

- RN07

### Prioridad

Media

---

## RF-10A – Gestionar Órdenes de Compra

### Descripción

El sistema debe permitir crear, revisar, aprobar y consultar órdenes de compra.

### Estados

- Borrador.
- Sugerida.
- Aprobada.
- Rechazada.

### Reglas Asociadas

- RN07

### Prioridad

Media

---

# 8. Seguridad

## RF-11 – Gestionar Usuarios y Roles

### Descripción

El sistema debe permitir administrar usuarios y permisos según rol.

### Operaciones

- Crear usuario.
- Modificar usuario.
- Desactivar usuario.
- Asignar rol.

### Roles

- Jefe de Farmacia.
- Técnico de Farmacia.
- Logística.
- Administrador TI.

> Nota APF1: rol Evaluador/Docente eliminado. Para demo usar usuario Jefe solo-lectura.

### Reglas Asociadas

- RN08

### Prioridad

Alta

---

# 9. Reportes

## RF-12 – Generar Reportes de Gestión

### Descripción

El sistema debe generar reportes operativos para apoyar la toma de decisiones.

### Reportes mínimos

- Consumo mensual por medicamento.
- Bajas por caducidad.
- Inventario actual.

### Exportación

- PDF.
- Excel.

### Reglas Asociadas

- RN10

### Prioridad

Media

---

## RF-13 – Mostrar Dashboard Operativo

### Descripción

El sistema debe mostrar información consolidada en tiempo real.

### Indicadores mínimos

- Lotes críticos.
- Próximos a vencer.
- Stock crítico.
- Consumo reciente.
- Órdenes sugeridas.

### Reglas Asociadas

- RN05
- RN10

### Prioridad

Alta

---

# 10. Gestión de Bajas

## RF-14 – Registrar Baja de Inventario

### Descripción

El sistema debe registrar bajas de inventario sin eliminar información histórica.

### Motivos permitidos

- VENCIDO_EN_ESTANTE
- MERMA
- DEVUELTO

### Acciones

- Registrar responsable.
- Registrar fecha.
- Actualizar stock.

### Reglas Asociadas

- RN06b

### Prioridad

Alta

---

# 11. Auditoría

## RF-15 – Registrar Auditoría de Operaciones

### Descripción

El sistema debe registrar todas las operaciones críticas realizadas por los usuarios.

### Datos registrados

- Usuario.
- Acción.
- Entidad afectada.
- Fecha.
- Hora.
- Valor anterior.
- Valor nuevo.

### Eventos auditables

- Registro de medicamentos.
- Modificación de medicamentos.
- Ingreso de lotes.
- Dispensaciones.
- Bajas.
- Configuraciones.
- Usuarios.
- Proveedores.
- Órdenes de compra.
- Excepciones FEFO.

### Reglas Asociadas

- RN08
- RN09

### Prioridad

Alta

---

# 12. Matriz de Trazabilidad RN → RF

| RN | RF |
|------|------|
| RN01 | RF-01, RF-05 |
| RN01b | RF-05 |
| RN01c | RF-05 |
| RN02 | RF-04 |
| RN03 | RF-07 |
| RN04 | RF-02, RF-03 |
| RN05 | RF-08, RF-09, RF-13 |
| RN05.0 | RF-05A, RF-05B, RF-06 |
| RN06 | RF-01, RF-08, RF-09 |
| RN06b | RF-14 |
| RN07 | RF-09, RF-10, RF-10A |
| RN08 | RF-11, RF-15 |
| RN09 | RF-06, RF-15 |
| RN10 | RF-12, RF-13 |

---

# 13. Criterio de Cierre

Los requerimientos funcionales se consideran completos cuando:

- Todo RF tiene una RN de origen.
- Toda RN genera al menos un RF.
- Todos los actores poseen funcionalidades asociadas.
- Todos los módulos definidos poseen requerimientos identificados.
- Existe trazabilidad completa RN → RF.

