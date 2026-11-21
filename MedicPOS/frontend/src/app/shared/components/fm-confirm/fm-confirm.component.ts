import { Component, EventEmitter, Input, Output } from '@angular/core';

export type ConfirmVariant = 'danger' | 'primary';

@Component({
  selector: 'app-fm-confirm',
  template: `
    <app-fm-modal [title]="title" (closed)="cancel()">
      <p class="fm-mb-0">{{ message }}</p>
      <ng-container modalFooter>
        <button type="button" class="fm-btn fm-btn--secondary" (click)="cancel()">Cancelar</button>
        <button type="button" class="fm-btn" [class.fm-btn--danger]="variant === 'danger'" [class.fm-btn--primary]="variant === 'primary'" (click)="accept()">
          {{ confirmText }}
        </button>
      </ng-container>
    </app-fm-modal>
  `,
  styles: [`.fm-mb-0 { margin-bottom: 0; }`]
})
export class FmConfirmComponent {
  @Input() title = 'Confirmar';
  @Input() message = '';
  @Input() confirmText = 'Confirmar';
  @Input() variant: ConfirmVariant = 'danger';
  @Output() readonly accepted = new EventEmitter<void>();
  @Output() readonly cancelled = new EventEmitter<void>();

  accept(): void {
    this.accepted.emit();
  }

  cancel(): void {
    this.cancelled.emit();
  }
}
