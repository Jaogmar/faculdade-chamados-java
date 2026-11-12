package br.com.sistemas.chamados.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistemas.chamados.dto.CadastroRequest;
import br.com.sistemas.chamados.dto.CadastroResponse;
import br.com.sistemas.chamados.dto.LoginRequest;
import br.com.sistemas.chamados.dto.TokenResponse;
import br.com.sistemas.chamados.security.ContextoUsuario;
import br.com.sistemas.chamados.security.UsuarioLogado;
import br.com.sistemas.chamados.service.AuthService;
import br.com.sistemas.chamados.service.CadastroService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final CadastroService cadastroService;
    private final ContextoUsuario contexto;

    public AuthController(AuthService service, CadastroService cadastroService,
                          ContextoUsuario contexto) {
        this.service = service;
        this.cadastroService = cadastroService;
        this.contexto = contexto;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<CadastroResponse> cadastrar(@Valid @RequestBody CadastroRequest dto) {
        CadastroResponse criado = cadastroService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.usuario().id())).body(criado);
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
