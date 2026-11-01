/* ============================================
   M05 DISPENSACION (caja)
   Reglas: RN02 lote que vence primero, RN03 nunca vencido, RN05.0 una receta una dispensacion.
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
  excepcionLoteDistinto: boolean;
  motivoExcepcionLoteDistinto: string | null;
}

export enum EstadoCobro {
  PENDIENTE = 'PENDIENTE',
  PAGADO = 'PAGADO',
  ANULADO = 'ANULADO'
}

export enum MetodoPago {
  EFECTIVO = 'EFECTIVO',
  TARJETA = 'TARJETA',
  TRANSFERENCIA = 'TRANSFERENCIA',
  MIXTO = 'MIXTO'
}

export interface Cobro {
  id: number;
  dispensacionId: number;
  numeroTicket: string;
  tipoReceta: string;
  pacienteNombre: string;
  subtotal: number;
  descuentoPorcentaje: number;
  descuentoMonto: number;
  total: number;
  estado: EstadoCobro;
  metodoPago: MetodoPago | null;
  observacion: string | null;
  pagadoEn: string | null;
  pagadoPorNombre: string | null;
  createdAt: string;
}

export interface CobroResponse {
  id: number;
  subtotal: number;
  descuentoPorcentaje: number;
  descuentoMonto: number;
  total: number;
  estado: EstadoCobro;
}

export interface PagarCobroRequest {
  metodoPago: 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA' | 'MIXTO';
  observacion: string;
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
  cobro?: CobroResponse;
}

/** Item del formulario de caja, con la seleccion de lote ya resuelta en el cliente. */
export interface DispensacionFormItem {
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  unidadMedida: string;
  lotesDisponibles: Lote[];
  loteSeleccionadoId: number | null;
  loteForzado: boolean;
  cantidad: number;
  excepcionLoteDistinto: boolean;
  motivoExcepcionLoteDistinto: string;
  error?: string;
}
