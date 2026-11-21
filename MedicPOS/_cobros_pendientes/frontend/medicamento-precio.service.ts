import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../constants/api-endpoints';
import { ApiService } from './api.service';

export interface MedicamentoPrecio {
  id: number;
  medicamentoId: number;
  medicamentoCodigo: string;
  medicamentoNombre: string;
  precioVenta: number;
  vigenteDesde: string;
  vigenteHasta: string | null;
  createdAt: string;
}

export interface CreateMedicamentoPrecioRequest {
  medicamentoId: number;
  precioVenta: number;
  vigenteDesde: string;
  vigenteHasta?: string | null;
}

export interface UpdateMedicamentoPrecioRequest {
  precioVenta?: number;
  vigenteDesde?: string;
  vigenteHasta?: string | null;
}

@Injectable({ providedIn: 'root' })
export class MedicamentoPrecioService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<MedicamentoPrecio[]> {
    return this.api.get<MedicamentoPrecio[]>(API.precios.base);
  }

  getByMedicamento(medicamentoId: number): Observable<MedicamentoPrecio[]> {
    return this.api.get<MedicamentoPrecio[]>(API.precios.byMedicamento(medicamentoId));
  }

  create(request: CreateMedicamentoPrecioRequest): Observable<MedicamentoPrecio> {
    return this.api.post<MedicamentoPrecio>(API.precios.base, request);
  }

  update(id: number, request: UpdateMedicamentoPrecioRequest): Observable<MedicamentoPrecio> {
    return this.api.put<MedicamentoPrecio>(API.precios.byId(id), request);
  }
}