import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import { CreatePacienteRequest, CreateRecetaRequest, Medico, Paciente, Receta, TipoReceta } from '../models/receta.model';

@Injectable({ providedIn: 'root' })
export class PacienteRecetaService {
  constructor(private readonly api: ApiService) {}

  getPacientes(): Observable<Paciente[]> {
    return this.api.get<Paciente[]>(API.pacientes.base);
  }

  buscarPacientes(texto: string): Observable<Paciente[]> {
    return this.api.get<Paciente[]>(API.pacientes.buscar, { texto });
  }

  getPaciente(id: number): Observable<Paciente> {
    return this.api.get<Paciente>(API.pacientes.byId(id));
  }

  createPaciente(request: CreatePacienteRequest): Observable<Paciente> {
    return this.api.post<Paciente>(API.pacientes.base, request);
  }

  getRecetas(filtros?: { tipo?: TipoReceta; desde?: string; hasta?: string; pendiente?: boolean }): Observable<Receta[]> {
    return this.api.get<Receta[]>(API.recetas.base, {
      tipo: filtros?.tipo,
      desde: filtros?.desde,
      hasta: filtros?.hasta,
      pendiente: filtros?.pendiente
    });
  }

  getReceta(id: number): Observable<Receta> {
    return this.api.get<Receta>(API.recetas.byId(id));
  }

  createReceta(request: CreateRecetaRequest): Observable<Receta> {
    return this.api.post<Receta>(API.recetas.base, request);
  }

  getProximoNumeroReceta(): Observable<{ valor: string }> {
    return this.api.get<{ valor: string }>(API.recetas.proximoNumero);
  }

  getMedicosActivos(): Observable<Medico[]> {
    return this.api.get<Medico[]>(API.medicos.base);
  }
}
