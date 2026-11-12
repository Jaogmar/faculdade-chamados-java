package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import br.com.sistemas.chamados.support.ApiTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest extends ApiTestSupport {

    @Test
    void adminListaUsuariosFiltrandoPorPerfil() throws Exception {
        cadastrar("Tec Lista", "tec@usr.com", "TECNICO");
        cadastrar("Cli Lista", "cli@usr.com", "CLIENTE");
        String admin = login(ADMIN_EMAIL, ADMIN_SENHA);

        mvc.perform(get("/usuarios").param("perfil", "TECNICO")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].perfil", Matchers.everyItem(Matchers.is("TECNICO"))))
            .andExpect(jsonPath("$[*].email", Matchers.hasItem("tec@usr.com")));
    }

    @Test
    void clienteSoAcessaOProprioCadastro() throws Exception {
        String ana = cadastrar("Ana", "ana@usr.com", "CLIENTE");
        String beto = cadastrar("Beto", "beto@usr.com", "CLIENTE");

        mvc.perform(get("/usuarios/" + id(ana)).header(HttpHeaders.AUTHORIZATION, bearer(token(ana))))
            .andExpect(status().isOk());
        mvc.perform(get("/usuarios/" + id(beto)).header(HttpHeaders.AUTHORIZATION, bearer(token(ana))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(token(ana))))
            .andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoPodeAlterarOProprioPerfil() throws Exception {
        String caio = cadastrar("Caio", "caio@usr.com", "CLIENTE");
        mvc.perform(put("/usuarios/" + id(caio)).header(HttpHeaders.AUTHORIZATION, bearer(token(caio)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Caio\",\"email\":\"caio@usr.com\",\"perfil\":\"ADMIN\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/usuarios/" + id(caio)).header(HttpHeaders.AUTHORIZATION, bearer(token(caio)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Caio Silva\",\"email\":\"caio@usr.com\",\"telefone\":\"1199\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Caio Silva"))
            .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void fluxoDeChamadoComUsuarioClienteETecnico() throws Exception {
        String admin = login(ADMIN_EMAIL, ADMIN_SENHA);
        String cliente = cadastrar("Dora", "dora@usr.com", "CLIENTE");
        String tecnico = cadastrar("Edu", "edu@usr.com", "TECNICO");

        String categoria = mvc.perform(post("/categorias").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Rede Usr\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        // cliente abre chamado sem informar clienteId: vem do token
        String chamado = mvc.perform(post("/chamados").header(HttpHeaders.AUTHORIZATION, bearer(token(cliente)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Sem rede\",\"descricao\":\"x\",\"prioridade\":\"ALTA\",\"categoriaId\":"
                    + id(categoria) + ",\"tecnicoId\":" + id(tecnico) + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clienteId").value(id(cliente)))
            .andExpect(jsonPath("$.tecnicoNome").value("Edu"))
            .andReturn().getResponse().getContentAsString();

        // técnico não pode ser informado como cliente
        mvc.perform(post("/chamados").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"T\",\"descricao\":\"d\",\"prioridade\":\"BAIXA\",\"clienteId\":"
                    + id(tecnico) + ",\"categoriaId\":" + id(categoria) + "}"))
            .andExpect(status().isConflict());

        mvc.perform(get("/usuarios/" + id(cliente) + "/chamados")
                .header(HttpHeaders.AUTHORIZATION, bearer(token(cliente))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(id(chamado)));

        // usuário com chamados vinculados não pode ser excluído
        mvc.perform(delete("/usuarios/" + id(cliente)).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isConflict());
    }
}
