package com.jaasielsilva.helpdesk.enums;

public enum PerfilUsuario {
    SUPER_ADMIN("Super Administrador"),
    ADMIN("Administrador"),
    SUPORTE("Suporte"),
    USER("Usuário");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPlatformOwner() {
        return this == SUPER_ADMIN;
    }

    public boolean isTenantStaff() {
        return this == ADMIN || this == SUPORTE;
    }
}
