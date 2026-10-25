import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { UsuarioService } from '../../core/services/usuario.service';
import { Rol } from '../../core/models/user.model';

@Component({
  selector: 'app-roles-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './roles-list.component.html',
  styleUrls: ['./roles-list.component.scss']
})
export class RolesListComponent {
  private readonly servicio = inject(UsuarioService);
  private readonly destroyRef = inject(DestroyRef);

  readonly roles = signal<Rol[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    this.servicio
      .getRoles()
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los roles.');
          return of([] as Rol[]);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.roles.set(lista));
  }

  totalUsuarios(): number {
    return this.roles().reduce((total, rol) => total + (rol.usuariosAsignados ?? 0), 0);
  }

  trackById(_index: number, rol: Rol): number {
    return rol.id;
  }
}
