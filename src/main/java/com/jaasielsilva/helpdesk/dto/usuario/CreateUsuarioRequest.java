package com.jaasielsilva.helpdesk.dto.usuario;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUsuarioRequest(
        @NotBlank(message = "Usuário é obrigatório")
        @Size(min = 3, max = 80, message = "Usuário deve ter entre 3 e 80 caracteres")
        String usuario,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha,

        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfil,

        Long empresaId) {
}
