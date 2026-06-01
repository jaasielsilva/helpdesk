package com.jaasielsilva.helpdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.helpdesk.model.Chamado;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    @Query("SELECT c FROM Chamado c WHERE c.deletedAt IS NULL")
    Page<Chamado> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Chamado c WHERE c.empresa.id = :empresaId AND c.deletedAt IS NULL")
    Page<Chamado> findAllActiveByEmpresa(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("""
            SELECT c FROM Chamado c
            WHERE c.empresa.id = :empresaId
              AND c.usuario.id = :usuarioId
              AND c.deletedAt IS NULL
            """)
    Page<Chamado> findAllActiveByEmpresaAndUsuario(
            @Param("empresaId") Long empresaId,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query("SELECT c FROM Chamado c WHERE c.id = :id AND c.deletedAt IS NULL")
    java.util.Optional<Chamado> findByIdActive(@Param("id") Long id);

    @Query("SELECT c FROM Chamado c WHERE c.id = :id AND c.empresa.id = :empresaId AND c.deletedAt IS NULL")
    java.util.Optional<Chamado> findByIdActiveAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
