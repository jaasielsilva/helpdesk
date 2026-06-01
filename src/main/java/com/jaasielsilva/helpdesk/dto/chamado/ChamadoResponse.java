package com.jaasielsilva.helpdesk.dto.chamado;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.enums.PrioridadeChamado;
import com.jaasielsilva.helpdesk.enums.StatusChamado;
import com.jaasielsilva.helpdesk.model.Chamado;

public record ChamadoResponse(
    Long id,
    String titulo,
    String descricao,
    StatusChamado status,
    PrioridadeChamado prioridade,
    Long usuarioId,
    String usuarioNome,
    Long usuarioAtribuidoId,
    String usuarioAtribuidoNome,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao,
    LocalDateTime dataFechamento
) {

    public static ChamadoResponse from(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getStatus(),
                chamado.getPrioridade(),
                chamado.getUsuario().getId(),
                chamado.getUsuario().getNome(),
                chamado.getUsuarioAtribuido() != null ? chamado.getUsuarioAtribuido().getId() : null,
                chamado.getUsuarioAtribuido() != null ? chamado.getUsuarioAtribuido().getNome() : null,
                chamado.getDataCriacao(),
                chamado.getDataAtualizacao(),
                chamado.getDataFechamento());
    }
}
