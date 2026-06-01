package com.jaasielsilva.helpdesk.dto.chamado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.jaasielsilva.helpdesk.enums.PrioridadeChamado;

public record ChamadoCreateRequest(
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    String titulo,

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
    String descricao,

    @NotNull(message = "ID do usuário responsável é obrigatório")
    Long usuarioId,

    PrioridadeChamado prioridade
) {
}
