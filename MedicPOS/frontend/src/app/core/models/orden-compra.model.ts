/* ============================================
   M06 ORDENES DE COMPRA
   Estados: BORRADOR, SUGERIDA, APROBADA, RECHAZADA.
   ============================================ */

export enum OrdenEstado {
  BORRADOR = 'BORRADOR',
  SUGERIDA = 'SUGERIDA',
  APROBADA = 'APROBADA',
  RECHAZADA = 'RECHAZADA'
}

export const ORDEN_ESTADO_LABELS: Record<OrdenEstado, string> = {
  [OrdenEstado.BORRADOR]: 'Borrador',
  [OrdenEstado.SUGERIDA]: 'Sugerida',
  [OrdenEstado.APROBADA]: 'Aprobada',
  [OrdenEstado.RECHAZADA]: 'Rechazada'
};

export interface SugerenciaCompra {
  medicamentoId: number;
  codigo: string;
  nombre: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  stockMaximo: number;
  cantidadSugerida: number;
  proveedorId: number;
  proveedorRazonSocial: string;
}

export interface OrdenCompraDetalle {
  id: number;
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  unidadMedida: string;
  cantidad: number;
  stockActual: number;
  sugerido: boolean;
}

export interface OrdenCompra {
  id: number;
  estado: OrdenEstado;
  observacion: string;
  fechaHora: string;
  fechaAprobacion: string | null;
  proveedorId: number;
  proveedorRuc: string;
  proveedorRazonSocial: string;
  usuarioId: number;
  usuarioNombre: string;
  usuarioRol: string;
  aprobadoPorId: number | null;
  aprobadoPorNombre: string | null;
  detalles: OrdenCompraDetalle[];
  totalItems: number;
  totalUnidades: number;
  createdAt: string;
  updatedAt: string;
}

export interface OrdenCompraItemRequest {
  medicamentoId: number;
  cantidad: number;
}

export interface CreateOrdenCompraRequest {
  proveedorId: number;
  items: OrdenCompraItemRequest[];
  observacion: string;
}

export interface CrearDesdeSugerenciaRequest {
  sugerencias: SugerenciaCompra[];
  observacion: string;
}

export interface ResolverOrdenCompraRequest {
  decision: OrdenEstado;
  observacion: string;
}
