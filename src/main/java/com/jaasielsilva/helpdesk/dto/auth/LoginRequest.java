package com.jaasielsilva.helpdesk.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Usuário é obrigatório")
        String usuario,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        String empresaSlug) {
}
