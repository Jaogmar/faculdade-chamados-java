package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;

public record ComentarioResponse(
    Long id,
    String texto,
    LocalDateTime dataCriacao,
    Long autorId,
    String autorNome,
    Perfil autorPerfil
) {
    public static ComentarioResponse de(Comentario c) {
        Usuario autor = c.getAutor();
        return new ComentarioResponse(c.getId(), c.getTexto(), c.getDataCriacao(),
            autor.getId(), autor.getNome(), autor.getPerfil());
    }
}
