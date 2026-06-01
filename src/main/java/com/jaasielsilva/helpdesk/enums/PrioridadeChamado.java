package com.jaasielsilva.helpdesk.enums;

public enum PrioridadeChamado {
    BAIXA("Baixa", "#10b981"),
    MEDIA("Média", "#f59e0b"),
    ALTA("Alta", "#f97316"),
    URGENTE("Urgente", "#ef4444");

    private final String label;
    private final String cor;

    PrioridadeChamado(String label, String cor) {
        this.label = label;
        this.cor = cor;
    }

    public String getLabel() {
        return label;
    }

    public String getCor() {
        return cor;
    }
}
