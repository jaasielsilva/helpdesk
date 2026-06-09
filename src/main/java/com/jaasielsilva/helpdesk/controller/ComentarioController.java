package com.jaasielsilva.helpdesk.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.comentario.ComentarioCreateRequest;
import com.jaasielsilva.helpdesk.dto.comentario.ComentarioResponse;
import com.jaasielsilva.helpdesk.service.ComentarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chamados/{chamadoId}/comentarios")
public class ComentarioController {

    private static final Logger log = LoggerFactory.getLogger(ComentarioController.class);

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<List<ComentarioResponse>>> listar(
            @PathVariable Long chamadoId,
            Authentication auth) {
        log.debug("GET /api/chamados/{}/comentarios por '{}'", chamadoId, auth.getName());
        List<ComentarioResponse> comentarios = service.listar(chamadoId, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Comentários listados", comentarios));
    }

    @PostMapping
    @PreAuthorize("@permService.can(authentication, T(com.jaasielsilva.helpdesk.enums.ModuloSistema).CHAMADOS, T(com.jaasielsilva.helpdesk.enums.PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<ComentarioResponse>> adicionar(
            @PathVariable Long chamadoId,
            @Valid @RequestBody ComentarioCreateRequest request,
            Authentication auth) {
        log.debug("POST /api/chamados/{}/comentarios por '{}'", chamadoId, auth.getName());
        ComentarioResponse comentario = service.adicionar(chamadoId, request, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.sucesso("Comentário adicionado", comentario));
    }
}
