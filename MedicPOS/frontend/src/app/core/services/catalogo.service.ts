import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import {
  CreateMedicamentoRequest,
  CreateProveedorRequest,
  Medicamento,
  Proveedor,
  UpdateMedicamentoRequest,
  UpdateProveedorRequest
} from '../models/catalogo.model';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  constructor(private readonly api: ApiService) {}

  getMedicamentos(soloActivos = true): Observable<Medicamento[]> {
    return this.api.get<Medicamento[]>(API.medicamentos.base, { soloActivos });
  }

  getMedicamento(id: number): Observable<Medicamento> {
    return this.api.get<Medicamento>(API.medicamentos.byId(id));
  }

  buscarMedicamentos(texto: string): Observable<Medicamento[]> {
    return this.api.get<Medicamento[]>(API.medicamentos.buscar, { texto });
  }

  getMedicamentosPorProveedor(proveedorId: number): Observable<Medicamento[]> {
    return this.api.get<Medicamento[]>(API.medicamentos.proveedor(proveedorId));
  }

  getMedicamentosStockCritico(): Observable<Medicamento[]> {
    return this.api.get<Medicamento[]>(API.medicamentos.stockCritico);
  }

  createMedicamento(request: CreateMedicamentoRequest): Observable<Medicamento> {
    return this.api.post<Medicamento>(API.medicamentos.base, request);
  }

  updateMedicamento(id: number, request: UpdateMedicamentoRequest): Observable<Medicamento> {
    return this.api.put<Medicamento>(API.medicamentos.byId(id), request);
  }

  toggleMedicamentoActivo(id: number): Observable<Medicamento> {
    return this.api.patch<Medicamento>(API.medicamentos.toggleActivo(id));
  }

  getProveedores(soloActivos = true): Observable<Proveedor[]> {
    return this.api.get<Proveedor[]>(API.proveedores.base, { soloActivos });
  }

  getProveedor(id: number): Observable<Proveedor> {
    return this.api.get<Proveedor>(API.proveedores.byId(id));
  }

  buscarProveedores(texto: string): Observable<Proveedor[]> {
    return this.api.get<Proveedor[]>(API.proveedores.buscar, { texto });
  }

  createProveedor(request: CreateProveedorRequest): Observable<Proveedor> {
    return this.api.post<Proveedor>(API.proveedores.base, request);
  }

  updateProveedor(id: number, request: UpdateProveedorRequest): Observable<Proveedor> {
    return this.api.put<Proveedor>(API.proveedores.byId(id), request);
  }

  toggleProveedorActivo(id: number): Observable<Proveedor> {
    return this.api.patch<Proveedor>(API.proveedores.toggleActivo(id));
  }
}
