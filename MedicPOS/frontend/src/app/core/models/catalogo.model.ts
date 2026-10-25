/* ============================================
   M02 MEDICAMENTOS Y M07 PROVEEDORES
   ============================================ */

export interface Medicamento {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  unidadMedida: string;
  proveedorId: number;
  proveedorRazonSocial: string;
  stockMinimo: number;
  stockMaximo: number;
  activo: boolean;
  stockVigente: number;
  stockVencido: number;
  cantidadSugerida: number;
  stockCritico: boolean;
  lotesActivos: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMedicamentoRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
  unidadMedida: string;
  proveedorId: number;
  stockMinimo: number;
  stockMaximo: number;
}

export interface UpdateMedicamentoRequest extends CreateMedicamentoRequest {
  activo: boolean;
}

export interface Proveedor {
  id: number;
  ruc: string;
  razonSocial: string;
  contacto: string;
  telefono: string;
  correo: string;
  activo: boolean;
  medicamentosAsociados: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProveedorRequest {
  ruc: string;
  razonSocial: string;
  contacto: string;
  telefono: string;
  correo: string;
}

export interface UpdateProveedorRequest {
  razonSocial: string;
  contacto: string;
  telefono: string;
  correo: string;
  activo: string;
}
