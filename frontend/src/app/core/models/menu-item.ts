import { UserRole } from './user-role';

export type AppModule =
  | 'core'
  | 'tickets'
  | 'catalog'
  | 'operations'
  | 'people'
  | 'knowledge'
  | 'automation'
  | 'insights'
  | 'communication'
  | 'compliance'
  | 'platform'
  | 'saas';

export interface MenuItem {
  id: string;
  label: string;
  route: string;
  icon: string;
  module: AppModule;
  roles: UserRole[];
}

export interface MenuGroup {
  id: string;
  label: string;
  items: MenuItem[];
}
