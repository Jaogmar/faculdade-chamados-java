package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    String telefone,
    Perfil perfil,
    boolean ativo
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
            u.getTelefone(), u.getPerfil(), u.isAtivo());
    }
}
