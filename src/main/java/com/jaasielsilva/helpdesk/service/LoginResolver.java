package com.jaasielsilva.helpdesk.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.jaasielsilva.helpdesk.dto.auth.LoginRequest;
import com.jaasielsilva.helpdesk.exception.LoginRequiresEmpresaSlugException;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;

@Service
public class LoginResolver {

    private final UsuarioRepository usuarioRepository;

    public LoginResolver(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario resolve(LoginRequest request) {
        String usuario = request.usuario().trim();
        String empresaSlug = request.empresaSlug() != null ? request.empresaSlug().trim() : null;

        var superAdmin = usuarioRepository.findSuperAdminByUsuarioIgnoreCase(usuario);
        if (superAdmin.isPresent()) {
            return superAdmin.get();
        }

        if (empresaSlug != null && !empresaSlug.isBlank()) {
            return usuarioRepository.findByUsuarioIgnoreCaseAndEmpresaSlug(usuario, empresaSlug)
                    .orElseThrow(() -> new BadCredentialsException("Usuário ou senha inválidos"));
        }

        var tenantUsers = usuarioRepository.findAllTenantUsersByUsuarioIgnoreCase(usuario);
        if (tenantUsers.isEmpty()) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }
        if (tenantUsers.size() == 1) {
            return tenantUsers.get(0);
        }

        throw new LoginRequiresEmpresaSlugException();
    }
}
