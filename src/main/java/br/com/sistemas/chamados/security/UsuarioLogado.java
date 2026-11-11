package br.com.sistemas.chamados.security;

import br.com.sistemas.chamados.enums.Perfil;

/**
 * Dados do usuário autenticado na requisição atual, extraídos do token JWT.
 * Exatamente um entre {@code tecnicoId} e {@code clienteId} é preenchido.
 */
public record UsuarioLogado(
    Long usuarioId,
    String email,
    Perfil perfil,
    Long tecnicoId,
    Long clienteId
) {
    public boolean isAdmin() { return perfil == Perfil.ADMIN; }
    public boolean isTecnico() { return perfil == Perfil.TECNICO; }
    public boolean isCliente() { return perfil == Perfil.CLIENTE; }

    /** ADMIN ou TECNICO. */
    public boolean isEquipe() { return isAdmin() || isTecnico(); }

    public boolean possuiPerfil(Perfil... perfis) {
        for (Perfil p : perfis) {
            if (p == perfil) {
                return true;
            }
        }
        return false;
    }
}
