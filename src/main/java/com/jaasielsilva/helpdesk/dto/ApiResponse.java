package com.jaasielsilva.helpdesk.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean sucesso,
    String mensagem,
    T dados,
    LocalDateTime timestamp
) {
    public ApiResponse(boolean sucesso, String mensagem, T dados) {
        this(sucesso, mensagem, dados, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> sucesso(String mensagem, T dados) {
        return new ApiResponse<>(true, mensagem, dados);
    }

    public static <T> ApiResponse<T> sucesso(String mensagem) {
        return new ApiResponse<>(true, mensagem, null);
    }

    public static <T> ApiResponse<T> erro(String mensagem) {
        return new ApiResponse<>(false, mensagem, null);
    }
}
