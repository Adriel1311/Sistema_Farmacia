# FarmaMed – Flujos de Usuario (User Flows)

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Tecnología:** Java MVC + PostgreSQL + Angular Material  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Este documento describe los flujos principales de interacción entre los usuarios y el sistema FarmaMed.

Los flujos representan los procesos operativos reales del negocio y sirven como base para:

- Casos de Uso.
- Especificaciones OpenSpec.
- Diseño de Pantallas.
- Modelo de Datos.
- Modelado de Procesos.
- Desarrollo Backend y Frontend.

---

# 2. Convenciones

## Participantes

- JF = Jefe de Farmacia
- TF = Técnico de Farmacia
- LOG = Logística
- ATI = Administrador TI
- SYS = Sistema FarmaMed

---

# F01 – Inicio de Sesión

## Actor Principal

Todos los usuarios.

## Flujo Principal

```text
Usuario
↓
Ingresa usuario y contraseña
↓
SYS valida credenciales
↓
¿Credenciales válidas?
├─ NO → Mostrar error
└─ SI
      ↓
      SYS identifica rol
      ↓
      SYS carga permisos
      ↓
      Mostrar Dashboard
      ↓
      Fin
```

---

# F02 – Registrar Medicamento

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Ingresa al módulo Medicamentos
↓
Selecciona Nuevo Medicamento
↓
Ingresa información requerida
↓
Guardar
↓
SYS valida datos
↓
¿Datos válidos?
├─ NO → Mostrar errores
└─ SI
      ↓
      Registrar medicamento
      ↓
      Registrar auditoría
      ↓
      Mostrar confirmación
      ↓
      Fin
```

---

# F03 – Registrar Ingreso de Lote

## Actor Principal

Técnico de Farmacia
Jefe de Farmacia

## Flujo Principal

```text
TF
↓
Ingresar al módulo Inventario
↓
Seleccionar Nuevo Lote
↓
Seleccionar Medicamento
↓
Registrar Número de Lote
↓
Registrar Cantidad
↓
Registrar Fecha de Ingreso
↓
Registrar Fecha de Vencimiento
↓
Guardar
↓
SYS valida reglas RN01
↓
¿Datos válidos?
├─ NO → Mostrar error
└─ SI
      ↓
      Registrar lote
      ↓
      Calcular semáforo
      ↓
      Actualizar inventario
      ↓
      Registrar auditoría
      ↓
      Fin
```

---

# F04 – Consultar Inventario

## Actor Principal

Jefe de Farmacia
Técnico de Farmacia

## Flujo Principal

```text
Usuario
↓
Ingresar a Inventario
↓
Seleccionar filtros
↓
Buscar
↓
SYS consulta lotes
↓
Mostrar resultados
↓
Mostrar semáforo
↓
Fin
```

---

# F05 – Registrar Dispensación

## Actor Principal

Técnico de Farmacia
Jefe de Farmacia (excepción FEFO)

## Nota

La receta se registra una vez en F05a/CU06 y se referencia 1:1. No se duplica por dispensación.

## Flujo Principal

```text
TF
↓
Ingresar a Dispensación
↓
Ingresar número de receta
↓
SYS valida receta
↓
Buscar medicamento
↓
Ingresar cantidad
↓
SYS ejecuta FEFO
↓
Seleccionar lote válido
↓
Validar stock
↓
Registrar dispensación
↓
Actualizar inventario
↓
Registrar auditoría
↓
Generar ticket interno
↓
Actualizar alertas
↓
Fin
```

---

# F06 – Intento de Dispensación con Stock Insuficiente

## Actor Principal

Técnico de Farmacia

## Flujo Alternativo

```text
TF solicita cantidad
↓
SYS verifica stock
↓
¿Stock suficiente?
├─ NO
│    ↓
│    Mostrar mensaje
│    "Stock insuficiente"
│    ↓
│    Cancelar operación
│
└─ SI
     Continuar flujo normal
```

---

# F07 – Intento de Dispensación con Lote Vencido

## Actor Principal

Técnico de Farmacia

## Flujo Alternativo

```text
SYS detecta lote vencido
↓
Bloquear dispensación
↓
Generar alerta crítica
↓
Mostrar mensaje
↓
Aplicar lote FEFO válido
↓
Continuar si existe stock
```

---

# F08 – Registrar Baja por Caducidad

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Consultar lotes vencidos
↓
Seleccionar lote
↓
Seleccionar motivo
↓
Confirmar baja
↓
SYS registra baja
↓
Actualizar existencia
↓
Registrar auditoría
↓
Mostrar confirmación
↓
Fin
```

---

# F09 – Registrar Baja por Merma

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Seleccionar lote
↓
Registrar cantidad
↓
Seleccionar motivo MERMA
↓
Confirmar
↓
SYS valida cantidad
↓
Actualizar existencia
↓
Registrar auditoría
↓
Fin
```

---

# F10 – Generación Automática de Alertas

## Actor Principal

Sistema

## Flujo Principal

```text
07:00 AM
↓
SYS revisa lotes
↓
SYS revisa vencimientos
↓
SYS revisa stock
↓
Identifica alertas
↓
Actualizar dashboard
↓
Actualizar campanita
↓
Fin
```

## Eventos Adicionales

```text
Ingreso de lote
↓
Recalcular alertas

Dispensación
↓
Recalcular alertas

Baja
↓
Recalcular alertas
```

---

# F11 – Atender Alerta

## Actor Principal

Jefe de Farmacia
Logística

## Flujo Principal

```text
Usuario
↓
Abrir Alertas
↓
Seleccionar alerta
↓
Ver detalle
↓
Ejecutar acción
↓
Marcar atendida
↓
Registrar auditoría
↓
Fin
```

---

# F12 – Generar Pedido Sugerido

## Actor Principal

Sistema

## Flujo Principal

```text
SYS detecta stock crítico
↓
Consultar stock máximo
↓
Calcular faltante
↓
Consultar proveedor habitual
↓
Generar sugerencia
↓
Mostrar alerta
↓
Fin
```

---

# F13 – Revisar Pedido Sugerido

## Actor Principal

Logística

## Flujo Principal

```text
LOG
↓
Abrir pedidos sugeridos
↓
Seleccionar pedido
↓
Revisar cantidad
↓
Modificar si corresponde
↓
Guardar
↓
Enviar para aprobación
↓
Fin
```

---

# F14 – Aprobar Orden de Compra

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Abrir orden
↓
Revisar información
↓
¿Aprueba?
├─ NO
│    ↓
│    Rechazar
│    ↓
│    Registrar motivo
│
└─ SI
      ↓
      Aprobar orden
      ↓
      Registrar auditoría
      ↓
      Fin
```

---

# F15 – Gestionar Proveedores

## Actor Principal

Logística

## Flujo Principal

```text
LOG
↓
Ingresar a Proveedores
↓
Crear o editar proveedor
↓
Guardar cambios
↓
SYS valida información
↓
Registrar proveedor
↓
Registrar auditoría
↓
Fin
```

---

# F16 – Generar Reporte de Consumo

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Ingresar a Reportes
↓
Seleccionar Consumo
↓
Seleccionar rango de fechas
↓
Generar reporte
↓
SYS procesa datos
↓
Mostrar resultado
↓
Exportar PDF o Excel
↓
Fin
```

---

# F17 – Generar Reporte de Bajas

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Seleccionar Reporte de Bajas
↓
Seleccionar período
↓
Generar reporte
↓
Mostrar resultado
↓
Exportar
↓
Fin
```

---

# F18 – Crear Usuario

## Actor Principal

Administrador TI

## Flujo Principal

```text
ATI
↓
Ingresar a Usuarios
↓
Seleccionar Crear Usuario
↓
Registrar datos
↓
Asignar rol
↓
Guardar
↓
SYS valida información
↓
Crear usuario
↓
Registrar auditoría
↓
Fin
```

---

# F19 – Modificar Usuario

## Actor Principal

Administrador TI

## Flujo Principal

```text
ATI
↓
Buscar usuario
↓
Editar información
↓
Guardar cambios
↓
Registrar auditoría
↓
Fin
```

---

# F20 – Configurar Parámetros del Sistema

## Actor Principal

Jefe de Farmacia

## Flujo Principal

```text
JF
↓
Ingresar a Configuración
↓
Modificar parámetros
↓
Guardar
↓
SYS valida parámetros
↓
Actualizar configuración
↓
Registrar auditoría
↓
Aplicar cambios
↓
Fin
```

---

# 3. Relación Flujo → Módulo

| Flujo | Módulo |
|---------|---------|
| F01 | Seguridad |
| F02 | Medicamentos |
| F03 | Inventario y Lotes |
| F04 | Inventario y Lotes |
| F05 | Dispensación |
| F06 | Dispensación |
| F07 | Dispensación |
| F08 | Inventario y Lotes |
| F09 | Inventario y Lotes |
| F10 | Alertas |
| F11 | Alertas |
| F12 | Alertas |
| F13 | Órdenes de Compra |
| F14 | Órdenes de Compra |
| F15 | Proveedores |
| F16 | Reportes |
| F17 | Reportes |
| F18 | Usuarios |
| F19 | Usuarios |
| F20 | Configuración |

---

# 4. Criterio de Cierre

Los User Flows se consideran completos cuando:

- Todos los módulos tienen al menos un flujo principal.
- Todos los roles participan en procesos definidos.
- Todas las reglas de negocio tienen representación operativa.
- Todos los RF están cubiertos por uno o más flujos.
- Existe trazabilidad entre RN → RF → Flujos.

Una vez aprobado este documento, el siguiente paso será:

1. Casos de Uso (use-cases.md)
2. Especificaciones OpenSpec
3. Modelo Entidad-Relación (DER)
4. Diccionario de Datos
5. Modelo Relacional PostgreSQL
6. Implementación del Sistema FarmaMed