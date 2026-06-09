package com.jaasielsilva.helpdesk.dto.auditoria;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.model.AuditoriaLog;

public record AuditoriaLogResponse(
        Long id,
        String usuario,
        String modulo,
        String acao,
        String descricao,
        LocalDateTime dataHora
) {
    public static AuditoriaLogResponse from(AuditoriaLog log) {
        return new AuditoriaLogResponse(
                log.getId(),
                log.getUsuarioNome(),
                log.getModulo().name(),
                log.getAcao().name(),
                log.getDescricao(),
                log.getDataHora()
        );
    }
}
