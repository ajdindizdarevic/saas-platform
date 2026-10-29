import { Component, inject, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="nav-left">
        <button class="hamburger" (click)="sidebarToggle.emit()">☰</button>
        <div class="brand"><span class="mark">S</span> SaaSPlatform</div>
      </div>
      <div class="nav-right">
        <div class="avatar-wrap" (click)="menuOpen = !menuOpen">
          <div class="avatar">{{ initials() }}</div>
          @if (menuOpen) {
            <div class="dropdown" (click)="$event.stopPropagation()">
              <div class="dropdown-name">{{ auth.currentUser()?.fullName }}</div>
              <div class="dropdown-email">{{ auth.currentUser()?.email }}</div>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item" (click)="openChangePw()">🔑 Change Password</button>
              <button class="dropdown-item danger" (click)="auth.logout()">Sign out</button>
            </div>
          }
        </div>
      </div>
    </nav>

    <!-- ── Change Password Modal ──────────────────────────────────────────── -->
    @if (changePwOpen()) {
      <div class="modal-backdrop" (click)="closeChangePw()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Change Password</h3>
            <button class="close-btn" (click)="closeChangePw()">✕</button>
          </div>
          <div class="modal-body">
            <div class="field">
              <label>Current password</label>
              <input [(ngModel)]="currentPw" type="password" placeholder="Your current password"
                     [class.err]="pwError() && !currentPw" />
            </div>
            <div class="field">
              <label>New password</label>
              <input [(ngModel)]="newPw" type="password" placeholder="Min 8 chars, letters + numbers"
                     [class.err]="pwError() && !newPw" />
            </div>
            <div class="field">
              <label>Confirm new password</label>
              <input [(ngModel)]="confirmPw" type="password" placeholder="Repeat new password"
                     [class.err]="pwError() && confirmPw !== newPw" />
            </div>
            @if (pwError()) {
              <div class="alert-err">{{ pwError() }}</div>
            }
            @if (pwSuccess()) {
              <div class="alert-ok">{{ pwSuccess() }}</div>
            }
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" (click)="closeChangePw()">Cancel</button>
            <button class="btn-submit" [disabled]="pwLoading()" (click)="submitChangePw()">
              @if (pwLoading()) { <span class="spin"></span> Saving… }
              @else { Save password }
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500&display=swap');

    .navbar {
      height: 60px;
      background: #fff;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      position: sticky; top: 0; z-index: 100;
      font-family: 'DM Sans', sans-serif;
    }
    .nav-left { display: flex; align-items: center; gap: 1rem; }
    .hamburger {
      background: none; border: none; cursor: pointer;
      font-size: 1.25rem; color: #52525b; padding: .25rem;
      display: none;
    }
    @media (max-width: 768px) { .hamburger { display: block; } }

    .brand {
      display: flex; align-items: center; gap: .5rem;
      font-weight: 500; font-size: .9rem; color: #0f0f11;
    }
    .mark {
      width: 28px; height: 28px; background: #4f46e5; color: #fff;
      border-radius: 7px; display: grid; place-items: center;
      font-weight: 700; font-size: .88rem;
    }

    .nav-right { display: flex; align-items: center; gap: 1rem; }

    .tenant-badge {
      background: #f4f4f5; border: 1px solid #e4e4e7;
      color: #52525b; padding: .2rem .75rem;
      border-radius: 999px; font-size: .75rem; font-weight: 500;
    }

    .avatar-wrap { position: relative; cursor: pointer; }
    .avatar {
      width: 34px; height: 34px; background: #4f46e5; color: #fff;
      border-radius: 50%; display: grid; place-items: center;
      font-size: .8rem; font-weight: 600;
    }

    .dropdown {
      position: absolute; right: 0; top: calc(100% + 8px);
      background: #fff; border: 1px solid #e4e4e7; border-radius: 12px;
      padding: .5rem; min-width: 200px;
      box-shadow: 0 8px 30px #00000012; z-index: 200;
    }
    .dropdown-name { font-size: .85rem; font-weight: 500; padding: .5rem .75rem .1rem; color: #0f0f11; }
    .dropdown-email { font-size: .75rem; color: #71717a; padding: 0 .75rem .5rem; }
    .dropdown-divider { height: 1px; background: #f0f0f0; margin: .25rem 0; }
    .dropdown-item {
      display: block; width: 100%; padding: .55rem .75rem;
      background: none; border: none; text-align: left;
      font-family: inherit; font-size: .85rem; cursor: pointer;
      border-radius: 7px; transition: background .15s;
    }
    .dropdown-item:hover { background: #f4f4f5; }
    .dropdown-item.danger { color: #ef4444; }
    .dropdown-item.danger:hover { background: #fef2f2; }

    /* ── change-password modal ───────────────────────────────────────────────── */
    .modal-backdrop {
      position: fixed; inset: 0; background: #00000055;
      display: flex; align-items: center; justify-content: center; z-index: 500;
      padding: 1rem;
    }
    .modal {
      background: #fff; border-radius: 16px; width: 100%; max-width: 400px;
      box-shadow: 0 20px 60px #00000022; overflow: hidden;
    }
    .modal-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 1.25rem 1.5rem .75rem;
    }
    .modal-header h3 {
      font-family: 'DM Sans', sans-serif; font-size: 1.1rem; font-weight: 600;
      color: #0f0f11; margin: 0;
    }
    .close-btn {
      background: none; border: none; font-size: 1rem; cursor: pointer;
      color: #71717a; padding: .25rem; transition: color .15s;
    }
    .close-btn:hover { color: #0f0f11; }
    .modal-body { padding: 0 1.5rem .75rem; }
    .field { margin-bottom: .9rem; }
    .field label { display: block; font-size: .78rem; font-weight: 500; color: #3f3f46; margin-bottom: .35rem; }
    .field input {
      width: 100%; padding: .6rem .85rem; border: 1.5px solid #e4e4e7;
      border-radius: 9px; font-family: inherit; font-size: .87rem;
      outline: none; box-sizing: border-box; transition: border-color .2s;
    }
    .field input:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px #4f46e51a; }
    .field input.err { border-color: #ef4444; }
    .alert-err {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      border-radius: 8px; padding: .6rem .85rem; font-size: .82rem; margin-top: .25rem;
    }
    .alert-ok {
      background: #f0fdf4; border: 1px solid #bbf7d0; color: #15803d;
      border-radius: 8px; padding: .6rem .85rem; font-size: .82rem; margin-top: .25rem;
    }
    .modal-footer {
      display: flex; justify-content: flex-end; gap: .65rem;
      padding: .75rem 1.5rem 1.25rem; border-top: 1px solid #f4f4f5;
    }
    .btn-cancel {
      background: none; border: 1.5px solid #e4e4e7; color: #52525b;
      padding: .55rem 1.1rem; border-radius: 8px; font-family: inherit;
      font-size: .85rem; cursor: pointer;
    }
    .btn-submit {
      background: #0f0f11; color: #fff; border: none;
      padding: .55rem 1.25rem; border-radius: 8px; font-family: inherit;
      font-size: .85rem; font-weight: 500; cursor: pointer;
      display: flex; align-items: center; gap: .35rem;
    }
    .btn-submit:disabled { opacity: .5; cursor: not-allowed; }
    .spin {
      width: 13px; height: 13px; border: 2px solid #fff4; border-top-color: #fff;
      border-radius: 50%; animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class NavbarComponent {
  auth        = inject(AuthService);
  private userSvc = inject(UserService);

  sidebarToggle = output<void>();
  menuOpen = false;

  // ── change-password modal state ──────────────────────────────────────────────
  changePwOpen = signal(false);
  pwLoading    = signal(false);
  pwError      = signal('');
  pwSuccess    = signal('');

  currentPw = '';
  newPw     = '';
  confirmPw = '';

  initials(): string {
    const name = this.auth.currentUser()?.fullName ?? '';
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  openChangePw(): void {
    this.menuOpen = false;
    this.currentPw = '';
    this.newPw = '';
    this.confirmPw = '';
    this.pwError.set('');
    this.pwSuccess.set('');
    this.changePwOpen.set(true);
  }

  closeChangePw(): void {
    this.changePwOpen.set(false);
  }

  submitChangePw(): void {
    this.pwError.set('');
    this.pwSuccess.set('');

    if (!this.currentPw) {
      this.pwError.set('Please enter your current password.');
      return;
    }
    if (this.newPw.length < 8 || !/[A-Za-z]/.test(this.newPw) || !/\d/.test(this.newPw)) {
      this.pwError.set('New password must be at least 8 characters with letters and numbers.');
      return;
    }
    if (this.newPw !== this.confirmPw) {
      this.pwError.set('New passwords do not match.');
      return;
    }

    this.pwLoading.set(true);
    this.userSvc.changePassword(this.currentPw, this.newPw).subscribe({
      next: () => {
        this.pwLoading.set(false);
        this.pwSuccess.set('Password changed successfully!');
        setTimeout(() => this.closeChangePw(), 1500);
      },
      error: err => {
        this.pwLoading.set(false);
        this.pwError.set(err?.error?.error ?? 'Failed to change password. Please try again.');
      }
    });
  }
}
