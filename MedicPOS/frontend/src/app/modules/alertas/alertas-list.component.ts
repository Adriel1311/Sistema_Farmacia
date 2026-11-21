import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AlertaService } from '../../core/services/alerta.service';
import { AuthService } from '../../core/services/auth.service';
import { Alerta, AlertaPrioridad, AlertaTipo, ALERTA_TIPO_LABELS } from '../../core/models/alerta.model';
import { UserRole } from '../../core/models/user.model';

interface GrupoAlertas {
  prioridad: AlertaPrioridad;
  alertas: Alerta[];
}

const ORDEN_PRIORIDAD: AlertaPrioridad[] = [AlertaPrioridad.ALTA, AlertaPrioridad.MEDIA, AlertaPrioridad.BAJA];

const CLASES_PRIORIDAD: Record<AlertaPrioridad, string> = {
  [AlertaPrioridad.ALTA]: 'fm-badge--danger',
  [AlertaPrioridad.MEDIA]: 'fm-badge--warning',
  [AlertaPrioridad.BAJA]: 'fm-badge--neutral'
};

const ETIQUETAS_PRIORIDAD: Record<AlertaPrioridad, string> = {
  [AlertaPrioridad.ALTA]: 'Alta',
  [AlertaPrioridad.MEDIA]: 'Media',
  [AlertaPrioridad.BAJA]: 'Baja'
};

const ICONOS_TIPO: Record<AlertaTipo, string> = {
  [AlertaTipo.STOCK_CRITICO]: 'box',
  [AlertaTipo.PROXIMO_VENCER]: 'calendar',
  [AlertaTipo.VENCIDO]: 'warning',
  [AlertaTipo.ORDEN_SUGERIDA]: 'orden',
  [AlertaTipo.RECETA_VIGENTE]: 'receta'
};

@Component({
  selector: 'app-alertas-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './alertas-list.component.html',
  styleUrls: ['./alertas-list.component.scss']
})
export class AlertasListComponent {
  private readonly alertasService = inject(AlertaService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly puedeAtender = this.auth.hasRole(UserRole.JEFE_FARMACIA, UserRole.LOGISTICA);

  readonly pendientes = signal<Alerta[]>([]);
  readonly todas = signal<Alerta[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly soloPendientes = signal(true);
  readonly objetivo = signal<Alerta | null>(null);
  readonly guardando = signal(false);
  readonly formError = signal('');

  readonly form = this.fb.nonNullable.group({
    comentario: ['', [Validators.maxLength(500)]]
  });

  readonly visibles = computed<Alerta[]>(() => (this.soloPendientes() ? this.pendientes() : this.todas()));

  readonly grupos = computed<GrupoAlertas[]>(() => {
    const lista = this.visibles();
    return ORDEN_PRIORIDAD.map(prioridad => ({
      prioridad,
      alertas: lista.filter(alerta => alerta.prioridad === prioridad)
    })).filter(grupo => grupo.alertas.length > 0);
  });

  readonly total = computed(() => this.visibles().length);
  readonly totalAlta = computed(() => this.visibles().filter(alerta => alerta.prioridad === AlertaPrioridad.ALTA).length);
  readonly totalStockCritico = computed(
    () => this.visibles().filter(alerta => alerta.tipo === AlertaTipo.STOCK_CRITICO).length
  );
  readonly totalVencimientos = computed(
    () =>
      this.visibles().filter(
        alerta => alerta.tipo === AlertaTipo.VENCIDO || alerta.tipo === AlertaTipo.PROXIMO_VENCER
      ).length
  );

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    forkJoin({ pendientes: this.alertasService.getPendientes(), todas: this.alertasService.getAll() })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar las alertas.');
          return of({ pendientes: [] as Alerta[], todas: [] as Alerta[] });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(resultado => {
        this.pendientes.set(resultado.pendientes);
        this.todas.set(resultado.todas);
      });
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  alternarSoloPendientes(): void {
    this.soloPendientes.update(valor => !valor);
  }

  clasePrioridad(prioridad: AlertaPrioridad): string {
    return CLASES_PRIORIDAD[prioridad] ?? 'fm-badge--neutral';
  }

  etiquetaPrioridad(prioridad: AlertaPrioridad): string {
    return ETIQUETAS_PRIORIDAD[prioridad] ?? prioridad;
  }

  esPrioridadAlta(prioridad: AlertaPrioridad): boolean {
    return prioridad === AlertaPrioridad.ALTA;
  }

  etiquetaTipo(tipo: AlertaTipo): string {
    return ALERTA_TIPO_LABELS[tipo] ?? tipo;
  }

  iconoTipo(tipo: AlertaTipo): string {
    return ICONOS_TIPO[tipo] ?? 'alerta';
  }

  referencia(alerta: Alerta): string {
    return `${alerta.referenciaTipo} #${alerta.referenciaId}`;
  }

  puedeAtenderAlerta(alerta: Alerta): boolean {
    return this.puedeAtender && !alerta.atendida;
  }

  abrirAtender(alerta: Alerta): void {
    if (!this.puedeAtenderAlerta(alerta)) {
      return;
    }
    this.objetivo.set(alerta);
    this.formError.set('');
    this.form.reset({ comentario: '' });
  }

  cerrarAtender(): void {
    this.objetivo.set(null);
    this.formError.set('');
  }

  atender(): void {
    const alerta = this.objetivo();
    if (!alerta) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const comentario = this.form.getRawValue().comentario.trim();
    this.guardando.set(true);
    this.formError.set('');

    this.alertasService
      .atender({
        tipo: alerta.tipo,
        referenciaId: String(alerta.referenciaId),
        comentario
      })
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.cerrarAtender();
          this.success.set(`Alerta "${alerta.titulo}" atendida correctamente.`);
          this.cargar();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo atender la alerta.')
      });
  }

  trackByClave(_index: number, alerta: Alerta): string {
    return alerta.clave;
  }
}
