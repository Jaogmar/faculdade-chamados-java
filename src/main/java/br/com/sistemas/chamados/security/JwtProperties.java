package br.com.sistemas.chamados.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param segredo          chave HMAC (HS256) com pelo menos 32 caracteres
 * @param expiracaoMinutos validade do token em minutos
 */
@ConfigurationProperties("seguranca.jwt")
public record JwtProperties(String segredo, long expiracaoMinutos) { }
