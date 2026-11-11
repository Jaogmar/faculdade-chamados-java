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
import br.com.sistemas.chamados.entity.Usuario;

@Service
public class TokenService {

    public static final String CLAIM_NOME = "nome";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_PERFIL = "perfil";

    private static final String EMISSOR = "chamados";

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;

    public TokenService(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    public TokenResponse gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.expiracaoMinutos(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(EMISSOR)
            .issuedAt(agora)
            .expiresAt(expiraEm)
            .subject(usuario.getId().toString())
            .claim(CLAIM_NOME, usuario.getNome())
            .claim(CLAIM_EMAIL, usuario.getEmail())
            .claim(CLAIM_PERFIL, usuario.getPerfil().name())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEm, usuario.getPerfil());
    }
}
