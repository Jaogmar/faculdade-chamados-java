package br.com.sistemas.chamados.dto;

import java.time.Instant;

import br.com.sistemas.chamados.enums.Perfil;

public record TokenResponse(String token, String tipo, Instant expiraEm, Perfil perfil) { }
