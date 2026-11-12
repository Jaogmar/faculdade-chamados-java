package br.com.sistemas.chamados.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.exception.AcessoNegadoException;
import br.com.sistemas.chamados.exception.NaoAutenticadoException;

/**
 * Ponto de acesso ao usuário logado para as services.
 *
 * <pre>{@code
 * private final ContextoUsuario contexto;
 *
 * UsuarioLogado usuario = contexto.atual();
 * if (usuario.isCliente()) {
 *     clienteId = usuario.id();
 * }
 * }</pre>
 *
 * Os dados vêm das claims do JWT, sem consulta ao banco.
 */
@Component
public class ContextoUsuario {

    /** Usuário logado; lança {@link NaoAutenticadoException} se não houver. */
    public UsuarioLogado atual() {
        return atualOpcional()
            .orElseThrow(() -> new NaoAutenticadoException("Usuário não autenticado"));
    }

    /** Usuário logado, ou vazio em contextos sem autenticação (ex.: rotas públicas, jobs). */
    public Optional<UsuarioLogado> atualOpcional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        return Optional.of(new UsuarioLogado(
            Long.valueOf(jwt.getSubject()),
            jwt.getClaimAsString(TokenService.CLAIM_NOME),
            jwt.getClaimAsString(TokenService.CLAIM_EMAIL),
            Perfil.valueOf(jwt.getClaimAsString(TokenService.CLAIM_PERFIL))));
    }

    /** Id do usuário logado. */
    public Long id() {
        return atual().id();
    }

    /** Lança 403 se o usuário logado não tiver nenhum dos perfis informados. */
    public UsuarioLogado exigirPerfil(Perfil... perfis) {
        UsuarioLogado usuario = atual();
        if (!usuario.possuiPerfil(perfis)) {
            throw new AcessoNegadoException(
                "Acesso permitido apenas para: " + Arrays.toString(perfis));
        }
        return usuario;
    }

    /**
     * Permite se o recurso pertence ao próprio usuário logado ou se ele tem um dos perfis
     * informados; caso contrário, lança 403.
     */
    public UsuarioLogado exigirProprioOuPerfil(Long usuarioId, Perfil... perfis) {
        UsuarioLogado usuario = atual();
        if (!usuario.id().equals(usuarioId) && !usuario.possuiPerfil(perfis)) {
            throw new AcessoNegadoException("Acesso negado ao usuário " + usuarioId);
        }
        return usuario;
    }
}
