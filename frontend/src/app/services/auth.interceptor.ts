import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const currentUser = authService.currentUser;

  if (currentUser) {
    const authReq = req.clone({
      setHeaders: {
        'X-User-Id': currentUser.userId,
        'X-User-Role': currentUser.role
      }
    });
    return next(authReq);
  }

  return next(req);
};
