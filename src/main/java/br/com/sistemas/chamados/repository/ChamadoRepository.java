package br.com.sistemas.chamados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    @EntityGraph(attributePaths = {"cliente", "categoria", "tecnico"})
    @Query("""
        select c from Chamado c
        where (:status is null or c.status = :status)
          and (:prioridade is null or c.prioridade = :prioridade)
          and (:clienteId is null or c.cliente.id = :clienteId)
        """)
    Page<Chamado> filtrar(StatusChamado status, Prioridade prioridade,
                          Long clienteId, Pageable pageable);
}
