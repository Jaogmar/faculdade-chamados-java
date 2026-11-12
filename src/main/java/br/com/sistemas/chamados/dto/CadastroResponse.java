package br.com.sistemas.chamados.dto;

/** Conta criada + token para o usuário já sair logado. */
public record CadastroResponse(UsuarioResponse usuario, TokenResponse autenticacao) { }
