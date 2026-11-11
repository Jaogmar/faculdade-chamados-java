package br.com.sistemas.chamados.enums;

/**
 * O que o usuário declara ser ao criar a própria conta.
 * ADMIN não é opção de autocadastro; só é criado pelo sistema/administrador.
 */
public enum TipoUsuario {
    CLIENTE(Perfil.CLIENTE),
    TECNICO(Perfil.TECNICO);

    private final Perfil perfil;

    TipoUsuario(Perfil perfil) {
        this.perfil = perfil;
    }

    public Perfil getPerfil() {
        return perfil;
    }
}
