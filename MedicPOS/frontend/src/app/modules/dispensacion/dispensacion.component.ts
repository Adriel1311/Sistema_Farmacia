import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { combineLatest, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuthService } from '../../core/services/auth.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { ConfiguracionService } from '../../core/services/alerta.service';
import { DispensacionService } from '../../core/services/dispensacion.service';
import { InventarioService } from '../../core/services/inventario.service';
import { PacienteRecetaService } from '../../core/services/paciente-receta.service';
import { Medicamento } from '../../core/models/catalogo.model';
import { Dispensacion, DispensacionFormItem, DispensacionItemRequest } from '../../core/models/dispensacion.model';
import { Lote, MovimientoInventario } from '../../core/models/inventario.model';
import { Receta, TipoReceta } from '../../core/models/receta.model';
import { UserRole } from '../../core/models/user.model';

type Vista = 'CAJA' | 'HISTORIAL';

@Component({
  selector: 'app-dispensacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dispensacion.component.html',
  styleUrls: ['./dispensacion.component.scss']
})
export class DispensacionComponent {
  private readonly servicio = inject(DispensacionService);
  private readonly recetasService = inject(PacienteRecetaService);
  private readonly inventario = inject(InventarioService);
  private readonly catalogo = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly configService = inject(ConfiguracionService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly TIPO_RECETA_LABELS = { ASEGURADO: 'Asegurado', PARTICULAR: 'Particular' } as Record<TipoReceta, string>;

  /** Solo el Jefe de Farmacia puede autorizar excepción al lote sugerido (RSP-07), y solo si la config lo permite. */
  readonly excepcionLoteDistintoPermitida = signal(true);
  readonly puedeExcepcionLoteDistinto = computed(() => 
    this.auth.hasRole(UserRole.JEFE_FARMACIA) && this.excepcionLoteDistintoPermitida()
  );

  readonly vista = signal<Vista>('CAJA');

  readonly recetas = signal<Receta[]>([]);
  readonly medicamentos = signal<Medicamento[]>([]);
  readonly historico = signal<Dispensacion[]>([]);

  readonly recetaSeleccionada = signal<Receta | null>(null);
  readonly searchReceta = signal('');
  readonly searchMedicamento = signal('');

  readonly items = signal<DispensacionFormItem[]>([]);
  readonly loteInspeccion = signal<Lote | null>(null);
  readonly movimientosLote = signal<MovimientoInventario[]>([]);

  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly excepcionModal = signal(false);
  readonly excepcionObjetivo = signal<{ item: DispensacionFormItem; lote: Lote } | null>(null);
  readonly ticketDetalle = signal<Dispensacion | null>(null);
  readonly confirmarAnular = signal<Dispensacion | null>(null);

  readonly excepcionForm = this.fb.nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(300)]]
  });

  readonly recetasFiltradas = computed(() => {
    const texto = this.searchReceta().trim().toLowerCase();
    return this.recetas().filter(receta => {
      if (receta.tieneDispensacion) {
        return false;
      }
      if (!texto) {
        return true;
      }
      return (
        receta.numeroReceta.toLowerCase().includes(texto) ||
        receta.pacienteNombreCompleto.toLowerCase().includes(texto) ||
        receta.pacienteDni.includes(texto)
      );
    });
  });

  readonly medicamentosFiltrados = computed(() => {
    const texto = this.searchMedicamento().trim().toLowerCase();
    if (!texto) {
      return this.medicamentos();
    }
    return this.medicamentos().filter(
      m => m.nombre.toLowerCase().includes(texto) || m.codigo.toLowerCase().includes(texto)
    );
  });

  readonly totalUnidades = computed(() => this.items().reduce((suma, item) => suma + item.cantidad, 0));

  readonly puedeConfirmar = computed(
    () => this.recetaSeleccionada() !== null && this.items().length > 0 && this.items().every(i => i.cantidad > 0 && i.loteSeleccionadoId !== null)
  );

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.cargar();
  }

  cambiarVista(vista: Vista): void {
    this.vista.set(vista);
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  onSearchReceta(event: Event): void {
    this.searchReceta.set((event.target as HTMLInputElement).value);
  }

  onSearchMedicamento(event: Event): void {
    this.searchMedicamento.set((event.target as HTMLInputElement).value);
  }

  seleccionarReceta(receta: Receta): void {
    this.recetaSeleccionada.set(receta);
    this.items.set([]);
    this.limpiarMensajes();
  }

  limpiarReceta(): void {
    this.recetaSeleccionada.set(null);
    this.items.set([]);
  }

  yaAgregado(medicamentoId: number): boolean {
    return this.items().some(item => item.medicamentoId === medicamentoId);
  }

  agregarMedicamento(medicamento: Medicamento): void {
    if (this.yaAgregado(medicamento.id)) {
      return;
    }

    this.inventario
      .getLotesPorMedicamento(medicamento.id)
      .pipe(
        catchError(() => of([] as Lote[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lotes => {
        // RN03: nunca se despensa un lote vencido.
        const dispensables = lotes
          .filter(lote => lote.estado === 'ACTIVO' && lote.cantidadActual > 0)
          .sort((a, b) => a.diasRestantes - b.diasRestantes);
        const sugerido = dispensables[0];
        this.items.update(items => [
          ...items,
          {
            medicamentoId: medicamento.id,
            medicamentoCodigo: medicamento.codigo,
            medicamentoNombre: medicamento.nombre,
            unidadMedida: medicamento.unidadMedida,
            lotesDisponibles: dispensables,
            loteSeleccionadoId: sugerido?.id ?? null,
            cantidad: 1,
            excepcionLoteDistinto: false,
            motivoExcepcionLoteDistinto: ''
          }
        ]);
        this.searchMedicamento.set('');
      });
  }

  quitarItem(medicamentoId: number): void {
    this.items.update(items => items.filter(item => item.medicamentoId !== medicamentoId));
  }

  setCantidad(medicamentoId: number, valor: string): void {
    const cantidad = Math.max(0, Number(valor) || 0);
    this.items.update(items =>
      items.map(item =>
        item.medicamentoId === medicamentoId
          ? { ...item, cantidad, error: this.validarItem(item, cantidad, item.loteSeleccionadoId) }
          : item
      )
    );
  }

  setLote(medicamentoId: number, loteId: number, esUltimoPorVencimiento: boolean): void {
    const item = this.items().find(i => i.medicamentoId === medicamentoId);
    if (!item) {
      return;
    }

    if (esUltimoPorVencimiento && !this.puedeExcepcionLoteDistinto) {
      this.error.set(
        'Solo el Jefe de Farmacia puede autorizar un lote distinto al sugerido (el que vence primero). Deja el lote sugerido o solicita autorización.'
      );
      return;
    }

    const loteSugerido = item.lotesDisponibles[0];
    const loteElegido = item.lotesDisponibles.find(l => l.id === loteId);
    const usaExcepcion = esUltimoPorVencimiento && loteSugerido !== undefined && loteElegido !== undefined && loteElegido.id !== loteSugerido.id;

    this.items.update(items =>
      items.map(i =>
        i.medicamentoId === medicamentoId
          ? {
              ...i,
              loteSeleccionadoId: loteId,
              excepcionLoteDistinto: usaExcepcion,
              motivoExcepcionLoteDistinto: usaExcepcion ? i.motivoExcepcionLoteDistinto : '',
              error: ''
            }
          : i
      )
    );
  }

  abrirExcepcion(item: DispensacionFormItem): void {
    if (!this.excepcionLoteDistintoPermitida()) {
      this.error.set('Las excepciones al lote sugerido están deshabilitadas en la configuración del sistema.');
      return;
    }
    this.excepcionForm.reset({ motivo: '' });
    this.excepcionObjetivo.set({ item, lote: item.lotesDisponibles[0] });
    this.excepcionModal.set(true);
  }

  cerrarExcepcion(): void {
    this.excepcionModal.set(false);
    this.excepcionObjetivo.set(null);
  }

  confirmarExcepcion(): void {
    const objetivo = this.excepcionObjetivo();
    if (!objetivo) {
      return;
    }
    if (this.excepcionForm.invalid) {
      this.excepcionForm.markAllAsTouched();
      return;
    }
    const motivo = this.excepcionForm.getRawValue().motivo.trim();
    const loteElegido = objetivo.item.lotesDisponibles.find(l => l.id !== objetivo.lote.id);

    if (!loteElegido) {
      this.error.set('Ese medicamento solo tiene un lote disponible, no hay excepción que aplicar.');
      this.cerrarExcepcion();
      return;
    }

    this.items.update(items =>
      items.map(i =>
        i.medicamentoId === objetivo.item.medicamentoId
          ? { ...i, loteSeleccionadoId: loteElegido.id, excepcionLoteDistinto: true, motivoExcepcionLoteDistinto: motivo, error: '' }
          : i
      )
    );
    this.cerrarExcepcion();
    this.success.set('Excepción registrada. Quedará en la auditoría de la dispensación.');
  }

  confirmar(): void {
    const receta = this.recetaSeleccionada();
    if (!receta) {
      this.error.set('Selecciona primero la receta a dispensar.');
      return;
    }
    if (!this.puedeConfirmar()) {
      this.error.set('Revisa los items: cada uno necesita cantidad y lote asignado.');
      return;
    }

    const payload: DispensacionItemRequest[] = this.items().map(item => ({
      medicamentoId: item.medicamentoId,
      cantidad: item.cantidad,
      loteId: item.loteSeleccionadoId,
      motivoExcepcionLoteDistinto: item.excepcionLoteDistinto ? item.motivoExcepcionLoteDistinto : null
    }));

    this.guardando.set(true);
    this.error.set('');

    this.servicio
      .create({ recetaId: receta.id, items: payload })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: dispensacion => {
          this.success.set(`Dispensacion ${dispensacion.numeroTicket} registrada correctamente.`);
          this.limpiarReceta();
          this.ticketDetalle.set(dispensacion);
          this.cargar();
        },
        error: (err: Error) => this.error.set(err.message || 'No se pudo registrar la dispensacion.')
      });
  }

  verTicket(dispensacion: Dispensacion): void {
    this.ticketDetalle.set(dispensacion);
  }

  cerrarTicket(): void {
    this.ticketDetalle.set(null);
  }

  pedirAnular(dispensacion: Dispensacion): void {
    this.confirmarAnular.set(dispensacion);
  }

  cerrarAnular(): void {
    this.confirmarAnular.set(null);
  }

  anular(): void {
    const dispensacion = this.confirmarAnular();
    if (!dispensacion) {
      return;
    }
    this.servicio
      .anular(dispensacion.id)
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.confirmarAnular.set(null);
          this.success.set(`Dispensacion ${dispensacion.numeroTicket} anulada. El stock volvio a los lotes de origen.`);
          this.cargar();
        },
        error: (err: Error) => {
          this.guardando.set(false);
          this.confirmarAnular.set(null);
          this.error.set(err.message || 'No se pudo anular la dispensacion.');
        }
      });
    this.guardando.set(true);
  }

  inspeccionarLote(lote: Lote): void {
    this.loteInspeccion.set(lote);
    this.movimientosLote.set([]);
    this.inventario
      .getMovimientosPorLote(lote.id)
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(movimientos => this.movimientosLote.set(movimientos));
  }

  cerrarLote(): void {
    this.loteInspeccion.set(null);
    this.movimientosLote.set([]);
  }

  etiquetaReceta(tipo: TipoReceta): string {
    return this.TIPO_RECETA_LABELS[tipo] ?? tipo;
  }

  private validarItem(item: DispensacionFormItem, cantidad: number, loteId: number | null): string {
    if (cantidad <= 0) {
      return 'La cantidad debe ser mayor a cero.';
    }
    if (loteId === null) {
      return 'Selecciona un lote.';
    }
    const lote = item.lotesDisponibles.find(l => l.id === loteId);
    if (lote && cantidad > lote.cantidadActual) {
      return `El lote ${lote.numeroLote} solo tiene ${lote.cantidadActual} disponibles.`;
    }
    if (item.lotesDisponibles.length === 0) {
      return 'No hay lotes vigentes para este medicamento.';
    }
    return '';
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    combineLatest({
      recetas: this.recetasService.getRecetas(),
      medicamentos: this.catalogo.getMedicamentos(),
      historico: this.servicio.getAll(),
      fefoConfig: this.configService.getFefoPermitirExcepcion()
    })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo cargar la caja.');
          return of({ recetas: [] as Receta[], medicamentos: [] as Medicamento[], historico: [] as Dispensacion[], fefoConfig: true });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ recetas, medicamentos, historico, fefoConfig }) => {
        this.recetas.set(recetas);
        this.medicamentos.set(medicamentos);
        this.historico.set(historico);
        this.excepcionLoteDistintoPermitida.set(fefoConfig);
      });
  }

  trackReceta(_index: number, receta: Receta): number {
    return receta.id;
  }

  trackMedicamento(_index: number, medicamento: Medicamento): number {
    return medicamento.id;
  }

  trackItem(_index: number, item: DispensacionFormItem): number {
    return item.medicamentoId;
  }

  trackDispensacion(_index: number, dispensacion: Dispensacion): number {
    return dispensacion.id;
  }
}
