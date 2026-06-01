package com.jaasielsilva.helpdesk.dto.chamado;

public record ChamadoStats(
    long total,
    long abertos,
    long emAtendimento,
    long resolvidos,
    long fechados
) {}
