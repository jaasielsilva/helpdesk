package com.jaasielsilva.helpdesk.tenant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.jaasielsilva.helpdesk.model.Chamado;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

@Service
public class TenantAccessService {

    public TenantContext requireContext() {
        TenantContext context = TenantContextHolder.get();
        if (context == null) {
            throw new AccessDeniedException("Contexto de tenant indisponível");
        }
        return context;
    }

    public boolean isSuperAdmin() {
        TenantContext context = TenantContextHolder.get();
        return context != null && context.superAdmin();
    }

    public Long requireEmpresaId() {
        TenantContext context = requireContext();
        if (context.superAdmin()) {
            throw new AccessDeniedException("Operação requer contexto de empresa");
        }
        if (context.empresaId() == null) {
            throw new AccessDeniedException("Usuário sem empresa vinculada");
        }
        return context.empresaId();
    }

    public void validateSameEmpresa(Usuario usuario) {
        if (isSuperAdmin()) {
            return;
        }

        Long empresaId = requireEmpresaId();
        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresaId)) {
            throw new AccessDeniedException("Recurso pertence a outra empresa");
        }
    }

    public void validateChamadoAccess(Chamado chamado, UsuarioAutenticado usuarioAutenticado) {
        if (usuarioAutenticado.isSuperAdmin()) {
            return;
        }

        Long empresaId = requireEmpresaId();
        if (!chamado.getEmpresa().getId().equals(empresaId)) {
            throw new AccessDeniedException("Chamado pertence a outra empresa");
        }

        if (usuarioAutenticado.getPerfil().isTenantStaff()) {
            return;
        }

        if (!chamado.getUsuario().getId().equals(usuarioAutenticado.getUsuarioId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar este chamado");
        }
    }

    public boolean canManageChamados(UsuarioAutenticado usuarioAutenticado) {
        return usuarioAutenticado.isSuperAdmin()
                || usuarioAutenticado.getPerfil().isTenantStaff();
    }
}
