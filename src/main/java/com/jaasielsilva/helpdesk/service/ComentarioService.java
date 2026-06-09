package com.jaasielsilva.helpdesk.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.comentario.ComentarioCreateRequest;
import com.jaasielsilva.helpdesk.dto.comentario.ComentarioResponse;

public interface ComentarioService {

    ComentarioResponse adicionar(Long chamadoId, ComentarioCreateRequest request, Authentication auth);

    List<ComentarioResponse> listar(Long chamadoId, Authentication auth);

    void adicionarEvento(Long chamadoId, String mensagem, Authentication auth);
}
