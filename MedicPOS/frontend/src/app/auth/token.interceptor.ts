import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../core/services/auth.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  constructor(private readonly auth: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.auth.getToken();
    const esLogin = request.url.includes('/auth/login');

    if (!token || esLogin) {
      return next.handle(request);
    }

    return next.handle(
      request.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      })
    );
  }
}
