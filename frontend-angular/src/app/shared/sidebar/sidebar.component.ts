import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <aside class="sidebar" [class.open]="open()">
      <div class="sidebar-inner">
        <nav class="nav">
          <div class="nav-section-label">Main</div>
          @for (item of visibleItems(); track item.route) {
            <a class="nav-item"
               [routerLink]="item.route"
               routerLinkActive="active"
               [routerLinkActiveOptions]="{ exact: false }">
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
            </a>
          }
        </nav>

        <div class="sidebar-footer">
          <div class="user-row">
            <div class="user-avatar">{{ initials() }}</div>
            <div class="user-info">
              <div class="user-name">{{ auth.currentUser()?.fullName }}</div>
              <div class="user-role">{{ primaryRole() }}</div>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- overlay on mobile -->
    @if (open()) {
      <div class="overlay" (click)="close()"></div>
    }
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500&display=swap');

    .sidebar {
      width: 240px;
      height: 100%;
      background: #0f0f11;
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      font-family: 'DM Sans', sans-serif;
    }
    .sidebar-inner {
      display: flex;
      flex-direction: column;
      height: 100%;
      padding: 1.25rem 0;
    }

    .nav { flex: 1; padding: 0 .75rem; }

    .nav-section-label {
      font-size: .68rem;
      font-weight: 500;
      letter-spacing: .08em;
      color: #52525b;
      text-transform: uppercase;
      padding: 0 .75rem .6rem;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: .75rem;
      padding: .6rem .75rem;
      border-radius: 8px;
      color: #a1a1aa;
      text-decoration: none;
      font-size: .87rem;
      font-weight: 400;
      transition: background .15s, color .15s;
      margin-bottom: .15rem;
    }
    .nav-item:hover { background: #ffffff0d; color: #e4e4e7; }
    .nav-item.active { background: #ffffff14; color: #fff; font-weight: 500; }

    .nav-icon { font-size: 1rem; width: 20px; text-align: center; }

    .sidebar-footer {
      padding: .75rem;
      border-top: 1px solid #1c1c1e;
      margin-top: .5rem;
    }
    .user-row { display: flex; align-items: center; gap: .65rem; }
    .user-avatar {
      width: 32px; height: 32px; border-radius: 8px;
      background: #4f46e5; color: #fff;
      display: grid; place-items: center;
      font-size: .75rem; font-weight: 600; flex-shrink: 0;
    }
    .user-name { font-size: .82rem; font-weight: 500; color: #e4e4e7; }
    .user-role { font-size: .72rem; color: #52525b; margin-top: .1rem; }

    /* mobile */
    @media (max-width: 768px) {
      .sidebar {
        position: fixed; left: -240px; top: 60px;
        height: calc(100vh - 60px); z-index: 200;
        transition: left .25s;
      }
      .sidebar.open { left: 0; }
      .overlay {
        position: fixed; inset: 0; top: 60px;
        background: #0009; z-index: 199;
      }
    }
  `]
})
export class SidebarComponent {
  auth = inject(AuthService);
  open = input(false);

  private allItems: NavItem[] = [
    { label: 'Dashboard',    icon: '⊞', route: '/dashboard', roles: ['ROLE_SUPER_ADMIN'] },
    { label: 'Users',        icon: '👥', route: '/users',     roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] },
  ];

  visibleItems() {
    const roles = this.auth.currentUser()?.roles ?? [];
    return this.allItems.filter(item =>
      !item.roles || item.roles.some(r => roles.includes(r))
    );
  }

  initials(): string {
    const name = this.auth.currentUser()?.fullName ?? '';
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  primaryRole(): string {
    const roles = this.auth.currentUser()?.roles ?? [];
    if (roles.includes('ROLE_SUPER_ADMIN')) return 'Super Admin';
    if (roles.includes('ROLE_ADMIN'))       return 'Admin';
    if (roles.includes('ROLE_MANAGER'))     return 'Manager';
    return 'User';
  }

  close() {}
}
