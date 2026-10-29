import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { SidebarComponent } from '../../shared/sidebar/sidebar.component';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { UserResponse } from '../../core/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, ReactiveFormsModule, NavbarComponent, SidebarComponent],
  template: `
    <div class="shell">
      <app-navbar (sidebarToggle)="sidebarOpen.set(!sidebarOpen())" />
      <div class="body">
        <app-sidebar [open]="sidebarOpen()" />

        <main class="page">

          <!-- header -->
          <div class="page-header">
            <div>
              <h1>Users</h1>
              <p class="subtitle">
                Manage users in your organisation — <strong>{{ filteredUsers().length }}</strong> total.
              </p>
            </div>
            @if (auth.isAdmin()) {
              <button class="btn-create" (click)="openCreate()">+ Add user</button>
            }
          </div>

          <!-- search + filter bar -->
          <div class="toolbar">
            <div class="search-wrap">
              <span class="search-icon">🔍</span>
              <input class="search-input" [(ngModel)]="searchTerm"
                     placeholder="Search by name or email…" />
            </div>
            <div class="filter-pills">
              <button class="pill" [class.active]="activeFilter() === 'all'"
                      (click)="activeFilter.set('all')">All</button>
              <button class="pill" [class.active]="activeFilter() === 'active'"
                      (click)="activeFilter.set('active')">Active</button>
              <button class="pill" [class.active]="activeFilter() === 'inactive'"
                      (click)="activeFilter.set('inactive')">Inactive</button>
            </div>
          </div>

          <!-- users grid -->
          @if (loading()) {
            <div class="loading-row"><span class="loader"></span> Loading users…</div>
          } @else if (filteredUsers().length === 0) {
            <div class="empty-state">
              <span>👤</span>
              <p>No users found</p>
              @if (auth.isAdmin()) {
                <button class="btn-create-sm" (click)="openCreate()">Add first user</button>
              }
            </div>
          } @else {
            <div class="users-grid">
              @for (u of filteredUsers(); track u.id) {
                <div class="user-card" [class.inactive]="!u.active">
                  <div class="card-top">
                    <div class="avatar" [style.background]="avatarColor(u)">
                      {{ initials(u) }}
                    </div>
                    <div class="card-meta">
                      <div class="card-name">{{ u.fullName }}</div>
                      <div class="card-username">&#64;{{ u.username }}</div>
                    </div>
                    @if (!u.active) {
                      <span class="inactive-chip">Inactive</span>
                    }
                  </div>

                  <div class="card-email">{{ u.email }}</div>

                  @if (u.phoneNumber) {
                    <div class="card-phone">{{ u.phoneNumber }}</div>
                  }

                  <div class="card-roles">
                    @for (r of u.roles; track r) {
                      <span class="role-chip" [class]="roleClass(r)">{{ formatRole(r) }}</span>
                    }
                  </div>

                  <div class="card-footer">
                    <span class="card-date">Joined {{ u.createdAt | date:'MMM y' }}</span>
                    @if (auth.isAdmin() && u.id !== auth.currentUser()?.id) {
                      <div class="card-actions">
                        @if (u.active) {
                          <button class="btn-action deactivate" (click)="deactivate(u)">Deactivate</button>
                        } @else {
                          <button class="btn-action activate" (click)="activate(u)">Activate</button>
                        }
                      </div>
                    }
                  </div>
                </div>
              }
            </div>

            <!-- pagination -->
            <div class="pagination">
              <button [disabled]="page() === 0" (click)="prevPage()">← Prev</button>
              <span>Page {{ page() + 1 }} of {{ totalPages() }}</span>
              <button [disabled]="page() >= totalPages() - 1" (click)="nextPage()">Next →</button>
            </div>
          }
        </main>
      </div>
    </div>

    <!-- ── Create User Modal ──────────────────────────────────────────────── -->
    @if (showCreateModal()) {
      <div class="modal-backdrop" (click)="closeCreate()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>Add new user</h3>
            <button class="close-btn" (click)="closeCreate()">✕</button>
          </div>

          <form [formGroup]="createForm" (ngSubmit)="submitCreate()">
            <div class="modal-body">
              <div class="row-2">
                <div class="field">
                  <label>First name <span class="req">*</span></label>
                  <input formControlName="firstName" placeholder="Jane"
                         [class.err]="t('firstName')" />
                  @if (t('firstName')) { <span class="errmsg">Required</span> }
                </div>
                <div class="field">
                  <label>Last name <span class="req">*</span></label>
                  <input formControlName="lastName" placeholder="Smith"
                         [class.err]="t('lastName')" />
                  @if (t('lastName')) { <span class="errmsg">Required</span> }
                </div>
              </div>

              <div class="field">
                <label>Email <span class="req">*</span></label>
                <input formControlName="email" type="email" placeholder="jane@company.com"
                       [class.err]="t('email')" />
                @if (t('email')) { <span class="errmsg">Valid email required</span> }
              </div>

              <div class="field">
                <label>Username <span class="req">*</span></label>
                <input formControlName="username" placeholder="jane_smith"
                       [class.err]="t('username')" />
                @if (t('username')) { <span class="errmsg">3–50 chars, letters/digits/._-</span> }
              </div>

              <div class="field">
                <label>Temporary password <span class="req">*</span></label>
                <input formControlName="password" type="password" placeholder="Min 8 characters"
                       [class.err]="t('password')" />
                @if (t('password')) { <span class="errmsg">Min 8 characters required</span> }
              </div>

              <div class="row-2">
                <div class="field">
                  <label>Phone <span class="opt">(optional)</span></label>
                  <input formControlName="phoneNumber" placeholder="+1 555 000 0000" />
                </div>
                <div class="field">
                  <label>Role</label>
                  <select formControlName="role">
                    <option value="USER">User</option>
                    <option value="MANAGER">Manager</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>
              </div>

              @if (createError()) {
                <div class="alert-err">{{ createError() }}</div>
              }
            </div>

            <div class="modal-footer">
              <button type="button" class="btn-cancel" (click)="closeCreate()">Cancel</button>
              <button type="submit" class="btn-submit" [disabled]="createLoading()">
                @if (createLoading()) { <span class="spin"></span> Creating… }
                @else { Create user }
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Fraunces:wght@600&family=DM+Sans:wght@300;400;500&display=swap');

    :host { display: block; }

    /* ── layout ────────────────────────────────────────────────────────────── */
    .shell { display: flex; flex-direction: column; height: 100vh; overflow: hidden; font-family: 'DM Sans', sans-serif; }
    .body  { display: flex; flex: 1; overflow: hidden; }
    .page  { flex: 1; overflow-y: auto; background: #f8f8f9; padding: 2rem; }

    /* ── page header ───────────────────────────────────────────────────────── */
    .page-header {
      display: flex; justify-content: space-between; align-items: flex-start;
      margin-bottom: 1.5rem;
    }
    h1 {
      font-family: 'Fraunces', serif; font-size: 1.75rem;
      font-weight: 600; color: #0f0f11; margin: 0 0 .25rem;
    }
    .subtitle { color: #71717a; font-size: .88rem; margin: 0; }

    .btn-create {
      background: #0f0f11; color: #fff; border: none;
      padding: .6rem 1.25rem; border-radius: 9px; font-family: inherit;
      font-size: .87rem; font-weight: 500; cursor: pointer;
      transition: background .2s;
    }
    .btn-create:hover { background: #27272a; }

    /* ── toolbar ───────────────────────────────────────────────────────────── */
    .toolbar { display: flex; gap: 1rem; margin-bottom: 1.5rem; flex-wrap: wrap; }

    .search-wrap { position: relative; flex: 1; min-width: 220px; }
    .search-icon { position: absolute; left: .85rem; top: 50%; transform: translateY(-50%); font-size: .9rem; }
    .search-input {
      width: 100%; padding: .6rem 1rem .6rem 2.4rem; border: 1.5px solid #e4e4e7;
      border-radius: 9px; font-family: inherit; font-size: .87rem;
      background: #fff; outline: none; box-sizing: border-box; transition: border-color .2s;
    }
    .search-input:focus { border-color: #4f46e5; }

    .filter-pills { display: flex; gap: .35rem; }
    .pill {
      background: #fff; border: 1.5px solid #e4e4e7; color: #52525b;
      padding: .4rem .9rem; border-radius: 999px; font-family: inherit;
      font-size: .79rem; cursor: pointer; transition: all .15s;
    }
    .pill.active { background: #0f0f11; border-color: #0f0f11; color: #fff; }

    /* ── users grid ────────────────────────────────────────────────────────── */
    .users-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }

    .user-card {
      background: #fff; border-radius: 14px; padding: 1.25rem;
      border: 1px solid #f0f0f0; transition: box-shadow .2s;
    }
    .user-card:hover { box-shadow: 0 4px 20px #00000008; }
    .user-card.inactive { opacity: .65; }

    .card-top { display: flex; align-items: center; gap: .85rem; margin-bottom: .75rem; }
    .avatar {
      width: 42px; height: 42px; border-radius: 12px;
      display: grid; place-items: center;
      font-size: .9rem; font-weight: 700; color: #fff; flex-shrink: 0;
    }
    .card-name     { font-size: .92rem; font-weight: 500; color: #0f0f11; }
    .card-username { font-size: .78rem; color: #71717a; margin-top: .1rem; }
    .inactive-chip {
      margin-left: auto; background: #f4f4f5; color: #71717a;
      font-size: .68rem; font-weight: 600; padding: .18rem .55rem;
      border-radius: 999px; text-transform: uppercase; letter-spacing: .04em;
    }

    .card-email { font-size: .82rem; color: #52525b; margin-bottom: .35rem; word-break: break-all; }
    .card-phone { font-size: .8rem; color: #71717a; margin-bottom: .6rem; }

    .card-roles { display: flex; gap: .35rem; flex-wrap: wrap; margin-bottom: 1rem; }
    .role-chip {
      padding: .18rem .6rem; border-radius: 6px; font-size: .72rem; font-weight: 600;
      text-transform: uppercase; letter-spacing: .04em;
    }
    .role-super_admin { background: #fdf4ff; color: #9333ea; }
    .role-admin       { background: #eff6ff; color: #1d4ed8; }
    .role-manager     { background: #f0fdf4; color: #15803d; }
    .role-user        { background: #f4f4f5; color: #52525b; }

    .card-footer {
      display: flex; justify-content: space-between; align-items: center;
      padding-top: .75rem; border-top: 1px solid #f4f4f5;
    }
    .card-date { font-size: .77rem; color: #a1a1aa; }

    .card-actions { display: flex; gap: .35rem; }
    .btn-action {
      padding: .25rem .75rem; border-radius: 6px; font-family: inherit;
      font-size: .75rem; font-weight: 500; cursor: pointer; border: 1.5px solid;
      transition: all .15s;
    }
    .btn-action.deactivate { background: #fef2f2; border-color: #fecaca; color: #dc2626; }
    .btn-action.deactivate:hover { background: #fee2e2; }
    .btn-action.activate { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }
    .btn-action.activate:hover { background: #dcfce7; }

    /* ── loading / empty ───────────────────────────────────────────────────── */
    .loading-row {
      display: flex; align-items: center; gap: .75rem; justify-content: center;
      padding: 3rem; color: #71717a; font-size: .9rem;
    }
    .loader {
      width: 18px; height: 18px; border: 2px solid #e4e4e7; border-top-color: #4f46e5;
      border-radius: 50%; animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state {
      text-align: center; padding: 4rem; color: #a1a1aa;
    }
    .empty-state span { font-size: 2.5rem; display: block; margin-bottom: .75rem; }
    .empty-state p { margin: 0 0 1.25rem; font-size: .9rem; }
    .btn-create-sm {
      background: #0f0f11; color: #fff; border: none;
      padding: .55rem 1.25rem; border-radius: 8px;
      font-family: inherit; font-size: .85rem; cursor: pointer;
    }

    /* ── pagination ────────────────────────────────────────────────────────── */
    .pagination {
      display: flex; align-items: center; justify-content: center;
      gap: 1rem; padding: 1.5rem 0;
    }
    .pagination button {
      background: #fff; border: 1.5px solid #e4e4e7; padding: .4rem 1rem;
      border-radius: 7px; font-family: inherit; font-size: .83rem; cursor: pointer;
    }
    .pagination button:hover:not(:disabled) { border-color: #a1a1aa; }
    .pagination button:disabled { opacity: .35; cursor: not-allowed; }
    .pagination span { font-size: .82rem; color: #71717a; }

    /* ── modal ─────────────────────────────────────────────────────────────── */
    .modal-backdrop {
      position: fixed; inset: 0; background: #00000055;
      display: flex; align-items: center; justify-content: center; z-index: 500;
      padding: 1rem;
    }
    .modal {
      background: #fff; border-radius: 16px; width: 100%; max-width: 500px;
      box-shadow: 0 20px 60px #00000022; overflow: hidden;
    }
    .modal-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 1.5rem 1.75rem 1rem;
    }
    .modal-header h3 {
      font-family: 'Fraunces', serif; font-size: 1.25rem; font-weight: 600;
      color: #0f0f11; margin: 0;
    }
    .close-btn {
      background: none; border: none; font-size: 1rem; cursor: pointer;
      color: #71717a; padding: .25rem; transition: color .15s;
    }
    .close-btn:hover { color: #0f0f11; }

    .modal-body { padding: 0 1.75rem 1rem; }

    .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
    .field { margin-bottom: 1rem; }
    label { display: block; font-size: .79rem; font-weight: 500; color: #3f3f46; margin-bottom: .38rem; }
    .req { color: #ef4444; }
    .opt { font-weight: 400; color: #a1a1aa; }
    input, select {
      width: 100%; padding: .62rem .9rem; border: 1.5px solid #e4e4e7;
      border-radius: 9px; font-family: inherit; font-size: .87rem;
      outline: none; transition: border-color .2s, box-shadow .2s;
      box-sizing: border-box; color: #0f0f11; background: #fff;
    }
    input:focus, select:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px #4f46e51a; }
    input.err { border-color: #ef4444; }
    .errmsg { font-size: .73rem; color: #ef4444; margin-top: .25rem; display: block; }

    .alert-err {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      border-radius: 8px; padding: .65rem .9rem; font-size: .83rem; margin-top: .5rem;
    }

    .modal-footer {
      display: flex; justify-content: flex-end; gap: .75rem;
      padding: 1rem 1.75rem 1.5rem;
      border-top: 1px solid #f4f4f5;
    }
    .btn-cancel {
      background: none; border: 1.5px solid #e4e4e7; color: #52525b;
      padding: .6rem 1.25rem; border-radius: 8px; font-family: inherit;
      font-size: .87rem; cursor: pointer;
    }
    .btn-submit {
      background: #0f0f11; color: #fff; border: none;
      padding: .6rem 1.5rem; border-radius: 8px; font-family: inherit;
      font-size: .87rem; font-weight: 500; cursor: pointer;
      display: flex; align-items: center; gap: .4rem;
    }
    .btn-submit:disabled { opacity: .5; cursor: not-allowed; }
    .spin {
      width: 14px; height: 14px; border: 2px solid #fff4; border-top-color: #fff;
      border-radius: 50%; animation: spin .7s linear infinite;
    }

    @media (max-width: 600px) {
      .page { padding: 1rem; }
      .users-grid { grid-template-columns: 1fr; }
      .page-header { flex-direction: column; gap: .75rem; }
    }
  `]
})
export class UsersComponent implements OnInit {
  private userSvc = inject(UserService);
  auth = inject(AuthService);
  private fb = inject(FormBuilder);

  sidebarOpen  = signal(false);
  users        = signal<UserResponse[]>([]);
  loading      = signal(false);
  page         = signal(0);
  totalPages   = signal(1);

  searchTerm   = '';
  activeFilter = signal<'all' | 'active' | 'inactive'>('all');

  showCreateModal = signal(false);
  createLoading   = signal(false);
  createError     = signal('');

  createForm = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    email:       ['', [Validators.required, Validators.email]],
    username:    ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9._-]+$/)]],
    password:    ['', [Validators.required, Validators.minLength(8)]],
    phoneNumber: [''],
    role:        ['USER'],
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.userSvc.getUsersByTenant(this.page(), 20).subscribe({
      next: res => {
        this.users.set(res.data.content);
        this.totalPages.set(res.data.totalPages || 1);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  filteredUsers() {
    let list = this.users();
    const term = this.searchTerm.toLowerCase().trim();
    if (term) {
      list = list.filter(u =>
        u.fullName.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.username.toLowerCase().includes(term)
      );
    }
    const f = this.activeFilter();
    if (f === 'active')   list = list.filter(u =>  u.active);
    if (f === 'inactive') list = list.filter(u => !u.active);
    return list;
  }

  prevPage(): void { this.page.update(p => p - 1); this.load(); }
  nextPage(): void { this.page.update(p => p + 1); this.load(); }

  activate(u: UserResponse): void {
    this.userSvc.activateUser(u.id).subscribe({ next: res => this.patchUser(res.data) });
  }

  deactivate(u: UserResponse): void {
    this.userSvc.deactivateUser(u.id).subscribe({ next: res => this.patchUser(res.data) });
  }

  openCreate(): void  { this.showCreateModal.set(true); this.createError.set(''); this.createForm.reset({ role: 'USER' }); }
  closeCreate(): void { this.showCreateModal.set(false); }

  submitCreate(): void {
    if (this.createForm.invalid) { this.createForm.markAllAsTouched(); return; }
    this.createLoading.set(true);
    this.createError.set('');

    const v = this.createForm.value;
    const body = {
      firstName:   v.firstName!,
      lastName:    v.lastName!,
      email:       v.email!,
      username:    v.username!,
      password:    v.password!,
      phoneNumber: v.phoneNumber || undefined,
      tenantId:    this.auth.currentUser()!.tenantId,
      tenantSchema: this.auth.currentUser()!.tenantSchema,
      roles:       [v.role!],
    };

    this.userSvc.createUser(body).subscribe({
      next: res => {
        this.users.update(list => [res.data, ...list]);
        this.createLoading.set(false);
        this.closeCreate();
      },
      error: err => {
        this.createError.set(err?.error?.error ?? 'Failed to create user.');
        this.createLoading.set(false);
      }
    });
  }

  t(field: string): boolean {
    const c = this.createForm.get(field);
    return !!(c?.invalid && c.touched);
  }

  initials(u: UserResponse): string {
    return u.fullName.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  avatarColor(u: UserResponse): string {
    const colors = ['#4f46e5','#0ea5e9','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899'];
    let h = 0;
    for (const c of u.id) h = ((h << 5) - h) + c.charCodeAt(0);
    return colors[Math.abs(h) % colors.length];
  }

  formatRole(r: string): string {
    return r.replace('ROLE_', '').replace('_', ' ');
  }

  roleClass(r: string): string {
    return 'role-' + r.replace('ROLE_', '').toLowerCase();
  }

  private patchUser(updated: UserResponse): void {
    this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
  }
}
