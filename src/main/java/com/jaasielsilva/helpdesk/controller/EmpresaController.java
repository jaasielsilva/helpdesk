package com.jaasielsilva.helpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.empresa.CreateEmpresaRequest;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaCreateResponse;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaResponse;
import com.jaasielsilva.helpdesk.dto.empresa.UpdateEmpresaStatusRequest;
import com.jaasielsilva.helpdesk.service.EmpresaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private static final Logger log = LoggerFactory.getLogger(EmpresaController.class);

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).EMPRESAS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).CRIAR)")
    public ResponseEntity<ApiResponse<EmpresaCreateResponse>> criar(
            @Valid @RequestBody CreateEmpresaRequest request,
            Authentication auth) {
        log.debug("POST /api/empresas por '{}'", auth.getName());
        EmpresaCreateResponse response = service.criar(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Empresa criada com sucesso", response));
    }

    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).EMPRESAS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<Page<EmpresaResponse>>> listar(Pageable pageable, Authentication auth) {
        log.debug("GET /api/empresas por '{}'", auth.getName());
        Page<EmpresaResponse> empresas = service.listar(pageable, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Empresas listadas com sucesso", empresas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).EMPRESAS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<EmpresaResponse>> buscar(@PathVariable Long id, Authentication auth) {
        log.debug("GET /api/empresas/{} por '{}'", id, auth.getName());
        EmpresaResponse empresa = service.buscarPorId(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Empresa encontrada", empresa));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).EMPRESAS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).EDITAR)")
    public ResponseEntity<ApiResponse<EmpresaResponse>> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmpresaStatusRequest request,
            Authentication auth) {
        log.debug("PATCH /api/empresas/{}/status por '{}'", id, auth.getName());
        EmpresaResponse empresa = service.atualizarStatus(id, request.ativo(), auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Status da empresa atualizado", empresa));
    }
}
