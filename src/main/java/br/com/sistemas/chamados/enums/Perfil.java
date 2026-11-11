package br.com.sistemas.chamados.enums;

/**
 * Perfil do usuário. CLIENTE e TECNICO podem ser escolhidos no autocadastro;
 * ADMIN só é criado pelo sistema ou por outro administrador.
 */
public enum Perfil {
    CLIENTE,
    TECNICO,
    ADMIN;

    public boolean isAutocadastravel() {
        return this != ADMIN;
    }

    /** TECNICO ou ADMIN: podem atender chamados. */
    public boolean isEquipe() {
        return this == TECNICO || this == ADMIN;
    }
}
