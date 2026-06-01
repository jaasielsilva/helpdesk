package com.jaasielsilva.helpdesk.security;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;

public final class MatrizPermissoes {

    private static final Map<PerfilUsuario, Map<ModuloSistema, Set<PermissaoAcao>>> MATRIZ = new EnumMap<>(PerfilUsuario.class);

    static {
        registrar(PerfilUsuario.USER, permissoesUsuario());
        registrar(PerfilUsuario.SUPORTE, permissoesSuporte());
        registrar(PerfilUsuario.ADMIN, permissoesAdmin());
        registrar(PerfilUsuario.SUPER_ADMIN, permissoesSuperAdmin());
    }

    private MatrizPermissoes() {
    }

    public static boolean permite(PerfilUsuario perfil, ModuloSistema modulo, PermissaoAcao acao) {
        Map<ModuloSistema, Set<PermissaoAcao>> modulos = MATRIZ.get(perfil);
        if (modulos == null) {
            return false;
        }
        Set<PermissaoAcao> acoes = modulos.get(modulo);
        return acoes != null && acoes.contains(acao);
    }

    private static void registrar(PerfilUsuario perfil, Map<ModuloSistema, Set<PermissaoAcao>> permissoes) {
        MATRIZ.put(perfil, permissoes);
    }

    private static Map<ModuloSistema, Set<PermissaoAcao>> permissoesUsuario() {
        Map<ModuloSistema, Set<PermissaoAcao>> map = new EnumMap<>(ModuloSistema.class);
        map.put(ModuloSistema.DASHBOARD, vis());
        map.put(ModuloSistema.CHAMADOS, visCriar());
        map.put(ModuloSistema.BASE_CONHECIMENTO, vis());
        map.put(ModuloSistema.MENSAGENS, visCriar());
        map.put(ModuloSistema.NOTIFICACOES, vis());
        return map;
    }

    private static Map<ModuloSistema, Set<PermissaoAcao>> permissoesSuporte() {
        Map<ModuloSistema, Set<PermissaoAcao>> map = permissoesUsuario();
        map.put(ModuloSistema.CHAMADOS, visCriarEditar());
        map.put(ModuloSistema.EQUIPES, vis());
        map.put(ModuloSistema.CATEGORIAS, visEditar());
        map.put(ModuloSistema.SLA, visEditar());
        map.put(ModuloSistema.ANALYTICS, vis());
        map.put(ModuloSistema.RELATORIOS, vis());
        return map;
    }

    private static Map<ModuloSistema, Set<PermissaoAcao>> permissoesAdmin() {
        Map<ModuloSistema, Set<PermissaoAcao>> map = permissoesSuporte();
        map.put(ModuloSistema.CHAMADOS, todas());
        map.put(ModuloSistema.EQUIPES, gerenciar());
        map.put(ModuloSistema.CATEGORIAS, gerenciar());
        map.put(ModuloSistema.SLA, gerenciar());
        map.put(ModuloSistema.ANALYTICS, vis());
        map.put(ModuloSistema.RELATORIOS, vis());
        map.put(ModuloSistema.AUTOMACAO, gerenciar());
        map.put(ModuloSistema.INTEGRACOES, gerenciar());
        map.put(ModuloSistema.USUARIOS, gerenciar());
        map.put(ModuloSistema.AUDITORIA, vis());
        map.put(ModuloSistema.ASSINATURA, gerenciar());
        return map;
    }

    private static Map<ModuloSistema, Set<PermissaoAcao>> permissoesSuperAdmin() {
        Map<ModuloSistema, Set<PermissaoAcao>> map = new EnumMap<>(ModuloSistema.class);
        for (ModuloSistema modulo : ModuloSistema.values()) {
            map.put(modulo, todas());
        }
        return map;
    }

    private static Set<PermissaoAcao> vis() {
        return EnumSet.of(PermissaoAcao.VISUALIZAR);
    }

    private static Set<PermissaoAcao> visCriar() {
        return EnumSet.of(PermissaoAcao.VISUALIZAR, PermissaoAcao.CRIAR);
    }

    private static Set<PermissaoAcao> visCriarEditar() {
        return EnumSet.of(PermissaoAcao.VISUALIZAR, PermissaoAcao.CRIAR, PermissaoAcao.EDITAR);
    }

    private static Set<PermissaoAcao> visEditar() {
        return EnumSet.of(PermissaoAcao.VISUALIZAR, PermissaoAcao.EDITAR);
    }

    private static Set<PermissaoAcao> gerenciar() {
        return EnumSet.of(
                PermissaoAcao.VISUALIZAR,
                PermissaoAcao.CRIAR,
                PermissaoAcao.EDITAR,
                PermissaoAcao.EXCLUIR,
                PermissaoAcao.GERENCIAR);
    }

    private static Set<PermissaoAcao> todas() {
        return EnumSet.allOf(PermissaoAcao.class);
    }
}
