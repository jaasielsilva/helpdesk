import { moduleForRoute, PERMISSION_MATRIX } from './permissions.config';
import { UserRole } from '../models/user-role';

const DEFAULT_ROLES: UserRole[] = ['SUPER_ADMIN', 'ADMIN', 'SUPORTE', 'USER'];

export function rolesForRoute(path: string): UserRole[] {
  const module = moduleForRoute(path);
  if (!module) {
    return DEFAULT_ROLES;
  }

  return DEFAULT_ROLES.filter((role) =>
    PERMISSION_MATRIX[role][module].includes('VISUALIZAR')
  );
}

export function hasRouteAccess(path: string, role: UserRole): boolean {
  return rolesForRoute(path).includes(role);
}
