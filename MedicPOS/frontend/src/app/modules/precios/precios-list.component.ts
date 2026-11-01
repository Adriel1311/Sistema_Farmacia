import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { combineLatest, of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { MedicamentoPrecioService, MedicamentoPrecio, CreateMedicamentoPrecioRequest, UpdateMedicamentoPrecioRequest } from '../../core/services/medicamento-precio.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { Medicamento } from '../../core/models/catalogo.model';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/user.model';

@Component({
  selector: 'app-precios-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './precios-list.component.html',
  styleUrls: ['./precios-list.component.scss']
})
export class PreciosListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly precioService = inject(MedicamentoPrecioService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly puedeGestionar = this.auth.hasRole(UserRole.JEFE_FARMACIA, UserRole.ADMIN_TI);

  readonly precios = signal<MedicamentoPrecio[]>([]);
  readonly medicamentos = signal<Medicamento[]>([]);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly modalAbierto = signal(false);
  readonly modo = signal<'alta' | 'edicion'>('alta');
  readonly edicion = signal<MedicamentoPrecio | null>(null);
  readonly formError = signal('');

  readonly form = this.fb.nonNullable.group({
    medicamentoId: [0, [Validators.required, Validators.min(1)]],
    precioVenta: [0, [Validators.required, Validators.min(0.01)]],
    vigenteDesde: ['', Validators.required],
    vigenteHasta: ['']
  });

  readonly medicamentosSinPrecio = computed(() => {
    const preciosIds = new Set(this.precios().map(p => p.medicamentoId));
    return this.medicamentos().filter(m => !preciosIds.has(m.id));
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set('');

    combineLatest({
      precios: this.precioService.getAll(),
      medicamentos: this.catalogoService.getMedicamentos()
    })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los precios.');
          return of({ precios: [] as MedicamentoPrecio[], medicamentos: [] as Medicamento[] });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ precios, medicamentos }) => {
        this.precios.set(precios);
        this.medicamentos.set(medicamentos);
      });
  }

  abrirAlta(): void {
    if (!this.puedeGestionar) return;
    this.modo.set('alta');
    this.edicion.set(null);
    this.formError.set('');
    const hoy = new Date().toISOString().split('T')[0];
    this.form.reset({ medicamentoId: 0, precioVenta: 0, vigenteDesde: hoy, vigenteHasta: '' });
    this.modalAbierto.set(true);
  }

  abrirEdicion(precio: MedicamentoPrecio): void {
    if (!this.puedeGestionar) return;
    this.modo.set('edicion');
    this.edicion.set(precio);
    this.formError.set('');
    this.form.reset({
      medicamentoId: precio.medicamentoId,
      precioVenta: precio.precioVenta,
      vigenteDesde: precio.vigenteDesde,
      vigenteHasta: precio.vigenteHasta ?? ''
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  guardar(): void {
    if (!this.puedeGestionar) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    this.guardando.set(true);
    this.formError.set('');

    const request: CreateMedicamentoPrecioRequest = {
      medicamentoId: valor.medicamentoId,
      precioVenta: valor.precioVenta,
      vigenteDesde: valor.vigenteDesde,
      vigenteHasta: valor.vigenteHasta || undefined
    };

    if (this.modo() === 'edicion' && this.edicion()) {
      this.precioService
        .update(this.edicion()!.id, request as UpdateMedicamentoPrecioRequest)
        .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.cerrarModal();
            this.success.set('Precio actualizado correctamente.');
            this.cargar();
          },
          error: (err: Error) => this.formError.set(err.message || 'No se pudo actualizar el precio.')
        });
      return;
    }

    this.precioService
      .create(request)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.cerrarModal();
          this.success.set('Precio registrado correctamente.');
          this.cargar();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar el precio.')
      });
  }

  trackById(_index: number, precio: MedicamentoPrecio): number {
    return precio.id;
  }
}