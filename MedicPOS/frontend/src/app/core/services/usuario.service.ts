import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { API } from '../constants/api-endpoints';
import {
  ChangePasswordRequest,
  CreateUsuarioRequest,
  Rol,
  UpdateUsuarioRequest,
  Usuario
} from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private readonly api: ApiService) {}

  getUsuarios(): Observable<Usuario[]> {
    return this.api.get<Usuario[]>(API.usuarios.base);
  }

  getUsuario(id: number): Observable<Usuario> {
    return this.api.get<Usuario>(API.usuarios.byId(id));
  }

  create(request: CreateUsuarioRequest): Observable<Usuario> {
    return this.api.post<Usuario>(API.usuarios.base, request);
  }

  update(id: number, request: UpdateUsuarioRequest): Observable<Usuario> {
    return this.api.put<Usuario>(API.usuarios.byId(id), request);
  }

  toggleActivo(id: number): Observable<Usuario> {
    return this.api.patch<Usuario>(API.usuarios.toggleActivo(id));
  }

  changePassword(id: number, request: ChangePasswordRequest): Observable<void> {
    return this.api.patch<void>(API.usuarios.password(id), request);
  }

  getRoles(): Observable<Rol[]> {
    return this.api.get<Rol[]>(API.roles.base);
  }
}
