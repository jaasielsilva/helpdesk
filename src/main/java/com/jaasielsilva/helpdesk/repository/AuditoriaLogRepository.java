package com.jaasielsilva.helpdesk.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;
import com.jaasielsilva.helpdesk.model.AuditoriaLog;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    /**
     * Busca paginada para SUPER_ADMIN (sem filtro de empresa) com todos os filtros opcionais.
     */
    @Query("""
            SELECT a FROM AuditoriaLog a
            WHERE (:usuario IS NULL OR LOWER(a.usuarioNome) LIKE LOWER(CONCAT('%', :usuario, '%')))
              AND (:modulo  IS NULL OR a.modulo = :modulo)
              AND (:acao    IS NULL OR a.acao   = :acao)
              AND (:inicio  IS NULL OR a.dataHora >= :inicio)
              AND (:fim     IS NULL OR a.dataHora <= :fim)
            ORDER BY a.dataHora DESC
            """)
    Page<AuditoriaLog> findAllWithFilters(
            @Param("usuario") String usuario,
            @Param("modulo")  ModuloSistema modulo,
            @Param("acao")    PermissaoAcao acao,
            @Param("inicio")  LocalDateTime inicio,
            @Param("fim")     LocalDateTime fim,
            Pageable pageable);

    /**
     * Busca paginada para ADMIN/SUPORTE (filtro de empresa obrigatório) com filtros opcionais.
     */
    @Query("""
            SELECT a FROM AuditoriaLog a
            WHERE a.empresa.id = :empresaId
              AND (:usuario IS NULL OR LOWER(a.usuarioNome) LIKE LOWER(CONCAT('%', :usuario, '%')))
              AND (:modulo  IS NULL OR a.modulo = :modulo)
              AND (:acao    IS NULL OR a.acao   = :acao)
              AND (:inicio  IS NULL OR a.dataHora >= :inicio)
              AND (:fim     IS NULL OR a.dataHora <= :fim)
            ORDER BY a.dataHora DESC
            """)
    Page<AuditoriaLog> findByEmpresaWithFilters(
            @Param("empresaId") Long empresaId,
            @Param("usuario")   String usuario,
            @Param("modulo")    ModuloSistema modulo,
            @Param("acao")      PermissaoAcao acao,
            @Param("inicio")    LocalDateTime inicio,
            @Param("fim")       LocalDateTime fim,
            Pageable pageable);
}
