/* ============================================
   DASHBOARD (M00)
   ============================================ */

import { Lote } from './inventario.model';
import { SugerenciaCompra } from './orden-compra.model';

export interface Dashboard {
  lotesCriticos: number;
  lotesProximosAVencer: number;
  lotesVencidos: number;
  medicamentosStockCritico: number;
  ordenesSugeridas: number;
  dispensacionesHoy: number;
  medicamentosActivos: number;
  proveedoresActivos: number;
  alertasTotales: number;
  alertasAltaPrioridad: number;
  diasCritico: number;
  diasProximo: number;
  proximosAVencer: Lote[];
  suggestions: SugerenciaCompra[];
  generadoEn: string;
}
