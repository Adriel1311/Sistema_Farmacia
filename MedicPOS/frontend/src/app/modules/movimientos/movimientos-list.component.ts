import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl } from '@angular/forms';
import { of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { InventarioService } from '../../core/services/inventario.service';
import { MovimientoInventario, TipoMovimiento } from '../../core/models/inventario.model';

interface OpcionTipo {
  valor: TipoMovimiento | '';
  etiqueta: string;
}

const OPCIONES_TIPO: OpcionTipo[] = [
  { valor: '', etiqueta: 'Todos los tipos' },
  { valor: TipoMovimiento.ENTRADA, etiqueta: 'Entrada' },
  { valor: TipoMovimiento.SALIDA, etiqueta: 'Salida' },
  { valor: TipoMovimiento.BAJA, etiqueta: 'Baja' },
  { valor: TipoMovimiento.AJUSTE, etiqueta: 'Ajuste' }
];

const CLASES_TIPO: Record<TipoMovimiento, string> = {
  [TipoMovimiento.ENTRADA]: 'fm-badge--success',
  [TipoMovimiento.SALIDA]: 'fm-badge--info',
  [TipoMovimiento.BAJA]: 'fm-badge--danger',
  [TipoMovimiento.AJUSTE]: 'fm-badge--neutral'
};

const ETIQUETAS_TIPO: Record<TipoMovimiento, string> = {
  [TipoMovimiento.ENTRADA]: 'Entrada',
  [TipoMovimiento.SALIDA]: 'Salida',
  [TipoMovimiento.BAJA]: 'Baja',
  [TipoMovimiento.AJUSTE]: 'Ajuste'
};

@Component({
  selector: 'app-movimientos-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './movimientos-list.component.html',
  styleUrls: ['./movimientos-list.component.scss']
})
export class MovimientosListComponent {
  private readonly servicio = inject(InventarioService);
  private readonly destroyRef = inject(DestroyRef);

  readonly opcionesTipo = OPCIONES_TIPO;

  readonly movimientos = signal<MovimientoInventario[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly tipoSeleccionado = signal<TipoMovimiento | ''>('');
  readonly texto = signal('');

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly visibles = computed<MovimientoInventario[]>(() => {
    const tipo = this.tipoSeleccionado();
    const texto = this.texto().trim().toLowerCase();
    return this.movimientos().filter(movimiento => {
      if (tipo !== '' && movimiento.tipoMovimiento !== tipo) {
        return false;
      }
      if (texto.length === 0) {
        return true;
      }
      return (
        movimiento.medicamentoNombre.toLowerCase().includes(texto) || movimiento.numeroLote.toLowerCase().includes(texto)
      );
    });
  });

  readonly totalEntradas = computed(
    () => this.movimientos().filter(movimiento => movimiento.tipoMovimiento === TipoMovimiento.ENTRADA).length
  );
  readonly totalSalidas = computed(
    () => this.movimientos().filter(movimiento => movimiento.tipoMovimiento === TipoMovimiento.SALIDA).length
  );
  readonly totalBajas = computed(
    () => this.movimientos().filter(movimiento => movimiento.tipoMovimiento === TipoMovimiento.BAJA).length
  );

  constructor() {
    this.searchControl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(valor => this.texto.set(valor));

    this.cargar();
  }

  refrescar(): void {
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    this.servicio
      .getMovimientos()
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los movimientos de inventario.');
          return of([] as MovimientoInventario[]);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.movimientos.set(lista));
  }

  cambiarTipo(valor: string): void {
    const encontrado = this.opcionesTipo.find(opcion => opcion.valor === valor);
    this.tipoSeleccionado.set(encontrado ? encontrado.valor : '');
  }

  limpiarFiltros(): void {
    this.searchControl.setValue('', { emitEvent: true });
    this.tipoSeleccionado.set('');
  }

  claseTipo(tipo: TipoMovimiento): string {
    return CLASES_TIPO[tipo] ?? 'fm-badge--neutral';
  }

  etiquetaTipo(tipo: TipoMovimiento): string {
    return ETIQUETAS_TIPO[tipo] ?? tipo;
  }

  referencia(movimiento: MovimientoInventario): string {
    return movimiento.referenciaTipo ? `${movimiento.referenciaTipo} #${movimiento.referenciaId}` : 'Sin referencia';
  }

  trackById(_index: number, movimiento: MovimientoInventario): number {
    return movimiento.id;
  }
}
