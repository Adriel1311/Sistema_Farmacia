/* ============================================
   M05 DISPENSACION (caja)
   Reglas: RN02 FEFO, RN03 nunca vencido, RN05.0 una receta una dispensacion.
   ============================================ */

import { Lote } from './inventario.model';

export enum DispensacionEstado {
  COMPLETADA = 'COMPLETADA',
  ANULADA = 'ANULADA'
}

export interface DispensacionItemRequest {
  medicamentoId: number;
  cantidad: number;
  loteId?: number | null;
  motivoExcepcionFefo?: string | null;
}

export interface CreateDispensacionRequest {
  recetaId: number;
  items: DispensacionItemRequest[];
}

export interface DispensacionDetalle {
  id: number;
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  unidadMedida: string;
  loteId: number;
  numeroLote: string;
  fechaVencimiento: string;
  estadoVencimiento: string;
  cantidad: number;
  excepcionFefo: boolean;
  motivoExcepcionFefo: string | null;
}

export interface Dispensacion {
  id: number;
  numeroTicket: string;
  estado: DispensacionEstado;
  fechaHora: string;
  recetaId: number;
  numeroReceta: string;
  medico: string;
  tipoReceta: string;
  fechaReceta: string;
  pacienteDni: string;
  pacienteNombreCompleto: string;
  usuarioId: number;
  usuarioNombre: string;
  usuarioRol: string;
  detalles: DispensacionDetalle[];
  totalItems: number;
  totalUnidades: number;
  createdAt: string;
}

/** Item del formulario de caja, con la seleccion de lote ya resuelta en el cliente. */
export interface DispensacionFormItem {
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  unidadMedida: string;
  lotesDisponibles: Lote[];
  loteSeleccionadoId: number | null;
  cantidad: number;
  excepcionFefo: boolean;
  motivoExcepcionFefo: string;
  error?: string;
}
