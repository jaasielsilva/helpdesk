export type StatusChamado = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO' | 'FECHADO';

export interface Chamado {
  id: number;
  titulo: string;
  descricao: string;
  status: StatusChamado;
  usuarioId: number;
  usuarioNome: string;
  usuarioAtribuidoId: number | null;
  usuarioAtribuidoNome: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
  dataFechamento: string | null;
}

export interface CriarChamadoRequest {
  titulo: string;
  descricao: string;
  usuarioId: number;
}

export interface AtualizarChamadoRequest {
  titulo?: string;
  descricao?: string;
  status?: StatusChamado;
  usuarioAtribuidoId?: number | null;
}

export interface ChamadoFiltros {
  status?: StatusChamado | '';
  busca?: string;
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

export const STATUS_CHAMADO_LABELS: Record<StatusChamado, string> = {
  ABERTO: 'Aberto',
  EM_ATENDIMENTO: 'Em Atendimento',
  RESOLVIDO: 'Resolvido',
  FECHADO: 'Fechado'
};

export const STATUS_LIST: StatusChamado[] = ['ABERTO', 'EM_ATENDIMENTO', 'RESOLVIDO', 'FECHADO'];
