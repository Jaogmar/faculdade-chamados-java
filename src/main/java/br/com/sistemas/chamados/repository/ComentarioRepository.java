package br.com.sistemas.chamados.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.sistemas.chamados.entity.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @EntityGraph(attributePaths = "autor")
    List<Comentario> findByChamadoIdOrderByDataCriacaoAscIdAsc(Long chamadoId);

    @Modifying
    @Query("delete from Comentario c where c.chamado.id = :chamadoId")
    void deleteByChamadoId(Long chamadoId);

    boolean existsByAutorId(Long autorId);
}
