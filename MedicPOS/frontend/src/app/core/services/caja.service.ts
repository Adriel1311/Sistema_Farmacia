import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import {
  AbrirCajaRequest,
  CerrarCajaRequest,
  Caja,
  MovimientoCaja,
  SalidaCajaRequest
} from '../models/caja.model';

@Injectable({ providedIn: 'root' })
export class CajaService {
  constructor(private readonly api: ApiService) {}

  /** Caja abierta, o null si no hay ninguna (el backend responde 204). */
  actual(): Observable<Caja | null> {
    return this.api.get<Caja | null>(API.caja.actual);
  }

  historial(): Observable<Caja[]> {
    return this.api.get<Caja[]>(API.caja.historial);
  }

  movimientos(cajaId: number): Observable<MovimientoCaja[]> {
    return this.api.get<MovimientoCaja[]>(API.caja.movimientos(cajaId));
  }

  abrir(request: AbrirCajaRequest): Observable<Caja> {
    return this.api.post<Caja>(API.caja.abrir, request);
  }

  registrarSalida(request: SalidaCajaRequest): Observable<Caja> {
    return this.api.post<Caja>(API.caja.salidas, request);
  }

  cerrar(request: CerrarCajaRequest): Observable<Caja> {
    return this.api.post<Caja>(API.caja.cerrar, request);
  }
}
