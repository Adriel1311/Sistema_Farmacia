/* ============================================
   M03 INVENTARIO: LOTES, MOVIMIENTOS Y BAJAS
   Reglas: RN01 todo ingreso es por lote, RN03 nada vencido se dispensa,
   RN04 semaforo 30/60 dias.
   ============================================ */

export enum LoteEstado {
  ACTIVO = 'ACTIVO',
  VENCIDO = 'VENCIDO',
  BAJA = 'BAJA'
}

export enum EstadoVencimiento {
  VERDE = 'VERDE',
  AMBAR = 'AMBAR',
  ROJO = 'ROJO',
  NEGRO = 'NEGRO'
}

export enum BajaMotivo {
  VENCIDO_EN_ESTANTE = 'VENCIDO_EN_ESTANTE',
  MERMA = 'MERMA',
  DEVUELTO = 'DEVUELTO'
}

export enum TipoMovimiento {
  ENTRADA = 'ENTRADA',
  SALIDA = 'SALIDA',
  BAJA = 'BAJA',
  AJUSTE = 'AJUSTE'
}

export interface Lote {
  id: number;
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  unidadMedida: string;
  proveedorRazonSocial: string;
  numeroLote: string;
  fechaIngreso: string;
  fechaVencimiento: string;
  cantidadInicial: number;
  cantidadActual: number;
  estado: LoteEstado;
  estadoVencimiento: EstadoVencimiento;
  diasRestantes: number;
  createdAt: string;
  updatedAt: string;
}

export interface RegistrarLoteRequest {
  medicamentoId: number;
  numeroLote: string;
  fechaIngreso: string;
  fechaVencimiento: string;
  cantidad: number;
  ordenCompraId?: number | null;
}

export interface MovimientoInventario {
  id: number;
  tipoMovimiento: TipoMovimiento;
  cantidad: number;
  stockAnterior: number;
  stockNuevo: number;
  referenciaTipo: string;
  referenciaId: number;
  fechaHora: string;
  medicamentoId: number;
  medicamentoNombre: string;
  loteId: number;
  numeroLote: string;
  usuarioNombre: string;
}

export interface BajaInventario {
  id: number;
  motivo: BajaMotivo;
  cantidad: number;
  observacion: string;
  fechaHora: string;
  loteId: number;
  numeroLote: string;
  fechaVencimientoLote: string;
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  usuarioId: number;
  usuarioNombre: string;
  usuarioRol: string;
}

export interface RegistrarBajaRequest {
  loteId: number;
  motivo: BajaMotivo;
  cantidad: number;
  observacion: string;
}
