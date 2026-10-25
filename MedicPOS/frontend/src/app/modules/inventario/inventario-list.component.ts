import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuthService } from '../../core/services/auth.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { InventarioService } from '../../core/services/inventario.service';
import { Lote, LoteEstado, EstadoVencimiento } from '../../core/models/inventario.model';
import { Medicamento } from '../../core/models/catalogo.model';
import { UserRole } from '../../core/models/user.model';

type VistaLotes = 'TODOS' | 'CRITICOS' | 'PROXIMOS' | 'VENCIDOS';

@Component({
  selector: 'app-inventario-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './inventario-list.component.html',
  styleUrls: ['./inventario-list.component.scss']
})
export class InventarioListComponent {
  private readonly inventario = inject(InventarioService);
  private readonly catalogo = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly puedeRegistrar = this.auth.hasRole(UserRole.JEFE_FARMACIA, UserRole.TECNICO_FARMACIA);

  readonly hoy = new Date();

  readonly lotes = signal<Lote[]>([]);
  readonly medicamentos = signal<Medicamento[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly vista = signal<VistaLotes>('TODOS');
  readonly medicamentoFiltro = signal<number | null>(null);
  readonly texto = signal('');

  readonly modalAbierto = signal(false);
  readonly guardando = signal(false);
  readonly formError = signal('');
  readonly sincronizando = signal(false);
  readonly loteCodigo = signal<Lote | null>(null);

  readonly form = this.fb.nonNullable.group({
    medicamentoId: [0, [Validators.required, Validators.min(1)]],
    numeroLote: ['', [Validators.required, Validators.maxLength(30)]],
    fechaIngreso: [this.toInputDate(new Date()), [Validators.required]],
    fechaVencimiento: ['', [Validators.required]],
    cantidad: [1, [Validators.required, Validators.min(1)]],
    ordenCompraId: this.fb.control<number | null>(null)
  });

  readonly visibles = computed<Lote[]>(() => {
    const filtro = this.texto().trim().toLowerCase();
    return this.lotes().filter(lote => {
      if (!filtro) {
        return true;
      }
      return (
        lote.numeroLote.toLowerCase().includes(filtro) ||
        lote.medicamentoNombre.toLowerCase().includes(filtro) ||
        lote.medicamentoCodigo.toLowerCase().includes(filtro)
      );
    });
  });

  readonly conteos = computed(() => {
    const lista = this.lotes();
    return {
      total: lista.length,
      criticos: lista.filter(l => l.estadoVencimiento === EstadoVencimiento.ROJO).length,
      proximos: lista.filter(l => l.estadoVencimiento === EstadoVencimiento.AMBAR).length,
      vencidos: lista.filter(l => l.estadoVencimiento === EstadoVencimiento.NEGRO).length,
      unidades: lista.reduce((suma, l) => suma + (l.estado === LoteEstado.BAJA ? 0 : l.cantidadActual), 0)
    };
  });

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.cargar();
  }

  cambiarVista(vista: VistaLotes): void {
    this.vista.set(vista);
  }

  filtrarPorMedicamento(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    this.medicamentoFiltro.set(valor ? Number(valor) : null);
  }

  onTexto(event: Event): void {
    this.texto.set((event.target as HTMLInputElement).value);
  }

  limpiarFiltros(): void {
    this.texto.set('');
    this.medicamentoFiltro.set(null);
    this.vista.set('TODOS');
  }

  abrirModal(): void {
    this.formError.set('');
    this.form.reset({
      medicamentoId: this.medicamentoFiltro() ?? 0,
      numeroLote: '',
      fechaIngreso: this.toInputDate(new Date()),
      fechaVencimiento: '',
      cantidad: 1,
      ordenCompraId: null
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  /** Abre la etiqueta con el codigo de barras del lote. */
  abrirCodigo(lote: Lote): void {
    this.loteCodigo.set(lote);
  }

  cerrarCodigo(): void {
    this.loteCodigo.set(null);
  }

  guardar(): void {
    if (this.form.invalid || this.fechaInvalida()) {
      this.form.markAllAsTouched();
      this.formError.set('Revisa los datos del lote: la fecha de vencimiento debe ser posterior al ingreso.');
      return;
    }

    const valor = this.form.getRawValue();
    this.guardando.set(true);
    this.formError.set('');

    this.inventario
      .registrarIngreso({
        medicamentoId: valor.medicamentoId,
        numeroLote: valor.numeroLote.trim(),
        fechaIngreso: valor.fechaIngreso,
        fechaVencimiento: valor.fechaVencimiento,
        cantidad: valor.cantidad,
        ordenCompraId: valor.ordenCompraId
      })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: lote => {
          this.cerrarModal();
          this.success.set(`Lote ${lote.numeroLote} registrado con ${lote.cantidadInicial} unidades.`);
          this.cargar();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar el lote.')
      });
  }

  sincronizarVencidos(): void {
    this.sincronizando.set(true);
    this.limpiarMensajes();
    this.inventario
      .sincronizarVencidos()
      .pipe(
        finalize(() => this.sincronizando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: cantidad => {
          this.success.set(`Se marcaron ${cantidad} lote(s) como vencidos.`);
          this.cargar();
        },
        error: (err: Error) => this.error.set(err.message || 'No se pudo sincronizar los vencimientos.')
      });
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    forkJoin({ lotes: this.inventario.getLotes(), medicamentos: this.catalogo.getMedicamentos() })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo cargar el inventario.');
          return of({ lotes: [] as Lote[], medicamentos: [] as Medicamento[] });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ lotes, medicamentos }) => {
        this.lotes.set(this.aplicarVista(lotes, this.vista()));
        this.medicamentos.set(medicamentos);
      });
  }

  private aplicarVista(lotes: Lote[], vista: VistaLotes): Lote[] {
    switch (vista) {
      case 'CRITICOS':
        return lotes.filter(l => l.estadoVencimiento === EstadoVencimiento.ROJO);
      case 'PROXIMOS':
        return lotes.filter(l => l.estadoVencimiento === EstadoVencimiento.AMBAR);
      case 'VENCIDOS':
        return lotes.filter(l => l.estadoVencimiento === EstadoVencimiento.NEGRO);
      default:
        return lotes;
    }
  }

  private fechaInvalida(): boolean {
    const { fechaIngreso, fechaVencimiento } = this.form.getRawValue();
    if (!fechaIngreso || !fechaVencimiento) {
      return true;
    }
    return new Date(fechaVencimiento) <= new Date(fechaIngreso);
  }

  private toInputDate(date: Date): string {
    const mes = String(date.getMonth() + 1).padStart(2, '0');
    const dia = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${mes}-${dia}`;
  }

  trackByLote(_index: number, lote: Lote): number {
    return lote.id;
  }
}
