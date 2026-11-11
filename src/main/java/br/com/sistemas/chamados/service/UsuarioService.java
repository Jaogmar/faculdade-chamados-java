package br.com.sistemas.chamados.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.UsuarioRepository;

/** Regras de credenciais compartilhadas entre os cadastros de Técnico e Cliente. */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Monta um novo Usuario (persistido em cascata junto com Tecnico/Cliente). */
    public Usuario novo(String email, String senha, Perfil perfil) {
        if (senha == null || senha.isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória");
        }
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe usuário com o e-mail " + email);
        }
        return new Usuario(email, passwordEncoder.encode(senha), perfil);
    }

    /** Atualiza e-mail e, se informada, a senha. */
    public void atualizar(Usuario usuario, String email, String senha) {
        if (repository.existsByEmailAndIdNot(email, usuario.getId())) {
            throw new RegraNegocioException("Já existe usuário com o e-mail " + email);
        }
        usuario.setEmail(email);
        if (senha != null && !senha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(senha));
        }
    }
}
