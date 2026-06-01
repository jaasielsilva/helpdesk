package com.jaasielsilva.helpdesk.enums;

public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em Atendimento"),
    RESOLVIDO("Resolvido"),
    FECHADO("Fechado");

    private final String descricao;

    StatusChamado(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
