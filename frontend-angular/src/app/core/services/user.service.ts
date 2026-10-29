import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserResponse, CreateUserRequest, ApiResponse, Page } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private readonly BASE = `${environment.apiUrl}/api/v1/users`;

  getMe(): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.BASE}/me`);
  }

  getUserById(id: string): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.BASE}/${id}`);
  }

  getUsersByTenant(page = 0, size = 20): Observable<ApiResponse<Page<UserResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<Page<UserResponse>>>(this.BASE, { params });
  }

  createUser(body: CreateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(this.BASE, body);
  }

  activateUser(id: string): Observable<ApiResponse<UserResponse>> {
    return this.http.patch<ApiResponse<UserResponse>>(`${this.BASE}/${id}/activate`, {});
  }

  deactivateUser(id: string): Observable<ApiResponse<UserResponse>> {
    return this.http.patch<ApiResponse<UserResponse>>(`${this.BASE}/${id}/deactivate`, {});
  }

  changePassword(currentPassword: string, newPassword: string): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.BASE}/me/password`, {
      currentPassword,
      newPassword
    });
  }
}
