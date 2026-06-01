export interface Chamado {
  id: number;
  titulo: string;
  descricao: string;
  status: StatusChamado;
  prioridade: PrioridadeChamado;
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
  prioridade?: PrioridadeChamado;
}

export interface AtualizarChamadoRequest {
  titulo?: string;
  descricao?: string;
  status?: StatusChamado;
  prioridade?: PrioridadeChamado;
  usuarioAtribuidoId?: number;
}

export type StatusChamado = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO' | 'FECHADO';

export const STATUS_CHAMADO_LABEL: Record<StatusChamado, string> = {
  ABERTO: 'Aberto',
  EM_ATENDIMENTO: 'Em Atendimento',
  RESOLVIDO: 'Resolvido',
  FECHADO: 'Fechado'
};

export const STATUS_CHAMADO_COLOR: Record<StatusChamado, string> = {
  ABERTO: '#3b82f6',
  EM_ATENDIMENTO: '#f59e0b',
  RESOLVIDO: '#10b981',
  FECHADO: '#64748b'
};

export type PrioridadeChamado = 'BAIXA' | 'MEDIA' | 'ALTA' | 'URGENTE';

export const PRIORIDADE_LABEL: Record<PrioridadeChamado, string> = {
  BAIXA: 'Baixa',
  MEDIA: 'Média',
  ALTA: 'Alta',
  URGENTE: 'Urgente'
};

export const PRIORIDADE_COLOR: Record<PrioridadeChamado, string> = {
  BAIXA: '#10b981',
  MEDIA: '#f59e0b',
  ALTA: '#f97316',
  URGENTE: '#ef4444'
};

export interface ChamadoStats {
  total: number;
  abertos: number;
  emAtendimento: number;
  resolvidos: number;
  fechados: number;
}
