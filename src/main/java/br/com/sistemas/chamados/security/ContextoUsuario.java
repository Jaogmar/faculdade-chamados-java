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
 *     clienteId = usuario.clienteId();
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
            jwt.getClaimAsString(TokenService.CLAIM_EMAIL),
            Perfil.valueOf(jwt.getClaimAsString(TokenService.CLAIM_PERFIL)),
            claimLong(jwt, TokenService.CLAIM_TECNICO_ID),
            claimLong(jwt, TokenService.CLAIM_CLIENTE_ID)));
    }

    /** Id do cliente logado; lança 403 se o usuário não for um cliente. */
    public Long clienteId() {
        UsuarioLogado usuario = atual();
        if (usuario.clienteId() == null) {
            throw new AcessoNegadoException("Operação disponível apenas para clientes");
        }
        return usuario.clienteId();
    }

    /** Id do técnico logado (ADMIN ou TECNICO); lança 403 se o usuário não for um técnico. */
    public Long tecnicoId() {
        UsuarioLogado usuario = atual();
        if (usuario.tecnicoId() == null) {
            throw new AcessoNegadoException("Operação disponível apenas para técnicos");
        }
        return usuario.tecnicoId();
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

    private static Long claimLong(Jwt jwt, String claim) {
        Object valor = jwt.getClaim(claim);
        if (valor instanceof Number n) {
            return n.longValue();
        }
        return valor != null ? Long.valueOf(valor.toString()) : null;
    }
}
