package com.jaasielsilva.helpdesk.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.helpdesk.dto.chamado.ChamadoCreateRequest;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoResponse;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoUpdateRequest;
import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.enums.StatusChamado;
import com.jaasielsilva.helpdesk.model.Chamado;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.ChamadoRepository;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;
import com.jaasielsilva.helpdesk.tenant.TenantAccessService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ChamadoServiceImpl implements ChamadoService {

    private static final Logger log = LoggerFactory.getLogger(ChamadoServiceImpl.class);

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TenantAccessService tenantAccessService;
    private final PermissaoService permissaoService;
    private final ComentarioService comentarioService;

    public ChamadoServiceImpl(
            ChamadoRepository chamadoRepository,
            UsuarioRepository usuarioRepository,
            TenantAccessService tenantAccessService,
            PermissaoService permissaoService,
            ComentarioService comentarioService) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tenantAccessService = tenantAccessService;
        this.permissaoService = permissaoService;
        this.comentarioService = comentarioService;
    }

    @Override
    @CacheEvict(value = "chamados", allEntries = true)
    public ChamadoResponse criar(ChamadoCreateRequest request, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.CHAMADOS, PermissaoAcao.CRIAR);
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.info("Criando chamado para usuário ID={} por '{}'", request.usuarioId(), auth.getName());

        if (!autenticado.isSuperAdmin()) {
            if (!autenticado.getUsuario().getId().equals(request.usuarioId())) {
                throw new AccessDeniedException("Você só pode criar chamados em seu próprio nome");
            }
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + request.usuarioId()));

        tenantAccessService.validateSameEmpresa(usuario);

        if (usuario.getEmpresa() == null) {
            throw new AccessDeniedException("Chamados exigem usuário vinculado a uma empresa");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setUsuario(usuario);
        chamado.setEmpresa(usuario.getEmpresa());
        chamado.setStatus(StatusChamado.ABERTO);

        Chamado salvo = chamadoRepository.save(chamado);
        log.info("Chamado ID={} criado com sucesso por '{}'", salvo.getId(), auth.getName());
        return converterParaResponse(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chamados", key = "#auth.name + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ChamadoResponse> listar(Pageable pageable, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.debug("Listando chamados para '{}' (perfil={})", auth.getName(), autenticado.getPerfil());

        if (autenticado.isSuperAdmin()) {
            return chamadoRepository.findAllActive(pageable).map(this::converterParaResponse);
        }

        Long empresaId = tenantAccessService.requireEmpresaId();

        if (autenticado.getPerfil().isTenantStaff()) {
            return chamadoRepository.findAllActiveByEmpresa(empresaId, pageable).map(this::converterParaResponse);
        }

        return chamadoRepository.findAllActiveByEmpresaAndUsuario(empresaId, autenticado.getUsuarioId(), pageable)
                .map(this::converterParaResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chamado", key = "#id")
    public ChamadoResponse buscarPorId(Long id, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.debug("Buscando chamado ID={} por '{}'", id, auth.getName());
        Chamado chamado = buscarChamadoAtivo(id, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);
        return converterParaResponse(chamado);
    }

    @Override
    @CacheEvict(value = {"chamados", "chamado"}, allEntries = true)
    public ChamadoResponse atualizar(Long id, ChamadoUpdateRequest request, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);

        if (!tenantAccessService.canManageChamados(autenticado)) {
            throw new AccessDeniedException("Você não tem permissão para atualizar chamados");
        }

        log.info("Atualizando chamado ID={} por '{}'", id, auth.getName());

        Chamado chamado = buscarChamadoAtivo(id, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);

        if (request.titulo() != null && !request.titulo().isEmpty()) {
            chamado.setTitulo(request.titulo());
        }
        if (request.descricao() != null && !request.descricao().isEmpty()) {
            chamado.setDescricao(request.descricao());
        }
        if (request.status() != null && chamado.getStatus() != request.status()) {
            StatusChamado novoStatus = request.status();
            if (novoStatus == StatusChamado.FECHADO && chamado.getDataFechamento() == null) {
                chamado.setDataFechamento(LocalDateTime.now());
            }
            String evento = gerarMensagemEvento(chamado.getStatus(), novoStatus);
            chamado.setStatus(novoStatus);
            Chamado salvo = chamadoRepository.save(chamado);
            comentarioService.adicionarEvento(salvo.getId(), evento, auth);
            log.info("Chamado ID={} status alterado para {} por '{}'", id, novoStatus, auth.getName());
            return converterParaResponse(salvo);
        }
        if (request.usuarioAtribuidoId() != null) {
            Usuario atribuido = usuarioRepository.findById(request.usuarioAtribuidoId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + request.usuarioAtribuidoId()));
            tenantAccessService.validateSameEmpresa(atribuido);
            chamado.setUsuarioAtribuido(atribuido);
            Chamado salvo = chamadoRepository.save(chamado);
            String nomeAtribuido = atribuido.getNome() != null ? atribuido.getNome() : atribuido.getUsuario();
            comentarioService.adicionarEvento(salvo.getId(), "👤 Chamado atribuído a " + nomeAtribuido, auth);
            log.info("Chamado ID={} atribuído a '{}' por '{}'", id, nomeAtribuido, auth.getName());
            return converterParaResponse(salvo);
        }

        Chamado salvo = chamadoRepository.save(chamado);
        log.info("Chamado ID={} atualizado por '{}'", id, auth.getName());
        return converterParaResponse(salvo);
    }

    @Override
    @CacheEvict(value = {"chamados", "chamado"}, allEntries = true)
    public void deletar(Long id, Authentication auth) {
        permissaoService.require(auth, ModuloSistema.CHAMADOS, PermissaoAcao.EXCLUIR);
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.info("Deletando chamado ID={} por '{}'", id, auth.getName());
        Chamado chamado = buscarChamadoAtivo(id, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);
        chamado.setDeletedAt(LocalDateTime.now());
        chamadoRepository.save(chamado);
        log.info("Chamado ID={} deletado (soft delete) por '{}'", id, auth.getName());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private static String gerarMensagemEvento(StatusChamado de, StatusChamado para) {
        return switch (para) {
            case EM_ATENDIMENTO -> "🔧 Atendimento iniciado";
            case RESOLVIDO      -> "✅ Chamado marcado como resolvido";
            case FECHADO        -> "🔒 Chamado encerrado";
            case ABERTO         -> "🔄 Chamado reaberto";
        };
    }

    private Chamado buscarChamadoAtivo(Long id, UsuarioAutenticado autenticado) {
        if (autenticado.isSuperAdmin()) {
            return chamadoRepository.findByIdActive(id)
                    .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: " + id));
        }
        Long empresaId = tenantAccessService.requireEmpresaId();
        return chamadoRepository.findByIdActiveAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: " + id));
    }

    private ChamadoResponse converterParaResponse(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getStatus(),
                chamado.getUsuario().getId(),
                chamado.getUsuario().getNome(),
                chamado.getUsuarioAtribuido() != null ? chamado.getUsuarioAtribuido().getId() : null,
                chamado.getUsuarioAtribuido() != null ? chamado.getUsuarioAtribuido().getNome() : null,
                chamado.getDataCriacao(),
                chamado.getDataAtualizacao(),
                chamado.getDataFechamento()
        );
    }
}
