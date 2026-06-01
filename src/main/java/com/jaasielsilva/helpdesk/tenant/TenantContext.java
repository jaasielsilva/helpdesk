package com.jaasielsilva.helpdesk.tenant;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;

public record TenantContext(
        Long usuarioId,
        Long empresaId,
        PerfilUsuario perfil,
        boolean superAdmin) {

    public boolean hasEmpresa() {
        return empresaId != null;
    }
}
