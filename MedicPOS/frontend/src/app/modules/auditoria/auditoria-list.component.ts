import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuditoriaService } from '../../core/services/reporte.service';
import { Auditoria } from '../../core/models/reporte.model';

type OrigenConsulta = 'todas' | 'rango' | 'entidad' | 'usuario';

@Component({
  selector: 'app-auditoria-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './auditoria-list.component.html',
  styleUrls: ['./auditoria-list.component.scss']
})
export class AuditoriaListComponent {
  private readonly servicio = inject(AuditoriaService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly auditorias = signal<Auditoria[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly origen = signal<OrigenConsulta>('todas');

  readonly filtros = this.fb.nonNullable.group({
    entidad: ['', [Validators.maxLength(60)]],
    usuarioId: [0, [Validators.min(0)]],
    desde: ['', [Validators.maxLength(20)]],
    hasta: ['', [Validators.maxLength(20)]]
  });

  readonly visibles = computed(() => this.auditorias());

  readonly total = computed(() => this.auditorias().length);
  readonly totalEntidades = computed(() => new Set(this.auditorias().map(item => item.entidad)).size);
  readonly totalUsuarios = computed(() => new Set(this.auditorias().map(item => item.usuarioId)).size);

  constructor() {
    this.cargar(this.servicio.getAll());
  }

  refrescar(): void {
    this.buscar();
  }

  buscar(): void {
    if (this.filtros.invalid) {
      this.filtros.markAllAsTouched();
      return;
    }
    this.limpiarError();

    const valor = this.filtros.getRawValue();
    const entidad = valor.entidad.trim();
    const usuarioId = Number(valor.usuarioId ?? 0);
    const desde = this.aIsoLocal(valor.desde);
    const hasta = this.aIsoLocal(valor.hasta);

    if (desde && hasta) {
      this.cargar(this.servicio.getByRango(desde, hasta), 'rango', lista => lista.filter(this.fueraDeRango(desde, hasta)));
      return;
    }
    if (entidad) {
      this.cargar(this.servicio.getByEntidad(entidad), 'entidad', lista =>
        lista.filter(item => this.coincideUsuario(item, usuarioId) && this.estaEnRango(item, desde, hasta))
      );
      return;
    }
    if (usuarioId > 0) {
      this.cargar(this.servicio.getByUsuario(usuarioId), 'usuario', lista =>
        lista.filter(item => this.coincideEntidad(item, entidad) && this.estaEnRango(item, desde, hasta))
      );
      return;
    }
    this.cargar(this.servicio.getAll(), 'todas');
  }

  limpiarFiltros(): void {
    this.filtros.reset({ entidad: '', usuarioId: 0, desde: '', hasta: '' });
    this.limpiarError();
    this.cargar(this.servicio.getAll(), 'todas');
  }

  private cargar(
    request$: Observable<Auditoria[]>,
    origen: OrigenConsulta = 'todas',
    refinar?: (lista: Auditoria[]) => Auditoria[]
  ): void {
    this.loading.set(true);
    this.error.set('');
    this.origen.set(origen);

    request$
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los registros de auditoria.');
          return of([] as Auditoria[]);
        }),
        finalize(() => this.loading.set(false)),
        tap(lista => this.auditorias.set(refinar ? refinar(lista) : lista)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();
  }

  private limpiarError(): void {
    this.error.set('');
  }

  private coincideEntidad(item: Auditoria, entidad: string): boolean {
    return entidad.length === 0 || item.entidad.toLowerCase().includes(entidad.toLowerCase());
  }

  private coincideUsuario(item: Auditoria, usuarioId: number): boolean {
    return usuarioId <= 0 || item.usuarioId === usuarioId;
  }

  private estaEnRango(item: Auditoria, desde: string, hasta: string): boolean {
    if (!desde && !hasta) {
      return true;
    }
    const momento = Date.parse(item.fechaHora);
    if (Number.isNaN(momento)) {
      return true;
    }
    const limiteInferior = desde ? Date.parse(desde) : Number.NEGATIVE_INFINITY;
    const limiteSuperior = hasta ? Date.parse(hasta) : Number.POSITIVE_INFINITY;
    return momento >= limiteInferior && momento <= limiteSuperior;
  }

  private fueraDeRango(desde: string, hasta: string): (item: Auditoria) => boolean {
    return (item: Auditoria) => this.estaEnRango(item, desde, hasta);
  }

  /** `datetime-local` entrega "yyyy-MM-ddTHH:mm"; la API espera segundos. */
  private aIsoLocal(valor: string): string {
    const limpio = valor.trim();
    if (limpio.length === 0) {
      return '';
    }
    return limpio.length === 16 ? `${limpio}:00` : limpio;
  }

  descripcionOrigen(): string {
    switch (this.origen()) {
      case 'rango':
        return 'Mostrando el rango de fechas indicado.';
      case 'entidad':
        return 'Mostrando los registros de la entidad indicada.';
      case 'usuario':
        return 'Mostrando los registros del usuario indicado.';
      default:
        return 'Mostrando todos los registros de auditoria.';
    }
  }

  trackById(_index: number, item: Auditoria): number {
    return item.id;
  }
}
