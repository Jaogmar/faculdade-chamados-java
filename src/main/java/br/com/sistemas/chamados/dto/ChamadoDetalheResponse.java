package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;

/** Detalhe do chamado (GET /chamados/{id}), com comentários e histórico de status. */
public record ChamadoDetalheResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataAbertura,
    LocalDateTime dataFechamento,
    Prioridade prioridade,
    StatusChamado status,
    Long clienteId,
    String clienteNome,
    Long categoriaId,
    String categoriaNome,
    Long tecnicoId,
    String tecnicoNome,
    List<ComentarioResponse> comentarios,
    List<HistoricoStatusResponse> historico
) {
    public static ChamadoDetalheResponse de(Chamado c, List<ComentarioResponse> comentarios,
                                            List<HistoricoStatusResponse> historico) {
        ChamadoResponse r = ChamadoResponse.de(c);
        return new ChamadoDetalheResponse(
            r.id(), r.titulo(), r.descricao(), r.dataAbertura(), r.dataFechamento(),
            r.prioridade(), r.status(), r.clienteId(), r.clienteNome(),
            r.categoriaId(), r.categoriaNome(), r.tecnicoId(), r.tecnicoNome(),
            comentarios, historico);
    }
}
