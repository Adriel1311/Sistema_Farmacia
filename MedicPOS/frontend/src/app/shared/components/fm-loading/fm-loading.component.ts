import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-fm-loading',
  template: `
    <div class="fm-loading" role="status" aria-live="polite">
      <span class="fm-loading__spinner" aria-hidden="true"></span>
      <span class="fm-loading__text">{{ text }}</span>
    </div>
  `,
  styles: [
    `
      .fm-loading {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.625rem;
        padding: 2.5rem 1rem;
        color: #47555f;
        font-size: 0.875rem;
      }
      .fm-loading__spinner {
        width: 1.25rem;
        height: 1.25rem;
        border: 2px solid #dde4e8;
        border-top-color: #186e57;
        border-radius: 50%;
        animation: fm-spin 0.7s linear infinite;
      }
      @keyframes fm-spin {
        to {
          transform: rotate(360deg);
        }
      }
    `
  ]
})
export class FmLoadingComponent {
  @Input() text = 'Cargando...';
  @Output() readonly retry = new EventEmitter<void>();
}
