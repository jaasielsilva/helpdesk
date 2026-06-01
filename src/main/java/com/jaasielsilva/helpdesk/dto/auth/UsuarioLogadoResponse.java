package com.jaasielsilva.helpdesk.dto.auth;

public record UsuarioLogadoResponse(
        Long id,
        String usuario,
        String nome,
        String perfil,
        Long empresaId,
        String empresaNome,
        String empresaSlug) {
}
