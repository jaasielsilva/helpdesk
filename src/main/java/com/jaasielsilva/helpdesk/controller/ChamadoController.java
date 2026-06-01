package com.jaasielsilva.helpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoCreateRequest;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoResponse;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoStats;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoUpdateRequest;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.enums.StatusChamado;
import com.jaasielsilva.helpdesk.service.ChamadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private static final Logger log = LoggerFactory.getLogger(ChamadoController.class);

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).CRIAR)")
    public ResponseEntity<ApiResponse<ChamadoResponse>> criar(
            @Valid @RequestBody ChamadoCreateRequest request,
            Authentication auth) {
        log.debug("POST /api/chamados por '{}'", auth.getName());
        ChamadoResponse chamado = service.criar(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Chamado criado com sucesso", chamado));
    }

    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<Page<ChamadoResponse>>> listar(
            Pageable pageable,
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) String busca,
            Authentication auth) {
        log.debug("GET /api/chamados por '{}' (status={}, busca={})", auth.getName(), status, busca);
        Page<ChamadoResponse> chamados = service.listar(pageable, status, busca, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Chamados listados com sucesso", chamados));
    }

    @GetMapping("/stats")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<ChamadoStats>> estatisticas(Authentication auth) {
        log.debug("GET /api/chamados/stats por '{}'", auth.getName());
        ChamadoStats stats = service.estatisticas(auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Estatísticas geradas", stats));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<ChamadoResponse>> buscar(@PathVariable Long id, Authentication auth) {
        log.debug("GET /api/chamados/{} por '{}'", id, auth.getName());
        ChamadoResponse chamado = service.buscarPorId(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Chamado encontrado", chamado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).EDITAR)")
    public ResponseEntity<ApiResponse<ChamadoResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoUpdateRequest request,
            Authentication auth) {
        log.debug("PUT /api/chamados/{} por '{}'", id, auth.getName());
        ChamadoResponse chamado = service.atualizar(id, request, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Chamado atualizado com sucesso", chamado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).EXCLUIR)")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id, Authentication auth) {
        log.debug("DELETE /api/chamados/{} por '{}'", id, auth.getName());
        service.deletar(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Chamado deletado com sucesso"));
    }
}
