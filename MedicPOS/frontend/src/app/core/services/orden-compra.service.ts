import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import {
  CrearDesdeSugerenciaRequest,
  CreateOrdenCompraRequest,
  OrdenCompra,
  OrdenEstado,
  ResolverOrdenCompraRequest,
  SugerenciaCompra
} from '../models/orden-compra.model';

@Injectable({ providedIn: 'root' })
export class OrdenCompraService {
  constructor(private readonly api: ApiService) {}

  getAll(filtros?: { estado?: OrdenEstado; proveedorId?: number }): Observable<OrdenCompra[]> {
    return this.api.get<OrdenCompra[]>(API.ordenes.base, {
      estado: filtros?.estado,
      proveedorId: filtros?.proveedorId
    });
  }

  getSugerencias(): Observable<SugerenciaCompra[]> {
    return this.api.get<SugerenciaCompra[]>(API.ordenes.sugerencias);
  }

  getById(id: number): Observable<OrdenCompra> {
    return this.api.get<OrdenCompra>(API.ordenes.byId(id));
  }

  create(request: CreateOrdenCompraRequest): Observable<OrdenCompra> {
    return this.api.post<OrdenCompra>(API.ordenes.base, request);
  }

  createDesdeSugerencia(request: CrearDesdeSugerenciaRequest): Observable<OrdenCompra> {
    return this.api.post<OrdenCompra>(API.ordenes.desdeSugerencia, request);
  }

  resolver(id: number, request: ResolverOrdenCompraRequest): Observable<OrdenCompra> {
    return this.api.patch<OrdenCompra>(API.ordenes.resolver(id), request);
  }
}
