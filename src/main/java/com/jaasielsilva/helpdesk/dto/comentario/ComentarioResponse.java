package com.jaasielsilva.helpdesk.dto.comentario;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.enums.TipoComentario;

public record ComentarioResponse(
    Long id,
    Long chamadoId,
    Long autorId,
    String autorNome,
    String autorPerfil,
    String conteudo,
    TipoComentario tipo,
    boolean interno,
    LocalDateTime dataCriacao
) {}
