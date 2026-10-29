import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const auth   = inject(AuthService);
  const schema = auth.getTenantSchema();

  if (!schema) return next(req);

  return next(req.clone({ setHeaders: { 'X-Tenant-Schema': schema } }));
};
