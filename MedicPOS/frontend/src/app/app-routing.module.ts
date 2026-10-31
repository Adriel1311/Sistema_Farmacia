import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { UserRole } from './core/models/user.model';
import { NoAutorizadoComponent } from './auth/no-autorizado.component';
import { LoginComponent } from './auth/login/login.component';
import { AppShellComponent } from './core/components/app-shell/app-shell.component';

const JEFE = UserRole.JEFE_FARMACIA;
const TECNICO = UserRole.TECNICO_FARMACIA;
const LOGISTICA = UserRole.LOGISTICA;
const ADMIN_TI = UserRole.ADMIN_TI;

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'auth/login' },
  { path: 'auth/login', component: LoginComponent, canActivate: [guestGuard], title: 'Ingresar | FarmaMed' },
  { path: 'no-autorizado', component: NoAutorizadoComponent, canActivate: [authGuard], title: 'Sin permisos | FarmaMed' },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO, LOGISTICA, ADMIN_TI] }, title: 'Panel | FarmaMed' },
      { path: 'dispensacion', loadChildren: () => import('./modules/dispensacion/dispensacion.module').then(m => m.DispensacionModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Dispensacion | FarmaMed' },
      { path: 'recetas', loadChildren: () => import('./modules/recetas/recetas.module').then(m => m.RecetasModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Recetas | FarmaMed' },
      { path: 'pacientes', loadChildren: () => import('./modules/pacientes/pacientes.module').then(m => m.PacientesModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Pacientes | FarmaMed' },
      { path: 'medicos', loadChildren: () => import('./modules/medicos/medicos.module').then(m => m.MedicosModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO, ADMIN_TI] }, title: 'Medicos | FarmaMed' },
      { path: 'inventario', loadChildren: () => import('./modules/inventario/inventario.module').then(m => m.InventarioModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Inventario y lotes | FarmaMed' },
      { path: 'bajas', loadChildren: () => import('./modules/bajas/bajas.module').then(m => m.BajasModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Bajas de inventario | FarmaMed' },
      { path: 'movimientos', loadChildren: () => import('./modules/movimientos/movimientos.module').then(m => m.MovimientosModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO] }, title: 'Movimientos de inventario | FarmaMed' },
      { path: 'medicamentos', loadChildren: () => import('./modules/medicamentos/medicamentos.module').then(m => m.MedicamentosModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO, LOGISTICA] }, title: 'Medicamentos | FarmaMed' },
      { path: 'proveedores', loadChildren: () => import('./modules/proveedores/proveedores.module').then(m => m.ProveedoresModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO, LOGISTICA] }, title: 'Proveedores | FarmaMed' },
      { path: 'ordenes', loadChildren: () => import('./modules/ordenes/ordenes.module').then(m => m.OrdenesModule), canActivate: [roleGuard], data: { roles: [JEFE, LOGISTICA] }, title: 'Ordenes de compra | FarmaMed' },
      { path: 'alertas', loadChildren: () => import('./modules/alertas/alertas.module').then(m => m.AlertasModule), canActivate: [roleGuard], data: { roles: [JEFE, TECNICO, LOGISTICA] }, title: 'Alertas | FarmaMed' },
      { path: 'reportes', loadChildren: () => import('./modules/reportes/reportes.module').then(m => m.ReportesModule), canActivate: [roleGuard], data: { roles: [JEFE, LOGISTICA, ADMIN_TI] }, title: 'Reportes | FarmaMed' },
      { path: 'configuracion', loadChildren: () => import('./modules/configuracion/configuracion.module').then(m => m.ConfiguracionModule), canActivate: [roleGuard], data: { roles: [JEFE] }, title: 'Configuracion | FarmaMed' },
      { path: 'auditoria', loadChildren: () => import('./modules/auditoria/auditoria.module').then(m => m.AuditoriaModule), canActivate: [roleGuard], data: { roles: [JEFE, ADMIN_TI] }, title: 'Auditoria | FarmaMed' },
      { path: 'usuarios', loadChildren: () => import('./modules/usuarios/usuarios.module').then(m => m.UsuariosModule), canActivate: [roleGuard], data: { roles: [ADMIN_TI] }, title: 'Usuarios | FarmaMed' },
      { path: 'roles', loadChildren: () => import('./modules/roles/roles.module').then(m => m.RolesModule), canActivate: [roleGuard], data: { roles: [ADMIN_TI] }, title: 'Roles | FarmaMed' },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      scrollPositionRestoration: 'enabled',
      onSameUrlNavigation: 'reload'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
