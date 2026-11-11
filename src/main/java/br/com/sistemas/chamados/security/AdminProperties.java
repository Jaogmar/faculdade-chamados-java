package br.com.sistemas.chamados.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credenciais do administrador criado automaticamente na subida da aplicação. */
@ConfigurationProperties("seguranca.admin")
public record AdminProperties(String nome, String email, String senha) { }
