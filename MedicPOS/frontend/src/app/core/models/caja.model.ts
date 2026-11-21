/* ============================================
   M11 CAJA
   Apertura con monto inicial, ventas por boleta, salidas (retiros) y cierre con arqueo.
   ============================================ */

export enum CajaEstado {
  ABIERTA = 'ABIERTA',
  CERRADA = 'CERRADA'
}

export enum TipoMovimientoCaja {
  APERTURA = 'APERTURA',
  VENTA = 'VENTA',
  SALIDA = 'SALIDA',
  DEVOLUCION = 'DEVOLUCION'
}

export interface Caja {
  id: number;
  estado: CajaEstado;
  montoInicial: number;
  fechaApertura: string;
  usuarioApertura: string | null;
  observacionApertura: string | null;
  totalVentas: number;
  cantidadVentas: number;
  totalDevoluciones: number;
  totalSalidas: number;
  /** Ventas menos devoluciones. */
  totalRecaudado: number;
  /** Efectivo que debe haber en caja: inicial + recaudado - salidas. */
  saldoActual: number;
  fechaCierre: string | null;
  usuarioCierre: string | null;
  montoEsperado: number | null;
  montoContado: number | null;
  diferencia: number | null;
  observacionCierre: string | null;
}

export interface ItemVentaCaja {
  medicamentoNombre: string;
  numeroLote: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface MovimientoCaja {
  id: number;
  cajaId: number;
  tipo: TipoMovimientoCaja;
  /** Siempre positivo; esIngreso indica si suma o resta. */
  monto: number;
  esIngreso: boolean;
  concepto: string;
  fechaHora: string;
  usuario: string | null;
  dispensacionId: number | null;
  numeroTicket: string | null;
  numeroReceta: string | null;
  pacienteNombreCompleto: string | null;
  pacienteDni: string | null;
  items: ItemVentaCaja[] | null;
}

export interface AbrirCajaRequest {
  montoInicial: number;
  observacion?: string;
}

export interface SalidaCajaRequest {
  monto: number;
  concepto: string;
}

export interface CerrarCajaRequest {
  montoContado: number;
  observacion?: string;
}
