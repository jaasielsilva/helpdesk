package com.jaasielsilva.helpdesk.exception;

public class LoginRequiresEmpresaSlugException extends RuntimeException {

    public LoginRequiresEmpresaSlugException() {
        super("Informe o identificador da empresa para continuar");
    }
}
