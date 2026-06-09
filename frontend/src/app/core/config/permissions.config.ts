import { UserRole } from '../models/user-role';

export type PermissionAction = 'VISUALIZAR' | 'CRIAR' | 'ATENDER' | 'ATRIBUIR' | 'EDITAR' | 'EXCLUIR' | 'GERENCIAR';

export type SystemModule =
  | 'DASHBOARD'
  | 'CHAMADOS'
  | 'BASE_CONHECIMENTO'
  | 'MENSAGENS'
  | 'NOTIFICACOES'
  | 'EQUIPES'
  | 'CATEGORIAS'
  | 'SLA'
  | 'ANALYTICS'
  | 'RELATORIOS'
  | 'AUTOMACAO'
  | 'INTEGRACOES'
  | 'USUARIOS'
  | 'AUDITORIA'
  | 'EMPRESAS'
  | 'ASSINATURA';

type PermissionMatrix = Record<SystemModule, PermissionAction[]>;

const ALL: PermissionAction[] = ['VISUALIZAR', 'CRIAR', 'ATENDER', 'ATRIBUIR', 'EDITAR', 'EXCLUIR', 'GERENCIAR'];
const VIS: PermissionAction[] = ['VISUALIZAR'];
const VIS_CRIAR: PermissionAction[] = ['VISUALIZAR', 'CRIAR'];
const VIS_CRIAR_ATENDER: PermissionAction[] = ['VISUALIZAR', 'CRIAR', 'ATENDER', 'ATRIBUIR', 'EDITAR'];
const VIS_EDITAR: PermissionAction[] = ['VISUALIZAR', 'EDITAR'];
const GERENCIAR: PermissionAction[] = ['VISUALIZAR', 'CRIAR', 'ATENDER', 'ATRIBUIR', 'EDITAR', 'EXCLUIR', 'GERENCIAR'];

const USER_MATRIX: PermissionMatrix = {
  DASHBOARD: VIS,
  CHAMADOS: VIS_CRIAR,
  BASE_CONHECIMENTO: VIS,
  MENSAGENS: VIS_CRIAR,
  NOTIFICACOES: VIS,
  EQUIPES: [],
  CATEGORIAS: [],
  SLA: [],
  ANALYTICS: [],
  RELATORIOS: [],
  AUTOMACAO: [],
  INTEGRACOES: [],
  USUARIOS: [],
  AUDITORIA: [],
  EMPRESAS: [],
  ASSINATURA: []
};

const SUPORTE_MATRIX: PermissionMatrix = {
  ...USER_MATRIX,
  CHAMADOS: VIS_CRIAR_ATENDER,
  EQUIPES: VIS,
  CATEGORIAS: VIS_EDITAR,
  SLA: VIS_EDITAR,
  ANALYTICS: VIS,
  RELATORIOS: VIS
};

const ADMIN_MATRIX: PermissionMatrix = {
  ...SUPORTE_MATRIX,
  CHAMADOS: ALL,
  EQUIPES: GERENCIAR,
  CATEGORIAS: GERENCIAR,
  SLA: GERENCIAR,
  AUTOMACAO: GERENCIAR,
  INTEGRACOES: GERENCIAR,
  USUARIOS: GERENCIAR,
  AUDITORIA: VIS,
  ASSINATURA: GERENCIAR
};

const SUPER_ADMIN_MATRIX = Object.fromEntries(
  (Object.keys(USER_MATRIX) as SystemModule[]).map((modulo) => [modulo, ALL])
) as PermissionMatrix;

export const PERMISSION_MATRIX: Record<UserRole, PermissionMatrix> = {
  USER: USER_MATRIX,
  SUPORTE: SUPORTE_MATRIX,
  ADMIN: ADMIN_MATRIX,
  SUPER_ADMIN: SUPER_ADMIN_MATRIX
};

export const ROUTE_MODULE_MAP: Record<string, SystemModule> = {
  dashboard: 'DASHBOARD',
  chamados: 'CHAMADOS',
  'base-conhecimento': 'BASE_CONHECIMENTO',
  mensagens: 'MENSAGENS',
  notificacoes: 'NOTIFICACOES',
  equipes: 'EQUIPES',
  categorias: 'CATEGORIAS',
  sla: 'SLA',
  analytics: 'ANALYTICS',
  relatorios: 'RELATORIOS',
  automacao: 'AUTOMACAO',
  integracoes: 'INTEGRACOES',
  usuarios: 'USUARIOS',
  auditoria: 'AUDITORIA',
  empresas: 'EMPRESAS',
  assinatura: 'ASSINATURA'
};

export function moduleForRoute(path: string): SystemModule | null {
  return ROUTE_MODULE_MAP[path] ?? null;
}
