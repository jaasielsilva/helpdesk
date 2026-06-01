package com.jaasielsilva.helpdesk.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.usuario.CreateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UpdateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UsuarioResponse;

public interface UsuarioService {

    Page<UsuarioResponse> listar(Long empresaId, Pageable pageable, Authentication auth);

    UsuarioResponse buscarPorId(Long id, Authentication auth);

    UsuarioResponse criar(CreateUsuarioRequest request, Authentication auth);

    UsuarioResponse atualizar(Long id, UpdateUsuarioRequest request, Authentication auth);

    void desativar(Long id, Authentication auth);
}
