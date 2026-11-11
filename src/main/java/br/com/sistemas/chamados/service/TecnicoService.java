package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class TecnicoService {

    private final TecnicoRepository repository;
    private final UsuarioService usuarioService;

    public TecnicoService(TecnicoRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest dto) {
        validarPerfil(dto.perfil());
        Usuario usuario = usuarioService.novo(dto.email(), dto.senha(), dto.perfil());
        Tecnico salvo = repository.save(new Tecnico(dto.nome(), usuario));
        return TecnicoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return repository.findAll().stream().map(TecnicoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscar(Long id) {
        return TecnicoResponse.de(buscarEntity(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest dto) {
        validarPerfil(dto.perfil());
        Tecnico tecnico = buscarEntity(id);
        usuarioService.atualizar(tecnico.getUsuario(), dto.email(), dto.senha());
        tecnico.getUsuario().setPerfil(dto.perfil());
        tecnico.setNome(dto.nome());
        return TecnicoResponse.de(tecnico);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private void validarPerfil(Perfil perfil) {
        if (perfil == Perfil.CLIENTE) {
            throw new RegraNegocioException("Técnico deve ter perfil ADMIN ou TECNICO");
        }
    }

    private Tecnico buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + id + " não encontrado"));
    }
}
