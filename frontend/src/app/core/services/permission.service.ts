import { Injectable, inject } from '@angular/core';

import {
  PERMISSION_MATRIX,
  PermissionAction,
  SystemModule,
  moduleForRoute
} from '../config/permissions.config';
import { UserRole } from '../models/user-role';
import { MenuService } from './menu.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly menuService = inject(MenuService);

  can(role: UserRole | string, module: SystemModule, action: PermissionAction): boolean {
    const resolvedRole = this.menuService.resolveRole(String(role));
    const actions = PERMISSION_MATRIX[resolvedRole][module];
    return actions.includes(action);
  }

  canViewRoute(role: UserRole | string, path: string): boolean {
    const module = moduleForRoute(path);
    if (!module) {
      return true;
    }
    return this.can(role, module, 'VISUALIZAR');
  }
}
