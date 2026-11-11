package br.com.sistemas.chamados.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistemas.chamados.dto.LoginRequest;
import br.com.sistemas.chamados.dto.TokenResponse;
import br.com.sistemas.chamados.security.ContextoUsuario;
import br.com.sistemas.chamados.security.UsuarioLogado;
import br.com.sistemas.chamados.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final ContextoUsuario contexto;

    public AuthController(AuthService service, ContextoUsuario contexto) {
        this.service = service;
        this.contexto = contexto;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest dto) {
        return service.login(dto);
    }

    @GetMapping("/me")
    public UsuarioLogado me() {
        return contexto.atual();
    }
}
