import { UserRole } from './user-role';

export interface Usuario {
  id: number;
  usuario: string;
  nome: string;
  perfil: UserRole;
  empresaId: number | null;
  empresaNome: string | null;
  empresaSlug: string | null;
  ativo: boolean;
  dataCriacao: string;
}

export interface CreateUsuarioRequest {
  usuario: string;
  senha: string;
  nome?: string;
  perfil: UserRole;
  empresaId?: number | null;
}

export interface UpdateUsuarioRequest {
  nome?: string;
  perfil?: UserRole;
  ativo?: boolean;
  senha?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  };
}
