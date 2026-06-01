package com.jaasielsilva.helpdesk.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.jaasielsilva.helpdesk.dto.empresa.CreateEmpresaRequest;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaCreateResponse;
import com.jaasielsilva.helpdesk.dto.empresa.EmpresaResponse;

public interface EmpresaService {

    EmpresaCreateResponse criar(CreateEmpresaRequest request, Authentication auth);

    Page<EmpresaResponse> listar(Pageable pageable, Authentication auth);

    EmpresaResponse buscarPorId(Long id, Authentication auth);

    EmpresaResponse atualizarStatus(Long id, boolean ativo, Authentication auth);
}
