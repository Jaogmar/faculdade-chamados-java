package br.com.sistemas.chamados.security;

import br.com.sistemas.chamados.enums.Perfil;

/** Dados do usuário autenticado na requisição atual, extraídos do token JWT. */
public record UsuarioLogado(
    Long id,
    String nome,
    String email,
    Perfil perfil
) {
    public boolean isAdmin() { return perfil == Perfil.ADMIN; }
    public boolean isTecnico() { return perfil == Perfil.TECNICO; }
    public boolean isCliente() { return perfil == Perfil.CLIENTE; }

    /** TECNICO ou ADMIN. */
    public boolean isEquipe() { return perfil.isEquipe(); }

    public boolean possuiPerfil(Perfil... perfis) {
        for (Perfil p : perfis) {
            if (p == perfil) {
                return true;
            }
        }
        return false;
    }
}
