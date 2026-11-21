import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-fm-empty',
  template: `
    <div class="fm-state">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#8d9ba5" stroke-width="1.5" aria-hidden="true">
        <path d="M3 7.5 12 3l9 4.5v9L12 21l-9-4.5v-9Z" />
        <path d="M3 7.5 12 12m0 0 9-4.5M12 12v9" />
      </svg>
      <span class="fm-state__title">{{ title }}</span>
      <span *ngIf="message">{{ message }}</span>
      <button *ngIf="action" type="button" class="fm-btn fm-btn--secondary fm-btn--sm" (click)="actionClick.emit()">
        {{ action }}
      </button>
    </div>
  `
})
export class FmEmptyComponent {
  @Input() title = 'Sin registros';
  @Input() message = '';
  @Input() action = '';
  @Output() readonly actionClick = new EventEmitter<void>();
}
