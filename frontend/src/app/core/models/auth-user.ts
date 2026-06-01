import { UserRole } from './user-role';

export interface AuthUser {
  usuario: string;
  nome: string;
  perfil: UserRole | string;
  id: number;
  empresaId: number | null;
  empresaNome: string | null;
  empresaSlug: string | null;
}
