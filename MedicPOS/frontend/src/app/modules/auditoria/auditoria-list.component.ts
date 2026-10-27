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

interface CambioCampo {
  campo: string;
  antes: string;
  despues: string;
}

// Mapeo de acciones técnicas a lenguaje natural para el usuario
const MAPA_ACCIONES: Record<string, string> = {
  // Catálogo
  MEDICAMENTO_CREADO: 'Medicamento creado',
  MEDICAMENTO_ACTUALIZADO: 'Medicamento actualizado',
  MEDICAMENTO_DESACTIVADO: 'Medicamento desactivado',
  // Inventario
  LOTE_INGRESADO: 'Ingreso de lote',
  LOTE_ACTUALIZADO: 'Lote actualizado',
  BAJA_REGISTRADA: 'Baja de inventario registrada',
  // Dispensación
  RECETA_REGISTRADA: 'Receta registrada',
  DISPENSACION_REGISTRADA: 'Dispensación registrada',
  DISPENSACION_ANULADA: 'Dispensación anulada',
  EXCEPCION_FEFO: 'Excepción: lote distinto al sugerido',
  // Abastecimiento
  PROVEEDOR_CREADO: 'Proveedor creado',
  PROVEEDOR_ACTUALIZADO: 'Proveedor actualizado',
  PROVEEDOR_DESACTIVADO: 'Proveedor desactivado',
  ORDEN_CREADA: 'Orden de compra creada',
  ORDEN_APROBADA: 'Orden de compra aprobada',
  ORDEN_RECHAZADA: 'Orden de compra rechazada',
  // Seguridad
  USUARIO_CREADO: 'Usuario creado',
  USUARIO_ACTUALIZADO: 'Usuario actualizado',
  USUARIO_DESACTIVADO: 'Usuario desactivado',
  USUARIO_ACTIVADO: 'Usuario activado',
  CONTRASENA_ACTUALIZADA: 'Contraseña actualizada',
  // Configuración y avisos
  CONFIGURACION_ACTUALIZADA: 'Configuración actualizada',
  ALERTA_ATENDIDA: 'Alerta atendida'
} as const;

interface CambioCampo {
  campo: string;
  antes: string;
  despues: string;
}

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

  readonly detalleAbierto = signal<Auditoria | null>(null);
  readonly cambiosDetalle = signal<CambioCampo[]>([]);

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

  /** Abre el modal de detalle para ver los cambios parseados */
  abrirDetalle(registro: Auditoria): void {
    const cambios = this.parsearCambios(registro.valorAnterior, registro.valorNuevo);
    this.cambiosDetalle.set(cambios);
    this.detalleAbierto.set(registro);
  }

  cerrarDetalle(): void {
    this.detalleAbierto.set(null);
    this.cambiosDetalle.set([]);
  }

  /** Parsea los valores anterior y nuevo y devuelve array de cambios legibles */
  private parsearCambios(valorAnterior: string | null, valorNuevo: string | null): Array<{campo: string, antes: string, despues: string}> {
    const cambios: Array<{campo: string, antes: string, despues: string}> = [];

    const parsearValor = (valor: string | null): string => {
      if (!valor || valor.trim() === '' || valor === 'null') {
        return '—';
      }
      const limpio = valor.trim();

      // Intentar parsear como JSON
      if ((limpio.startsWith('{') && limpio.endsWith('}')) || (limpio.startsWith('[') && limpio.endsWith(']'))) {
        try {
          const obj = JSON.parse(limpio);
          return this.formatearObjeto(obj);
        } catch {
          return limpio;
        }
      }

      // Booleanos
      if (limpio === 'true' || limpio === 'false') {
        return limpio === 'true' ? 'Sí' : 'No';
      }

      // IDs numéricos que parecen IDs internos
      if (/^\d+$/.test(limpio) && limpio.length <= 10) {
        return `ID: ${limpio}`;
      }

      return limpio;
    };

    const parsearObjeto = (valor: string | null): Record<string, string> => {
      if (!valor || valor.trim() === '' || valor === 'null') {
        return {};
      }
      const limpio = valor.trim();
      if ((limpio.startsWith('{') && limpio.endsWith('}'))) {
        try {
          return JSON.parse(limpio);
        } catch {
          return {};
        }
      }
      return {};
    };

    const objAnterior = parsearObjeto(valorAnterior);
    const objNuevo = parsearObjeto(valorNuevo);

    // Recopilar todas las claves únicas
    const todasClaves = new Set([...Object.keys(objAnterior), ...Object.keys(objNuevo)]);

    for (const clave of todasClaves) {
      const antes = objAnterior[clave];
      const despues = objNuevo[clave];

      // Solo mostrar si hay cambio real
      if (antes !== despues) {
        cambios.push({
          campo: this.formatearNombreCampo(clave),
          antes: parsearValor(antes ?? ''),
          despues: parsearValor(despues ?? '')
        });
      }
    }

    // Si no hubo cambios en objetos, comparar valores directos
    if (cambios.length === 0) {
      const antesParseado = parsearValor(valorAnterior);
      const despuesParseado = parsearValor(valorNuevo);
      if (antesParseado !== despuesParseado) {
        cambios.push({
          campo: 'Valor',
          antes: antesParseado,
          despues: despuesParseado
        });
      }
    }

    return cambios;
  }

  private formatearObjeto(obj: Record<string, unknown>): string {
    return Object.entries(obj)
      .map(([k, v]) => `${this.formatearNombreCampo(k)}: ${String(v)}`)
      .join('; ');
  }

  private formatearNombreCampo(campo: string): string {
    // Convertir camelCase/snake_case a legible
    return campo
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .replace(/^\w/, c => c.toUpperCase())
      .trim();
  }

  trackById(_index: number, item: Auditoria): number {
    return item.id;
  }

  trackCambio(_index: number, cambio: {campo: string, antes: string, despues: string}): string {
    return cambio.campo;
  }

  /** Convierte acción técnica a lenguaje natural para el usuario */
  accionLegible(accion: string): string {
    return MAPA_ACCIONES[accion] ?? accion;
  }
}
