package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import br.com.sistemas.chamados.repository.UsuarioRepository;
import br.com.sistemas.chamados.security.ContextoUsuario;

@Service
public class ComentarioService {

    private final ComentarioRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoService chamadoService;
    private final ContextoUsuario contexto;

    public ComentarioService(ComentarioRepository repository,
                             UsuarioRepository usuarioRepository,
                             ChamadoService chamadoService,
                             ContextoUsuario contexto) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.chamadoService = chamadoService;
        this.contexto = contexto;
    }

    /** O autor é sempre o usuário logado; cliente só comenta nos próprios chamados. */
    @Transactional
    public ComentarioResponse adicionar(Long chamadoId, ComentarioRequest dto) {
        Chamado chamado = chamadoService.buscarEntityComAcesso(chamadoId);
        Comentario comentario = new Comentario(
            dto.texto(), chamado, usuarioRepository.getReferenceById(contexto.id()));
        return ComentarioResponse.de(repository.save(comentario));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar(Long chamadoId) {
        chamadoService.buscarEntityComAcesso(chamadoId);
        return repository.findByChamadoIdOrderByDataCriacaoAscIdAsc(chamadoId).stream()
            .map(ComentarioResponse::de)
            .toList();
    }
}
