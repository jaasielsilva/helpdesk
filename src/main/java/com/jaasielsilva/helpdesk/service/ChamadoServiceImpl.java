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
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoStats;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoUpdateRequest;
import com.jaasielsilva.helpdesk.enums.PrioridadeChamado;
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

    public ChamadoServiceImpl(
            ChamadoRepository chamadoRepository,
            UsuarioRepository usuarioRepository,
            TenantAccessService tenantAccessService) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    @CacheEvict(value = "chamados", allEntries = true)
    public ChamadoResponse criar(ChamadoCreateRequest request, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.info("Criando chamado para usuário ID={} por '{}'", request.usuarioId(), auth.getName());

        Usuario solicitante = autenticado.getUsuario();

        if (!autenticado.isSuperAdmin()) {
            if (!solicitante.getId().equals(request.usuarioId())) {
                log.warn("Usuário '{}' tentou criar chamado em nome do usuário ID={}", auth.getName(), request.usuarioId());
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
        chamado.setPrioridade(request.prioridade() != null ? request.prioridade() : PrioridadeChamado.MEDIA);

        Chamado salvo = chamadoRepository.save(chamado);
        log.info("Chamado ID={} criado com sucesso por '{}'", salvo.getId(), auth.getName());
        return ChamadoResponse.from(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chamados", key = "#auth.name.concat('_').concat(#pageable.pageNumber).concat('_').concat(#pageable.pageSize).concat('_').concat(#status).concat('_').concat(#busca)")
    public Page<ChamadoResponse> listar(Pageable pageable, StatusChamado status, String busca, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.debug("Listando chamados para '{}' (perfil={}) com filtros status={}, busca={}", auth.getName(), autenticado.getPerfil(), status, busca);

        String buscaParam = (busca != null && !busca.isBlank()) ? busca.trim() : null;

        if (autenticado.isSuperAdmin()) {
            return chamadoRepository.findAllActiveFiltered(status, pageable).map(ChamadoResponse::from);
        }

        Long empresaId = tenantAccessService.requireEmpresaId();

        if (autenticado.getPerfil().isTenantStaff()) {
            return chamadoRepository.findByEmpresaFiltered(empresaId, status, buscaParam, pageable).map(ChamadoResponse::from);
        }

        return chamadoRepository.findByEmpresaAndUsuarioFiltered(empresaId, autenticado.getUsuarioId(), status, buscaParam, pageable)
                .map(ChamadoResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chamado", key = "#id")
    public ChamadoResponse buscarPorId(Long id, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.debug("Buscando chamado ID={} por '{}'", id, auth.getName());

        Chamado chamado = buscarChamadoAtivo(id, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);
        return ChamadoResponse.from(chamado);
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
        if (request.status() != null) {
            if (request.status() == StatusChamado.FECHADO && chamado.getDataFechamento() == null) {
                chamado.setDataFechamento(LocalDateTime.now());
            }
            chamado.setStatus(request.status());
        }
        if (request.prioridade() != null) {
            chamado.setPrioridade(request.prioridade());
        }
        if (request.usuarioAtribuidoId() != null) {
            Usuario usuarioAtribuido = usuarioRepository.findById(request.usuarioAtribuidoId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + request.usuarioAtribuidoId()));
            tenantAccessService.validateSameEmpresa(usuarioAtribuido);
            chamado.setUsuarioAtribuido(usuarioAtribuido);
        }

        Chamado atualizado = chamadoRepository.save(chamado);
        log.info("Chamado ID={} atualizado com sucesso por '{}'", id, auth.getName());
        return ChamadoResponse.from(atualizado);
    }

    @Override
    @CacheEvict(value = {"chamados", "chamado"}, allEntries = true)
    public void deletar(Long id, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);

        log.info("Deletando chamado ID={} por '{}'", id, auth.getName());

        Chamado chamado = buscarChamadoAtivo(id, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);

        chamado.setDeletedAt(LocalDateTime.now());
        chamadoRepository.save(chamado);
        log.info("Chamado ID={} deletado (soft delete) por '{}'", id, auth.getName());
    }

    private Chamado buscarChamadoAtivo(Long id, UsuarioAutenticado autenticado) {
        if (autenticado.isSuperAdmin()) {
            return chamadoRepository.findByIdActive(id)
                    .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado com ID: " + id));
        }

        Long empresaId = tenantAccessService.requireEmpresaId();
        return chamadoRepository.findByIdActiveAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado com ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ChamadoStats estatisticas(Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        log.debug("Gerando estatísticas de chamados para '{}' (perfil={})", auth.getName(), autenticado.getPerfil());

        java.util.List<Object[]> contagens;
        if (autenticado.isSuperAdmin()) {
            contagens = chamadoRepository.countAllByStatus();
        } else {
            Long empresaId = tenantAccessService.requireEmpresaId();
            contagens = chamadoRepository.countByEmpresaAndStatus(empresaId);
        }

        long abertos = 0, emAtendimento = 0, resolvidos = 0, fechados = 0;
        for (Object[] linha : contagens) {
            StatusChamado s = (StatusChamado) linha[0];
            long count = (Long) linha[1];
            switch (s) {
                case ABERTO -> abertos = count;
                case EM_ATENDIMENTO -> emAtendimento = count;
                case RESOLVIDO -> resolvidos = count;
                case FECHADO -> fechados = count;
            }
        }

        return new ChamadoStats(abertos + emAtendimento + resolvidos + fechados, abertos, emAtendimento, resolvidos, fechados);
    }

}
