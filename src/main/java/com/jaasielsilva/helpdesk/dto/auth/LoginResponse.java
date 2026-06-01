package com.jaasielsilva.helpdesk.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UsuarioLogadoResponse usuario) {
}
