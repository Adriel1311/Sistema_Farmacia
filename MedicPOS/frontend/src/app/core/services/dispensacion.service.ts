import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import { CreateDispensacionRequest, Dispensacion } from '../models/dispensacion.model';

@Injectable({ providedIn: 'root' })
export class DispensacionService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Dispensacion[]> {
    return this.api.get<Dispensacion[]>(API.dispensaciones.base);
  }

  getById(id: number): Observable<Dispensacion> {
    return this.api.get<Dispensacion>(API.dispensaciones.byId(id));
  }

  getByReceta(recetaId: number): Observable<Dispensacion[]> {
    return this.api.get<Dispensacion[]>(API.dispensaciones.byReceta(recetaId));
  }

  create(request: CreateDispensacionRequest): Observable<Dispensacion> {
    return this.api.post<Dispensacion>(API.dispensaciones.base, request);
  }

  anular(id: number): Observable<Dispensacion> {
    return this.api.patch<Dispensacion>(API.dispensaciones.anular(id));
  }
}
