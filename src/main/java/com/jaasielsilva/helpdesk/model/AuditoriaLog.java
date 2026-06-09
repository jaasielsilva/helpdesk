package com.jaasielsilva.helpdesk.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(
    name = "auditoria_logs",
    indexes = {
        @Index(name = "idx_audit_usuario",    columnList = "usuario_id"),
        @Index(name = "idx_audit_empresa",    columnList = "empresa_id"),
        @Index(name = "idx_audit_modulo",     columnList = "modulo"),
        @Index(name = "idx_audit_data_hora",  columnList = "data_hora")
    }
)
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuário que realizou a ação (referência para histórico mesmo que o usuário seja excluído) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Nome capturado no momento da ação (snapshot imutável) */
    @Column(nullable = false, length = 100)
    private String usuarioNome;

    /** Empresa do usuário no momento da ação */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModuloSistema modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissaoAcao acao;

    @Column(nullable = false, length = 500)
    private String descricao;

    @CreationTimestamp
    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    public AuditoriaLog(
            Usuario usuario,
            String usuarioNome,
            Empresa empresa,
            ModuloSistema modulo,
            PermissaoAcao acao,
            String descricao) {
        this.usuario = usuario;
        this.usuarioNome = usuarioNome;
        this.empresa = empresa;
        this.modulo = modulo;
        this.acao = acao;
        this.descricao = descricao;
    }
}
