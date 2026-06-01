export interface Chamado {
  id: number;
  titulo: string;
  descricao: string;
  status: string;
  usuarioId: number;
  usuarioNome: string;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface CriarChamadoRequest {
  titulo: string;
  descricao: string;
  usuarioId: number;
}

export interface PageResponse<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    number: number;  // página atual (0-indexed)
    size: number;
  };
}
