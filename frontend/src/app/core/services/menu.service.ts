import { Injectable } from '@angular/core';

import { MENU_GROUPS } from '../config/menu.config';
import { moduleForRoute, PERMISSION_MATRIX } from '../config/permissions.config';
import { AuthUser } from '../models/auth-user';
import { MenuGroup, MenuItem } from '../models/menu-item';
import { UserRole } from '../models/user-role';

@Injectable({ providedIn: 'root' })
export class MenuService {
  getMenuGroupsForUser(user: AuthUser | null): MenuGroup[] {
    if (!user) {
      return [];
    }

    const role = this.resolveRole(user.perfil);

    return MENU_GROUPS
      .map((group) => ({
        ...group,
        items: group.items.filter((item) => this.canViewItem(item, role))
      }))
      .filter((group) => group.items.length > 0);
  }

  getMenuForUser(user: AuthUser | null): MenuItem[] {
    return this.getMenuGroupsForUser(user).flatMap((group) => group.items);
  }

  canViewItem(item: MenuItem, role: UserRole): boolean {
    const path = item.route.replace(/^\//, '');
    const module = moduleForRoute(path);
    if (!module) {
      return item.roles.includes(role);
    }
    return PERMISSION_MATRIX[role][module].includes('VISUALIZAR');
  }

  resolveRole(perfil: string): UserRole {
    const normalized = perfil.trim().toUpperCase();

    switch (normalized) {
      case 'SUPER_ADMIN': return 'SUPER_ADMIN';
      case 'ADMIN':       return 'ADMIN';
      case 'SUPORTE':     return 'SUPORTE';
      default:            return 'USER';
    }
  }
}
