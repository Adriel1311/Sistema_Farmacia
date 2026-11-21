import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { combineLatest, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { CajaService } from '../../core/services/caja.service';
import { Caja, CajaEstado, MovimientoCaja, TipoMovimientoCaja } from '../../core/models/caja.model';

type FiltroTipo = TipoMovimientoCaja | '';

const OPCIONES_TIPO: { valor: FiltroTipo; etiqueta: string }[] = [
  { valor: '', etiqueta: 'Todos los movimientos' },
  { valor: TipoMovimientoCaja.VENTA, etiqueta: 'Ventas' },
  { valor: TipoMovimientoCaja.SALIDA, etiqueta: 'Salidas' },
  { valor: TipoMovimientoCaja.DEVOLUCION, etiqueta: 'Devoluciones' },
  { valor: TipoMovimientoCaja.APERTURA, etiqueta: 'Apertura' }
];

const ETIQUETAS_TIPO: Record<TipoMovimientoCaja, string> = {
  [TipoMovimientoCaja.APERTURA]: 'Apertura',
  [TipoMovimientoCaja.VENTA]: 'Venta',
  [TipoMovimientoCaja.SALIDA]: 'Salida',
  [TipoMovimientoCaja.DEVOLUCION]: 'Devolución'
};

const CLASES_TIPO: Record<TipoMovimientoCaja, string> = {
  [TipoMovimientoCaja.APERTURA]: 'fm-badge--neutral',
  [TipoMovimientoCaja.VENTA]: 'fm-badge--success',
  [TipoMovimientoCaja.SALIDA]: 'fm-badge--danger',
  [TipoMovimientoCaja.DEVOLUCION]: 'fm-badge--warning'
};

@Component({
  selector: 'app-caja',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './caja.component.html',
  styleUrls: ['./caja.component.scss']
})
export class CajaComponent {
  private readonly servicio = inject(CajaService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly opcionesTipo = OPCIONES_TIPO;
  readonly CajaEstado = CajaEstado;
  readonly Tipo = TipoMovimientoCaja;

  readonly cajaActual = signal<Caja | null>(null);
  readonly historial = signal<Caja[]>([]);
  /** Caja cuyos movimientos se muestran: la abierta o una anterior elegida del historial. */
  readonly cajaVista = signal<Caja | null>(null);
  readonly movimientos = signal<MovimientoCaja[]>([]);

  readonly loading = signal(false);
  readonly loadingMov = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly formError = signal('');

  readonly filtroTipo = signal<FiltroTipo>('');

  readonly modalApertura = signal(false);
  readonly modalSalida = signal(false);
  readonly modalCierre = signal(false);
  readonly detalleVenta = signal<MovimientoCaja | null>(null);

  readonly aperturaForm = this.fb.nonNullable.group({
    montoInicial: [0, [Validators.required, Validators.min(0)]],
    observacion: ['', [Validators.maxLength(255)]]
  });

  readonly salidaForm = this.fb.nonNullable.group({
    monto: [0, [Validators.required, Validators.min(0.01)]],
    concepto: ['', [Validators.required, Validators.maxLength(255)]]
  });

  readonly cierreForm = this.fb.nonNullable.group({
    montoContado: [0, [Validators.required, Validators.min(0)]],
    observacion: ['', [Validators.maxLength(255)]]
  });

  readonly hayCajaAbierta = computed(() => this.cajaActual() !== null);

  /** Se pueden registrar salidas y cerrar solo si la caja que se ve es la abierta. */
  readonly viendoCajaAbierta = computed(() => {
    const vista = this.cajaVista();
    return vista !== null && vista.estado === CajaEstado.ABIERTA;
  });

  readonly visibles = computed(() => {
    const tipo = this.filtroTipo();
    return tipo === '' ? this.movimientos() : this.movimientos().filter(m => m.tipo === tipo);
  });

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.success.set('');
    this.cargar(this.cajaVista()?.id);
  }

  // ------------------------------------------------------------------
  // Carga
  // ------------------------------------------------------------------

  private cargar(seleccionarId?: number): void {
    this.loading.set(true);
    this.error.set('');

    combineLatest({
      actual: this.servicio.actual().pipe(catchError(() => of(null))),
      historial: this.servicio.historial().pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo cargar la caja.');
          return of([] as Caja[]);
        })
      )
    })
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ actual, historial }) => {
        this.cajaActual.set(actual);
        this.historial.set(historial);

        const elegida =
          historial.find(c => c.id === seleccionarId) ??
          (actual ? historial.find(c => c.id === actual.id) ?? actual : null) ??
          historial[0] ??
          null;
        this.cajaVista.set(elegida);
        this.cargarMovimientos(elegida);
      });
  }

  private cargarMovimientos(caja: Caja | null): void {
    if (!caja) {
      this.movimientos.set([]);
      return;
    }
    this.loadingMov.set(true);
    this.servicio
      .movimientos(caja.id)
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los movimientos de la caja.');
          return of([] as MovimientoCaja[]);
        }),
        finalize(() => this.loadingMov.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.movimientos.set(lista));
  }

  verCaja(caja: Caja): void {
    this.cajaVista.set(caja);
    this.filtroTipo.set('');
    this.cargarMovimientos(caja);
  }

  cambiarFiltro(valor: string): void {
    const encontrado = this.opcionesTipo.find(opcion => opcion.valor === valor);
    this.filtroTipo.set(encontrado ? encontrado.valor : '');
  }

  // ------------------------------------------------------------------
  // Apertura
  // ------------------------------------------------------------------

  abrirApertura(): void {
    this.formError.set('');
    this.aperturaForm.reset({ montoInicial: 0, observacion: '' });
    this.modalApertura.set(true);
  }

  cerrarApertura(): void {
    this.modalApertura.set(false);
    this.formError.set('');
  }

  guardarApertura(): void {
    if (this.aperturaForm.invalid) {
      this.aperturaForm.markAllAsTouched();
      return;
    }
    const valor = this.aperturaForm.getRawValue();
    this.guardando.set(true);
    this.formError.set('');
    this.servicio
      .abrir({ montoInicial: Number(valor.montoInicial), observacion: valor.observacion.trim() || undefined })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: caja => {
          this.modalApertura.set(false);
          this.success.set('Caja abierta. Ya puedes cobrar las recetas particulares.');
          this.filtroTipo.set('');
          this.cargar(caja.id);
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo abrir la caja.')
      });
  }

  // ------------------------------------------------------------------
  // Salida (retiro de dinero)
  // ------------------------------------------------------------------

  abrirSalida(): void {
    this.formError.set('');
    this.salidaForm.reset({ monto: 0, concepto: '' });
    this.modalSalida.set(true);
  }

  cerrarSalida(): void {
    this.modalSalida.set(false);
    this.formError.set('');
  }

  guardarSalida(): void {
    if (this.salidaForm.invalid) {
      this.salidaForm.markAllAsTouched();
      return;
    }
    const valor = this.salidaForm.getRawValue();
    this.guardando.set(true);
    this.formError.set('');
    this.servicio
      .registrarSalida({ monto: Number(valor.monto), concepto: valor.concepto.trim() })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.modalSalida.set(false);
          this.success.set('Salida registrada en la caja.');
          this.cargar(this.cajaVista()?.id);
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar la salida.')
      });
  }

  // ------------------------------------------------------------------
  // Cierre con arqueo
  // ------------------------------------------------------------------

  abrirCierre(): void {
    const caja = this.cajaActual();
    this.formError.set('');
    this.cierreForm.reset({ montoContado: caja ? caja.saldoActual : 0, observacion: '' });
    this.modalCierre.set(true);
  }

  cerrarCierre(): void {
    this.modalCierre.set(false);
    this.formError.set('');
  }

  /** Diferencia entre el efectivo contado y el esperado, para mostrarla antes de confirmar. */
  diferenciaEstimada(): number {
    const caja = this.cajaActual();
    if (!caja) {
      return 0;
    }
    const contado = Number(this.cierreForm.controls.montoContado.value) || 0;
    return Math.round((contado - caja.saldoActual) * 100) / 100;
  }

  guardarCierre(): void {
    if (this.cierreForm.invalid) {
      this.cierreForm.markAllAsTouched();
      return;
    }
    const valor = this.cierreForm.getRawValue();
    this.guardando.set(true);
    this.formError.set('');
    this.servicio
      .cerrar({ montoContado: Number(valor.montoContado), observacion: valor.observacion.trim() || undefined })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: caja => {
          this.modalCierre.set(false);
          this.success.set('Caja cerrada correctamente.');
          this.cargar(caja.id);
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo cerrar la caja.')
      });
  }

  // ------------------------------------------------------------------
  // Detalle de venta
  // ------------------------------------------------------------------

  verDetalle(movimiento: MovimientoCaja): void {
    this.detalleVenta.set(movimiento);
  }

  cerrarDetalle(): void {
    this.detalleVenta.set(null);
  }

  tieneDetalle(movimiento: MovimientoCaja): boolean {
    return movimiento.dispensacionId !== null && (movimiento.items?.length ?? 0) > 0;
  }

  totalDetalle(movimiento: MovimientoCaja): number {
    return movimiento.monto;
  }

  // ------------------------------------------------------------------
  // Presentacion
  // ------------------------------------------------------------------

  etiquetaTipo(tipo: TipoMovimientoCaja): string {
    return ETIQUETAS_TIPO[tipo] ?? tipo;
  }

  claseTipo(tipo: TipoMovimientoCaja): string {
    return CLASES_TIPO[tipo] ?? 'fm-badge--neutral';
  }

  trackMovimiento(_index: number, movimiento: MovimientoCaja): number {
    return movimiento.id;
  }

  trackCaja(_index: number, caja: Caja): number {
    return caja.id;
  }
}
