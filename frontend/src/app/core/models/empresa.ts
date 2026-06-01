export interface Empresa {
  id: number;
  slug: string;
  nome: string;
  ativo: boolean;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface CreateEmpresaRequest {
  slug: string;
  nome: string;
  adminUsuario: string;
  adminSenha: string;
  adminNome?: string;
}

export interface EmpresaCreateResponse {
  empresa: Empresa;
  adminUsuario: string;
  adminPerfil: string;
}

