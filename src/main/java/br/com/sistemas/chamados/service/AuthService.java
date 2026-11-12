package br.com.sistemas.chamados.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.LoginRequest;
import br.com.sistemas.chamados.dto.TokenResponse;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.exception.CredenciaisInvalidasException;
import br.com.sistemas.chamados.repository.UsuarioRepository;
import br.com.sistemas.chamados.security.TokenService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
            .filter(Usuario::isAtivo)
            .filter(u -> passwordEncoder.matches(dto.senha(), u.getSenha()))
            .orElseThrow(CredenciaisInvalidasException::new);
        return tokenService.gerar(usuario);
    }
}
