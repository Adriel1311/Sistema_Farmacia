import { Component, Input } from '@angular/core';
import { EstadoVencimiento } from '../../../core/models/inventario.model';

const ETIQUETAS: Record<EstadoVencimiento, string> = {
  [EstadoVencimiento.VERDE]: 'Vigente',
  [EstadoVencimiento.AMBAR]: 'Proximo a vencer',
  [EstadoVencimiento.ROJO]: 'Critico',
  [EstadoVencimiento.NEGRO]: 'Vencido / dado de baja'
};

/** Semaforo de vencimiento RN04: verde >60, ambar 31-60, rojo <=30, negro vencido. */
@Component({
  selector: 'app-fm-semaforo',
  template: `
    <span class="fm-semaforo" [class]="clase" [attr.title]="titulo">
      <span class="fm-semaforo__dot" aria-hidden="true"></span>
      <span class="fm-semaforo__label">{{ etiqueta }}</span>
      <span *ngIf="diasRestantes !== null" class="fm-xs fm-muted">({{ diasRestantes }} d)</span>
    </span>
  `
})
export class FmSemaforoComponent {
  @Input({ required: true }) estado: EstadoVencimiento = EstadoVencimiento.VERDE;
  @Input() diasRestantes: number | null = null;

  get etiqueta(): string {
    return ETIQUETAS[this.estado] ?? this.estado;
  }

  get titulo(): string {
    if (this.diasRestantes === null) {
      return this.etiqueta;
    }
    return `${this.etiqueta} - ${this.diasRestantes} dias`;
  }

  get clase(): string {
    return `fm-semaforo--${(this.estado ?? 'VERDE').toLowerCase()}`;
  }
}
