package br.com.sistemas.chamados.entity;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.enums.StatusChamado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Registro de uma mudança de status de um chamado. */
@Entity
@Table(name = "historico_status")
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    /** Nulo no registro de abertura do chamado. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusChamado statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusChamado statusNovo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    public HistoricoStatus() { }

    public HistoricoStatus(Chamado chamado, StatusChamado statusAnterior,
                           StatusChamado statusNovo, Usuario responsavel) {
        this.chamado = chamado;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.responsavel = responsavel;
    }

    @PrePersist
    void aoRegistrar() {
        this.dataAlteracao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Chamado getChamado() { return chamado; }
    public StatusChamado getStatusAnterior() { return statusAnterior; }
    public StatusChamado getStatusNovo() { return statusNovo; }
    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public Usuario getResponsavel() { return responsavel; }
}
