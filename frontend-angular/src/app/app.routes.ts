import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'onboarding',
    loadComponent: () =>
      import('./pages/onboarding-request/onboarding-request.component').then(m => m.OnboardingRequestComponent)
  },

  // ── authenticated shell ─────────────────────────────────────────────────────
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    data: { roles: ['ROLE_SUPER_ADMIN'] }
  },
  {
    path: 'users',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/users/users.component').then(m => m.UsersComponent),
    data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER'] }
  },

  { path: '**', redirectTo: '/login' }
];
