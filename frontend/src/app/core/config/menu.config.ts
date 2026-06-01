import { MenuGroup, MenuItem } from '../models/menu-item';

const ALL_ROLES = ['SUPER_ADMIN', 'ADMIN', 'SUPORTE', 'USER'] as const;
const STAFF_ROLES = ['SUPER_ADMIN', 'ADMIN', 'SUPORTE'] as const;
const ADMIN_ROLES = ['SUPER_ADMIN', 'ADMIN'] as const;

const CORE_ITEMS: MenuItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    route: '/dashboard',
    icon: 'dashboard',
    module: 'core',
    roles: [...ALL_ROLES]
  },
  {
    id: 'chamados',
    label: 'Chamados',
    route: '/chamados',
    icon: 'chamados',
    module: 'tickets',
    roles: [...ALL_ROLES]
  },
  {
    id: 'base-conhecimento',
    label: 'Base de Conhecimento',
    route: '/base-conhecimento',
    icon: 'base-conhecimento',
    module: 'knowledge',
    roles: [...ALL_ROLES]
  },
  {
    id: 'mensagens',
    label: 'Mensagens',
    route: '/mensagens',
    icon: 'mensagens',
    module: 'communication',
    roles: [...ALL_ROLES]
  },
  {
    id: 'notificacoes',
    label: 'Notificações',
    route: '/notificacoes',
    icon: 'notificacoes',
    module: 'communication',
    roles: [...ALL_ROLES]
  }
];

const OPERACOES_ITEMS: MenuItem[] = [
  {
    id: 'equipes',
    label: 'Equipes',
    route: '/equipes',
    icon: 'equipes',
    module: 'people',
    roles: [...STAFF_ROLES]
  },
  {
    id: 'categorias',
    label: 'Categorias',
    route: '/categorias',
    icon: 'categorias',
    module: 'catalog',
    roles: [...STAFF_ROLES]
  },
  {
    id: 'sla',
    label: 'SLA',
    route: '/sla',
    icon: 'sla',
    module: 'operations',
    roles: [...STAFF_ROLES]
  }
];

const INSIGHTS_ITEMS: MenuItem[] = [
  {
    id: 'analytics',
    label: 'Analytics',
    route: '/analytics',
    icon: 'analytics',
    module: 'insights',
    roles: [...STAFF_ROLES]
  },
  {
    id: 'relatorios',
    label: 'Relatórios',
    route: '/relatorios',
    icon: 'relatorios',
    module: 'insights',
    roles: [...STAFF_ROLES]
  }
];

const PLATAFORMA_ITEMS: MenuItem[] = [
  {
    id: 'automacao',
    label: 'Automação',
    route: '/automacao',
    icon: 'automacao',
    module: 'automation',
    roles: [...ADMIN_ROLES]
  },
  {
    id: 'integracoes',
    label: 'Integrações',
    route: '/integracoes',
    icon: 'integracoes',
    module: 'platform',
    roles: [...ADMIN_ROLES]
  }
];

const ADMINISTRACAO_ITEMS: MenuItem[] = [
  {
    id: 'usuarios',
    label: 'Usuários',
    route: '/usuarios',
    icon: 'usuarios',
    module: 'people',
    roles: [...ADMIN_ROLES]
  },
  {
    id: 'auditoria',
    label: 'Auditoria',
    route: '/auditoria',
    icon: 'auditoria',
    module: 'compliance',
    roles: [...ADMIN_ROLES]
  },
  {
    id: 'empresas',
    label: 'Empresas',
    route: '/empresas',
    icon: 'empresas',
    module: 'saas',
    roles: ['SUPER_ADMIN']
  },
  {
    id: 'assinatura',
    label: 'Assinatura',
    route: '/assinatura',
    icon: 'assinatura',
    module: 'saas',
    roles: [...ADMIN_ROLES]
  }
];

export const MENU_GROUPS: MenuGroup[] = [
  { id: 'core', label: 'Core', items: CORE_ITEMS },
  { id: 'operacoes', label: 'Operações', items: OPERACOES_ITEMS },
  { id: 'insights', label: 'Insights', items: INSIGHTS_ITEMS },
  { id: 'plataforma', label: 'Plataforma', items: PLATAFORMA_ITEMS },
  { id: 'administracao', label: 'Administração', items: ADMINISTRACAO_ITEMS }
];

export const MENU_ITEMS: MenuItem[] = MENU_GROUPS.flatMap((group) => group.items);
