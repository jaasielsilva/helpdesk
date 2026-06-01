package com.jaasielsilva.helpdesk.dto.chamado;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.enums.StatusChamado;

public record ChamadoResponse(
    Long id,
    String titulo,
    String descricao,
    StatusChamado status,
    Long usuarioId,
    String usuarioNome,
    Long usuarioAtribuidoId,
    String usuarioAtribuidoNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao,
    LocalDateTime dataFechamento
) {
}
