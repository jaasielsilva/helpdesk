package com.jaasielsilva.helpdesk.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jaasielsilva.helpdesk.enums.TipoComentario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "chamado_comentarios")
public class ChamadoComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComentario tipo = TipoComentario.COMENTARIO;

    @Column(nullable = false)
    private boolean interno = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
