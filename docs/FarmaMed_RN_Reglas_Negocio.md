# FarmaMed – Reglas de Negocio (RN)
**Empresa:** Hospital Santa María del Socorro – Ica (Cat. II-1) – validado
**Proyecto:** Sistema de Gestión Hospitalaria para Control de Caducidad, Inventario y Dispensación
**Tecnología:** Java MVC + PostgreSQL + Angular Material – validado PostgreSQL como BD definitiva
**Versión:** v0.4 – 27/09/2026 – Cierre formal sin cambiar lo validado (30/60, solo pantalla, Socorro, PostgreSQL)
**Autor:** Berrocal Ramos Francisco Gabriel (u24221371)

> Este archivo es la fuente de verdad. Todo lo que hace el sistema (requerimientos, pantallas y base de datos) nace de aquí. Si cambia una regla, se actualiza su requerimiento.

---

## 1. ¿Qué problema resolvemos y qué incluye?

**Problema en simple:** en guardia y quirófano a veces faltan medicamentos, se pierden medicinas porque vencen sin que nadie avise, y hay errores al recetar y entregar. Como antecedente, en 2023 el 75.93% de recetas no cumplía las buenas prácticas de prescripción (UNICA).

**Lo que SÍ hace el sistema en esta entrega (APF1) – 5 módulos:**
1. **Inventario por lotes:** registrar cada ingreso con su vencimiento.
2. **Venta / Dispensación (POS):** entregar siempre lo más próximo a vencer primero.
3. **Avisos en pantalla + pedidos sugeridos:** campanita que avisa y sugiere qué comprar.
4. **Panel / Reportes:** ver de un vistazo qué está crítico y qué se consumió.
5. **Seguridad y control:** quién hizo qué, con roles.

**Lo que NO hace en esta entrega (queda para Fase 2):** conexión con otros sistemas de historia clínica (HIS), facturación electrónica SUNAT, app de celular, varias sedes, envío de correos automáticos.

---

## 2. Reglas de negocio explicadas punto por punto

### RN01 – Todo se controla por lote, nada sin lote
**En simple:** cada caja o grupo de medicinas que entra tiene un código de lote y una fecha de vencimiento. Sin esos datos, el sistema no deja registrar ni mover nada.

**Ejemplo:** ingresan 100 Paracetamol lote A-001 que vence el 10/10/2026. Queda anotado quién lo registró, cuándo y cuánto entró.

**Dato técnico:** guarda medicamento + lote único + cantidad + fecha_venc + usuario + fecha_hora.
→ Deriva a: RF-01, RF-05, RNF-01.

### RN01b – Un lote es de un solo medicamento y no se repite
**En simple:** un código de lote solo puede pertenecer a un medicamento y no se repite para ese medicamento.

**Ejemplo:** Paracetamol lote A-001 es único. No puede existir otro Paracetamol lote A-001 ni Amoxicilina lote A-001.

### RN01c – Todo lote entra válido y en positivo
**En simple:** todo lote se registra con número, vencimiento posterior al ingreso y cantidad mayor a cero, si no, se rechaza.

### RN02 – Siempre sale primero lo que vence primero (FEFO)
**En simple:** cuando se atiende en farmacia, el sistema solo elige el lote vigente que vence más pronto. El trabajador no escoge a mano para no equivocarse.

**Ejemplo:** hay lote A-001 que vence en octubre y lote A-002 que vence en diciembre. El sistema entrega automáticamente del A-001.

Solo el Jefe de Farmacia puede elegir otro lote y debe dejar el motivo registrado y auditado.
→ Deriva a: RF-04.

### RN03 – Lo vencido se bloquea, no se puede vender
**En simple:** si ya pasó la fecha de vencimiento, ese lote vale cero para la venta aunque físicamente siga en el estante. Solo vale como vigente lo no vencido y no dado de baja. La baja se registra por VENCIDO_EN_ESTANTE / MERMA / DEVUELTO con responsable.

**Ejemplo:** intentan vender un lote vencido ayer → el sistema muestra un mensaje de bloqueo y crea un aviso de “vencido bloqueado”.
→ Deriva a: RF-07.

### RN04 – Semáforo de vencimiento 30 / 60 – validado
**En simple:** cada lote tiene un color según cuántos días le faltan para vencer. Se calcula solo cada día.

* **ROJO / Crítico:** le quedan 0 a 30 días.
* **AMBAR / Próximo a vencer:** le quedan 31 a 60 días.
* **VERDE / Vigente:** le quedan más de 60 días.
* **NEGRO / Vencido:** ya venció.

**Decisión validada:** se mantienen 30 y 60 días, no se cambia a 15/45.
Solo el Jefe de Farmacia puede cambiar esos plazos. El cambio se refleja en menos de 5 minutos y queda auditado.
→ Deriva a: RF-02, RF-03, RNF-11.

### RN05 – Avisos solo en pantalla, sin correos – validado
**En simple:** no se envían correos. Todo se avisa dentro del sistema con una campanita, mensajes y el panel. Los avisos no se guardan en una tabla, se calculan al momento de abrir cada pantalla.

**Decisión validada:** solo notificador en pantalla en APF1. Correo queda para Fase 2.

#### RN05.0 – Sin receta no hay atención (1 Receta = 1 Dispensación)
**En simple:** para atender a alguien siempre se pide el número de receta, el médico y el DNI del paciente. Hay dos tipos:

* **ASEGURADO:** no se cobra, se carga al seguro.
* **PARTICULAR:** se cobra y se da un ticket interno (no es boleta SUNAT).

Sin número de receta el sistema no deja avanzar. Cada receta solo puede generar una dispensación (receta_id UNIQUE en dispensación). No se permiten dispensaciones vacías: toda dispensación debe tener al menos un detalle con cantidad > 0 y registrar usuario responsable + fecha_hora.
→ RF-05a/b, RF-06.

#### RN05.1 – Los 5 avisos que existen
1. **Vencido bloqueado – Prioridad ALTA:** se intentó vender algo vencido.
2. **Crítico 30 días – ALTA:** al lote le quedan 30 días o menos.
3. **Próximo 60 días – MEDIA:** al lote le quedan entre 31 y 60 días.
4. **Stock crítico – ALTA:** la suma de lo vigente llegó al mínimo. Ejemplo: quedan 20 y el mínimo es 100.
5. **Orden sugerida – MEDIA:** nace del anterior y ya propone proveedor y cantidad. Ejemplo: si el máximo es 200 y quedan 20, sugiere comprar 180.

#### RN05.2 – ¿Cuándo aparecen?
Se revisan cada día a las 07:00 y también justo después de cada venta o ingreso. No se duplican ni se guardan para siempre, solo se muestran.

#### RN05.3 – ¿Cómo los ve el usuario?
* Campanita arriba con el número de avisos.
* Al entrar, si hay algo grave, sale un mensaje rojo.
* Panel lateral con la lista ordenada por prioridad, con botones: Ver lote / Generar orden / Marcar atendida (marcar atendida = registro en AUDITORIA, no existe tabla ALERTA).
* Panel principal con 3 tarjetas: Críticos, Por vencer, Stock crítico + tabla de colores.
* En caja, si algo está vencido, sale ventana que bloquea y dice qué lote sí se usó.
**Nota:** los avisos no se persisten. ACTIVA/VISTA/ATENDIDA son estados lógicos de visualización, no tabla.
→ Deriva a: RF-08, RF-09, RF-13, RNF-03/04.

### RN06 – Mínimos y máximos de stock (solo Jefe)
**En simple:** cada medicina tiene un mínimo para avisar que hay que comprar y un máximo para no sobrestockearse. Solo el Jefe los configura y todo cambio queda auditado. Solo se cuenta lo vigente, lo vencido no suma.

**Ejemplo:** Paracetamol mínimo 100, quedan 20 vigentes → se genera aviso de stock crítico.
→ Deriva a: RF-08, RF-09.

### RN06b – Dar de baja es cambiar estado, no borrar
**En simple:** lo vencido o dañado no se borra, solo se marca como BAJA con su motivo y responsable, y su cantidad queda en cero. Nada se borra, medicamentos, usuarios y proveedores solo se desactivan por baja lógica.

Motivos: VENCIDO_EN_ESTANTE / MERMA / DEVUELTO.
→ RF-14.

### RN07 – El sistema sugiere el pedido, Logística decide
**En simple:** cuando falta stock, el sistema ya sugiere a quién comprarle (proveedor habitual) y cuánto. Pero la persona de Logística revisa, corrige y aprueba. No se compra solo ni se manda correo al proveedor en esta entrega.

Cada medicina tiene un proveedor habitual. El catálogo de proveedores tiene alta y baja lógica.
→ Deriva a: RF-09, RF-10.

### RN08 – Quién puede hacer qué (roles – 4 roles APF1)
* **Jefe de Farmacia:** todo, configura plazos y mínimos, atiende avisos, aprueba pedidos. Único que puede autorizar excepción FEFO.
* **Técnico de Farmacia:** registra inventario y atiende en caja, avisos solo los ve. No configura ni aprueba.
* **Logística / Compras:** maneja proveedores y pedidos, puede atender avisos de stock. No dispensa.
* **Administrador TI:** crea usuarios y respaldos, no vende en caja ni toca inventario.
* **Nota:** rol Evaluador/Docente eliminado en APF1. Para demo docente usar usuario Jefe solo-lectura.

**Venta en 3 pasos:** 1) buscar medicina, 2) poner cantidad + paciente, 3) confirmar. El lote lo pone solo el sistema.

**Auditoría:** toda operación crítica guarda quién lo hizo, qué hizo, fecha y hora.
→ Deriva a: RF-11, RNF-03/05.

### RN09 – Nadie puede dejar stock en negativo (PostgreSQL)
**En simple:** el sistema cuida que dos personas vendiendo al mismo tiempo no vendan lo mismo ni dejen números negativos. Todo lo que mueve stock se hace en una sola operación segura. Si algo falla, se cancela todo.

**Dato técnico:** transacción ACID con bloqueo FEFO, índices en (medicamento, vencimiento), soporta 20 usuarios a la vez, venta en menos de 2 segundos, respaldo diario y en cada tabla se guarda quién creó y modificó.
→ Deriva a: RNF-01/02/06/07/08/09/10.

### RN-BD – Cómo se conecta con la base de datos (resumen)
1. **Entrada:** se inserta el lote y se valida que el vencimiento sea posterior al ingreso → se pinta el semáforo.
2. **Venta:** se busca el lote FEFO, se descuenta, se registra la atención con su receta. Si falta stock, se cancela.
3. **Después:** se suma lo vigente y se compara con el mínimo para avisar.
4. **Relaciones:** Medicamento tiene muchos Lotes, 1 Receta origina 1 Dispensación (receta_id UNIQUE), Proveedor surte muchas Órdenes.

### RN10 – Reportes mínimos
**En simple:** el Jefe necesita 3 cosas al entrar:
* Qué se dio de baja por vencer en un rango de fechas (se puede exportar).
* Cuánto se consumió por medicina cada mes (para comprar mejor).
* Panel en vivo al iniciar sesión.
→ Deriva a: RF-12, RF-13.

---

## 3. Palabras que debes conocer (glosario)
* **FEFO:** sale primero lo que vence primero (First Expired First Out).
* **Lote vigente:** aún no vence y no está dado de baja.
* **Días restantes:** días que faltan desde hoy hasta el vencimiento.
* **ACTIVA / VISTA / ATENDIDA:** estados lógicos de visualización del aviso (no persisten, solo se recalculan; atendida = registro en AUDITORIA).
* **Ticket interno:** comprobante simple de farmacia, no es boleta SUNAT.
* **Baja lógica:** no se borra, solo se marca como inactivo o dado de baja.

## 4. Validaciones con docente – cerradas el 13/09/2026
* [x] Umbrales 30/60 días – **validado, se mantiene 30/60.**
* [x] Solo notificador en pantalla – **validado, correo pasa a Fase 2.**
* [x] Empresa Socorro – **validado, Hospital Santa María del Socorro.**
* [x] Base de datos PostgreSQL – **validado, se descarta SQL Server. Actualizar RNF-15/16 en Word.**

## 5. Historial
* v0.1 04/09/2026: creación inicial desde contexto SACIC + Capítulo1 + rúbrica APF1.
* v0.2 04/09/2026: detalle según Word + semáforo 30/60 PostgreSQL.
* v0.3 13/09/2026: validación docente (30/60, solo pantalla, Socorro, PostgreSQL) + redacción simple para personas.
