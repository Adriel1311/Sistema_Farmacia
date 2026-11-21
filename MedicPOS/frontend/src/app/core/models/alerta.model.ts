/* ============================================
   M11 ALERTAS Y M10 CONFIGURACION
   ============================================ */

export enum AlertaTipo {
  STOCK_CRITICO = 'STOCK_CRITICO',
  PROXIMO_VENCER = 'PROXIMO_VENCER',
  VENCIDO = 'VENCIDO',
  ORDEN_SUGERIDA = 'ORDEN_SUGERIDA',
  RECETA_VIGENTE = 'RECETA_VIGENTE'
}

export enum AlertaPrioridad {
  ALTA = 'ALTA',
  MEDIA = 'MEDIA',
  BAJA = 'BAJA'
}

export const ALERTA_TIPO_LABELS: Record<AlertaTipo, string> = {
  [AlertaTipo.STOCK_CRITICO]: 'Stock crítico',
  [AlertaTipo.PROXIMO_VENCER]: 'Próximo a vencer',
  [AlertaTipo.VENCIDO]: 'Vencido',
  [AlertaTipo.ORDEN_SUGERIDA]: 'Orden de compra sugerida',
  [AlertaTipo.RECETA_VIGENTE]: 'Receta vigente'
};

export const ALERTA_PRIORIDAD_LABELS: Record<AlertaPrioridad, string> = {
  [AlertaPrioridad.ALTA]: 'Alta',
  [AlertaPrioridad.MEDIA]: 'Media',
  [AlertaPrioridad.BAJA]: 'Baja'
};

export interface Alerta {
  clave: string;
  tipo: AlertaTipo;
  prioridad: AlertaPrioridad;
  titulo: string;
  mensaje: string;
  referenciaId: number;
  referenciaTipo: string;
  datos: Record<string, unknown>;
  atendida: boolean;
  atendidaEn: string | null;
  atendidaPor: string | null;
}

export interface AtenderAlertaRequest {
  tipo: string;
  referenciaId: string;
  comentario: string;
}

export interface Configuracion {
  id: number;
  clave: string;
  valor: string;
  descripcion: string;
  activo: boolean;
  categoria: string;
  tipoDato: string;
  updatedAt: string;
  updatedByNombre: string;
}

export interface UpdateConfiguracionRequest {
  valores: Record<string, string>;
}

export interface SemaforoConfig {
  diasCritico: number;
  diasProximo: number;
  [key: string]: unknown;
}
