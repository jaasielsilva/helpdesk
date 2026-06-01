package com.jaasielsilva.helpdesk.dto.empresa;

public record EmpresaCreateResponse(
        EmpresaResponse empresa,
        String adminUsuario,
        String adminPerfil) {
}
