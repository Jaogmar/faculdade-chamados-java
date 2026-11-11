package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotNull(message = "Perfil é obrigatório")
    Perfil perfil,

    /** Obrigatória no cadastro; na atualização, vazia mantém a senha atual. */
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
    String senha
) { }
