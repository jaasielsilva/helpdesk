package com.jaasielsilva.helpdesk.dto.usuario;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;

import jakarta.validation.constraints.Size;

public record UpdateUsuarioRequest(
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        PerfilUsuario perfil,

        Boolean ativo,

        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha) {
}
