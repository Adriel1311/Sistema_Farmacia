import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import {
  BajaInventario,
  BajaMotivo,
  Lote,
  LoteEstado,
  MovimientoInventario,
  RegistrarBajaRequest,
  RegistrarLoteRequest
} from '../models/inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  constructor(private readonly api: ApiService) {}

  getLotes(filtros?: { medicamentoId?: number; estado?: LoteEstado }): Observable<Lote[]> {
    return this.api.get<Lote[]>(API.lotes.base, {
      medicamentoId: filtros?.medicamentoId,
      estado: filtros?.estado
    });
  }

  getLote(id: number): Observable<Lote> {
    return this.api.get<Lote>(API.lotes.byId(id));
  }

  getLotesVencidos(): Observable<Lote[]> {
    return this.api.get<Lote[]>(API.lotes.vencidos);
  }

  getLotesProximosAVencer(): Observable<Lote[]> {
    return this.api.get<Lote[]>(API.lotes.proximosVencer);
  }

  getLotesCriticos(): Observable<Lote[]> {
    return this.api.get<Lote[]>(API.lotes.criticos);
  }

  getLotesPorMedicamento(medicamentoId: number): Observable<Lote[]> {
    return this.api.get<Lote[]>(API.lotes.medicamento(medicamentoId));
  }

  registrarIngreso(request: RegistrarLoteRequest): Observable<Lote> {
    return this.api.post<Lote>(API.lotes.base, request);
  }

  sincronizarVencidos(): Observable<number> {
    return this.api.post<number>(API.lotes.sincronizarVencidos);
  }

  getMovimientos(): Observable<MovimientoInventario[]> {
    return this.api.get<MovimientoInventario[]>(API.movimientos.base);
  }

  getMovimientosPorLote(loteId: number): Observable<MovimientoInventario[]> {
    return this.api.get<MovimientoInventario[]>(API.movimientos.lote(loteId));
  }

  getMovimientosPorReferencia(tipo: string, id: number): Observable<MovimientoInventario[]> {
    return this.api.get<MovimientoInventario[]>(API.movimientos.referencia, { tipo, id });
  }

  getBajas(): Observable<BajaInventario[]> {
    return this.api.get<BajaInventario[]>(API.bajas.base);
  }

  getBajasPendientes(): Observable<BajaInventario[]> {
    return this.api.get<BajaInventario[]>(API.bajas.pendientes);
  }

  getBajasPorRango(desde: string, hasta: string): Observable<BajaInventario[]> {
    return this.api.get<BajaInventario[]>(API.bajas.rango, { desde, hasta });
  }

  getBajasPorMotivo(motivo: BajaMotivo): Observable<BajaInventario[]> {
    return this.api.get<BajaInventario[]>(API.bajas.motivo(motivo));
  }

  registrarBaja(request: RegistrarBajaRequest): Observable<BajaInventario> {
    return this.api.post<BajaInventario>(API.bajas.base, request);
  }
}
