package br.com.sistemas.chamados.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.sistemas.chamados.entity.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    @EntityGraph(attributePaths = "responsavel")
    List<HistoricoStatus> findByChamadoIdOrderByDataAlteracaoAscIdAsc(Long chamadoId);

    @Modifying
    @Query("delete from HistoricoStatus h where h.chamado.id = :chamadoId")
    void deleteByChamadoId(Long chamadoId);

    boolean existsByResponsavelId(Long responsavelId);
}
