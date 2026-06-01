import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  if (!tokenStorage.hasToken()) {
    return router.createUrlTree(['/login']);
  }

  return authService.me().pipe(
    map(() => true),
    catchError(() => {
      authService.clearSession();
      return of(router.createUrlTree(['/login']));
    })
  );
};
