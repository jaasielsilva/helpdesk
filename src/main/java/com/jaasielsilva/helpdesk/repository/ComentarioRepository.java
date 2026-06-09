package com.jaasielsilva.helpdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.helpdesk.model.ChamadoComentario;

public interface ComentarioRepository extends JpaRepository<ChamadoComentario, Long> {

    @Query("""
            SELECT c FROM ChamadoComentario c
            WHERE c.chamado.id = :chamadoId
            ORDER BY c.dataCriacao ASC
            """)
    List<ChamadoComentario> findByChamadoId(@Param("chamadoId") Long chamadoId);

    @Query("""
            SELECT c FROM ChamadoComentario c
            WHERE c.chamado.id = :chamadoId
              AND (c.interno = false OR :isTenantStaff = true)
            ORDER BY c.dataCriacao ASC
            """)
    List<ChamadoComentario> findVisiveisByChamadoId(
            @Param("chamadoId") Long chamadoId,
            @Param("isTenantStaff") boolean isTenantStaff);
}
