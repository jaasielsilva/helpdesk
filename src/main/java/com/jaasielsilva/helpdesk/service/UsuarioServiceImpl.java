package com.jaasielsilva.helpdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.helpdesk.dto.usuario.CreateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UpdateUsuarioRequest;
import com.jaasielsilva.helpdesk.dto.usuario.UsuarioResponse;
import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.exception.ConflictException;
import com.jaasielsilva.helpdesk.model.Empresa;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.EmpresaRepository;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;
import com.jaasielsilva.helpdesk.tenant.TenantAccessService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final TenantAccessService tenantAccessService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            TenantAccessService tenantAccessService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.tenantAccessService = tenantAccessService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Long empresaId, Pageable pageable, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);

        Long filtroEmpresaId = resolveEmpresaFiltro(empresaId, autenticado);
        return usuarioRepository.findTenantUsers(filtroEmpresaId, pageable).map(UsuarioResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id, Authentication auth) {
        Usuario usuario = findAccessibleUser(id, auth);
        return UsuarioResponse.from(usuario);
    }

    @Override
    public UsuarioResponse criar(CreateUsuarioRequest request, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);

        validatePerfilCriacao(request.perfil(), autenticado);

        Empresa empresa = resolveEmpresaForCreate(request.empresaId(), autenticado);
        String usuarioLogin = request.usuario().trim();

        if (usuarioRepository.existsByUsuarioIgnoreCaseAndEmpresaId(usuarioLogin, empresa.getId())) {
            throw new ConflictException("Usuário já existe nesta empresa");
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(usuarioLogin);
        usuario.setSenha(passwordEncoder.encode(request.senha().trim()));
        usuario.setNome(request.nome() != null && !request.nome().isBlank()
                ? request.nome().trim()
                : usuarioLogin);
        usuario.setPerfil(request.perfil());
        usuario.setEmpresa(empresa);
        usuario.setAtivo(true);

        log.info("Usuário '{}' criado na empresa '{}'", usuarioLogin, empresa.getSlug());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponse atualizar(Long id, UpdateUsuarioRequest request, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        Usuario usuario = findAccessibleUser(id, auth);

        if (request.perfil() != null) {
            validatePerfilEdicao(request.perfil(), autenticado, usuario);
            usuario.setPerfil(request.perfil());
        }
        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome().trim());
        }
        if (request.ativo() != null) {
            usuario.setAtivo(request.ativo());
        }
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha().trim()));
        }

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    public void desativar(Long id, Authentication auth) {
        Usuario usuario = findAccessibleUser(id, auth);
        usuario.setAtivo(false);
        usuario.setDeletedAt(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        log.info("Usuário ID={} desativado", id);
    }

    private Long resolveEmpresaFiltro(Long empresaId, UsuarioAutenticado autenticado) {
        if (autenticado.isSuperAdmin()) {
            return empresaId;
        }
        return tenantAccessService.requireEmpresaId();
    }

    private Empresa resolveEmpresaForCreate(Long empresaId, UsuarioAutenticado autenticado) {
        if (autenticado.isSuperAdmin()) {
            if (empresaId == null) {
                throw new IllegalStateException("empresaId é obrigatório para SUPER_ADMIN");
            }
            return empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada com ID: " + empresaId));
        }

        Long tenantEmpresaId = tenantAccessService.requireEmpresaId();
        return empresaRepository.findById(tenantEmpresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
    }

    private Usuario findAccessibleUser(Long id, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        Usuario usuario = usuarioRepository.findTenantUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));

        if (!autenticado.isSuperAdmin()) {
            tenantAccessService.validateSameEmpresa(usuario);
        }

        return usuario;
    }

    private void validatePerfilCriacao(PerfilUsuario perfil, UsuarioAutenticado autenticado) {
        if (perfil == PerfilUsuario.SUPER_ADMIN) {
            throw new AccessDeniedException("Não é permitido criar SUPER_ADMIN");
        }
        if (!autenticado.isSuperAdmin() && perfil == PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("ADMIN do tenant não pode criar outro ADMIN");
        }
        if (!autenticado.isSuperAdmin()
                && perfil != PerfilUsuario.SUPORTE
                && perfil != PerfilUsuario.USER) {
            throw new AccessDeniedException("Perfil não permitido");
        }
    }

    private void validatePerfilEdicao(PerfilUsuario perfil, UsuarioAutenticado autenticado, Usuario alvo) {
        if (perfil == PerfilUsuario.SUPER_ADMIN) {
            throw new AccessDeniedException("Não é permitido atribuir SUPER_ADMIN");
        }
        if (!autenticado.isSuperAdmin() && perfil == PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("ADMIN do tenant não pode promover para ADMIN");
        }
        if (!autenticado.isSuperAdmin()
                && alvo.getPerfil() == PerfilUsuario.ADMIN
                && perfil != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Não é permitido alterar perfil de outro ADMIN");
        }
    }
}
