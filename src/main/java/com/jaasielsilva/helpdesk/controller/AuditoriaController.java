package com.jaasielsilva.helpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.auditoria.AuditoriaLogResponse;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.service.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * GET /api/auditoria
     *
     * Query params (todos opcionais):
     *   - usuario     : busca parcial por nome/login (LIKE)
     *   - modulo      : enum ModuloSistema
     *   - acao        : enum PermissaoAcao
     *   - dataInicio  : yyyy-MM-dd
     *   - dataFim     : yyyy-MM-dd
     *   - page, size, sort (Pageable padrão do Spring)
     *
     * Requer perfil ADMIN ou SUPER_ADMIN.
     */
    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).AUDITORIA, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<Page<AuditoriaLogResponse>>> listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) ModuloSistema modulo,
            @RequestParam(required = false) PermissaoAcao acao,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {

        log.debug("GET /api/auditoria por '{}'", auth.getName());

        Page<AuditoriaLogResponse> resultado = auditoriaService.listar(
                usuario, modulo, acao, dataInicio, dataFim, pageable, auth);

        return ResponseEntity.ok(ApiResponse.sucesso("Logs de auditoria listados com sucesso", resultado));
    }
}
