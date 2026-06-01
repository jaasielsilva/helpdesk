package com.jaasielsilva.helpdesk.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEmpresaRequest(
        @NotBlank(message = "Slug é obrigatório")
        @Size(min = 2, max = 80, message = "Slug deve ter entre 2 e 80 caracteres")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Usuário admin é obrigatório")
        @Size(min = 3, max = 80, message = "Usuário admin deve ter entre 3 e 80 caracteres")
        String adminUsuario,

        @NotBlank(message = "Senha admin é obrigatória")
        @Size(min = 6, max = 100, message = "Senha admin deve ter entre 6 e 100 caracteres")
        String adminSenha,

        @Size(max = 150, message = "Nome admin deve ter no máximo 150 caracteres")
        String adminNome) {
}
