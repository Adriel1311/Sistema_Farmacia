import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable, from, throwError } from 'rxjs';
import { catchError, map, mergeMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export type QueryParams = Record<string, string | number | boolean | null | undefined>;

/**
 * Envoltura HTTP de FarmaMed. Resuelve el host de la API segun desde donde se
 * abra la app (localhost o una IP de la red) y normaliza los errores del backend.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiUrl = this.resolveApiUrl();

  constructor(private readonly http: HttpClient) {}

  get<T>(endpoint: string, params?: QueryParams): Observable<T> {
    return this.http
      .get<T>(this.url(endpoint), { headers: this.headers(), params: this.toParams(params) })
      .pipe(catchError(this.handleError));
  }

  post<T>(endpoint: string, body: unknown = {}): Observable<T> {
    return this.http
      .post<T>(this.url(endpoint), body, { headers: this.headers() })
      .pipe(catchError(this.handleError));
  }

  put<T>(endpoint: string, body: unknown = {}): Observable<T> {
    return this.http
      .put<T>(this.url(endpoint), body, { headers: this.headers() })
      .pipe(catchError(this.handleError));
  }

  patch<T>(endpoint: string, body: unknown = {}): Observable<T> {
    return this.http
      .patch<T>(this.url(endpoint), body, { headers: this.headers() })
      .pipe(catchError(this.handleError));
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http
      .delete<T>(this.url(endpoint), { headers: this.headers() })
      .pipe(catchError(this.handleError));
  }

  /**
   * Descarga un archivo binario (PDF, XLSX, imagen). El token de sesion lo
   * agrega el interceptor igual que en el resto de peticiones. Devuelve la
   * respuesta completa para poder leer el nombre sugerido del adjunto.
   */
  getBlob(endpoint: string, params?: QueryParams): Observable<HttpResponse<Blob>> {
    return this.http
      .get(this.url(endpoint), {
        headers: this.headers(),
        params: this.toParams(params),
        responseType: 'blob',
        observe: 'response'
      })
      .pipe(catchError(this.handleError));
  }

  private url(endpoint: string): string {
    return `${this.apiUrl}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  private toParams(params?: QueryParams): HttpParams {
    let httpParams = new HttpParams();
    if (!params) {
      return httpParams;
    }
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return httpParams;
  }

  private resolveApiUrl(): string {
    if (environment.apiUrl) {
      return environment.apiUrl;
    }
    const hostname = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
    return `http://${hostname}:8080/api`;
  }

  private handleError = (error: HttpErrorResponse): Observable<never> => {
    // En las descargas binarias el cuerpo del error tambien llega como Blob.
    if (error.error instanceof Blob) {
      return from(error.error.text()).pipe(
        map(texto => this.buildError(error, this.parseBody(texto))),
        mergeMap((err: Error) => throwError(() => err))
      );
    }
    return throwError(() => new Error(this.extractMessage(error)));
  }

  private buildError(error: HttpErrorResponse, body: { message?: string; error?: string } | null): Error {
    if (body?.message) {
      return new Error(body.message);
    }
    if (body?.error) {
      return new Error(body.error);
    }
    return new Error(this.extractMessage(error));
  }

  private parseBody(texto: string): { message?: string; error?: string } | null {
    try {
      return JSON.parse(texto) as { message?: string; error?: string };
    } catch {
      return null;
    }
  }

  private extractMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No hay conexión con el servidor de FarmaMed.';
    }
    const body = error.error as { message?: string; error?: string } | null;
    if (body?.message) {
      return body.message;
    }
    if (body?.error) {
      return body.error;
    }
    if (error.status === 401) {
      return 'Sesión expirada. Vuelve a iniciar sesión.';
    }
    if (error.status === 403) {
      return 'No tienes permisos para realizar esta acción.';
    }
    if (error.status === 404) {
      return 'El recurso solicitado no existe.';
    }
    return `Error ${error.status}: ${error.message}`;
  }
}
