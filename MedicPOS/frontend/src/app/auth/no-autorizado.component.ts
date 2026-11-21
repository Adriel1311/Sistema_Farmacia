import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService, defaultRouteForRole } from '../core/services/auth.service';
import { SvgIconComponent } from '../core/components/svg-icon/svg-icon.component';

@Component({
  selector: 'app-no-autorizado',
  standalone: true,
  imports: [RouterLink, SvgIconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <div class="fm-card">
        <div class="fm-state">
          <svg-icon name="roles" [size]="40" />
          <span class="fm-state__title">No tienes permisos para esta seccion</span>
          <span>
            Tu rol no incluye este modulo. Si crees que es un error, comunicate con el Jefe de Farmacia
            o con Administracion TI.
          </span>
          <a class="fm-btn fm-btn--primary fm-btn--sm" [routerLink]="destino">Volver a mi inicio</a>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 560px;
        margin: 0 auto;
        padding: 2rem 1rem;
      }
    `
  ]
})
export class NoAutorizadoComponent {
  private readonly auth = inject(AuthService);

  readonly destino = defaultRouteForRole(this.auth.getRole()) || '/dashboard';
}
