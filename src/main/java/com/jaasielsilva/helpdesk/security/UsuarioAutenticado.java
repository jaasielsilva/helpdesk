package com.jaasielsilva.helpdesk.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.model.Usuario;

public class UsuarioAutenticado implements UserDetails {

    private final Usuario usuario;

    public UsuarioAutenticado(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    public Long getEmpresaId() {
        return usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null;
    }

    public String getEmpresaNome() {
        return usuario.getEmpresa() != null ? usuario.getEmpresa().getNome() : null;
    }

    public String getEmpresaSlug() {
        return usuario.getEmpresa() != null ? usuario.getEmpresa().getSlug() : null;
    }

    public PerfilUsuario getPerfil() {
        return usuario.getPerfil();
    }

    public boolean isSuperAdmin() {
        return usuario.isSuperAdmin();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getUsuario();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isAtivo();
    }
}
