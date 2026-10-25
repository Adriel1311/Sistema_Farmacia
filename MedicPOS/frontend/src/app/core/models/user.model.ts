export enum UserRole {
  JEFE_FARMACIA = 'JEFE_FARMACIA',
  TECNICO_FARMACIA = 'TECNICO_FARMACIA',
  LOGISTICA = 'LOGISTICA',
  ADMIN_TI = 'ADMIN_TI'
}

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  [UserRole.JEFE_FARMACIA]: 'Jefe de farmacia',
  [UserRole.TECNICO_FARMACIA]: 'Técnico de farmacia',
  [UserRole.LOGISTICA]: 'Logística',
  [UserRole.ADMIN_TI]: 'Administración TI'
};

export interface User {
  id: number;
  usuario: string;
  fullName: string;
  correo: string;
  role: UserRole;
  activo: boolean;
}

export interface LoginRequest {
  usuario: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  username: string;
  nombres: string;
  apellidos: string;
  fullName: string;
  email: string;
  role: string;
  token: string;
  type: string;
}

export interface Rol {
  id: number;
  nombre: string;
  descripcion: string;
  activo: boolean;
  usuariosAsignados: number;
  createdAt: string;
}

export interface Usuario {
  id: number;
  usuario: string;
  nombres: string;
  apellidos: string;
  fullName: string;
  correo: string;
  rolId: number;
  rolNombre: string;
  activo: boolean;
  ultimoAcceso: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUsuarioRequest {
  usuario: string;
  nombres: string;
  apellidos: string;
  correo: string;
  password: string;
  rolId: number;
}

export interface UpdateUsuarioRequest {
  nombres: string;
  apellidos: string;
  correo: string;
  rolId: number;
  activo: boolean;
}

export interface ChangePasswordRequest {
  passwordActual: string;
  passwordNuevo: string;
}

export function isUserRole(value: string | null | undefined): value is UserRole {
  return !!value && Object.values(UserRole).includes(value as UserRole);
}
