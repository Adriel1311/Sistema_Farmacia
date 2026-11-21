import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../constants/api-endpoints';
import { Cobro, PagarCobroRequest } from '../models/dispensacion.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class CobroService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Cobro[]> {
    return this.api.get<Cobro[]>(API.cobros.base);
  }

  getById(id: number): Observable<Cobro> {
    return this.api.get<Cobro>(API.cobros.byId(id));
  }

  getByDispensacionId(dispensacionId: number): Observable<Cobro | null> {
    return this.api.get<Cobro | null>(`${API.cobros.base}/dispensacion/${dispensacionId}`);
  }

  pagar(id: number, request: PagarCobroRequest): Observable<Cobro> {
    return this.api.post<Cobro>(`${API.cobros.byId(id)}/pagar`, request);
  }
}