/* ============================================
   M04 PACIENTES Y RECETAS
   Regla RN09: receta medica obligatoria para la dispensacion.
   ============================================ */

export enum TipoReceta {
  ASEGURADO = 'ASEGURADO',
  PARTICULAR = 'PARTICULAR'
}

export interface Paciente {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  fullName: string;
  activo: boolean;
  createdAt: string;
}

export interface CreatePacienteRequest {
  dni: string;
  nombres: string;
  apellidos: string;
}

export interface Medico {
  id: number;
  nombre: string;
  especialidad: string | null;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface CreateMedicoRequest {
  nombre: string;
  especialidad: string;
}

export interface UpdateMedicoRequest extends CreateMedicoRequest {
  activo: boolean;
}

export interface Receta {
  id: number;
  numeroReceta: string;
  medico: string;
  tipoReceta: TipoReceta;
  fechaReceta: string;
  pacienteId: number;
  pacienteDni: string;
  pacienteNombres: string;
  pacienteApellidos: string;
  pacienteNombreCompleto: string;
  tieneDispensacion: boolean;
  dispensacionId: number | null;
  createdAt: string;
}

export interface CreateRecetaRequest {
  pacienteId: number;
  numeroReceta: string;
  medicoId: number;
  tipoReceta: TipoReceta;
  fechaReceta: string;
}

export const TIPO_RECETA_LABELS: Record<TipoReceta, string> = {
  [TipoReceta.ASEGURADO]: 'Asegurado',
  [TipoReceta.PARTICULAR]: 'Particular'
};
