package com.jaasielsilva.helpdesk.dto.comentario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioCreateRequest(
    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(min = 1, max = 2000, message = "Conteúdo deve ter entre 1 e 2000 caracteres")
    String conteudo,

    boolean interno
) {}
