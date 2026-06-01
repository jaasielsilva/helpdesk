package com.jaasielsilva.helpdesk.dto.usuario;

import java.time.LocalDateTime;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.model.Usuario;

public record UsuarioResponse(
        Long id,
        String usuario,
        String nome,
        String perfil,
        Long empresaId,
        String empresaNome,
        String empresaSlug,
        boolean ativo,
        LocalDateTime dataCriacao) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getNome(),
                usuario.getPerfil().name(),
                usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null,
                usuario.getEmpresa() != null ? usuario.getEmpresa().getNome() : null,
                usuario.getEmpresa() != null ? usuario.getEmpresa().getSlug() : null,
                usuario.isAtivo(),
                usuario.getDataCriacao());
    }
}
