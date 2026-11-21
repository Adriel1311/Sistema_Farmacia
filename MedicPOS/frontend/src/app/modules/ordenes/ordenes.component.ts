import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, Validators } from '@angular/forms';
import { combineLatest, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuthService } from '../../core/services/auth.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { OrdenCompraService } from '../../core/services/orden-compra.service';
import { Proveedor } from '../../core/models/catalogo.model';
import {
  CrearDesdeSugerenciaRequest,
  OrdenCompra,
  OrdenEstado,
  ORDEN_ESTADO_LABELS,
  SugerenciaCompra
} from '../../core/models/orden-compra.model';
import { UserRole } from '../../core/models/user.model';

type Vista = 'LISTADO' | 'SUGERENCIAS';

@Component({
  selector: 'app-ordenes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ordenes.component.html',
  styleUrls: ['./ordenes.component.scss']
})
export class OrdenesComponent {
  private readonly servicio = inject(OrdenCompraService);
  private readonly catalogo = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  /** Aprobar o rechazar es potestad del Jefe de Farmacia (RSP-06). */
  readonly puedeResolver = this.auth.hasRole(UserRole.JEFE_FARMACIA);

  readonly ORDEN_ESTADO_LABELS = ORDEN_ESTADO_LABELS;
  readonly OrdenEstado = OrdenEstado;

  readonly vista = signal<Vista>('LISTADO');
  readonly ordenes = signal<OrdenCompra[]>([]);
  readonly sugerencias = signal<SugerenciaCompra[]>([]);
  readonly proveedores = signal<Proveedor[]>([]);

  readonly filtroEstado = signal<OrdenEstado | null>(null);
  readonly texto = signal('');

  readonly seleccion = signal<SugerenciaCompra[]>([]);
  readonly modalSugerencia = signal(false);
  readonly modalResolver = signal<OrdenCompra | null>(null);
  readonly detalle = signal<OrdenCompra | null>(null);

  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly observacionForm = this.fb.nonNullable.group({
    observacion: ['', [Validators.maxLength(300)]]
  });

  readonly visibles = computed(() => {
    const texto = this.texto().trim().toLowerCase();
    const estado = this.filtroEstado();
    return this.ordenes().filter(orden => {
      if (estado && orden.estado !== estado) {
        return false;
      }
      if (!texto) {
        return true;
      }
      return (
        orden.proveedorRazonSocial.toLowerCase().includes(texto) ||
        orden.proveedorRuc.toLowerCase().includes(texto)
      );
    });
  });

  readonly conSugerenciasPendientes = computed(() => this.sugerencias().filter(s => s.cantidadSugerida > 0));

  readonly totalSeleccionado = computed(() => this.seleccion().reduce((suma, s) => suma + s.cantidadSugerida, 0));

  readonly resumen = computed(() => {
    const lista = this.ordenes();
    return {
      total: lista.length,
      sugeridas: lista.filter(o => o.estado === OrdenEstado.SUGERIDA).length,
      aprobadas: lista.filter(o => o.estado === OrdenEstado.APROBADA).length,
      rechazadas: lista.filter(o => o.estado === OrdenEstado.RECHAZADA).length
    };
  });

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

  filtrarEstado(event: Event): void {
    const valor = (event.target as HTMLSelectElement).value;
    this.filtroEstado.set(valor ? (valor as OrdenEstado) : null);
  }

  onTexto(event: Event): void {
    this.texto.set((event.target as HTMLInputElement).value);
  }

  alternarSugerencia(sugerencia: SugerenciaCompra): void {
    this.seleccion.update(actual =>
      actual.some(s => s.medicamentoId === sugerencia.medicamentoId)
        ? actual.filter(s => s.medicamentoId !== sugerencia.medicamentoId)
        : [...actual, sugerencia]
    );
  }

  estaSeleccionada(medicamentoId: number): boolean {
    return this.seleccion().some(s => s.medicamentoId === medicamentoId);
  }

  seleccionarTodas(): void {
    const pendientes = this.conSugerenciasPendientes();
    const todasSeleccionadas = pendientes.every(s => this.estaSeleccionada(s.medicamentoId));
    this.seleccion.set(todasSeleccionadas ? [] : [...pendientes]);
  }

  abrirSugerencia(): void {
    this.observacionForm.reset({ observacion: '' });
    this.seleccion.set(this.conSugerenciasPendientes());
    this.modalSugerencia.set(true);
  }

  cerrarSugerencia(): void {
    this.modalSugerencia.set(false);
    this.seleccion.set([]);
  }

  crearDesdeSugerencia(): void {
    if (this.seleccion().length === 0) {
      this.error.set('Selecciona al menos un medicamento para la orden.');
      return;
    }

    const request: CrearDesdeSugerenciaRequest = {
      sugerencias: this.seleccion(),
      observacion: this.observacionForm.getRawValue().observacion.trim()
    };

    this.guardando.set(true);
    this.servicio
      .createDesdeSugerencia(request)
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: orden => {
          this.cerrarSugerencia();
          this.success.set(`Orden ${orden.id} creada como SUGERIDA con ${orden.totalItems} item(s).`);
          this.vista.set('LISTADO');
          this.cargar();
        },
        error: (err: Error) => this.error.set(err.message || 'No se pudo crear la orden.')
      });
  }

  abrirResolver(orden: OrdenCompra): void {
    if (!this.puedeResolver) {
      return;
    }
    this.observacionForm.reset({ observacion: '' });
    this.modalResolver.set(orden);
  }

  cerrarResolver(): void {
    this.modalResolver.set(null);
  }

  resolver(decision: OrdenEstado): void {
    const orden = this.modalResolver();
    if (!orden) {
      return;
    }

    this.guardando.set(true);
    this.servicio
      .resolver(orden.id, { decision, observacion: this.observacionForm.getRawValue().observacion.trim() })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: actualizada => {
          this.cerrarResolver();
          this.success.set(`Orden ${actualizada.id} marcada como ${ORDEN_ESTADO_LABELS[actualizada.estado]}.`);
          this.cargar();
        },
        error: (err: Error) => this.error.set(err.message || 'No se pudo resolver la orden.')
      });
  }

  verDetalle(orden: OrdenCompra): void {
    this.detalle.set(orden);
  }

  cerrarDetalle(): void {
    this.detalle.set(null);
  }

  claseEstado(estado: OrdenEstado): string {
    switch (estado) {
      case OrdenEstado.APROBADA:
        return 'fm-badge--success';
      case OrdenEstado.RECHAZADA:
        return 'fm-badge--danger';
      case OrdenEstado.SUGERIDA:
        return 'fm-badge--info';
      default:
        return 'fm-badge--neutral';
    }
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    combineLatest({
      ordenes: this.servicio.getAll(),
      sugerencias: this.servicio.getSugerencias(),
      proveedores: this.catalogo.getProveedores()
    })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar las ordenes.');
          return of({ ordenes: [] as OrdenCompra[], sugerencias: [] as SugerenciaCompra[], proveedores: [] as Proveedor[] });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ ordenes, sugerencias, proveedores }) => {
        this.ordenes.set(ordenes);
        this.sugerencias.set(sugerencias);
        this.proveedores.set(proveedores);
      });
  }

  trackOrden(_index: number, orden: OrdenCompra): number {
    return orden.id;
  }

  trackSugerencia(_index: number, sugerencia: SugerenciaCompra): number {
    return sugerencia.medicamentoId;
  }
}
