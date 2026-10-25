import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Observable, Subject, merge, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { PacienteRecetaService } from '../../core/services/paciente-receta.service';
import { CreatePacienteRequest, Paciente } from '../../core/models/receta.model';

@Component({
  selector: 'app-pacientes-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pacientes-list.component.html',
  styleUrls: ['./pacientes-list.component.scss']
})
export class PacientesListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PacienteRecetaService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pacientes = signal<Paciente[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly modalAbierto = signal(false);
  readonly guardando = signal(false);
  readonly formError = signal('');

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly form = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]]
  });

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(this.reload$, this.searchControl.valueChanges.pipe(distinctUntilChanged(), debounceTime(300), map(() => undefined)))
      .pipe(startWith(undefined), switchMap(() => this.cargarPacientes()), takeUntilDestroyed(this.destroyRef))
      .subscribe();
  }

  refrescar(): void {
    this.success.set('');
    this.error.set('');
    this.reload$.next();
  }

  private cargarPacientes(): Observable<Paciente[]> {
    this.loading.set(true);
    this.error.set('');
    const texto = this.searchControl.value.trim();

    const request$ = texto.length > 0 ? this.service.buscarPacientes(texto) : this.service.getPacientes();

    return request$.pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar los pacientes.');
        return of([] as Paciente[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.pacientes.set(lista))
    );
  }

  abrirAlta(): void {
    this.formError.set('');
    this.form.reset({ dni: '', nombres: '', apellidos: '' });
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
    const payload: CreatePacienteRequest = {
      dni: valor.dni.trim(),
      nombres: valor.nombres.trim(),
      apellidos: valor.apellidos.trim()
    };

    this.guardando.set(true);
    this.formError.set('');

    this.service
      .createPaciente(payload)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: creado => {
          this.cerrarModal();
          this.success.set(`Paciente ${creado.fullName} registrado correctamente.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar el paciente.')
      });
  }

  trackById(_index: number, paciente: Paciente): number {
    return paciente.id;
  }
}
