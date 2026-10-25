import { ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

interface IconDef {
  /** Paths SVG con viewBox 24x24, sin atributos de color. */
  paths: string;
  filled?: boolean;
}

/** Set de iconos de linea de FarmaMed. Un solo trazo, 24x24. */
const ICONS: Record<string, IconDef> = {
  dashboard: { paths: '<path d="M3 13h8V3H3v10Zm0 8h8v-6H3v6Zm10 0h8V11h-8v10Zm0-18v6h8V3h-8Z"/>' },
  dispensacion: {
    paths:
      '<path d="M3 6h18M3 6v12a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6M8 6V4h8v2"/><path d="M8 11h3M8 15h6"/>'
  },
  receta: {
    paths:
      '<path d="M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z"/><path d="M9 8h6M9 12h6M9 16h3"/>'
  },
  paciente: {
    paths: '<path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z"/><path d="M4 21a8 8 0 0 1 16 0"/>'
  },
  inventario: {
    paths:
      '<path d="M3 7.5 12 3l9 4.5v9L12 21l-9-4.5v-9Z"/><path d="m3 7.5 9 4.5 9-4.5M12 12v9"/>'
  },
  baja: { paths: '<path d="M12 3v10"/><path d="m8 9 4 4 4-4"/><path d="M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2"/>' },
  movimiento: {
    paths: '<path d="M4 8h13l-3-3M20 16H7l3 3"/>'
  },
  medicamento: {
    paths:
      '<path d="M10.5 20.5 3.5 13.5a5 5 0 0 1 7-7l7 7a5 5 0 0 1-7 7Z"/><path d="m7 10 7 7"/><path d="M15 4.5 19.5 9"/><path d="m17 2.5 4.5 4.5"/>'
  },
  proveedor: {
    paths:
      '<path d="M3 9h18l-1.5 11a2 2 0 0 1-2 2H6.5a2 2 0 0 1-2-2L3 9Z"/><path d="M8 9V6a4 4 0 0 1 8 0v3"/>'
  },
  orden: {
    paths:
      '<path d="M7 3h7l5 5v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M14 3v5h5"/><path d="M9 13h7M9 17h4"/>'
  },
  alerta: {
    paths:
      '<path d="M12 4.5 2.8 20h18.4L12 4.5Z"/><path d="M12 10v4.5M12 17.4h.01"/>'
  },
  reporte: { paths: '<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>' },
  configuracion: {
    paths:
      '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M2 12h3M19 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1"/>'
  },
  auditoria: {
    paths:
      '<path d="M5 3h9l5 5v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M14 3v5h5"/><circle cx="11" cy="13" r="1.6"/><path d="M8 18a3 3 0 0 1 6 0"/>'
  },
  usuarios: {
    paths:
      '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 5.2a3.5 3.5 0 0 1 0 5.6M18 20a6.4 6.4 0 0 0-2.2-4.8"/>'
  },
  roles: {
    paths:
      '<path d="M12 3 4 6.5v5c0 4.5 3.2 8.3 8 9.5 4.8-1.2 8-5 8-9.5v-5L12 3Z"/><path d="M9.5 12.2l1.8 1.8 3.4-3.6"/>'
  },
  user: { paths: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>' },
  logout: {
    paths:
      '<path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3"/><path d="M10 8 6 12l4 4"/><path d="M6 12h9"/>'
  },
  search: { paths: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.6-3.6"/>' },
  menu: { paths: '<path d="M4 7h16M4 12h16M4 17h16"/>' },
  close: { paths: '<path d="m6 6 12 12M18 6 6 18"/>' },
  plus: { paths: '<path d="M12 5v14M5 12h14"/>' },
  filter: { paths: '<path d="M3 5h18l-7 8v6l-4 2v-8L3 5Z"/>' },
  refresh: {
    paths:
      '<path d="M20 12a8 8 0 1 1-2.3-5.6"/><path d="M20 4v5h-5"/>'
  },
  check: { paths: '<path d="m5 13 4.5 4.5L19 7"/>' },
  error: { paths: '<circle cx="12" cy="12" r="9"/><path d="M12 7.5v5M12 16h.01"/>' },
  warning: {
    paths: '<path d="M12 4.5 2.8 20h18.4L12 4.5Z"/><path d="M12 10v4.5M12 17.4h.01"/>'
  },
  success: { paths: '<circle cx="12" cy="12" r="9"/><path d="m8 12.5 2.7 2.7L16 10"/>' },
  calendar: {
    paths: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 3v4M16 3v4M3 10h18"/>'
  },
  box: {
    paths:
      '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M3 11h18M8 7V4h8v3"/>'
  },
  clock: { paths: '<circle cx="12" cy="12" r="9"/><path d="M12 7.5V12l3 2"/>' },
  notificacion: {
    paths:
      '<path d="M18 15V10a6 6 0 0 0-12 0v5l-1.6 2.4A1 1 0 0 0 5.2 19h13.6a1 1 0 0 0 .8-1.6L18 15Z"/><path d="M10 19a2 2 0 0 0 4 0"/>'
  },
  verificado: {
    paths:
      '<path d="m9 12 2 2 4-4"/><path d="M12 2.8 4.5 6v5.5c0 4.4 3.1 8.2 7.5 9.3 4.4-1.1 7.5-4.9 7.5-9.3V6L12 2.8Z"/>'
  },
  chevronRight: { paths: '<path d="m9 5 7 7-7 7"/>' },
  candado: {
    paths: '<rect x="4.5" y="10" width="15" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>'
  },
  credencial: {
    paths: '<rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="9" cy="11" r="2"/><path d="M5.5 16c.6-1.4 1.9-2 3.5-2s2.9.6 3.5 2M15 10h4M15 13h4"/>'
  },
  escudo: {
    paths:
      '<path d="M12 2.8 4.5 6v5.5c0 4.4 3.1 8.2 7.5 9.3 4.4-1.1 7.5-4.9 7.5-9.3V6L12 2.8Z"/>'
  },
  download: {
    paths:
      '<path d="M12 4v10"/><path d="m8 11 4 4 4-4"/><path d="M4 18v1a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-1"/>'
  }
};

@Component({
  selector: 'svg-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg
      [attr.width]="size"
      [attr.height]="size"
      viewBox="0 0 24 24"
      [attr.fill]="filled ? 'currentColor' : 'none'"
      [attr.stroke]="filled ? 'none' : 'currentColor'"
      [attr.stroke-width]="strokeWidth"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
      focusable="false"
      [innerHTML]="svg"
    ></svg>
  `,
  styles: [
    `
      :host {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        line-height: 0;
        flex: 0 0 auto;
      }
    `
  ]
})
export class SvgIconComponent implements OnChanges {
  @Input({ required: true }) name = '';
  @Input() size = 20;
  @Input() filled = false;
  @Input() strokeWidth = 1.7;

  svg: SafeHtml = '';

  private readonly sanitizer = inject(DomSanitizer);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['name']) {
      this.render();
    }
  }

  private render(): void {
    const icon = ICONS[this.name];
    this.svg = icon
      ? this.sanitizer.bypassSecurityTrustHtml(icon.paths)
      : this.sanitizer.bypassSecurityTrustHtml('');
  }
}
