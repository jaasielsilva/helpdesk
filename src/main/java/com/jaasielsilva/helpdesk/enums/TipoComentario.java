package com.jaasielsilva.helpdesk.enums;

public enum TipoComentario {
    COMENTARIO("Comentário"),
    EVENTO_SISTEMA("Evento do Sistema");

    private final String descricao;

    TipoComentario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
