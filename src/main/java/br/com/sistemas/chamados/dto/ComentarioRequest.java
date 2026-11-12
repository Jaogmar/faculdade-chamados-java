package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
    @NotBlank(message = "Texto é obrigatório")
    @Size(max = 2000, message = "Texto deve ter no máximo 2000 caracteres")
    String texto
) { }
