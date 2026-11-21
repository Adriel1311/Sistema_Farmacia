# FarmaMed – Requerimientos No Funcionales (RNF)

**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1)  
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación  
**Tecnología:** Java MVC + PostgreSQL + Angular Material  
**Versión:** 1.0  
**Autor:** Francisco Gabriel Berrocal Ramos

---

# 1. Introducción

Los Requerimientos No Funcionales (RNF) describen las características de calidad que debe cumplir FarmaMed.

Estos requisitos no definen funcionalidades del sistema, sino condiciones técnicas, operativas y de calidad necesarias para garantizar su correcto funcionamiento.

---

# 2. Rendimiento

## RNF-01 – Tiempo de respuesta en dispensación

### Descripción

El sistema debe completar una operación de dispensación en un tiempo máximo de 2 segundos bajo condiciones normales de operación.

### Prioridad

Alta

### Justificación

La atención farmacéutica debe realizarse sin retrasos para no afectar la atención de pacientes.

---

## RNF-02 – Usuarios concurrentes

### Descripción

El sistema debe soportar al menos 20 usuarios concurrentes realizando operaciones simultáneamente.

### Prioridad

Alta

### Justificación

Garantiza el funcionamiento en las áreas de farmacia, logística y supervisión.

---

## RNF-03 – Carga de dashboard

### Descripción

El dashboard principal debe mostrarse en menos de 3 segundos.

### Prioridad

Media

---

## RNF-04 – Carga de inventario

### Descripción

Las consultas de inventario deben mostrarse en menos de 3 segundos para listados de hasta 10 000 registros.

### Prioridad

Media

---

# 3. Seguridad

## RNF-05 – Autenticación obligatoria

### Descripción

Todo usuario debe autenticarse mediante credenciales válidas antes de acceder al sistema.

### Prioridad

Alta

---

## RNF-06 – Control de acceso por roles

### Descripción

El acceso a funcionalidades debe estar restringido según el rol asignado al usuario.

### Roles

- Jefe de Farmacia
- Técnico de Farmacia
- Logística
- Administrador TI

> Nota APF1: rol Evaluador/Docente eliminado. Para demo usar usuario Jefe solo-lectura.

### Prioridad

Alta

---

## RNF-07 – Protección de sesiones

### Descripción

Las sesiones inactivas deben cerrarse automáticamente después de 30 minutos sin actividad.

### Prioridad

Media

---

## RNF-08 – Contraseñas seguras

### Descripción

Las contraseñas deben almacenarse cifradas y nunca en texto plano.

### Prioridad

Alta

---

# 4. Auditoría y Trazabilidad

## RNF-09 – Auditoría obligatoria

### Descripción

Toda operación crítica debe dejar evidencia de auditoría.

### Datos mínimos

- Usuario
- Acción realizada
- Fecha
- Hora
- Entidad afectada

### Prioridad

Alta

---

## RNF-10 – Conservación del historial

### Descripción

La información histórica no debe eliminarse físicamente de la base de datos.

### Aplica a

- Medicamentos
- Usuarios
- Proveedores
- Órdenes
- Dispensaciones
- Bajas

### Prioridad

Alta

---

# 5. Disponibilidad y Respaldo

## RNF-11 – Respaldo diario

### Descripción

La base de datos debe respaldarse automáticamente una vez por día.

### Prioridad

Alta

---

## RNF-12 – Recuperación ante fallos

### Descripción

El sistema debe permitir restaurar la información mediante respaldos autorizados.

### Prioridad

Media

---

# 6. Integridad de Datos

## RNF-13 – Transacciones ACID

### Descripción

Todas las operaciones que modifiquen inventario deben ejecutarse dentro de transacciones ACID.

### Prioridad

Alta

### Aplica a

- Ingresos
- Dispensaciones
- Bajas
- Ajustes

---

## RNF-14 – Consistencia de stock

### Descripción

El sistema no debe permitir valores negativos de inventario.

### Prioridad

Alta

---

## RNF-15 – Integridad referencial

### Descripción

La base de datos debe garantizar la integridad de relaciones mediante claves foráneas.

### Prioridad

Alta

---

## RNF-16 – Validación de datos obligatoria

### Descripción

Todos los datos ingresados deben validarse antes de almacenarse.

### Ejemplos

- Fechas válidas.
- Cantidades positivas.
- Campos obligatorios.
- Identificadores únicos.

### Prioridad

Alta

---

# 7. Base de Datos

## RNF-17 – PostgreSQL como SGBD oficial

### Descripción

La base de datos oficial del proyecto será PostgreSQL.

### Prioridad

Alta

---

## RNF-18 – Índices de consulta

### Descripción

La base de datos debe contar con índices para optimizar consultas frecuentes.

### Índices mínimos

- medicamento
- lote
- fecha_vencimiento
- receta
- dispensacion

### Prioridad

Media

---

## RNF-19 – Optimización FEFO

### Descripción

La búsqueda del lote FEFO debe ejecutarse utilizando estructuras de consulta optimizadas.

### Prioridad

Alta

---

# 8. Usabilidad

## RNF-20 – Interfaz intuitiva

### Descripción

La interfaz debe ser simple y comprensible para personal no técnico.

### Prioridad

Alta

---

## RNF-21 – Consistencia visual

### Descripción

Todas las pantallas deben mantener una apariencia uniforme.

### Prioridad

Media

---

## RNF-22 – Uso de Angular Material

### Descripción

La interfaz debe utilizar Angular Material como biblioteca principal de componentes.

### Prioridad

Media

---

## RNF-23 – Navegación clara

### Descripción

Las funcionalidades principales deben estar accesibles en un máximo de tres clics.

### Prioridad

Media

---

# 9. Compatibilidad

## RNF-24 – Compatibilidad con navegadores modernos

### Descripción

El sistema debe funcionar correctamente en:

- Google Chrome
- Microsoft Edge
- Mozilla Firefox

### Prioridad

Media

---

## RNF-25 – Diseño responsive

### Descripción

Las pantallas deben adaptarse correctamente a diferentes resoluciones de escritorio y portátiles.

### Prioridad

Media

---

# 10. Mantenibilidad

## RNF-26 – Arquitectura MVC

### Descripción

El backend debe seguir el patrón Modelo Vista Controlador (MVC).

### Prioridad

Alta

---

## RNF-27 – Código documentado

### Descripción

Las clases, métodos y componentes principales deben incluir documentación técnica.

### Prioridad

Media

---

## RNF-28 – Separación por capas

### Descripción

La aplicación debe separar responsabilidades mediante capas independientes.

### Capas mínimas

- Presentación
- Negocio
- Persistencia

### Prioridad

Alta

---

# 11. Escalabilidad

## RNF-29 – Preparación para Fase 2

### Descripción

La arquitectura debe permitir futuras integraciones sin rediseñar completamente el sistema.

### Futuras integraciones

- HIS
- SUNAT
- Correo electrónico
- Aplicación móvil
- Multi sede

### Prioridad

Media

---

## RNF-30 – Configuración parametrizable

### Descripción

Los parámetros críticos deben almacenarse en configuración y no estar codificados en el sistema.

### Ejemplos

- Semáforo de vencimiento.
- Tiempo de sesión.
- Límites de stock.
- Parámetros de alertas.

### Prioridad

Alta

---

# 12. Matriz de Trazabilidad RN → RNF

| RN | RNF |
|------|------|
| RN01 | RNF-13, RNF-16 |
| RN02 | RNF-19 |
| RN03 | RNF-14, RNF-16 |
| RN04 | RNF-30 |
| RN05 | RNF-03, RNF-20 |
| RN06 | RNF-09, RNF-16 |
| RN06b | RNF-10 |
| RN07 | RNF-09 |
| RN08 | RNF-05, RNF-06 |
| RN09 | RNF-01, RNF-02, RNF-11, RNF-13, RNF-14, RNF-17, RNF-18 |
| RN10 | RNF-03, RNF-04 |

---

# 13. Criterio de Cierre

Los Requerimientos No Funcionales se consideran completos cuando:

- Se definen criterios de rendimiento.
- Se definen criterios de seguridad.
- Se establecen mecanismos de auditoría.
- Se especifican requisitos de respaldo.
- Se documentan reglas de integridad.
- Se establecen restricciones tecnológicas.
- Se documentan requisitos de usabilidad.
- Se define la mantenibilidad del sistema.
- Se considera la escalabilidad futura.

Una vez aprobados los RNF, el siguiente paso será:

1. Roles y Permisos.
2. Módulos del Sistema.
3. Flujos de Usuario.
4. Casos de Uso.
5. Especificaciones OpenSpec.
6. Modelo Entidad Relación (DER).
7. Diccionario de Datos.
8. Modelo Relacional PostgreSQL.
9. Implementación del Sistema FarmaMed.

---

# 14. Historial

- v1.0: Primera versión formal de Requerimientos No Funcionales derivada de las Reglas de Negocio v0.4.