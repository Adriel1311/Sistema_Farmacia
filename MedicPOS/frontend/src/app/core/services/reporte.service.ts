import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import { Auditoria, ReporteBajas, ReporteConsumo, ReporteInventario, ReporteOrdenes } from '../models/reporte.model';
import { Dashboard } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  constructor(private readonly api: ApiService) {}

  getInventario(): Observable<ReporteInventario> {
    return this.api.get<ReporteInventario>(API.reportes.inventario);
  }

  getConsumo(desde?: string, hasta?: string): Observable<ReporteConsumo> {
    return this.api.get<ReporteConsumo>(API.reportes.consumo, { desde, hasta });
  }

  getBajas(desde?: string, hasta?: string): Observable<ReporteBajas> {
    return this.api.get<ReporteBajas>(API.reportes.bajas, { desde, hasta });
  }

  getOrdenes(desde?: string, hasta?: string): Observable<ReporteOrdenes> {
    return this.api.get<ReporteOrdenes>(API.reportes.ordenes, { desde, hasta });
  }

  /**
   * Descarga el reporte en PDF. Para el inventario el rango se ignora.
   */
  exportarPdf(tipo: string, desde?: string, hasta?: string): Observable<HttpResponse<Blob>> {
    return this.api.getBlob(API.reportes.exportarPdf(tipo), { desde, hasta });
  }

  /**
   * Descarga el reporte en XLSX (Excel). Para el inventario el rango se ignora.
   */
  exportarXlsx(tipo: string, desde?: string, hasta?: string): Observable<HttpResponse<Blob>> {
    return this.api.getBlob(API.reportes.exportarXlsx(tipo), { desde, hasta });
  }
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly api: ApiService) {}

  get(): Observable<Dashboard> {
    return this.api.get<Dashboard>(API.dashboard);
  }
}

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Auditoria[]> {
    return this.api.get<Auditoria[]>(API.auditoria.base);
  }

  getByEntidad(entidad: string): Observable<Auditoria[]> {
    return this.api.get<Auditoria[]>(API.auditoria.entidad(entidad));
  }

  getByUsuario(usuarioId: number): Observable<Auditoria[]> {
    return this.api.get<Auditoria[]>(API.auditoria.usuario(usuarioId));
  }

  getByRango(desde: string, hasta: string): Observable<Auditoria[]> {
    return this.api.get<Auditoria[]>(API.auditoria.rango, { desde, hasta });
  }
}
