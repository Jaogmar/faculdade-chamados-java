package br.com.sistemas.chamados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.CadastroRequest;
import br.com.sistemas.chamados.dto.CadastroResponse;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import br.com.sistemas.chamados.security.TokenService;
import br.com.sistemas.chamados.security.UsuarioLogado;

/** Autocadastro: o próprio usuário cria a conta e informa se é CLIENTE ou TECNICO. */
@Service
public class CadastroService {

    private final UsuarioService usuarioService;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final TokenService tokenService;

    public CadastroService(UsuarioService usuarioService,
                           ClienteRepository clienteRepository,
                           TecnicoRepository tecnicoRepository,
                           TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest dto) {
        Usuario usuario = usuarioService.novo(dto.email(), dto.senha(), dto.tipo().getPerfil());

        Long id;
        UsuarioLogado logado;
        switch (dto.tipo()) {
            case CLIENTE -> {
                Cliente cliente = clienteRepository.save(
                    new Cliente(dto.nome(), dto.telefone(), usuario));
                id = cliente.getId();
                logado = new UsuarioLogado(usuario.getId(), usuario.getEmail(),
                    usuario.getPerfil(), null, id);
            }
            case TECNICO -> {
                Tecnico tecnico = tecnicoRepository.save(new Tecnico(dto.nome(), usuario));
                id = tecnico.getId();
                logado = new UsuarioLogado(usuario.getId(), usuario.getEmail(),
                    usuario.getPerfil(), id, null);
            }
            default -> throw new IllegalStateException("Tipo não suportado: " + dto.tipo());
        }

        return new CadastroResponse(id, dto.nome(), usuario.getEmail(), dto.tipo(),
            tokenService.gerar(logado));
    }
}
