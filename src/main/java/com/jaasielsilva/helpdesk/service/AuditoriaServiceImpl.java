package com.jaasielsilva.helpdesk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.helpdesk.dto.auditoria.AuditoriaLogResponse;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.model.AuditoriaLog;
import com.jaasielsilva.helpdesk.model.Empresa;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.AuditoriaLogRepository;
import com.jaasielsilva.helpdesk.repository.EmpresaRepository;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;
import com.jaasielsilva.helpdesk.tenant.TenantAccessService;

@Service
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final AuditoriaLogRepository auditoriaLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PermissaoService permissaoService;
    private final TenantAccessService tenantAccessService;

    public AuditoriaServiceImpl(
            AuditoriaLogRepository auditoriaLogRepository,
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PermissaoService permissaoService,
            TenantAccessService tenantAccessService) {
        this.auditoriaLogRepository = auditoriaLogRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.permissaoService = permissaoService;
        this.tenantAccessService = tenantAccessService;
    }

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Override
    public Page<AuditoriaLogResponse> listar(
            String usuario,
            ModuloSistema modulo,
            PermissaoAcao acao,
            String dataInicio,
            String dataFim,
            Pageable pageable,
            Authentication auth) {

        permissaoService.require(auth, ModuloSistema.AUDITORIA, PermissaoAcao.VISUALIZAR);

        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);

        LocalDateTime inicio = parseDataInicio(dataInicio);
        LocalDateTime fim    = parseDataFim(dataFim);
        String usuarioFiltro = (usuario != null && !usuario.isBlank()) ? usuario.trim() : null;

        log.debug("Listando auditoria | usuario='{}' modulo={} acao={} inicio={} fim={} por '{}'",
                usuarioFiltro, modulo, acao, inicio, fim, auth.getName());

        if (autenticado.isSuperAdmin()) {
            return auditoriaLogRepository
                    .findAllWithFilters(usuarioFiltro, modulo, acao, inicio, fim, pageable)
                    .map(AuditoriaLogResponse::from);
        }

        Long empresaId = tenantAccessService.requireEmpresaId();
        return auditoriaLogRepository
                .findByEmpresaWithFilters(empresaId, usuarioFiltro, modulo, acao, inicio, fim, pageable)
                .map(AuditoriaLogResponse::from);
    }

    // ── Registro de eventos ──────────────────────────────────────────────────

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            Authentication auth,
            ModuloSistema modulo,
            PermissaoAcao acao,
            String descricao) {
        try {
            if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado autenticado)) {
                return;
            }

            Usuario usuarioEntity = usuarioRepository.findById(autenticado.getUsuarioId())
                    .orElse(null);

            Empresa empresa = autenticado.getEmpresaId() != null
                    ? empresaRepository.findById(autenticado.getEmpresaId()).orElse(null)
                    : null;

            String nomeSnapshot = autenticado.getUsername();

            AuditoriaLog entrada = new AuditoriaLog(
                    usuarioEntity,
                    nomeSnapshot,
                    empresa,
                    modulo,
                    acao,
                    descricao
            );

            auditoriaLogRepository.save(entrada);
            log.debug("[AUDIT] {} | {} | {} | {}", nomeSnapshot, modulo, acao, descricao);

        } catch (Exception ex) {
            // Nunca deixar falha de auditoria derrubar a requisição principal
            log.error("[AUDIT] Falha ao registrar entrada de auditoria: {}", ex.getMessage(), ex);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LocalDateTime parseDataInicio(String data) {
        if (data == null || data.isBlank()) return null;
        try {
            return LocalDate.parse(data).atStartOfDay();
        } catch (DateTimeParseException e) {
            log.warn("[AUDIT] dataInicio inválida: '{}' — ignorada", data);
            return null;
        }
    }

    private LocalDateTime parseDataFim(String data) {
        if (data == null || data.isBlank()) return null;
        try {
            return LocalDate.parse(data).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            log.warn("[AUDIT] dataFim inválida: '{}' — ignorada", data);
            return null;
        }
    }
}
