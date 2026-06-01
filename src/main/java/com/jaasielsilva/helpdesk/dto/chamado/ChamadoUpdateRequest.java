package com.jaasielsilva.helpdesk.dto.chamado;

import jakarta.validation.constraints.Size;
import com.jaasielsilva.helpdesk.enums.PrioridadeChamado;
import com.jaasielsilva.helpdesk.enums.StatusChamado;

public record ChamadoUpdateRequest(
    @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    String titulo,

    @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
    String descricao,

    StatusChamado status,

    PrioridadeChamado prioridade,

    Long usuarioAtribuidoId
) {
}
