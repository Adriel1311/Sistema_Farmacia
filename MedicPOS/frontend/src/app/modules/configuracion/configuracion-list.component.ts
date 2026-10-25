import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { ConfiguracionService } from '../../core/services/alerta.service';
import { Configuracion, SemaforoConfig, UpdateConfiguracionRequest } from '../../core/models/alerta.model';

interface GrupoConfiguracion {
  categoria: string;
  items: Configuracion[];
}

@Component({
  selector: 'app-configuracion-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './configuracion-list.component.html',
  styleUrls: ['./configuracion-list.component.scss']
})
export class ConfiguracionListComponent {
  private readonly servicio = inject(ConfiguracionService);
  private readonly destroyRef = inject(DestroyRef);

  readonly configuraciones = signal<Configuracion[]>([]);
  readonly semaforo = signal<SemaforoConfig | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly borrador = signal<Record<string, string>>({});
  readonly guardandoCategoria = signal('');

  readonly grupos = computed<GrupoConfiguracion[]>(() => {
    const mapa = new Map<string, Configuracion[]>();
    for (const item of this.configuraciones()) {
      const lista = mapa.get(item.categoria);
      if (lista) {
        lista.push(item);
      } else {
        mapa.set(item.categoria, [item]);
      }
    }
    return [...mapa.entries()]
      .map(([categoria, items]) => ({ categoria, items }))
      .sort((a, b) => a.categoria.localeCompare(b.categoria, 'es'));
  });

  readonly totalActivas = computed(() => this.configuraciones().filter(item => item.activo).length);
  readonly totalInactivas = computed(() => this.configuraciones().length - this.totalActivas());

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.borrador.set({});
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    forkJoin({ configuraciones: this.servicio.getAll(), semaforo: this.servicio.getSemaforo() })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo cargar la configuracion del sistema.');
          return of({ configuraciones: [] as Configuracion[], semaforo: null as SemaforoConfig | null });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(resultado => {
        this.configuraciones.set(resultado.configuraciones);
        this.semaforo.set(resultado.semaforo);
      });
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  valorEditado(item: Configuracion): string {
    const borrador = this.borrador();
    return Object.prototype.hasOwnProperty.call(borrador, item.clave) ? borrador[item.clave] : item.valor;
  }

  editar(item: Configuracion, event: Event): void {
    const input = event.target as HTMLInputElement;
    this.borrador.update(actual => ({ ...actual, [item.clave]: input.value }));
  }

  hayCambios(grupo: GrupoConfiguracion): boolean {
    const borrador = this.borrador();
    return grupo.items.some(item => borrador[item.clave] !== undefined && borrador[item.clave] !== item.valor);
  }

  cantidadCambios(grupo: GrupoConfiguracion): number {
    const borrador = this.borrador();
    return grupo.items.filter(item => borrador[item.clave] !== undefined && borrador[item.clave] !== item.valor).length;
  }

  guardarGrupo(grupo: GrupoConfiguracion): void {
    if (!this.hayCambios(grupo) || this.guardandoCategoria() !== '') {
      return;
    }

    const borrador = this.borrador();
    const valores: Record<string, string> = {};
    for (const item of grupo.items) {
      const valor = borrador[item.clave];
      if (valor === undefined || valor.trim().length === 0) {
        this.error.set(`El valor de "${item.clave}" no puede quedar vacio.`);
        return;
      }
      valores[item.clave] = valor.trim();
    }

    const request: UpdateConfiguracionRequest = { valores };
    this.guardandoCategoria.set(grupo.categoria);
    this.error.set('');

    this.servicio
      .update(request)
      .pipe(
        finalize(() => this.guardandoCategoria.set('')),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: actualizadas => {
          this.configuraciones.set(actualizadas);
          this.limpiarBorrador(grupo);
          this.success.set(`Configuracion de "${grupo.categoria}" actualizada.`);
          this.cargarSemaforo();
        },
        error: (err: Error) => this.error.set(err.message || 'No se pudo guardar la configuracion.')
      });
  }

  private limpiarBorrador(grupo: GrupoConfiguracion): void {
    const claves = new Set(grupo.items.map(item => item.clave));
    this.borrador.update(actual => {
      const siguiente: Record<string, string> = {};
      for (const clave of Object.keys(actual)) {
        if (!claves.has(clave)) {
          siguiente[clave] = actual[clave];
        }
      }
      return siguiente;
    });
  }

  private cargarSemaforo(): void {
    this.servicio
      .getSemaforo()
      .pipe(
        catchError(() => of(null as SemaforoConfig | null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(valor => this.semaforo.set(valor));
  }

  diasCritico = computed<number | null>(() => this.semaforo()?.diasCritico ?? null);

  diasProximo = computed<number | null>(() => this.semaforo()?.diasProximo ?? null);

  trackByClave(_index: number, item: Configuracion): number {
    return item.id;
  }
}
