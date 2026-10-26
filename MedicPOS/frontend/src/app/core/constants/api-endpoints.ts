/* Endpoints FarmaMed. Coinciden con los @RequestMapping del backend. */

export const API = {
  auth: {
    login: '/auth/login',
    logout: '/auth/logout',
    me: '/auth/me'
  },
  dashboard: '/dashboard',
  alertas: {
    base: '/alertas',
    pendientes: '/alertas/pendientes',
    atender: '/alertas/atender'
  },
  auditoria: {
    base: '/auditoria',
    entidad: (entidad: string) => `/auditoria/entidad/${encodeURIComponent(entidad)}`,
    usuario: (usuarioId: number) => `/auditoria/usuario/${usuarioId}`,
    rango: '/auditoria/rango'
  },
  bajas: {
    base: '/bajas',
    rango: '/bajas/rango',
    motivo: (motivo: string) => `/bajas/motivo/${encodeURIComponent(motivo)}`,
    pendientes: '/bajas/pendientes'
  },
  configuracion: {
    base: '/configuracion',
    semaforo: '/configuracion/semaforo'
  },
  dispensaciones: {
    base: '/dispensaciones',
    byId: (id: number) => `/dispensaciones/${id}`,
    byReceta: (recetaId: number) => `/dispensaciones/receta/${recetaId}`,
    anular: (id: number) => `/dispensaciones/${id}/anular`
  },
  lotes: {
    base: '/lotes',
    vencidos: '/lotes/vencidos',
    proximosVencer: '/lotes/proximos-vencer',
    criticos: '/lotes/criticos',
    byId: (id: number) => `/lotes/${id}`,
    medicamento: (medicamentoId: number) => `/lotes/medicamento/${medicamentoId}`,
    sincronizarVencidos: '/lotes/sincronizar-vencidos'
  },
  medicamentos: {
    base: '/medicamentos',
    buscar: '/medicamentos/buscar',
    proveedor: (proveedorId: number) => `/medicamentos/proveedor/${proveedorId}`,
    stockCritico: '/medicamentos/stock-critico',
    byId: (id: number) => `/medicamentos/${id}`,
    toggleActivo: (id: number) => `/medicamentos/${id}/toggle-activo`
  },
  movimientos: {
    base: '/movimientos',
    lote: (loteId: number) => `/movimientos/lote/${loteId}`,
    referencia: '/movimientos/referencia'
  },
  ordenes: {
    base: '/ordenes',
    sugerencias: '/ordenes/sugerencias',
    byId: (id: number) => `/ordenes/${id}`,
    desdeSugerencia: '/ordenes/desde-sugerencia',
    resolver: (id: number) => `/ordenes/${id}/resolver`
  },
  pacientes: {
    base: '/pacientes',
    buscar: '/pacientes/buscar',
    byId: (id: number) => `/pacientes/${id}`
  },
  dni: {
    base: '/dni',
    consultar: (dni: string) => `/dni/${dni}`
  },
  proveedores: {
    base: '/proveedores',
    buscar: '/proveedores/buscar',
    byId: (id: number) => `/proveedores/${id}`,
    toggleActivo: (id: number) => `/proveedores/${id}/toggle-activo`
  },
  recetas: {
    base: '/recetas',
    byId: (id: number) => `/recetas/${id}`
  },
  reportes: {
    consumo: '/reportes/consumo',
    inventario: '/reportes/inventario',
    bajas: '/reportes/bajas',
    ordenes: '/reportes/ordenes',
    exportarPdf: (tipo: string) => `/reportes/${tipo}/pdf`,
    exportarXlsx: (tipo: string) => `/reportes/${tipo}/xlsx`
  },
  roles: {
    base: '/roles',
    byId: (id: number) => `/roles/${id}`
  },
  usuarios: {
    base: '/usuarios',
    byId: (id: number) => `/usuarios/${id}`,
    toggleActivo: (id: number) => `/usuarios/${id}/toggle-activo`,
    password: (id: number) => `/usuarios/${id}/password`
  }
} as const;
