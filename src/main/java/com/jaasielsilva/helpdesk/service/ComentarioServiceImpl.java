package com.jaasielsilva.helpdesk.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.helpdesk.dto.comentario.ComentarioCreateRequest;
import com.jaasielsilva.helpdesk.dto.comentario.ComentarioResponse;
import com.jaasielsilva.helpdesk.enums.TipoComentario;
import com.jaasielsilva.helpdesk.model.Chamado;
import com.jaasielsilva.helpdesk.model.ChamadoComentario;
import com.jaasielsilva.helpdesk.repository.ChamadoRepository;
import com.jaasielsilva.helpdesk.repository.ComentarioRepository;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;
import com.jaasielsilva.helpdesk.tenant.TenantAccessService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ComentarioServiceImpl implements ComentarioService {

    private static final Logger log = LoggerFactory.getLogger(ComentarioServiceImpl.class);

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final TenantAccessService tenantAccessService;

    public ComentarioServiceImpl(
            ComentarioRepository comentarioRepository,
            ChamadoRepository chamadoRepository,
            TenantAccessService tenantAccessService) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    public ComentarioResponse adicionar(Long chamadoId, ComentarioCreateRequest request, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        Chamado chamado = buscarChamado(chamadoId, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);

        // Nota interna só para staff
        boolean isTenantStaff = autenticado.isSuperAdmin() || autenticado.getPerfil().isTenantStaff();
        if (request.interno() && !isTenantStaff) {
            throw new AccessDeniedException("Apenas agentes podem criar notas internas");
        }

        ChamadoComentario comentario = new ChamadoComentario();
        comentario.setChamado(chamado);
        comentario.setAutor(autenticado.getUsuario());
        comentario.setConteudo(request.conteudo());
        comentario.setTipo(TipoComentario.COMENTARIO);
        comentario.setInterno(isTenantStaff && request.interno());

        ChamadoComentario salvo = comentarioRepository.save(comentario);
        log.info("Comentário adicionado ao chamado ID={} por '{}'", chamadoId, auth.getName());
        return converterParaResponse(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar(Long chamadoId, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        Chamado chamado = buscarChamado(chamadoId, autenticado);
        tenantAccessService.validateChamadoAccess(chamado, autenticado);

        boolean isTenantStaff = autenticado.isSuperAdmin() || autenticado.getPerfil().isTenantStaff();
        return comentarioRepository
                .findVisiveisByChamadoId(chamadoId, isTenantStaff)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Override
    public void adicionarEvento(Long chamadoId, String mensagem, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: " + chamadoId));

        ChamadoComentario evento = new ChamadoComentario();
        evento.setChamado(chamado);
        evento.setAutor(autenticado.getUsuario());
        evento.setConteudo(mensagem);
        evento.setTipo(TipoComentario.EVENTO_SISTEMA);
        evento.setInterno(false);

        comentarioRepository.save(evento);
    }

    private Chamado buscarChamado(Long chamadoId, UsuarioAutenticado autenticado) {
        if (autenticado.isSuperAdmin()) {
            return chamadoRepository.findByIdActive(chamadoId)
                    .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: " + chamadoId));
        }
        Long empresaId = tenantAccessService.requireEmpresaId();
        return chamadoRepository.findByIdActiveAndEmpresaId(chamadoId, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: " + chamadoId));
    }

    private ComentarioResponse converterParaResponse(ChamadoComentario c) {
        return new ComentarioResponse(
                c.getId(),
                c.getChamado().getId(),
                c.getAutor() != null ? c.getAutor().getId() : null,
                c.getAutor() != null ? (c.getAutor().getNome() != null ? c.getAutor().getNome() : c.getAutor().getUsuario()) : "Sistema",
                c.getAutor() != null ? c.getAutor().getPerfil().name() : null,
                c.getConteudo(),
                c.getTipo(),
                c.isInterno(),
                c.getDataCriacao()
        );
    }
}
