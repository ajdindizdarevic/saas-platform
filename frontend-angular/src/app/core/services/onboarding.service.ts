import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateOnboardingRequest, OnboardingResponse,
  ApproveRequest, ApiResponse, Page, RequestStatus
} from '../models';

@Injectable({ providedIn: 'root' })
export class OnboardingService {
  private http = inject(HttpClient);
  private readonly BASE = `${environment.apiUrl}/api/v1`;

  // ── public ──────────────────────────────────────────────────────────────────

  submitRequest(body: CreateOnboardingRequest): Observable<ApiResponse<OnboardingResponse>> {
    return this.http.post<ApiResponse<OnboardingResponse>>(`${this.BASE}/onboarding`, body);
  }

  getRequestById(id: string): Observable<ApiResponse<OnboardingResponse>> {
    return this.http.get<ApiResponse<OnboardingResponse>>(`${this.BASE}/onboarding/${id}`);
  }

  getAllRequests(page = 0, size = 20): Observable<ApiResponse<Page<OnboardingResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<Page<OnboardingResponse>>>(`${this.BASE}/onboarding`, { params });
  }

  getRequestsByStatus(
    status: RequestStatus, page = 0, size = 20
  ): Observable<ApiResponse<Page<OnboardingResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<Page<OnboardingResponse>>>(
      `${this.BASE}/onboarding/status/${status}`, { params }
    );
  }

  cancelRequest(id: string): Observable<ApiResponse<OnboardingResponse>> {
    return this.http.patch<ApiResponse<OnboardingResponse>>(`${this.BASE}/onboarding/${id}/cancel`, {});
  }

  // ── admin ───────────────────────────────────────────────────────────────────

  processApproval(id: string, body: ApproveRequest): Observable<ApiResponse<OnboardingResponse>> {
    return this.http.put<ApiResponse<OnboardingResponse>>(
      `${this.BASE}/admin/onboarding/${id}/review`, body
    );
  }

  markUnderReview(id: string): Observable<ApiResponse<OnboardingResponse>> {
    return this.http.patch<ApiResponse<OnboardingResponse>>(
      `${this.BASE}/admin/onboarding/${id}/under-review`, {}
    );
  }
}
