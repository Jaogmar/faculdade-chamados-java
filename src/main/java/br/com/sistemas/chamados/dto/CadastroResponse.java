package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.enums.TipoUsuario;

/** Conta criada + token para o usuário já sair logado. */
public record CadastroResponse(
    Long id,
    String nome,
    String email,
    TipoUsuario tipo,
    TokenResponse autenticacao
) { }
