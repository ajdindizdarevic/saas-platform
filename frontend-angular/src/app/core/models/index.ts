// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  password: string;
  phoneNumber?: string;
  tenantId?: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
  user: UserResponse;
  tenantId: string;
  tenantSchema: string;
  roles: string[];
}

// ─── User ─────────────────────────────────────────────────────────────────────

export interface UserResponse {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  username: string;
  phoneNumber?: string;
  tenantId: string;
  tenantSchema: string;
  roles: string[];
  active: boolean;
  emailVerified: boolean;
  profilePictureUrl?: string;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  password: string;
  phoneNumber?: string;
  tenantId: string;
  tenantSchema?: string;
  roles?: string[];
}

// ─── Onboarding ───────────────────────────────────────────────────────────────

export type RequestStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface CreateOnboardingRequest {
  companyName: string;
  contactEmail: string;
  contactName: string;
  phoneNumber?: string;
  businessType?: string;
  expectedUsers?: number;
  notes?: string;
}

export interface OnboardingResponse {
  id: string;
  companyName: string;
  contactEmail: string;
  contactName: string;
  phoneNumber?: string;
  businessType?: string;
  expectedUsers?: number;
  notes?: string;
  status: RequestStatus;
  rejectionReason?: string;
  reviewedBy?: string;
  tenantId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApproveRequest {
  approved: boolean;
  rejectionReason?: string;
  reviewedBy?: string;
}

// ─── Tenant ───────────────────────────────────────────────────────────────────

export type TenantStatus = 'PROVISIONING' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED';

export interface TenantResponse {
  id: string;
  companyName: string;
  schemaName: string;
  contactEmail: string;
  contactName: string;
  phoneNumber?: string;
  businessType?: string;
  expectedUsers?: number;
  maxUsers: number;
  maxStorageGb: number;
  plan: string;
  status: TenantStatus;
  onboardingRequestId?: string;
  createdAt: string;
  updatedAt: string;
}

// ─── Pagination ───────────────────────────────────────────────────────────────

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
  timestamp: string;
}

// ─── UI helpers ───────────────────────────────────────────────────────────────

export interface CurrentUser {
  id: string;
  fullName: string;
  email: string;
  username: string;
  tenantId: string;
  tenantSchema: string;
  roles: string[];
}
