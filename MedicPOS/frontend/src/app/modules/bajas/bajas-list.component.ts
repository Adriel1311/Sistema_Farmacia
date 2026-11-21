import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, FormControl, ValidationErrors, Validators } from '@angular/forms';
import { Observable, Subject, of } from 'rxjs';
import { catchError, finalize, startWith, switchMap, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { InventarioService } from '../../core/services/inventario.service';
import { BajaInventario, BajaMotivo, Lote, RegistrarBajaRequest } from '../../core/models/inventario.model';

interface OpcionMotivo {
  valor: BajaMotivo | '';
  etiqueta: string;
}

const OPCIONES_MOTIVO: OpcionMotivo[] = [
  { valor: '', etiqueta: 'Todos los motivos' },
  { valor: BajaMotivo.VENCIDO_EN_ESTANTE, etiqueta: 'Vencido en estante' },
  { valor: BajaMotivo.MERMA, etiqueta: 'Merma' },
  { valor: BajaMotivo.DEVUELTO, etiqueta: 'Devuelto' }
];

const CLASES_MOTIVO: Record<BajaMotivo, string> = {
  [BajaMotivo.VENCIDO_EN_ESTANTE]: 'fm-badge--danger',
  [BajaMotivo.MERMA]: 'fm-badge--warning',
  [BajaMotivo.DEVUELTO]: 'fm-badge--neutral'
};

const ETIQUETAS_MOTIVO: Record<BajaMotivo, string> = {
  [BajaMotivo.VENCIDO_EN_ESTANTE]: 'Vencido en estante',
  [BajaMotivo.MERMA]: 'Merma',
  [BajaMotivo.DEVUELTO]: 'Devuelto'
};

/** La cantidad de baja no puede superar las unidades disponibles del lote elegido. */
function cantidadDentroDelLote(obtenerLotes: () => Lote[]): (control: AbstractControl) => ValidationErrors | null {
  return (control: AbstractControl): ValidationErrors | null => {
    const loteId = Number(control.get('loteId')?.value ?? 0);
    const cantidad = Number(control.get('cantidad')?.value ?? 0);
    if (!loteId || !cantidad) {
      return null;
    }
    const lote = obtenerLotes().find(item => item.id === loteId);
    if (!lote) {
      return null;
    }
    return cantidad <= lote.cantidadActual ? null : { cantidadExcedeLote: { max: lote.cantidadActual } };
  };
}

@Component({
  selector: 'app-bajas-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './bajas-list.component.html',
  styleUrls: ['./bajas-list.component.scss']
})
export class BajasListComponent {
  private readonly servicio = inject(InventarioService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly opcionesMotivo = OPCIONES_MOTIVO;

  readonly bajas = signal<BajaInventario[]>([]);
  readonly lotes = signal<Lote[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly motivoSeleccionado = signal<BajaMotivo | ''>('');
  readonly texto = signal('');

  readonly modalAbierto = signal(false);
  readonly guardando = signal(false);
  readonly formError = signal('');

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly form = this.fb.nonNullable.group(
    {
      loteId: [0, [Validators.required, Validators.min(1)]],
      motivo: [BajaMotivo.VENCIDO_EN_ESTANTE as BajaMotivo, [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      observacion: ['', [Validators.maxLength(500)]]
    },
    { validators: cantidadDentroDelLote(() => this.lotes()) }
  );

  readonly motivosRegistrables: OpcionMotivo[] = OPCIONES_MOTIVO.filter(opcion => opcion.valor !== '');

  readonly visibles = computed<BajaInventario[]>(() => {
    const texto = this.texto().trim().toLowerCase();
    if (texto.length === 0) {
      return this.bajas();
    }
    return this.bajas().filter(baja =>
      [baja.medicamentoNombre, baja.medicamentoCodigo, baja.numeroLote, baja.observacion, baja.usuarioNombre].some(campo =>
        campo.toLowerCase().includes(texto)
      )
    );
  });

  readonly totalUnidades = computed(() => this.visibles().reduce((total, baja) => total + baja.cantidad, 0));
  readonly totalVencidos = computed(
    () => this.visibles().filter(baja => baja.motivo === BajaMotivo.VENCIDO_EN_ESTANTE).length
  );
  readonly totalMermas = computed(() => this.visibles().filter(baja => baja.motivo === BajaMotivo.MERMA).length);

  private readonly reload$ = new Subject<void>();

  constructor() {
    this.searchControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(valor => this.texto.set(valor));

    this.reload$
      .pipe(
        startWith(undefined),
        switchMap(() => this.cargarBajas()),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe();

    this.servicio
      .getLotes()
      .pipe(
        catchError(() => of([] as Lote[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.lotes.set(lista));
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.reload$.next();
  }

  private cargarBajas(): Observable<BajaInventario[]> {
    this.loading.set(true);
    this.error.set('');
    const motivo = this.motivoSeleccionado();

    return (motivo === '' ? this.servicio.getBajas() : this.servicio.getBajasPorMotivo(motivo)).pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar las bajas de inventario.');
        return of([] as BajaInventario[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.bajas.set(lista))
    );
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  cambiarMotivo(valor: string): void {
    const encontrado = this.opcionesMotivo.find(opcion => opcion.valor === valor);
    this.motivoSeleccionado.set(encontrado ? encontrado.valor : '');
    this.refrescar();
  }

  limpiarFiltros(): void {
    this.searchControl.setValue('');
    this.motivoSeleccionado.set('');
    this.refrescar();
  }

  claseMotivo(motivo: BajaMotivo): string {
    return CLASES_MOTIVO[motivo] ?? 'fm-badge--neutral';
  }

  etiquetaMotivo(motivo: BajaMotivo): string {
    return ETIQUETAS_MOTIVO[motivo] ?? motivo;
  }

  etiquetaLote(lote: Lote): string {
    return `${lote.numeroLote} - ${lote.medicamentoNombre} - ${lote.cantidadActual} ${lote.unidadMedida}`;
  }

  loteDelFormulario(): Lote | null {
    const loteId = Number(this.form.getRawValue().loteId ?? 0);
    return this.lotes().find(lote => lote.id === loteId) ?? null;
  }

  abrirRegistro(): void {
    this.formError.set('');
    this.form.reset({
      loteId: 0,
      motivo: BajaMotivo.VENCIDO_EN_ESTANTE,
      cantidad: 1,
      observacion: ''
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const request: RegistrarBajaRequest = {
      loteId: Number(valor.loteId),
      motivo: valor.motivo,
      cantidad: Number(valor.cantidad),
      observacion: valor.observacion.trim()
    };

    this.guardando.set(true);
    this.formError.set('');

    this.servicio
      .registrarBaja(request)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: baja => {
          this.cerrarModal();
          this.success.set(`Baja de ${baja.cantidad} unidad(es) de ${baja.medicamentoNombre} registrada.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar la baja.')
      });
  }

  trackById(_index: number, baja: BajaInventario): number {
    return baja.id;
  }
}
