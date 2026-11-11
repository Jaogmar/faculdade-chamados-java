package br.com.sistemas.chamados.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<Usuario> findByPerfilOrderByNome(Perfil perfil);
    List<Usuario> findAllByOrderByNome();
}
