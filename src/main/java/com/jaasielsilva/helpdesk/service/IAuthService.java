package com.jaasielsilva.helpdesk.service;

import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.auth.LoginRequest;
import com.jaasielsilva.helpdesk.dto.auth.LoginResponse;
import com.jaasielsilva.helpdesk.dto.auth.UsuarioLogadoResponse;

public interface IAuthService {
    LoginResponse autenticar(LoginRequest request);

    UsuarioLogadoResponse buscarUsuarioLogado(String username);

    UsuarioLogadoResponse buscarUsuarioLogado(Authentication authentication);
}
