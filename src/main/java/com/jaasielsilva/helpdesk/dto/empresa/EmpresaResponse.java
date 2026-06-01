package com.jaasielsilva.helpdesk.dto.empresa;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.model.Empresa;

public record EmpresaResponse(
        Long id,
        String slug,
        String nome,
        boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao) {

    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getSlug(),
                empresa.getNome(),
                empresa.isAtivo(),
                empresa.getDataCriacao(),
                empresa.getDataAtualizacao());
    }
}
