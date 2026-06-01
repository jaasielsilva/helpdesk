package com.jaasielsilva.helpdesk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.helpdesk.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);
}
