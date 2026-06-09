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

import com.jaasielsilva.helpdesk.config.Auditavel;
import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.usuario.CreateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UpdateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UsuarioResponse;
import com.jaasielsilva.helpdesk.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).USUARIOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listar(
            @RequestParam(required = false) Long empresaId,
            Pageable pageable,
            Authentication auth) {
        log.debug("GET /api/usuarios por '{}'", auth.getName());
        Page<UsuarioResponse> usuarios = service.listar(empresaId, pageable, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Usuários listados com sucesso", usuarios));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).USUARIOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscar(@PathVariable Long id, Authentication auth) {
        log.debug("GET /api/usuarios/{} por '{}'", id, auth.getName());
        UsuarioResponse usuario = service.buscarPorId(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Usuário encontrado", usuario));
    }

    @PostMapping
    @Auditavel(modulo = com.jaasielsilva.helpdesk.enums.ModuloSistema.USUARIOS, acao = com.jaasielsilva.helpdesk.enums.PermissaoAcao.CRIAR, descricao = "Usuário criado")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).USUARIOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).CRIAR)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> criar(
            @Valid @RequestBody CreateUsuarioRequest request,
            Authentication auth) {
        log.debug("POST /api/usuarios por '{}'", auth.getName());
        UsuarioResponse usuario = service.criar(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Usuário criado com sucesso", usuario));
    }

    @PutMapping("/{id}")
    @Auditavel(modulo = com.jaasielsilva.helpdesk.enums.ModuloSistema.USUARIOS, acao = com.jaasielsilva.helpdesk.enums.PermissaoAcao.EDITAR, descricao = "Usuário atualizado")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).USUARIOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).EDITAR)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request,
            Authentication auth) {
        log.debug("PUT /api/usuarios/{} por '{}'", id, auth.getName());
        UsuarioResponse usuario = service.atualizar(id, request, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Usuário atualizado com sucesso", usuario));
    }

    @DeleteMapping("/{id}")
    @Auditavel(modulo = com.jaasielsilva.helpdesk.enums.ModuloSistema.USUARIOS, acao = com.jaasielsilva.helpdesk.enums.PermissaoAcao.EXCLUIR, descricao = "Usuário desativado")
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).USUARIOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).EXCLUIR)")
    public ResponseEntity<ApiResponse<Void>> desativar(@PathVariable Long id, Authentication auth) {
        log.debug("DELETE /api/usuarios/{} por '{}'", id, auth.getName());
        service.desativar(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Usuário desativado com sucesso"));
    }
}
