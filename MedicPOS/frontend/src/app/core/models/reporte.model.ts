/* ============================================
   M08 REPORTES Y M12 AUDITORIA
   Los reportes son de solo lectura (RF-12).
   ============================================ */

import { BajaInventario } from './inventario.model';
import { OrdenCompra } from './orden-compra.model';

export interface RangoFechas {
  desde: string;
  hasta: string;
}

export interface ItemInventario {
  medicamentoId: number;
  codigo: string;
  nombre: string;
  unidadMedida: string;
  proveedorRazonSocial: string;
  stockVigente: number;
  stockVencido: number;
  stockMinimo: number;
  stockMaximo: number;
  stockCritico: boolean;
  cantidadSugerida: number;
  lotesActivos: number;
  lotesVencidos: number;
}

export interface ReporteInventario {
  items: ItemInventario[];
  totalMedicamentos: number;
  totalLotes: number;
  totalUnidadesVigentes: number;
  totalUnidadesVencidas: number;
  medicamentosStockCritico: number;
}

export interface ItemConsumo {
  medicamentoId: number;
  codigo: string;
  nombre: string;
  unidadMedida: string;
  unidadesConsumidas: number;
  dispensaciones: number;
  promedioPorDispensacion: number;
}

export interface ReporteConsumo {
  desde: string;
  hasta: string;
  items: ItemConsumo[];
  totalMedicamentos: number;
  totalUnidades: number;
  totalDispensaciones: number;
  diasReporte: number;
}

export interface ResumenBajaPorMedicamento {
  medicamentoId: number;
  codigo: string;
  nombre: string;
  unidadesDadasDeBaja: number;
}

export interface ReporteBajas {
  desde: string;
  hasta: string;
  bajas: BajaInventario[];
  totalRegistros: number;
  totalUnidades: number;
  totalVencidoEnEstante: number;
  totalMerma: number;
  totalDevuelto: number;
  resumenPorMedicamento: ResumenBajaPorMedicamento[];
}

export interface ResumenOrdenPorProveedor {
  proveedorId: number;
  razonSocial: string;
  ruc: string;
  ordenes: number;
  unidades: number;
}

export interface ReporteOrdenes {
  desde: string;
  hasta: string;
  ordenes: OrdenCompra[];
  totalOrdenes: number;
  totalUnidades: number;
  totalBorrador: number;
  totalSugerida: number;
  totalAprobada: number;
  totalRechazada: number;
  resumenPorProveedor: ResumenOrdenPorProveedor[];
}

export interface Auditoria {
  id: number;
  usuarioId: number;
  usuarioNombre: string;
  usuarioRol: string;
  entidad: string;
  accion: string;
  descripcion: string;
  valorAnterior: string;
  valorNuevo: string;
  fechaHora: string;
}
