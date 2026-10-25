import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import { Alerta, AtenderAlertaRequest, Configuracion, SemaforoConfig, UpdateConfiguracionRequest } from '../models/alerta.model';

@Injectable({ providedIn: 'root' })
export class AlertaService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Alerta[]> {
    return this.api.get<Alerta[]>(API.alertas.base);
  }

  getPendientes(): Observable<Alerta[]> {
    return this.api.get<Alerta[]>(API.alertas.pendientes);
  }

  atender(request: AtenderAlertaRequest): Observable<void> {
    return this.api.post<void>(API.alertas.atender, request);
  }
}

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  constructor(private readonly api: ApiService) {}

  getAll(): Observable<Configuracion[]> {
    return this.api.get<Configuracion[]>(API.configuracion.base);
  }

  getSemaforo(): Observable<SemaforoConfig> {
    return this.api.get<SemaforoConfig>(API.configuracion.semaforo);
  }

  update(request: UpdateConfiguracionRequest): Observable<Configuracion[]> {
    return this.api.put<Configuracion[]>(API.configuracion.base, request);
  }
}
