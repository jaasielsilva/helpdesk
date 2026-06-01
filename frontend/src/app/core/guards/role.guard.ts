import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { PermissionService } from '../services/permission.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const permissionService = inject(PermissionService);
  const router = inject(Router);
  const notificationService = inject(NotificationService);

  const path = route.routeConfig?.path;
  if (!path) {
    return true;
  }

  const user = authService.usuarioAtual;
  if (!user) {
    return router.createUrlTree(['/login']);
  }

  if (permissionService.canViewRoute(user.perfil, path)) {
    return true;
  }

  notificationService.warning('Você não tem permissão para acessar esta página.');
  return router.createUrlTree(['/dashboard']);
};
