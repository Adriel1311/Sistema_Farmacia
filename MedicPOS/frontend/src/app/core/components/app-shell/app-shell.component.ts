import { ChangeDetectionStrategy, Component, HostListener, computed, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { navItemsForRole, NavItem } from '../../constants/nav-items';
import { USER_ROLE_LABELS } from '../../models/user.model';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { DOCUMENT } from '@angular/common';

type Viewport = 'mobile' | 'tablet' | 'desktop';

interface NavGroup {
  label: string;
  items: NavItem[];
}

const MOBILE_MAX = 640;
const TABLET_MAX = 1024;

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, SvgIconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss']
})
export class AppShellComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);

  readonly roleLabels = USER_ROLE_LABELS;
  readonly viewport = signal<Viewport>('desktop');
  /** Panel lateral plegado en tablet / escritorio. */
  readonly collapsed = signal(false);
  /** Cajon deslizante en movil. */
  readonly drawerOpen = signal(false);

  readonly groups = computed<NavGroup[]>(() => {
    const items = navItemsForRole(this.auth.getRole());
    const map = new Map<string, NavItem[]>();
    items.forEach(item => {
      const bucket = map.get(item.group) ?? [];
      bucket.push(item);
      map.set(item.group, bucket);
    });
    return Array.from(map.entries()).map(([label, groupItems]) => ({ label, items: groupItems }));
  });

  readonly isMobile = computed(() => this.viewport() === 'mobile');

  readonly isCollapsed = computed(() => {
    const current = this.viewport();
    if (current === 'mobile') {
      return false;
    }
    if (current === 'tablet') {
      return true;
    }
    return this.collapsed();
  });

  constructor() {
    this.updateViewport();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.drawerOpen.set(false);
        this.collapsed.set(false);
      });

    // Body scroll lock when drawer opens/closes
    effect(() => {
      const isOpen = this.drawerOpen();
      if (isOpen) {
        this.document.body.style.overflow = 'hidden';
      } else {
        this.document.body.style.overflow = '';
      }
    });
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateViewport();
  }

  @HostListener('window:keydown.escape')
  onEscape(): void {
    if (this.drawerOpen()) {
      this.closeDrawer();
    }
  }

  toggleDrawer(): void {
    this.drawerOpen.update(open => !open);
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
  }

  toggleCollapsed(): void {
    this.collapsed.update(value => !value);
  }

  logout(): void {
    this.auth.logout();
  }

  initials(name: string | undefined): string {
    if (!name) {
      return 'FM';
    }
    return name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');
  }

  private updateViewport(): void {
    const width = typeof window === 'undefined' ? 1280 : window.innerWidth;
    const next: Viewport = width <= MOBILE_MAX ? 'mobile' : width <= TABLET_MAX ? 'tablet' : 'desktop';
    const previous = this.viewport();
    this.viewport.set(next);
    if (next !== previous && next !== 'desktop') {
      this.collapsed.set(false);
    }
  }
}
