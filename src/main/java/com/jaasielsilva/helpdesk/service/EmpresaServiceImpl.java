package com.jaasielsilva.helpdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.helpdesk.dto.empresa.CreateEmpresaRequest;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaCreateResponse;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaResponse;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.exception.ConflictException;
import com.jaasielsilva.helpdesk.model.Empresa;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.EmpresaRepository;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmpresaServiceImpl implements EmpresaService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaServiceImpl.class);

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoService permissaoService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public EmpresaServiceImpl(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PermissaoService permissaoService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.permissaoService = permissaoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmpresaCreateResponse criar(CreateEmpresaRequest request, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.EMPRESAS, PermissaoAcao.CRIAR);
        UsuarioDetailsService.requireUsuarioAutenticado(auth);

        String slug = request.slug().trim().toLowerCase();
        String nome = request.nome().trim();
        String adminUsuario = request.adminUsuario().trim();
        String adminSenha = request.adminSenha().trim();

        if (empresaRepository.existsBySlugIgnoreCase(slug)) {
            throw new ConflictException("Já existe uma empresa com o slug '" + slug + "'");
        }

        Empresa empresa = new Empresa();
        empresa.setSlug(slug);
        empresa.setNome(nome);
        empresa.setAtivo(true);
        empresa = empresaRepository.save(empresa);

        if (usuarioRepository.existsByUsuarioIgnoreCaseAndEmpresaId(adminUsuario, empresa.getId())) {
            throw new ConflictException("Usuário admin já existe nesta empresa");
        }

        Usuario admin = new Usuario();
        admin.setUsuario(adminUsuario);
        admin.setSenha(passwordEncoder.encode(adminSenha));
        admin.setNome(request.adminNome() != null && !request.adminNome().isBlank()
                ? request.adminNome().trim()
                : "Administrador");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setEmpresa(empresa);
        admin.setAtivo(true);
        usuarioRepository.save(admin);

        log.info("Tenant '{}' criado com admin '{}'", slug, adminUsuario);
        return new EmpresaCreateResponse(
                EmpresaResponse.from(empresa),
                admin.getUsuario(),
                admin.getPerfil().name());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.EMPRESAS, PermissaoAcao.VISUALIZAR);
        return empresaRepository.findAll(pageable).map(EmpresaResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(Long id, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.EMPRESAS, PermissaoAcao.VISUALIZAR);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada com ID: " + id));
        return EmpresaResponse.from(empresa);
    }

    @Override
    public EmpresaResponse atualizarStatus(Long id, boolean ativo, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.EMPRESAS, PermissaoAcao.EDITAR);
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada com ID: " + id));
        empresa.setAtivo(ativo);
        log.info("Empresa '{}' {}ativada", empresa.getSlug(), ativo ? "" : "des");
        return EmpresaResponse.from(empresaRepository.save(empresa));
    }
}
