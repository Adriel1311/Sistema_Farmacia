import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, of } from 'rxjs';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AlertaService } from '../../core/services/alerta.service';
import { DashboardService } from '../../core/services/reporte.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { Lote } from '../../core/models/inventario.model';
import { SugerenciaCompra } from '../../core/models/orden-compra.model';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly alertasService = inject(AlertaService);
  private readonly destroyRef = inject(DestroyRef);

  readonly data = signal<Dashboard | null>(null);
  readonly alertasAlta = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly today = new Date();

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.error.set('');
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    this.dashboardService
      .get()
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo cargar el panel.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => this.data.set(data));

    this.alertasService
      .getPendientes()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(alertas => this.alertasAlta.set(alertas.filter(a => a.prioridad === 'ALTA').length));
  }

  trackLote(_index: number, lote: Lote): number {
    return lote.id;
  }

  trackSugerencia(_index: number, sugerencia: SugerenciaCompra): number {
    return sugerencia.medicamentoId;
  }
}
