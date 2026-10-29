import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const requiredRoles = route.data?.['roles'] as string[] | undefined;
  if (requiredRoles?.length) {
    const userRoles = auth.currentUser()?.roles ?? [];
    const hasRole   = requiredRoles.some(r => userRoles.includes(r));
    if (!hasRole) {
      // ADMIN users can't access SUPER_ADMIN-only routes → send to their home
      const dest = userRoles.includes('ROLE_SUPER_ADMIN') ? '/dashboard' : '/users';
      router.navigate([dest]);
      return false;
    }
  }

  return true;
};
