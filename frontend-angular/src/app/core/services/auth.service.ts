import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  LoginRequest, LoginResponse, RegisterRequest,
  UserResponse, ApiResponse, CurrentUser
} from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);

  private readonly BASE = `${environment.apiUrl}/api/v1/auth`;

  // ── reactive state ──────────────────────────────────────────────────────────
  private _currentUser = signal<CurrentUser | null>(this.loadUserFromStorage());
  private _token       = signal<string | null>(localStorage.getItem('access_token'));

  readonly currentUser  = this._currentUser.asReadonly();
  readonly isLoggedIn   = computed(() => !!this._token());
  readonly isAdmin      = computed(() => this._currentUser()?.roles.includes('ROLE_ADMIN') ?? false);
  readonly isSuperAdmin = computed(() => this._currentUser()?.roles.includes('ROLE_SUPER_ADMIN') ?? false);

  // ── auth operations ─────────────────────────────────────────────────────────

  login(body: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.BASE}/login`, body).pipe(
      tap(res => this.handleLoginSuccess(res.data)),
      catchError(err => throwError(() => err))
    );
  }

  register(body: RegisterRequest): Observable<ApiResponse<UserResponse>> {
    // Strip optional fields that are empty strings — backend @Pattern fails on "".
    const payload: Record<string, unknown> = { ...body };
    if (!payload['phoneNumber']) delete payload['phoneNumber'];
    if (!payload['tenantId'])    delete payload['tenantId'];
    return this.http.post<ApiResponse<UserResponse>>(`${this.BASE}/register`, payload);
  }

  refresh(): Observable<ApiResponse<LoginResponse>> {
    const refreshToken = localStorage.getItem('refresh_token') ?? '';
    return this.http.post<ApiResponse<LoginResponse>>(
      `${this.BASE}/refresh`, {},
      { headers: new HttpHeaders({ 'X-Refresh-Token': refreshToken }) }
    ).pipe(
      tap(res => this.handleLoginSuccess(res.data))
    );
  }

  logout(): void {
    localStorage.clear();
    this._token.set(null);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  getTenantSchema(): string {
    return this._currentUser()?.tenantSchema ?? '';
  }

  // ── private helpers ─────────────────────────────────────────────────────────

  private handleLoginSuccess(data: LoginResponse): void {
    localStorage.setItem('access_token',  data.accessToken);
    localStorage.setItem('refresh_token', data.refreshToken);
    localStorage.setItem('tenant_schema', data.tenantSchema);
    localStorage.setItem('tenant_id',     data.tenantId);

    const user: CurrentUser = {
      id:           data.user.id,
      fullName:     data.user.fullName,
      email:        data.user.email,
      username:     data.user.username,
      tenantId:     data.tenantId,
      tenantSchema: data.tenantSchema,
      roles:        data.roles,
    };
    localStorage.setItem('current_user', JSON.stringify(user));
    this._token.set(data.accessToken);
    this._currentUser.set(user);
  }

  private loadUserFromStorage(): CurrentUser | null {
    try {
      const raw = localStorage.getItem('current_user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
