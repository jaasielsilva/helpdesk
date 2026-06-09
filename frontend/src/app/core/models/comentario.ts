export type TipoComentario = 'COMENTARIO' | 'EVENTO_SISTEMA';

export interface Comentario {
  id: number;
  chamadoId: number;
  autorId: number | null;
  autorNome: string;
  autorPerfil: string | null;
  conteudo: string;
  tipo: TipoComentario;
  interno: boolean;
  dataCriacao: string;
}

export interface CriarComentarioRequest {
  conteudo: string;
  interno: boolean;
}
