package com.jaasielsilva.helpdesk.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jaasielsilva.helpdesk.enums.ModuloSistema;
import com.jaasielsilva.helpdesk.enums.PermissaoAcao;

/**
 * Marca um método de controller para ser auditado automaticamente pelo {@link AuditoriaAspect}.
 * O registro é gravado APÓS a execução bem-sucedida do método (sem exceção).
 *
 * <pre>
 *   &#64;Auditavel(modulo = ModuloSistema.CHAMADOS, acao = PermissaoAcao.CRIAR, descricao = "Chamado criado")
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditavel {
    ModuloSistema modulo();
    PermissaoAcao acao();
    /** Descrição estática. Se vazio, será gerada automaticamente. */
    String descricao() default "";
}
