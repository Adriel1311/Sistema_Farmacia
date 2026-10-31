import { UserRole } from '../models/user.model';

export interface NavItem {
  label: string;
  route: string;
  icon: string;
  roles: UserRole[];
  group: string;
}

const JEFE = UserRole.JEFE_FARMACIA;
const TECNICO = UserRole.TECNICO_FARMACIA;
const LOGISTICA = UserRole.LOGISTICA;
const ADMIN_TI = UserRole.ADMIN_TI;

/**
 * Navegacion de FarmaMed. Los roles coinciden con los matchers de
 * SecurityConfig, que es la autoridad real del lado del servidor.
 * ADMIN_TI tiene acceso completo a todo para pruebas.
 */
export const NAV_ITEMS: NavItem[] = [
  { label: 'Panel', route: '/dashboard', icon: 'dashboard', roles: [JEFE, TECNICO, LOGISTICA, ADMIN_TI], group: 'Inicio' },
  { label: 'Dispensacion', route: '/dispensacion', icon: 'dispensacion', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Recetas', route: '/recetas', icon: 'receta', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Pacientes', route: '/pacientes', icon: 'paciente', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Medicos', route: '/medicos', icon: 'user', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Inventario y lotes', route: '/inventario', icon: 'inventario', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Bajas de inventario', route: '/bajas', icon: 'baja', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Movimientos', route: '/movimientos', icon: 'movimiento', roles: [JEFE, TECNICO, ADMIN_TI], group: 'Operacion' },
  { label: 'Medicamentos', route: '/medicamentos', icon: 'medicamento', roles: [JEFE, TECNICO, LOGISTICA, ADMIN_TI], group: 'Catalogo' },
  { label: 'Proveedores', route: '/proveedores', icon: 'proveedor', roles: [JEFE, LOGISTICA, ADMIN_TI], group: 'Catalogo' },
  { label: 'Ordenes de compra', route: '/ordenes', icon: 'orden', roles: [JEFE, LOGISTICA, ADMIN_TI], group: 'Catalogo' },
  { label: 'Alertas', route: '/alertas', icon: 'alerta', roles: [JEFE, LOGISTICA, ADMIN_TI], group: 'Control' },
  { label: 'Reportes', route: '/reportes', icon: 'reporte', roles: [JEFE, LOGISTICA, ADMIN_TI], group: 'Control' },
  { label: 'Configuracion', route: '/configuracion', icon: 'configuracion', roles: [JEFE, ADMIN_TI], group: 'Control' },
  { label: 'Auditoria', route: '/auditoria', icon: 'auditoria', roles: [JEFE, ADMIN_TI], group: 'Seguridad' },
  { label: 'Usuarios', route: '/usuarios', icon: 'usuarios', roles: [ADMIN_TI], group: 'Seguridad' },
  { label: 'Roles', route: '/roles', icon: 'roles', roles: [ADMIN_TI], group: 'Seguridad' }
];

export function navItemsForRole(role: UserRole | null): NavItem[] {
  if (!role) {
    return [];
  }
  return NAV_ITEMS.filter(item => item.roles.includes(role));
}
