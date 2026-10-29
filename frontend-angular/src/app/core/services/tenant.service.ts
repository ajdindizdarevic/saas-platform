import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TenantResponse, ApiResponse, Page, TenantStatus } from '../models';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private http = inject(HttpClient);
  private readonly BASE = `${environment.apiUrl}/api/v1/tenants`;

  getAllTenants(page = 0, size = 20): Observable<ApiResponse<Page<TenantResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<Page<TenantResponse>>>(this.BASE, { params });
  }

  getTenantById(id: string): Observable<ApiResponse<TenantResponse>> {
    return this.http.get<ApiResponse<TenantResponse>>(`${this.BASE}/${id}`);
  }

  getTenantsByStatus(status: TenantStatus, page = 0, size = 20): Observable<ApiResponse<Page<TenantResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<Page<TenantResponse>>>(`${this.BASE}/status/${status}`, { params });
  }

  suspendTenant(id: string): Observable<ApiResponse<TenantResponse>> {
    return this.http.patch<ApiResponse<TenantResponse>>(`${this.BASE}/${id}/suspend`, {});
  }

  activateTenant(id: string): Observable<ApiResponse<TenantResponse>> {
    return this.http.patch<ApiResponse<TenantResponse>>(`${this.BASE}/${id}/activate`, {});
  }
}
