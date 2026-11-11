package br.com.sistemas.chamados.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sistemas.chamados.entity.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    Optional<Tecnico> findByUsuarioId(Long usuarioId);
}
