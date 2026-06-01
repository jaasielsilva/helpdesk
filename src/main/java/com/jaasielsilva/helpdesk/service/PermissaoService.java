package com.jaasielsilva.helpdesk.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.security.MatrizPermissoes;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

@Service("permService")
public class PermissaoService {

    public boolean can(Authentication authentication, ModuloSistema modulo, PermissaoAcao acao) {
        UsuarioAutenticado usuario = UsuarioDetailsService.requireUsuarioAutenticado(authentication);
        return MatrizPermissoes.permite(usuario.getPerfil(), modulo, acao);
    }

    public void require(Authentication authentication, ModuloSistema modulo, PermissaoAcao acao) {
        if (!can(authentication, modulo, acao)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Sem permissão para " + acao.name().toLowerCase() + " em " + modulo.name());
        }
    }
}
