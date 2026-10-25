import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, defaultRouteForRole } from '../../core/services/auth.service';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [NgIf, ReactiveFormsModule, SvgIconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly form = this.fb.nonNullable.group({
    usuario: ['', [Validators.required]],
    password: ['', [Validators.required]],
    recordar: [false]
  });

  readonly loading = signal(false);
  readonly error = signal('');
  readonly showPassword = signal(false);

  private readonly returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

  constructor() {
    if (this.auth.isAuthenticated()) {
      void this.router.navigateByUrl(this.returnUrl ?? defaultRouteForRole(this.auth.getRole()));
    }
  }

  get usuario() {
    return this.form.controls.usuario;
  }

  get password() {
    return this.form.controls.password;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigateByUrl(this.returnUrl ?? defaultRouteForRole(this.auth.getRole()));
      },
      error: (err: Error) => {
        this.loading.set(false);
        this.error.set(err.message || 'No se pudo iniciar sesion.');
      }
    });
  }
}
