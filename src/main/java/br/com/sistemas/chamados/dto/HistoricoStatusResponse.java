package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.entity.HistoricoStatus;
import br.com.sistemas.chamados.enums.StatusChamado;

public record HistoricoStatusResponse(
    StatusChamado statusAnterior,
    StatusChamado statusNovo,
    LocalDateTime dataAlteracao,
    Long responsavelId,
    String responsavelNome
) {
    public static HistoricoStatusResponse de(HistoricoStatus h) {
        return new HistoricoStatusResponse(h.getStatusAnterior(), h.getStatusNovo(),
            h.getDataAlteracao(), h.getResponsavel().getId(), h.getResponsavel().getNome());
    }
}
