import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-fm-modal',
  template: `
    <div class="fm-modal-backdrop" (click)="onBackdrop($event)">
      <div class="fm-modal" [class.fm-modal--wide]="wide" role="dialog" aria-modal="true" [attr.aria-label]="title">
        <div class="fm-modal__header">
          <h2 class="fm-modal__title">{{ title }}</h2>
          <button type="button" class="fm-modal__close" aria-label="Cerrar" (click)="closed.emit()">&times;</button>
        </div>
        <div class="fm-modal__body">
          <ng-content></ng-content>
        </div>
        <div class="fm-modal__footer">
          <ng-content select="[modalFooter]"></ng-content>
        </div>
      </div>
    </div>
  `
})
export class FmModalComponent {
  @Input() title = '';
  @Input() wide = false;
  @Output() readonly closed = new EventEmitter<void>();

  onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closed.emit();
    }
  }
}
