package br.com.sistemas.chamados.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import br.com.sistemas.chamados.repository.UsuarioRepository;
import br.com.sistemas.chamados.security.AdminProperties;

/** Garante um administrador para o primeiro login (o banco H2 é em memória). */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final AdminProperties admin;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminProperties admin,
                       UsuarioRepository usuarioRepository,
                       TecnicoRepository tecnicoRepository,
                       PasswordEncoder passwordEncoder) {
        this.admin = admin;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(admin.email())) {
            return;
        }
        Usuario usuario = new Usuario(admin.email(), passwordEncoder.encode(admin.senha()), Perfil.ADMIN);
        tecnicoRepository.save(new Tecnico(admin.nome(), usuario));
    }
}
