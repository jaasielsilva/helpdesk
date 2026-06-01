package com.jaasielsilva.helpdesk.dto.empresa;

import jakarta.validation.constraints.NotNull;

public record UpdateEmpresaStatusRequest(
        @NotNull(message = "Status ativo é obrigatório")
        Boolean ativo) {
}
