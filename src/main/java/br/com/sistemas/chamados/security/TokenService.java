package br.com.sistemas.chamados.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.sistemas.chamados.dto.TokenResponse;

@Service
public class TokenService {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_TECNICO_ID = "tecnicoId";
    public static final String CLAIM_CLIENTE_ID = "clienteId";

    private static final String EMISSOR = "chamados";

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;

    public TokenService(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    public TokenResponse gerar(UsuarioLogado usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.expiracaoMinutos(), ChronoUnit.MINUTES);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
            .issuer(EMISSOR)
            .issuedAt(agora)
            .expiresAt(expiraEm)
            .subject(usuario.usuarioId().toString())
            .claim(CLAIM_EMAIL, usuario.email())
            .claim(CLAIM_PERFIL, usuario.perfil().name());
        if (usuario.tecnicoId() != null) {
            claims.claim(CLAIM_TECNICO_ID, usuario.tecnicoId());
        }
        if (usuario.clienteId() != null) {
            claims.claim(CLAIM_CLIENTE_ID, usuario.clienteId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEm, usuario.perfil());
    }
}
