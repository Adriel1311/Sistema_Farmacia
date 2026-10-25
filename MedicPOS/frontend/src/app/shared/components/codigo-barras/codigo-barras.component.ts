import { ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';

import { ApiService } from '../../../core/services/api.service';

export type TipoCodigo = 'medicamento' | 'lote' | 'dispensacion';

/**
 * Muestra la etiqueta PNG (CODE 128 o QR) que genera el backend para un
 * medicamento, un lote o el comprobante de entrega. La imagen se pide como
 * blob para que el interceptor de sesion adjunte el token.
 */
@Component({
  selector: 'app-codigo-barras',
  template: `
    <div class="cb">
      <p class="cb__cargando" *ngIf="cargando()">Generando codigo...</p>
      <p class="cb__error" *ngIf="error()" role="alert">{{ error() }}</p>
      <img class="cb__imagen" *ngIf="imagen()" [src]="imagen()" [alt]="titulo || 'Codigo de barras'" />
      <div class="cb__acciones" *ngIf="imagen()">
        <button type="button" class="fm-btn fm-btn--ghost fm-btn--sm" (click)="descargar()">Descargar PNG</button>
        <button type="button" class="fm-btn fm-btn--ghost fm-btn--sm" (click)="imprimir()">Imprimir</button>
      </div>
    </div>
  `,
  styles: [
    `
      .cb {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        text-align: center;
      }
      .cb__cargando,
      .cb__error {
        margin: 0;
        color: var(--fm-muted, #5f6b7a);
        font-size: 0.875rem;
      }
      .cb__error {
        color: var(--fm-danger, #b3261e);
      }
      .cb__imagen {
        max-width: 320px;
        max-height: 220px;
        background: #fff;
        padding: 8px;
        border: 1px solid var(--fm-border, #dfe3e8);
        border-radius: 8px;
      }
      .cb__acciones {
        display: flex;
        gap: 8px;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CodigoBarrasComponent implements OnChanges {
  private readonly api = inject(ApiService);

  @Input() tipo: TipoCodigo = 'lote';
  @Input() id = 0;
  @Input() titulo = '';

  readonly imagen = signal('');
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tipo'] || changes['id']) {
      this.cargar();
    }
  }

  descargar(): void {
    const url = this.imagen();
    if (!url) {
      return;
    }
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `farmamed-${this.tipo}-${this.id}.png`;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
  }

  imprimir(): void {
    const url = this.imagen();
    if (url) {
      window.open(url, '_blank');
    }
  }

  private cargar(): void {
    if (!this.id) {
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.api.getBlob(`/codigos-barras/${this.tipo}/${this.id}`).subscribe({
      next: respuesta => {
        const blob = respuesta.body;
        if (!blob) {
          this.error.set('El backend no devolvio la imagen de la etiqueta.');
          this.cargando.set(false);
          return;
        }
        this.liberarImagen();
        this.imagen.set(URL.createObjectURL(blob));
        this.cargando.set(false);
      },
      error: (err: Error) => {
        this.error.set(err.message || 'No se pudo generar el codigo.');
        this.cargando.set(false);
      }
    });
  }

  private liberarImagen(): void {
    const anterior = this.imagen();
    if (anterior) {
      URL.revokeObjectURL(anterior);
      this.imagen.set('');
    }
  }
}
