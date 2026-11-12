package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.UsuarioRequest;
import br.com.sistemas.chamados.dto.UsuarioResponse;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.enums.Perfil;
import br.com.sistemas.chamados.exception.AcessoNegadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import br.com.sistemas.chamados.repository.HistoricoStatusRepository;
import br.com.sistemas.chamados.repository.UsuarioRepository;
import br.com.sistemas.chamados.security.ContextoUsuario;
import br.com.sistemas.chamados.security.UsuarioLogado;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ChamadoRepository chamadoRepository;
    private final ComentarioRepository comentarioRepository;
    private final HistoricoStatusRepository historicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContextoUsuario contexto;

    public UsuarioService(UsuarioRepository repository,
                          ChamadoRepository chamadoRepository,
                          ComentarioRepository comentarioRepository,
                          HistoricoStatusRepository historicoRepository,
                          PasswordEncoder passwordEncoder,
                          ContextoUsuario contexto) {
        this.repository = repository;
        this.chamadoRepository = chamadoRepository;
        this.comentarioRepository = comentarioRepository;
        this.historicoRepository = historicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.contexto = contexto;
    }

    /** Cria e persiste um usuário (usado pelo ADMIN em /usuarios e pelo autocadastro). */
    @Transactional
    public Usuario novo(String nome, String email, String senha, String telefone, Perfil perfil) {
        if (perfil == null) {
            throw new RegraNegocioException("Perfil é obrigatório");
        }
        if (senha == null || senha.isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória");
        }
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe usuário com o e-mail " + email);
        }
        return repository.save(
            new Usuario(nome, email, passwordEncoder.encode(senha), telefone, perfil));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest dto) {
        return UsuarioResponse.de(
            novo(dto.nome(), dto.email(), dto.senha(), dto.telefone(), dto.perfil()));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Perfil perfil) {
        List<Usuario> usuarios = perfil == null
            ? repository.findAllByOrderByNome()
            : repository.findByPerfilOrderByNome(perfil);
        return usuarios.stream().map(UsuarioResponse::de).toList();
    }

    /** O próprio usuário, ou ADMIN/TECNICO. */
    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        contexto.exigirProprioOuPerfil(id, Perfil.ADMIN, Perfil.TECNICO);
        return UsuarioResponse.de(buscarEntity(id));
    }

    /** O próprio usuário ou ADMIN; apenas ADMIN altera o perfil. */
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest dto) {
        UsuarioLogado logado = contexto.exigirProprioOuPerfil(id, Perfil.ADMIN);
        Usuario usuario = buscarEntity(id);

        if (repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new RegraNegocioException("Já existe usuário com o e-mail " + dto.email());
        }
        if (dto.perfil() != null && dto.perfil() != usuario.getPerfil()) {
            if (!logado.isAdmin()) {
                throw new AcessoNegadoException("Apenas ADMIN pode alterar o perfil");
            }
            usuario.setPerfil(dto.perfil());
        }
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setTelefone(dto.telefone());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }
        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarEntity(id);
        if (chamadoRepository.existsByClienteIdOrTecnicoId(id, id)
                || comentarioRepository.existsByAutorId(id)
                || historicoRepository.existsByResponsavelId(id)) {
            throw new RegraNegocioException(
                "Usuário " + id + " possui chamados vinculados e não pode ser excluído");
        }
        repository.delete(usuario);
    }

    private Usuario buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Usuário " + id + " não encontrado"));
    }
}
