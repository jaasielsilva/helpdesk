package com.jaasielsilva.helpdesk.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

@Service
public class UsuarioDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final com.jaasielsilva.helpdesk.repository.UsuarioRepository repository;

    public UsuarioDetailsService(com.jaasielsilva.helpdesk.repository.UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String username) {
        return repository.findByUsuarioIgnoreCase(username.trim())
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Usuario nao encontrado"));
    }

    public static UsuarioAutenticado requireUsuarioAutenticado(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário autenticado inválido");
        }
        return usuarioAutenticado;
    }
}
