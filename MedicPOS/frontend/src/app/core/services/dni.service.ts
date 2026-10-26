import { Injectable } from '@angular/core';
import { Observable, of, map, catchError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';

export interface DniResponse {
  nombres: string;
  apellidos: string;
  dni: string;
  nombreCompleto?: string;
  direccion?: string;
}

@Injectable({ providedIn: 'root' })
export class DniService {
  constructor(private readonly api: ApiService) {}

  consultarDni(dni: string): Observable<DniResponse> {
    return this.api.get<DniResponse>(API.dni.consultar(dni));
  }

  checkExists(dni: string): Observable<boolean> {
    return this.consultarDni(dni).pipe(
      map(() => false),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 409) return of(true);
        return of(false);
      })
    );
  }
}