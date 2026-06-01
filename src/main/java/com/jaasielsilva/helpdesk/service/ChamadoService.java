package com.jaasielsilva.helpdesk.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.chamado.ChamadoCreateRequest;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoResponse;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoStats;
import com.jaasielsilva.helpdesk.dto.chamado.ChamadoUpdateRequest;
import com.jaasielsilva.helpdesk.enums.StatusChamado;

public interface ChamadoService {

    ChamadoResponse criar(ChamadoCreateRequest request, Authentication auth);

    Page<ChamadoResponse> listar(Pageable pageable, StatusChamado status, String busca, Authentication auth);

    ChamadoResponse buscarPorId(Long id, Authentication auth);

    ChamadoResponse atualizar(Long id, ChamadoUpdateRequest request, Authentication auth);

    void deletar(Long id, Authentication auth);

    ChamadoStats estatisticas(Authentication auth);
}
