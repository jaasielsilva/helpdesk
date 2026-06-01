package com.jaasielsilva.helpdesk.tenant;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado) {
                TenantContextHolder.set(new TenantContext(
                        usuarioAutenticado.getUsuarioId(),
                        usuarioAutenticado.getEmpresaId(),
                        usuarioAutenticado.getPerfil(),
                        usuarioAutenticado.isSuperAdmin()));
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
