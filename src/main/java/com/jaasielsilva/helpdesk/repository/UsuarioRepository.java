package com.jaasielsilva.helpdesk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.helpdesk.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.usuario) = LOWER(:usuario)
              AND u.deletedAt IS NULL
            """)
    Optional<Usuario> findByUsuarioIgnoreCase(@Param("usuario") String usuario);

    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN u.empresa
            WHERE LOWER(u.usuario) = LOWER(:usuario)
              AND LOWER(u.empresa.slug) = LOWER(:empresaSlug)
              AND u.deletedAt IS NULL
            """)
    Optional<Usuario> findByUsuarioIgnoreCaseAndEmpresaSlug(
            @Param("usuario") String usuario,
            @Param("empresaSlug") String empresaSlug);

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.usuario) = LOWER(:usuario)
              AND u.perfil = com.jaasielsilva.helpdesk.enums.PerfilUsuario.SUPER_ADMIN
              AND u.deletedAt IS NULL
            """)
    Optional<Usuario> findSuperAdminByUsuarioIgnoreCase(@Param("usuario") String usuario);

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.usuario) = LOWER(:usuario)
              AND u.empresa IS NOT NULL
              AND u.deletedAt IS NULL
            """)
    List<Usuario> findAllTenantUsersByUsuarioIgnoreCase(@Param("usuario") String usuario);

    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.empresa
            WHERE u.id = :id
              AND u.deletedAt IS NULL
            """)
    Optional<Usuario> findByIdWithEmpresa(@Param("id") Long id);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM Usuario u
            WHERE LOWER(u.usuario) = LOWER(:usuario)
              AND u.empresa.id = :empresaId
              AND u.deletedAt IS NULL
            """)
    boolean existsByUsuarioIgnoreCaseAndEmpresaId(
            @Param("usuario") String usuario,
            @Param("empresaId") Long empresaId);

    @Query(
            value = """
            SELECT u FROM Usuario u
            LEFT JOIN u.empresa
            WHERE u.deletedAt IS NULL
              AND u.perfil <> com.jaasielsilva.helpdesk.enums.PerfilUsuario.SUPER_ADMIN
              AND (:empresaId IS NULL OR u.empresa.id = :empresaId)
            """,
            countQuery = """
            SELECT COUNT(u) FROM Usuario u
            WHERE u.deletedAt IS NULL
              AND u.perfil <> com.jaasielsilva.helpdesk.enums.PerfilUsuario.SUPER_ADMIN
              AND (:empresaId IS NULL OR u.empresa.id = :empresaId)
            """)
    org.springframework.data.domain.Page<Usuario> findTenantUsers(
            @Param("empresaId") Long empresaId,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.empresa
            WHERE u.id = :id
              AND u.deletedAt IS NULL
              AND u.perfil <> com.jaasielsilva.helpdesk.enums.PerfilUsuario.SUPER_ADMIN
            """)
    Optional<Usuario> findTenantUserById(@Param("id") Long id);
}
