import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { SidebarComponent } from '../../shared/sidebar/sidebar.component';
import { OnboardingService } from '../../core/services/onboarding.service';
import { TenantService } from '../../core/services/tenant.service';
import { AuthService } from '../../core/services/auth.service';
import { OnboardingResponse, TenantResponse, ApproveRequest, RequestStatus } from '../../core/models';

type ActiveTab = 'queue' | 'tenants';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, NavbarComponent, SidebarComponent],
  template: `
    <div class="shell">
      <app-navbar (sidebarToggle)="sidebarOpen.set(!sidebarOpen())" />
      <div class="body">
        <app-sidebar [open]="sidebarOpen()" />

        <main class="page">
          <!-- page header -->
          <div class="page-header">
            <div>
              <h1>Admin Dashboard</h1>
              <p class="subtitle">Manage onboarding requests and tenant accounts.</p>
            </div>
          </div>

          <!-- stat cards -->
          <div class="stats-grid">
            @for (s of stats(); track s.label) {
              <div class="stat-card" [class]="s.accent">
                <div class="stat-icon">{{ s.icon }}</div>
                <div class="stat-body">
                  <div class="stat-value">{{ s.value }}</div>
                  <div class="stat-label">{{ s.label }}</div>
                </div>
              </div>
            }
          </div>

          <!-- tab strip -->
          <div class="tab-strip">
            <button class="tab" [class.active]="activeTab() === 'queue'" (click)="activeTab.set('queue')">
              Onboarding Queue
              @if (pendingCount() > 0) { <span class="badge">{{ pendingCount() }}</span> }
            </button>
            <button class="tab" [class.active]="activeTab() === 'tenants'" (click)="loadTenants(); activeTab.set('tenants')">
              Active Tenants
            </button>
          </div>

          <!-- ── Onboarding Queue ───────────────────────────────────────────── -->
          @if (activeTab() === 'queue') {
            <div class="panel">
              <div class="panel-toolbar">
                <div class="filter-pills">
                  @for (f of statusFilters; track f.value) {
                    <button class="pill" [class.active]="statusFilter() === f.value"
                            (click)="setFilter(f.value)">
                      {{ f.label }}
                    </button>
                  }
                </div>
              </div>

              @if (loadingRequests()) {
                <div class="loading-row"><span class="loader"></span> Loading…</div>
              } @else if (filteredRequests().length === 0) {
                <div class="empty-state">
                  <span>📭</span>
                  <p>No requests in this category</p>
                </div>
              } @else {
                <div class="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Company</th>
                        <th>Contact</th>
                        <th>Submitted</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (r of filteredRequests(); track r.id) {
                        <tr>
                          <td>
                            <div class="cell-primary">{{ r.companyName }}</div>
                            @if (r.businessType) {
                              <div class="cell-secondary">{{ r.businessType }}</div>
                            }
                          </td>
                          <td>
                            <div class="cell-primary">{{ r.contactName }}</div>
                            <div class="cell-secondary">{{ r.contactEmail }}</div>
                          </td>
                          <td class="cell-date">{{ r.createdAt | date:'MMM d, y' }}</td>
                          <td><span class="status-badge" [class]="'s-' + r.status.toLowerCase()">{{ r.status }}</span></td>
                          <td>
                            <div class="action-group">
                              @if (r.status === 'PENDING') {
                                <button class="btn-review" (click)="markUnderReview(r.id)">Review</button>
                              }
                              @if (r.status === 'PENDING' || r.status === 'UNDER_REVIEW') {
                                <button class="btn-approve" (click)="openApprove(r)">Approve</button>
                                <button class="btn-reject"  (click)="openReject(r)">Reject</button>
                              }
                            </div>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>

                <!-- pagination -->
                <div class="pagination">
                  <button [disabled]="requestPage() === 0" (click)="prevRequestPage()">← Prev</button>
                  <span>Page {{ requestPage() + 1 }} of {{ requestTotalPages() }}</span>
                  <button [disabled]="requestPage() >= requestTotalPages() - 1" (click)="nextRequestPage()">Next →</button>
                </div>
              }
            </div>
          }

          <!-- ── Tenants ─────────────────────────────────────────────────────── -->
          @if (activeTab() === 'tenants') {
            <div class="panel">
              @if (loadingTenants()) {
                <div class="loading-row"><span class="loader"></span> Loading…</div>
              } @else if (tenants().length === 0) {
                <div class="empty-state"><span>🏢</span><p>No tenants yet</p></div>
              } @else {
                <div class="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Company</th>
                        <th>Schema</th>
                        <th>Plan</th>
                        <th>Users</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (t of tenants(); track t.id) {
                        <tr>
                          <td>
                            <div class="cell-primary">{{ t.companyName }}</div>
                            <div class="cell-secondary">{{ t.contactEmail }}</div>
                          </td>
                          <td><code class="schema-pill">{{ t.schemaName }}</code></td>
                          <td><span class="plan-badge">{{ t.plan }}</span></td>
                          <td class="cell-date">{{ t.maxUsers }}</td>
                          <td><span class="status-badge" [class]="'s-' + t.status.toLowerCase()">{{ t.status }}</span></td>
                          <td class="cell-date">{{ t.createdAt | date:'MMM d, y' }}</td>
                          <td>
                            <div class="action-group">
                              @if (t.status === 'ACTIVE') {
                                <button class="btn-reject" (click)="suspendTenant(t.id)">Suspend</button>
                              }
                              @if (t.status === 'SUSPENDED') {
                                <button class="btn-approve" (click)="activateTenant(t.id)">Activate</button>
                              }
                            </div>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>

                <div class="pagination">
                  <button [disabled]="tenantPage() === 0" (click)="prevTenantPage()">← Prev</button>
                  <span>Page {{ tenantPage() + 1 }} of {{ tenantTotalPages() }}</span>
                  <button [disabled]="tenantPage() >= tenantTotalPages() - 1" (click)="nextTenantPage()">Next →</button>
                </div>
              }
            </div>
          }
        </main>
      </div>
    </div>

    <!-- ── Approve modal ──────────────────────────────────────────────────── -->
    @if (approveModal()) {
      <div class="modal-backdrop" (click)="closeModals()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Approve request</h3>
          <p>Approve <strong>{{ approveModal()!.companyName }}</strong>? This will provision a tenant schema.</p>
          @if (modalError()) { <div class="alert-err">{{ modalError() }}</div> }
          <div class="modal-actions">
            <button class="btn-back" (click)="closeModals()">Cancel</button>
            <button class="btn-approve-lg" [disabled]="modalLoading()"
                    (click)="confirmApprove()">
              @if (modalLoading()) { Processing… } @else { ✓ Approve }
            </button>
          </div>
        </div>
      </div>
    }

    <!-- ── Admin credentials notice ─────────────────────────────────────── -->
    @if (approvedCredentials()) {
      <div class="modal-backdrop" (click)="closeModals()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="cred-check">✓</div>
          <h3>Company approved!</h3>
          <p><strong>{{ approvedCredentials()!.company }}</strong> has been approved and is being provisioned.</p>
          <div class="cred-box">
            <div class="cred-row"><span class="cred-label">Email</span><code>{{ approvedCredentials()!.email }}</code></div>
            <div class="cred-row"><span class="cred-label">Password</span><code>admin</code></div>
            <div class="cred-row"><span class="cred-label">Username</span><code>admin1</code></div>
          </div>
          <p class="cred-note">⚠ Share these credentials with the company admin. They should change their password on first login via the profile menu.</p>
          <div class="modal-actions">
            <button class="btn-approve-lg" (click)="closeModals()">Got it</button>
          </div>
        </div>
      </div>
    }

    <!-- ── Reject modal ───────────────────────────────────────────────────── -->
    @if (rejectModal()) {
      <div class="modal-backdrop" (click)="closeModals()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Reject request</h3>
          <p>Reject <strong>{{ rejectModal()!.companyName }}</strong>?</p>
          <label class="modal-label">Reason <span class="req">*</span></label>
          <textarea [(ngModel)]="rejectReason" rows="3"
                    placeholder="Please provide a clear reason…"
                    class="modal-textarea"></textarea>
          @if (modalError()) { <div class="alert-err">{{ modalError() }}</div> }
          <div class="modal-actions">
            <button class="btn-back" (click)="closeModals()">Cancel</button>
            <button class="btn-reject-lg" [disabled]="modalLoading() || !rejectReason.trim()"
                    (click)="confirmReject()">
              @if (modalLoading()) { Processing… } @else { ✗ Reject }
            </button>
          </div>
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
    .page-header { margin-bottom: 1.75rem; }
    h1 {
      font-family: 'Fraunces', serif;
      font-size: 1.75rem; font-weight: 600; color: #0f0f11; margin: 0 0 .25rem;
    }
    .subtitle { color: #71717a; font-size: .88rem; margin: 0; }

    /* ── stats ─────────────────────────────────────────────────────────────── */
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
    .stat-card {
      background: #fff; border-radius: 14px; padding: 1.25rem 1.5rem;
      display: flex; align-items: center; gap: 1rem;
      border: 1px solid #f0f0f0;
    }
    .stat-card.indigo { border-left: 3px solid #4f46e5; }
    .stat-card.amber  { border-left: 3px solid #f59e0b; }
    .stat-card.green  { border-left: 3px solid #22c55e; }
    .stat-card.slate  { border-left: 3px solid #64748b; }
    .stat-icon { font-size: 1.4rem; }
    .stat-value { font-size: 1.5rem; font-weight: 600; color: #0f0f11; line-height: 1; }
    .stat-label { font-size: .78rem; color: #71717a; margin-top: .2rem; }

    /* ── tabs ──────────────────────────────────────────────────────────────── */
    .tab-strip { display: flex; gap: .25rem; margin-bottom: 1.25rem; }
    .tab {
      background: none; border: none; padding: .55rem 1.1rem;
      border-radius: 8px; font-family: inherit; font-size: .87rem;
      color: #71717a; cursor: pointer; font-weight: 400;
      display: flex; align-items: center; gap: .4rem;
      transition: background .15s, color .15s;
    }
    .tab:hover { background: #f0f0f0; color: #0f0f11; }
    .tab.active { background: #0f0f11; color: #fff; font-weight: 500; }
    .badge {
      background: #ef4444; color: #fff; font-size: .68rem; font-weight: 700;
      padding: .1rem .45rem; border-radius: 999px;
    }

    /* ── panel ─────────────────────────────────────────────────────────────── */
    .panel { background: #fff; border-radius: 14px; border: 1px solid #f0f0f0; overflow: hidden; }
    .panel-toolbar { padding: 1rem 1.25rem; border-bottom: 1px solid #f4f4f5; }

    .filter-pills { display: flex; gap: .4rem; flex-wrap: wrap; }
    .pill {
      background: #f4f4f5; border: 1px solid #e4e4e7;
      color: #52525b; padding: .3rem .85rem; border-radius: 999px;
      font-family: inherit; font-size: .78rem; cursor: pointer;
      transition: all .15s;
    }
    .pill:hover { border-color: #a1a1aa; }
    .pill.active { background: #0f0f11; border-color: #0f0f11; color: #fff; }

    /* ── table ─────────────────────────────────────────────────────────────── */
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    thead { background: #fafafa; }
    th {
      text-align: left; padding: .75rem 1.25rem;
      font-size: .75rem; font-weight: 500; color: #71717a;
      text-transform: uppercase; letter-spacing: .04em;
      border-bottom: 1px solid #f0f0f0;
    }
    td { padding: .9rem 1.25rem; border-bottom: 1px solid #f7f7f7; vertical-align: middle; }
    tr:last-child td { border-bottom: none; }
    tr:hover td { background: #fafafa; }

    .cell-primary   { font-size: .87rem; font-weight: 500; color: #0f0f11; }
    .cell-secondary { font-size: .77rem; color: #71717a; margin-top: .15rem; }
    .cell-date      { font-size: .82rem; color: #52525b; }

    /* ── status badges ─────────────────────────────────────────────────────── */
    .status-badge {
      display: inline-block; padding: .22rem .75rem;
      border-radius: 999px; font-size: .73rem; font-weight: 600;
      text-transform: uppercase; letter-spacing: .04em;
    }
    .s-pending      { background: #fef9c3; color: #a16207; }
    .s-under_review { background: #eff6ff; color: #1d4ed8; }
    .s-approved,
    .s-active       { background: #f0fdf4; color: #15803d; }
    .s-rejected     { background: #fef2f2; color: #dc2626; }
    .s-cancelled,
    .s-suspended    { background: #f4f4f5; color: #52525b; }
    .s-provisioning { background: #fdf4ff; color: #9333ea; }
    .s-deactivated  { background: #f4f4f5; color: #71717a; }

    .schema-pill {
      background: #f4f4f5; color: #4f46e5; padding: .2rem .6rem;
      border-radius: 6px; font-size: .78rem; font-family: monospace;
    }
    .plan-badge {
      background: #f0f9ff; color: #0369a1; padding: .2rem .65rem;
      border-radius: 6px; font-size: .75rem; font-weight: 600;
    }

    /* ── action buttons ────────────────────────────────────────────────────── */
    .action-group { display: flex; gap: .4rem; align-items: center; }
    .btn-review, .btn-approve, .btn-reject {
      padding: .28rem .7rem; border-radius: 6px;
      font-family: inherit; font-size: .78rem; font-weight: 500; cursor: pointer;
      border: 1.5px solid; transition: all .15s;
    }
    .btn-review  { background: #f0f9ff; border-color: #bae6fd; color: #0369a1; }
    .btn-review:hover  { background: #e0f2fe; }
    .btn-approve { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }
    .btn-approve:hover { background: #dcfce7; }
    .btn-reject  { background: #fef2f2; border-color: #fecaca; color: #dc2626; }
    .btn-reject:hover  { background: #fee2e2; }

    /* ── pagination ────────────────────────────────────────────────────────── */
    .pagination {
      display: flex; align-items: center; justify-content: center;
      gap: 1rem; padding: 1rem; border-top: 1px solid #f4f4f5;
    }
    .pagination button {
      background: none; border: 1.5px solid #e4e4e7; padding: .35rem .9rem;
      border-radius: 7px; font-family: inherit; font-size: .82rem; cursor: pointer;
      transition: border-color .15s;
    }
    .pagination button:hover:not(:disabled) { border-color: #a1a1aa; }
    .pagination button:disabled { opacity: .35; cursor: not-allowed; }
    .pagination span { font-size: .82rem; color: #71717a; }

    /* ── loading / empty ───────────────────────────────────────────────────── */
    .loading-row {
      display: flex; align-items: center; gap: .75rem; justify-content: center;
      padding: 2.5rem; color: #71717a; font-size: .9rem;
    }
    .loader {
      width: 18px; height: 18px; border: 2px solid #e4e4e7; border-top-color: #4f46e5;
      border-radius: 50%; animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    .empty-state {
      text-align: center; padding: 3rem; color: #a1a1aa;
    }
    .empty-state span { font-size: 2rem; display: block; margin-bottom: .75rem; }
    .empty-state p { font-size: .88rem; margin: 0; }

    /* ── modal ─────────────────────────────────────────────────────────────── */
    .modal-backdrop {
      position: fixed; inset: 0; background: #00000055;
      display: flex; align-items: center; justify-content: center; z-index: 500;
    }
    .modal {
      background: #fff; border-radius: 16px; padding: 2rem;
      width: 100%; max-width: 440px; margin: 1rem;
      box-shadow: 0 20px 60px #00000022;
    }
    .modal h3 {
      font-family: 'Fraunces', serif; font-size: 1.3rem; font-weight: 600;
      color: #0f0f11; margin: 0 0 .5rem;
    }
    .modal p { color: #52525b; font-size: .9rem; margin: 0 0 1.25rem; }
    .modal-label { font-size: .8rem; font-weight: 500; color: #3f3f46; display: block; margin-bottom: .4rem; }
    .req { color: #ef4444; }
    .modal-textarea {
      width: 100%; padding: .65rem .9rem; border: 1.5px solid #e4e4e7;
      border-radius: 9px; font-family: inherit; font-size: .87rem;
      outline: none; resize: vertical; box-sizing: border-box; margin-bottom: 1rem;
    }
    .modal-textarea:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px #4f46e51a; }
    .alert-err {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      border-radius: 8px; padding: .65rem .9rem; font-size: .83rem; margin-bottom: 1rem;
    }
    .modal-actions { display: flex; gap: .75rem; justify-content: flex-end; }

    /* credentials notice */
    .cred-check { font-size: 2rem; color: #15803d; text-align: center; margin-bottom: .5rem; }
    .cred-box {
      background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px;
      padding: 1rem 1.25rem; margin: 1rem 0; display: flex; flex-direction: column; gap: .55rem;
    }
    .cred-row { display: flex; align-items: center; gap: .75rem; font-size: .87rem; }
    .cred-label { width: 70px; color: #64748b; font-weight: 500; flex-shrink: 0; }
    .cred-row code { background: #e2e8f0; padding: .2rem .5rem; border-radius: 5px; font-size: .84rem; }
    .cred-note { font-size: .78rem; color: #92400e; background: #fffbeb; border: 1px solid #fde68a;
      border-radius: 8px; padding: .6rem .85rem; margin: 0; }
    .btn-back {
      background: none; border: 1.5px solid #e4e4e7; color: #52525b;
      padding: .6rem 1.25rem; border-radius: 8px; font-family: inherit;
      font-size: .88rem; cursor: pointer;
    }
    .btn-approve-lg {
      background: #15803d; color: #fff; border: none;
      padding: .6rem 1.5rem; border-radius: 8px; font-family: inherit;
      font-size: .88rem; font-weight: 500; cursor: pointer;
    }
    .btn-approve-lg:disabled { opacity: .5; cursor: not-allowed; }
    .btn-reject-lg {
      background: #dc2626; color: #fff; border: none;
      padding: .6rem 1.5rem; border-radius: 8px; font-family: inherit;
      font-size: .88rem; font-weight: 500; cursor: pointer;
    }
    .btn-reject-lg:disabled { opacity: .5; cursor: not-allowed; }

    @media (max-width: 900px) {
      .stats-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 600px) {
      .stats-grid { grid-template-columns: 1fr; }
      .page { padding: 1rem; }
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  private onboardingSvc = inject(OnboardingService);
  private tenantSvc     = inject(TenantService);
  auth = inject(AuthService);

  sidebarOpen = signal(false);
  activeTab   = signal<ActiveTab>('queue');

  // ── requests state ──────────────────────────────────────────────────────────
  requests        = signal<OnboardingResponse[]>([]);
  loadingRequests = signal(false);
  requestPage     = signal(0);
  requestTotalPages = signal(1);
  statusFilter    = signal<RequestStatus | 'ALL'>('ALL');

  // ── tenants state ───────────────────────────────────────────────────────────
  tenants         = signal<TenantResponse[]>([]);
  loadingTenants  = signal(false);
  tenantPage      = signal(0);
  tenantTotalPages = signal(1);

  // ── modals ──────────────────────────────────────────────────────────────────
  approveModal  = signal<OnboardingResponse | null>(null);
  rejectModal   = signal<OnboardingResponse | null>(null);
  rejectReason  = '';
  modalLoading  = signal(false);
  modalError    = signal('');

  statusFilters: { label: string; value: RequestStatus | 'ALL' }[] = [
    { label: 'All',          value: 'ALL'         },
    { label: 'Pending',      value: 'PENDING'      },
    { label: 'Under Review', value: 'UNDER_REVIEW' },
    { label: 'Approved',     value: 'APPROVED'     },
    { label: 'Rejected',     value: 'REJECTED'     },
  ];

  pendingCount = computed(() => this.requests().filter(r => r.status === 'PENDING').length);

  filteredRequests = computed(() => {
    const f = this.statusFilter();
    return f === 'ALL' ? this.requests() : this.requests().filter(r => r.status === f);
  });

  stats = computed(() => [
    { icon: '📋', label: 'Total Requests', value: this.requests().length,    accent: 'indigo' },
    { icon: '⏳', label: 'Pending',        value: this.pendingCount(),        accent: 'amber'  },
    { icon: '🏢', label: 'Active Tenants', value: this.tenants().length,      accent: 'green'  },
    { icon: '✓',  label: 'Approved',       value: this.requests().filter(r => r.status === 'APPROVED').length, accent: 'slate' },
  ]);

  ngOnInit(): void { this.loadRequests(); }

  // ── data loading ─────────────────────────────────────────────────────────────

  loadRequests(): void {
    this.loadingRequests.set(true);
    this.onboardingSvc.getAllRequests(this.requestPage(), 50).subscribe({
      next: res => {
        this.requests.set(res.data.content);
        this.requestTotalPages.set(res.data.totalPages || 1);
        this.loadingRequests.set(false);
      },
      error: () => this.loadingRequests.set(false)
    });
  }

  loadTenants(): void {
    if (this.tenants().length) return;
    this.loadingTenants.set(true);
    this.tenantSvc.getAllTenants(this.tenantPage(), 20).subscribe({
      next: res => {
        this.tenants.set(res.data.content);
        this.tenantTotalPages.set(res.data.totalPages || 1);
        this.loadingTenants.set(false);
      },
      error: () => this.loadingTenants.set(false)
    });
  }

  // ── filters / pagination ────────────────────────────────────────────────────

  setFilter(f: RequestStatus | 'ALL'): void { this.statusFilter.set(f); }

  prevRequestPage(): void { this.requestPage.update(p => p - 1); this.loadRequests(); }
  nextRequestPage(): void { this.requestPage.update(p => p + 1); this.loadRequests(); }
  prevTenantPage():  void { this.tenantPage.update(p => p - 1); this.tenants.set([]); this.loadTenants(); }
  nextTenantPage():  void { this.tenantPage.update(p => p + 1); this.tenants.set([]); this.loadTenants(); }

  // ── actions ──────────────────────────────────────────────────────────────────

  markUnderReview(id: string): void {
    this.onboardingSvc.markUnderReview(id).subscribe({
      next: res => this.patchRequest(res.data),
      error: () => {}
    });
  }

  openApprove(r: OnboardingResponse): void  { this.approveModal.set(r); this.modalError.set(''); }
  openReject(r: OnboardingResponse):  void  { this.rejectModal.set(r);  this.rejectReason = ''; this.modalError.set(''); }
  closeModals(): void { this.approveModal.set(null); this.rejectModal.set(null); this.approvedCredentials.set(null); }

  approvedCredentials = signal<{ email: string; company: string } | null>(null);

  confirmApprove(): void {
    const r = this.approveModal();
    if (!r) return;
    this.modalLoading.set(true);
    const body: ApproveRequest = { approved: true, reviewedBy: this.auth.currentUser()?.username };
    this.onboardingSvc.processApproval(r.id, body).subscribe({
      next: res => {
        this.patchRequest(res.data);
        this.approveModal.set(null);   // close approve modal
        this.modalLoading.set(false);
        // Show credentials notice
        this.approvedCredentials.set({ email: r.contactEmail, company: r.companyName });
      },
      error: err => { this.modalError.set(err?.error?.error ?? 'Failed'); this.modalLoading.set(false); }
    });
  }

  confirmReject(): void {
    const r = this.rejectModal();
    if (!r || !this.rejectReason.trim()) return;
    this.modalLoading.set(true);
    const body: ApproveRequest = {
      approved: false,
      rejectionReason: this.rejectReason,
      reviewedBy: this.auth.currentUser()?.username
    };
    this.onboardingSvc.processApproval(r.id, body).subscribe({
      next: res => { this.patchRequest(res.data); this.closeModals(); this.modalLoading.set(false); },
      error: err => { this.modalError.set(err?.error?.error ?? 'Failed'); this.modalLoading.set(false); }
    });
  }

  suspendTenant(id: string): void {
    this.tenantSvc.suspendTenant(id).subscribe({
      next: res => this.patchTenant(res.data)
    });
  }

  activateTenant(id: string): void {
    this.tenantSvc.activateTenant(id).subscribe({
      next: res => this.patchTenant(res.data)
    });
  }

  private patchRequest(updated: OnboardingResponse): void {
    this.requests.update(list => list.map(r => r.id === updated.id ? updated : r));
  }

  private patchTenant(updated: TenantResponse): void {
    this.tenants.update(list => list.map(t => t.id === updated.id ? updated : t));
  }
}
