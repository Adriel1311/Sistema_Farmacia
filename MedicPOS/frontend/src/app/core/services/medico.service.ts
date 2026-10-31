import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../constants/api-endpoints';
import { CreateMedicoRequest, Medico, UpdateMedicoRequest } from '../models/receta.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class MedicoService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Medico[]> {
    return this.api.get<Medico[]>(`${API.medicos.base}/all`);
  }

  create(request: CreateMedicoRequest): Observable<Medico> {
    return this.api.post<Medico>(API.medicos.base, request);
  }

  update(id: number, request: UpdateMedicoRequest): Observable<Medico> {
    return this.api.put<Medico>(API.medicos.byId(id), request);
  }

  toggleActivo(id: number): Observable<Medico> {
    return this.api.patch<Medico>(`${API.medicos.byId(id)}/toggle`);
  }
}
