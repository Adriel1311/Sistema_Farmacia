import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, defaultRouteForRole } from '../services/auth.service';
import { UserRole } from '../models/user.model';

export interface RoleData {
  roles: UserRole[];
}

/**
 * Guard de rol para el frontend. El backend sigue siendo la autoridad: esto
 * solo evita mostrar pantallas que el usuario no va a poder usar.
 */
export const roleGuard: CanActivateFn = route => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  const roles = (route.data['roles'] as RoleData | undefined)?.roles ?? [];
  if (roles.length === 0 || auth.hasAnyRole(roles)) {
    return true;
  }

  return router.createUrlTree(['/no-autorizado'], {
    queryParams: { from: router.url }
  });
};

export function rolesData(...roles: UserRole[]): RoleData {
  return { roles };
}
