import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { ReporteService } from '../../core/services/reporte.service';
import { ReporteBajas, ReporteConsumo, ReporteInventario, ReporteOrdenes } from '../../core/models/reporte.model';

type Tab = 'INVENTARIO' | 'CONSUMO' | 'BAJAS' | 'ORDENES';

@Component({
  selector: 'app-reportes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './reportes.component.html',
  styleUrls: ['./reportes.component.scss']
})
export class ReportesComponent {
  private readonly servicio = inject(ReporteService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly tab = signal<Tab>('INVENTARIO');
  readonly loading = signal(false);
  readonly descargando = signal(false);
  readonly error = signal('');

  readonly inventario = signal<ReporteInventario | null>(null);
  readonly consumo = signal<ReporteConsumo | null>(null);
  readonly bajas = signal<ReporteBajas | null>(null);
  readonly ordenes = signal<ReporteOrdenes | null>(null);

  readonly rango = this.fb.nonNullable.group({
    desde: [this.inicioDelMes()],
    hasta: [this.hoy()]
  });

  constructor() {
    this.cargar();
  }

  cambiarTab(tab: Tab): void {
    if (this.tab() === tab) {
      return;
    }
    this.tab.set(tab);
    this.error.set('');
    this.cargar();
  }

  aplicarRango(): void {
    this.cargar();
  }

  refrescar(): void {
    this.error.set('');
    this.cargar();
  }

  /** Descarga el reporte de la pestaña activa en PDF o XLSX. */
  exportar(formato: 'pdf' | 'xlsx'): void {
    if (this.descargando()) {
      return;
    }
    this.descargando.set(true);
    this.error.set('');
    const tipo = this.tipoDelTab();
    const { desde, hasta } = this.rango.getRawValue();
    const peticion =
      formato === 'pdf'
        ? this.servicio.exportarPdf(tipo, desde, hasta)
        : this.servicio.exportarXlsx(tipo, desde, hasta);

    peticion
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo exportar el reporte.');
          return of(null);
        }),
        finalize(() => this.descargando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(respuesta => {
        if (respuesta?.body) {
          this.guardarArchivo(respuesta.body, this.nombreDescarga(respuesta, tipo, formato));
        }
      });
  }

  /** Nombre del adjunto: el que sugiere el backend o, por defecto, farmamed-{tipo}.{ext}. */
  private nombreDescarga(respuesta: HttpResponse<Blob>, tipo: string, formato: string): string {
    const disposicion = respuesta.headers.get('Content-Disposition') ?? '';
    const encontrado = /filename="?([^";]+)"?/i.exec(disposicion);
    return encontrado ? encontrado[1] : `farmamed-${tipo}.${formato}`;
  }

  private tipoDelTab(): string {
    switch (this.tab()) {
      case 'CONSUMO':
        return 'consumo';
      case 'BAJAS':
        return 'bajas';
      case 'ORDENES':
        return 'ordenes';
      default:
        return 'inventario';
    }
  }

  private guardarArchivo(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');
    const { desde, hasta } = this.rango.getRawValue();

    switch (this.tab()) {
      case 'CONSUMO':
        this.cargarConsumo(desde, hasta);
        break;
      case 'BAJAS':
        this.cargarBajas(desde, hasta);
        break;
      case 'ORDENES':
        this.cargarOrdenes(desde, hasta);
        break;
      default:
        this.cargarInventario();
    }
  }

  private cargarInventario(): void {
    this.servicio
      .getInventario()
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo generar el reporte de inventario.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => this.inventario.set(data));
  }

  private cargarConsumo(desde: string, hasta: string): void {
    this.servicio
      .getConsumo(desde, hasta)
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo generar el reporte de consumo.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => this.consumo.set(data));
  }

  private cargarBajas(desde: string, hasta: string): void {
    this.servicio
      .getBajas(desde, hasta)
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo generar el reporte de bajas.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => this.bajas.set(data));
  }

  private cargarOrdenes(desde: string, hasta: string): void {
    this.servicio
      .getOrdenes(desde, hasta)
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudo generar el reporte de ordenes.');
          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(data => this.ordenes.set(data));
  }

  private hoy(): string {
    return this.toInputDate(new Date());
  }

  private inicioDelMes(): string {
    const hoy = new Date();
    return this.toInputDate(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
  }

  private toInputDate(date: Date): string {
    const mes = String(date.getMonth() + 1).padStart(2, '0');
    const dia = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${mes}-${dia}`;
  }
}
