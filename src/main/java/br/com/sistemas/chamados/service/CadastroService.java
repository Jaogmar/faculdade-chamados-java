package br.com.sistemas.chamados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.CadastroRequest;
import br.com.sistemas.chamados.dto.CadastroResponse;
import br.com.sistemas.chamados.dto.UsuarioResponse;
import br.com.sistemas.chamados.entity.Usuario;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.security.TokenService;

/** Autocadastro: o próprio usuário cria a conta e informa se é CLIENTE ou TECNICO. */
@Service
public class CadastroService {

    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    public CadastroService(UsuarioService usuarioService, TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest dto) {
        if (!dto.perfil().isAutocadastravel()) {
            throw new RegraNegocioException("Perfil deve ser CLIENTE ou TECNICO");
        }
        Usuario usuario = usuarioService.novo(
            dto.nome(), dto.email(), dto.senha(), dto.telefone(), dto.perfil());
        return new CadastroResponse(UsuarioResponse.de(usuario), tokenService.gerar(usuario));
    }
}
