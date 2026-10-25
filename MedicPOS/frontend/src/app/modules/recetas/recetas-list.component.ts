import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Observable, Subject, of } from 'rxjs';
import { catchError, finalize, startWith, switchMap, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { PacienteRecetaService } from '../../core/services/paciente-receta.service';
import { AuthService } from '../../core/services/auth.service';
import { CreateRecetaRequest, Paciente, Receta, TIPO_RECETA_LABELS, TipoReceta } from '../../core/models/receta.model';
import { UserRole } from '../../core/models/user.model';

function todayIso(): string {
  const now = new Date();
  const mes = String(now.getMonth() + 1).padStart(2, '0');
  const dia = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${mes}-${dia}`;
}

function noFutureDate(control: AbstractControl): ValidationErrors | null {
  const value = String(control.value ?? '');
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return null;
  }
  return value > todayIso() ? { fechaFutura: true } : null;
}

@Component({
  selector: 'app-recetas-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recetas-list.component.html',
  styleUrls: ['./recetas-list.component.scss']
})
export class RecetasListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PacienteRecetaService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly tipos = Object.values(TipoReceta);
  readonly tipoLabels = TIPO_RECETA_LABELS;
  readonly hoy = todayIso();

  readonly puedeCrear = this.auth.hasRole(UserRole.JEFE_FARMACIA, UserRole.TECNICO_FARMACIA);

  readonly recetas = signal<Receta[]>([]);
  readonly pacientes = signal<Paciente[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly tipoFiltro = signal<TipoReceta | ''>('');
  readonly desdeFiltro = signal('');
  readonly hastaFiltro = signal('');

  readonly modalAbierto = signal(false);
  readonly guardando = signal(false);
  readonly formError = signal('');

  readonly form = this.fb.nonNullable.group({
    pacienteId: [0, [Validators.required, Validators.min(1)]],
    numeroReceta: ['', [Validators.required, Validators.maxLength(30)]],
    medico: ['', [Validators.required, Validators.maxLength(120)]],
    tipoReceta: [TipoReceta.ASEGURADO, [Validators.required]],
    fechaReceta: [todayIso(), [Validators.required, noFutureDate]]
  });

  private readonly reload$ = new Subject<void>();

  constructor() {
    this.reload$
      .pipe(startWith(undefined), switchMap(() => this.cargarRecetas()), takeUntilDestroyed(this.destroyRef))
      .subscribe();

    this.service
      .getPacientes()
      .pipe(
        catchError(() => of([] as Paciente[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.pacientes.set(lista));
  }

  refrescar(): void {
    this.success.set('');
    this.error.set('');
    this.reload$.next();
  }

  private cargarRecetas(): Observable<Receta[]> {
    this.loading.set(true);
    this.error.set('');

    const tipo = this.tipoFiltro();
    const desde = this.desdeFiltro();
    const hasta = this.hastaFiltro();

    return this.service.getRecetas({ tipo: tipo || undefined, desde: desde || undefined, hasta: hasta || undefined }).pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar las recetas.');
        return of([] as Receta[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.recetas.set(lista))
    );
  }

  cambiarTipoFiltro(valor: string): void {
    this.tipoFiltro.set(valor === '' ? '' : (valor as TipoReceta));
    this.refrescar();
  }

  cambiarDesde(valor: string): void {
    this.desdeFiltro.set(valor);
    this.refrescar();
  }

  cambiarHasta(valor: string): void {
    this.hastaFiltro.set(valor);
    this.refrescar();
  }

  limpiarFiltros(): void {
    this.tipoFiltro.set('');
    this.desdeFiltro.set('');
    this.hastaFiltro.set('');
    this.refrescar();
  }

  abrirAlta(): void {
    this.formError.set('');
    this.form.reset({
      pacienteId: 0,
      numeroReceta: '',
      medico: '',
      tipoReceta: TipoReceta.ASEGURADO,
      fechaReceta: todayIso()
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
    const payload: CreateRecetaRequest = {
      pacienteId: Number(valor.pacienteId),
      numeroReceta: valor.numeroReceta.trim(),
      medico: valor.medico.trim(),
      tipoReceta: valor.tipoReceta,
      fechaReceta: valor.fechaReceta
    };

    this.guardando.set(true);
    this.formError.set('');

    this.service
      .createReceta(payload)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: creada => {
          this.cerrarModal();
          this.success.set(`Receta ${creada.numeroReceta} registrada para ${creada.pacienteNombreCompleto}.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar la receta.')
      });
  }

  trackById(_index: number, receta: Receta): number {
    return receta.id;
  }

  tipoBadge(tipo: TipoReceta): string {
    return tipo === TipoReceta.ASEGURADO ? 'fm-badge--info' : 'fm-badge--neutral';
  }
}
