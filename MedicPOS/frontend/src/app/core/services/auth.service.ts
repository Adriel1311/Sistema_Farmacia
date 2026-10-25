import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import { AuthResponse, isUserRole, LoginRequest, User, UserRole } from '../models/user.model';

const TOKEN_KEY = 'farmamed_token';
const USER_KEY = 'farmamed_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSubject = new BehaviorSubject<User | null>(this.restoreUser());

  readonly currentUser$: Observable<User | null> = this.currentUserSubject.asObservable();

  constructor(
    private readonly api: ApiService,
    private readonly router: Router
  ) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>(API.auth.login, credentials).pipe(
      tap(response => {
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(this.toUser(response)));
        this.currentUserSubject.next(this.toUser(response));
      })
    );
  }

  logout(): void {
    if (this.isAuthenticated()) {
      this.api.post<void>(API.auth.logout).pipe(catchError(() => of(void 0))).subscribe();
    }
    this.clearSession();
    void this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken() && !!this.getCurrentUser();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getRole(): UserRole | null {
    return this.getCurrentUser()?.role ?? null;
  }

  hasRole(...roles: UserRole[]): boolean {
    const role = this.getRole();
    return role !== null && roles.includes(role);
  }

  hasAnyRole(roles: UserRole[]): boolean {
    return this.hasRole(...roles);
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSubject.next(null);
  }

  private toUser(response: AuthResponse): User {
    return {
      id: response.userId,
      usuario: response.username,
      fullName: response.fullName || `${response.nombres} ${response.apellidos}`.trim() || response.username,
      correo: response.email ?? '',
      role: isUserRole(response.role) ? response.role : UserRole.TECNICO_FARMACIA,
      activo: true
    };
  }

  private restoreUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    const token = localStorage.getItem(TOKEN_KEY);
    if (!raw || !token) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as User;
      return isUserRole(parsed.role) ? parsed : null;
    } catch {
      return null;
    }
  }
}

/** Ruta de inicio sugerida para cada rol. */
export const DEFAULT_ROUTE_BY_ROLE: Record<UserRole, string> = {
  [UserRole.JEFE_FARMACIA]: '/dashboard',
  [UserRole.TECNICO_FARMACIA]: '/dispensacion',
  [UserRole.LOGISTICA]: '/inventario',
  [UserRole.ADMIN_TI]: '/usuarios'
};

export function defaultRouteForRole(role: UserRole | null): string {
  return role ? DEFAULT_ROUTE_BY_ROLE[role] : '/auth/login';
}
