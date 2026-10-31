import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuthService } from '../../core/services/auth.service';
import { MedicoService } from '../../core/services/medico.service';
import { CreateMedicoRequest, Medico, UpdateMedicoRequest } from '../../core/models/receta.model';
import { UserRole } from '../../core/models/user.model';

type ModoFormulario = 'alta' | 'edicion';

@Component({
  selector: 'app-medicos-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './medicos-list.component.html',
  styleUrls: ['./medicos-list.component.scss']
})
export class MedicosListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly medicosService = inject(MedicoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly puedeGestionar = this.auth.hasRole(UserRole.JEFE_FARMACIA, UserRole.ADMIN_TI);
  readonly medicos = signal<Medico[]>([]);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly busqueda = signal('');
  readonly modalAbierto = signal(false);
  readonly modo = signal<ModoFormulario>('alta');
  readonly edicion = signal<Medico | null>(null);
  readonly objetivo = signal<Medico | null>(null);
  readonly formError = signal('');

  readonly visibles = computed(() => {
    const texto = this.busqueda().trim().toLocaleLowerCase();
    return this.medicos().filter(medico =>
      !texto ||
      medico.nombre.toLocaleLowerCase().includes(texto) ||
      (medico.especialidad ?? '').toLocaleLowerCase().includes(texto)
    );
  });

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(200)]],
    especialidad: ['', [Validators.maxLength(100)]]
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set('');
    this.medicosService
      .getAll()
      .pipe(finalize(() => this.loading.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: medicos => this.medicos.set(medicos),
        error: (err: Error) => this.error.set(err.message || 'No se pudieron cargar los médicos.')
      });
  }

  onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  abrirAlta(): void {
    if (!this.puedeGestionar) {
      return;
    }
    this.modo.set('alta');
    this.edicion.set(null);
    this.formError.set('');
    this.form.reset({ nombre: '', especialidad: '' });
    this.modalAbierto.set(true);
  }

  abrirEdicion(medico: Medico): void {
    if (!this.puedeGestionar) {
      return;
    }
    this.modo.set('edicion');
    this.edicion.set(medico);
    this.formError.set('');
    this.form.reset({ nombre: medico.nombre, especialidad: medico.especialidad ?? '' });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  guardar(): void {
    if (!this.puedeGestionar) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const original = this.edicion();
    this.guardando.set(true);
    this.formError.set('');

    if (this.modo() === 'edicion' && original) {
      const request: UpdateMedicoRequest = {
        nombre: valor.nombre.trim(),
        especialidad: valor.especialidad.trim(),
        activo: original.activo
      };
      this.medicosService
        .update(original.id, request)
        .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: medico => {
            this.cerrarModal();
            this.success.set(`${medico.nombre} actualizado correctamente.`);
            this.cargar();
          },
          error: (err: Error) => this.formError.set(err.message || 'No se pudo actualizar el médico.')
        });
      return;
    }

    const request: CreateMedicoRequest = {
      nombre: valor.nombre.trim(),
      especialidad: valor.especialidad.trim()
    };
    this.medicosService
      .create(request)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: medico => {
          this.cerrarModal();
          this.success.set(`${medico.nombre} registrado correctamente.`);
          this.cargar();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar el médico.')
      });
  }

  pedirToggle(medico: Medico): void {
    if (this.puedeGestionar) {
      this.objetivo.set(medico);
    }
  }

  cancelarToggle(): void {
    this.objetivo.set(null);
  }

  confirmarToggle(): void {
    const medico = this.objetivo();
    if (!medico || !this.puedeGestionar) {
      return;
    }
    this.guardando.set(true);
    this.medicosService
      .toggleActivo(medico.id)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: actualizado => {
          this.objetivo.set(null);
          this.success.set(`${actualizado.nombre} ${actualizado.activo ? 'activado' : 'desactivado'} correctamente.`);
          this.cargar();
        },
        error: (err: Error) => {
          this.objetivo.set(null);
          this.error.set(err.message || 'No se pudo cambiar el estado del médico.');
        }
      });
  }

  trackById(_index: number, medico: Medico): number {
    return medico.id;
  }
}
