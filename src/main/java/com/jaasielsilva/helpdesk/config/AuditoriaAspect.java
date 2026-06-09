package com.jaasielsilva.helpdesk.config;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.service.AuditoriaService;

/**
 * Intercepta métodos anotados com {@link Auditavel} e registra a entrada
 * na tabela de auditoria APÓS a execução bem-sucedida do método.
 *
 * Usa {@code @AfterReturning} para garantir que apenas operações que
 * retornaram sem exceção sejam registradas.
 */
@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @AfterReturning("@annotation(auditavel)")
    public void auditar(JoinPoint joinPoint, Auditavel auditavel) {
        try {
            Authentication auth = resolverAuthentication(joinPoint.getArgs());
            if (auth == null) {
                log.warn("[AUDIT] Nenhuma Authentication encontrada em {}", joinPoint.getSignature().getName());
                return;
            }

            String descricao = auditavel.descricao().isBlank()
                    ? gerarDescricao(auditavel.modulo(), auditavel.acao(), joinPoint.getSignature().getName())
                    : auditavel.descricao();

            auditoriaService.registrar(auth, auditavel.modulo(), auditavel.acao(), descricao);

        } catch (Exception ex) {
            log.error("[AUDIT] Falha no aspect de auditoria para {}: {}",
                    joinPoint.getSignature().getName(), ex.getMessage(), ex);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Authentication resolverAuthentication(Object[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg instanceof Authentication)
                .map(Authentication.class::cast)
                .findFirst()
                .orElse(null);
    }

    private String gerarDescricao(ModuloSistema modulo, PermissaoAcao acao, String metodo) {
        return String.format("%s em %s (operação: %s)",
                formatarAcao(acao), formatarModulo(modulo), metodo);
    }

    private String formatarAcao(PermissaoAcao acao) {
        return switch (acao) {
            case VISUALIZAR -> "Visualizou";
            case CRIAR      -> "Criou";
            case EDITAR     -> "Editou";
            case EXCLUIR    -> "Excluiu";
            case GERENCIAR  -> "Gerenciou";
            default         -> acao.name();
        };
    }

    private String formatarModulo(ModuloSistema modulo) {
        return modulo.name().replace("_", " ").toLowerCase();
    }
}
