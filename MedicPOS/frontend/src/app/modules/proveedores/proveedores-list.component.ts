import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Observable, Subject, merge, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { CatalogoService } from '../../core/services/catalogo.service';
import { AuthService } from '../../core/services/auth.service';
import { CreateProveedorRequest, Proveedor, UpdateProveedorRequest } from '../../core/models/catalogo.model';
import { UserRole } from '../../core/models/user.model';

type ModoFormulario = 'alta' | 'edicion';

@Component({
  selector: 'app-proveedores-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './proveedores-list.component.html',
  styleUrls: ['./proveedores-list.component.scss']
})
export class ProveedoresListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly catalogo = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly puedeEscribir = this.auth.hasRole(UserRole.LOGISTICA);

  readonly proveedores = signal<Proveedor[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly objetivo = signal<Proveedor | null>(null);
  readonly modalAbierto = signal(false);
  readonly modo = signal<ModoFormulario>('alta');
  readonly guardando = signal(false);
  readonly formError = signal('');

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly form = this.fb.nonNullable.group({
    ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    contacto: ['', [Validators.required, Validators.maxLength(100)]],
    telefono: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]]
  });

  readonly edicion = signal<Proveedor | null>(null);

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(this.reload$, this.searchControl.valueChanges.pipe(distinctUntilChanged(), debounceTime(300), map(() => undefined)))
      .pipe(startWith(undefined), switchMap(() => this.cargarProveedores()), takeUntilDestroyed(this.destroyRef))
      .subscribe();
  }

  refrescar(): void {
    this.success.set('');
    this.error.set('');
    this.reload$.next();
  }

  private cargarProveedores(): Observable<Proveedor[]> {
    this.loading.set(true);
    this.error.set('');
    const texto = this.searchControl.value.trim();

    const request$ = texto.length > 0 ? this.catalogo.buscarProveedores(texto) : this.catalogo.getProveedores(false);

    return request$.pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar los proveedores.');
        return of([] as Proveedor[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.proveedores.set(lista))
    );
  }

  abrirAlta(): void {
    this.modo.set('alta');
    this.edicion.set(null);
    this.formError.set('');
    this.form.reset({ ruc: '', razonSocial: '', contacto: '', telefono: '', correo: '' });
    this.modalAbierto.set(true);
  }

  abrirEdicion(proveedor: Proveedor): void {
    if (!this.puedeEscribir) {
      return;
    }
    this.modo.set('edicion');
    this.edicion.set(proveedor);
    this.formError.set('');
    this.form.reset({
      ruc: proveedor.ruc,
      razonSocial: proveedor.razonSocial,
      contacto: proveedor.contacto,
      telefono: proveedor.telefono,
      correo: proveedor.correo
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const original = this.edicion();
    const esEdicion = this.modo() === 'edicion' && original !== null;

    this.guardando.set(true);
    this.formError.set('');

    if (esEdicion && original) {
      const payload: UpdateProveedorRequest = {
        razonSocial: valor.razonSocial.trim(),
        contacto: valor.contacto.trim(),
        telefono: valor.telefono.trim(),
        correo: valor.correo.trim(),
        activo: String(original.activo)
      };

      this.catalogo
        .updateProveedor(original.id, payload)
        .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: guardado => {
            this.cerrarModal();
            this.success.set(`${guardado.razonSocial} actualizada correctamente.`);
            this.reload$.next();
          },
          error: (err: Error) => this.formError.set(err.message || 'No se pudo guardar el proveedor.')
        });
      return;
    }

    const payload: CreateProveedorRequest = {
      ruc: valor.ruc.trim(),
      razonSocial: valor.razonSocial.trim(),
      contacto: valor.contacto.trim(),
      telefono: valor.telefono.trim(),
      correo: valor.correo.trim()
    };

    this.catalogo
      .createProveedor(payload)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: guardado => {
          this.cerrarModal();
          this.success.set(`${guardado.razonSocial} registrada correctamente.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo guardar el proveedor.')
      });
  }

  pedirToggle(proveedor: Proveedor): void {
    if (!this.puedeEscribir) {
      return;
    }
    this.objetivo.set(proveedor);
  }

  cancelarToggle(): void {
    this.objetivo.set(null);
  }

  confirmarToggle(): void {
    const objetivo = this.objetivo();
    if (!objetivo) {
      return;
    }

    this.guardando.set(true);
    this.catalogo
      .toggleProveedorActivo(objetivo.id)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: actualizado => {
          this.objetivo.set(null);
          this.success.set(
            `${actualizado.razonSocial} ${actualizado.activo ? 'activada' : 'desactivada'} correctamente.`
          );
          this.reload$.next();
        },
        error: (err: Error) => {
          this.objetivo.set(null);
          this.error.set(err.message || 'No se pudo cambiar el estado del proveedor.');
        }
      });
  }

  trackById(_index: number, proveedor: Proveedor): number {
    return proveedor.id;
  }
}
