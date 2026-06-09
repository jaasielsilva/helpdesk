package com.jaasielsilva.helpdesk.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.auditoria.AuditoriaLogResponse;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;

public interface AuditoriaService {

    /**
     * Lista os logs de auditoria com filtros opcionais.
     * O escopo (todas empresas vs empresa do usuário) é resolvido internamente.
     */
    Page<AuditoriaLogResponse> listar(
            String usuario,
            ModuloSistema modulo,
            PermissaoAcao acao,
            String dataInicio,
            String dataFim,
            Pageable pageable,
            Authentication auth);

    /**
     * Registra uma entrada de auditoria. Usado pelo AuditoriaAspect e pelos services.
     * Não lança exceções — falhas são logadas silenciosamente para não impactar o fluxo principal.
     */
    void registrar(
            Authentication auth,
            ModuloSistema modulo,
            PermissaoAcao acao,
            String descricao);
}
