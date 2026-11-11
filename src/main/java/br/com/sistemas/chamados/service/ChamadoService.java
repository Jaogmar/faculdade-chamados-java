package br.com.sistemas.chamados.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.exception.AcessoNegadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import br.com.sistemas.chamados.security.ContextoUsuario;
import br.com.sistemas.chamados.security.UsuarioLogado;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ContextoUsuario contexto;

    public ChamadoService(ChamadoRepository repository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository,
                          ContextoUsuario contexto) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.contexto = contexto;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Chamado chamado = new Chamado(
            dto.titulo(), dto.descricao(), dto.prioridade(),
            buscarCliente(resolverClienteId(dto.clienteId())),
            buscarCategoria(dto.categoriaId()),
            buscarTecnico(dto.tecnicoId()));
        return ChamadoResponse.de(repository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
                                        Long clienteId, Pageable pageable) {
        UsuarioLogado usuario = contexto.atual();
        if (usuario.isCliente()) {
            clienteId = usuario.clienteId(); // cliente só enxerga os próprios chamados
        }
        return repository.filtrar(status, prioridade, clienteId, pageable)
            .map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        verificarAcessoCliente(clienteId);
        buscarCliente(clienteId);
        return listar(null, null, clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id) {
        return ChamadoResponse.de(buscarEntityComAcesso(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        Chamado chamado = buscarEntityComAcesso(id);
        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(buscarCliente(resolverClienteId(dto.clienteId())));
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        chamado.setTecnico(buscarTecnico(dto.tecnicoId()));
        if (dto.status() != null) {
            chamado.setStatus(dto.status());
            chamado.setDataFechamento(
                dto.status() == StatusChamado.FECHADO ? LocalDateTime.now() : null);
        }
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntityComAcesso(id));
    }

    /** Para CLIENTE o id vem do token; para ADMIN/TECNICO deve vir no request. */
    private Long resolverClienteId(Long informado) {
        UsuarioLogado usuario = contexto.atual();
        if (usuario.isCliente()) {
            return usuario.clienteId();
        }
        if (informado == null) {
            throw new RegraNegocioException("Cliente é obrigatório");
        }
        return informado;
    }

    private void verificarAcessoCliente(Long clienteId) {
        UsuarioLogado usuario = contexto.atual();
        if (usuario.isCliente() && !clienteId.equals(usuario.clienteId())) {
            throw new AcessoNegadoException("Acesso negado aos chamados do cliente " + clienteId);
        }
    }

    private Chamado buscarEntityComAcesso(Long id) {
        Chamado chamado = buscarEntity(id);
        UsuarioLogado usuario = contexto.atual();
        if (usuario.isCliente() && !chamado.getCliente().getId().equals(usuario.clienteId())) {
            throw new AcessoNegadoException("Acesso negado ao chamado " + id);
        }
        return chamado;
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Chamado " + id + " não encontrado"));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Cliente " + id + " não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Categoria " + id + " não encontrada"));
    }

    private Tecnico buscarTecnico(Long id) {
        if (id == null) {
            return null;
        }
        return tecnicoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + id + " não encontrado"));
    }
}
