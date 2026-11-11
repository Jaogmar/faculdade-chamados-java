package br.com.sistemas.chamados.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.LoginRequest;
import br.com.sistemas.chamados.dto.TokenResponse;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.exception.CredenciaisInvalidasException;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import br.com.sistemas.chamados.repository.UsuarioRepository;
import br.com.sistemas.chamados.security.TokenService;
import br.com.sistemas.chamados.security.UsuarioLogado;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       TecnicoRepository tecnicoRepository,
                       ClienteRepository clienteRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
            .filter(Usuario::isAtivo)
            .filter(u -> passwordEncoder.matches(dto.senha(), u.getSenha()))
            .orElseThrow(CredenciaisInvalidasException::new);

        Long tecnicoId = tecnicoRepository.findByUsuarioId(usuario.getId())
            .map(Tecnico::getId).orElse(null);
        Long clienteId = clienteRepository.findByUsuarioId(usuario.getId())
            .map(Cliente::getId).orElse(null);
        if (tecnicoId == null && clienteId == null) {
            throw new CredenciaisInvalidasException();
        }

        return tokenService.gerar(new UsuarioLogado(
            usuario.getId(), usuario.getEmail(), usuario.getPerfil(), tecnicoId, clienteId));
    }
}
