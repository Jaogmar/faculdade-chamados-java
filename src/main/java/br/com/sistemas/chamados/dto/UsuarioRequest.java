package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cadastro/edição de usuário pela API /usuarios. */
public record UsuarioRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres")
    String email,

    /** Obrigatória no cadastro; na atualização, vazia mantém a senha atual. */
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
    String senha,

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    String telefone,

    /** Obrigatório no cadastro; na atualização, vazio mantém o perfil atual (só ADMIN altera). */
    Perfil perfil
) { }
