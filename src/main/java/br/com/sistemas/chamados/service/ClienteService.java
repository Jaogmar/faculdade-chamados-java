package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.exception.AcessoNegadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.security.ContextoUsuario;
import br.com.sistemas.chamados.security.UsuarioLogado;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final UsuarioService usuarioService;
    private final ContextoUsuario contexto;

    public ClienteService(ClienteRepository repository,
                          UsuarioService usuarioService,
                          ContextoUsuario contexto) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.contexto = contexto;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest dto) {
        Usuario usuario = usuarioService.novo(dto.email(), dto.senha(), Perfil.CLIENTE);
        Cliente salvo = repository.save(new Cliente(dto.nome(), dto.telefone(), usuario));
        return ClienteResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        verificarAcesso(id);
        return ClienteResponse.de(buscarEntity(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest dto) {
        verificarAcesso(id);
        Cliente cliente = buscarEntity(id);
        usuarioService.atualizar(cliente.getUsuario(), dto.email(), dto.senha());
        cliente.setNome(dto.nome());
        cliente.setTelefone(dto.telefone());
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    /** Cliente só acessa o próprio cadastro; ADMIN/TECNICO acessam qualquer um. */
    public void verificarAcesso(Long clienteId) {
        UsuarioLogado usuario = contexto.atual();
        if (usuario.isCliente() && !clienteId.equals(usuario.clienteId())) {
            throw new AcessoNegadoException("Acesso negado ao cliente " + clienteId);
        }
    }

    private Cliente buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Cliente " + id + " não encontrado"));
    }
}
