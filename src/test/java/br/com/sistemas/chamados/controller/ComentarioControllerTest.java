package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class ComentarioControllerTest extends ApiTestSupport {

    @Test
    void clienteComentaEDetalheTrazComentarioEHistorico() throws Exception {
        String cliente = cadastrar("Carla", "carla@coment.com", "CLIENTE");
        long chamadoId = abrirChamado(token(cliente), criarCategoria("Rede"));

        mvc.perform(comentario(chamadoId, token(cliente), "Ainda sem internet"))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION,
                Matchers.startsWith("/chamados/" + chamadoId + "/comentarios/")))
            .andExpect(jsonPath("$.texto").value("Ainda sem internet"))
            .andExpect(jsonPath("$.autorNome").value("Carla"))
            .andExpect(jsonPath("$.autorPerfil").value("CLIENTE"));

        mvc.perform(get("/chamados/" + chamadoId).header(HttpHeaders.AUTHORIZATION, bearer(token(cliente))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comentarios.length()").value(1))
            .andExpect(jsonPath("$.comentarios[0].autorNome").value("Carla"))
            .andExpect(jsonPath("$.historico.length()").value(1))
            .andExpect(jsonPath("$.historico[0].statusAnterior").doesNotExist())
            .andExpect(jsonPath("$.historico[0].statusNovo").value("ABERTO"));

        mvc.perform(get("/chamados/" + chamadoId + "/comentarios")
                .header(HttpHeaders.AUTHORIZATION, bearer(token(cliente))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].texto").value("Ainda sem internet"));
    }

    @Test
    void comentarioSemTextoRetorna400() throws Exception {
        String cliente = cadastrar("Davi", "davi@coment.com", "CLIENTE");
        long chamadoId = abrirChamado(token(cliente), criarCategoria("Hardware"));

        mvc.perform(comentario(chamadoId, token(cliente), "  "))
            .andExpect(status().isBadRequest());
    }

    @Test
    void clienteNaoComentaChamadoDeOutroEChamadoInexistenteRetorna404() throws Exception {
        String dono = cadastrar("Elisa", "elisa@coment.com", "CLIENTE");
        String outro = cadastrar("Fabio", "fabio@coment.com", "CLIENTE");
        long chamadoId = abrirChamado(token(dono), criarCategoria("Software"));

        mvc.perform(comentario(chamadoId, token(outro), "Intrometido"))
            .andExpect(status().isForbidden());
        mvc.perform(comentario(999_999, token(dono), "Oi"))
            .andExpect(status().isNotFound());
    }

    @Test
    void mudancaDeStatusGeraHistoricoEExclusaoRemoveComentarios() throws Exception {
        String cliente = cadastrar("Gabi", "gabi@coment.com", "CLIENTE");
        long categoriaId = criarCategoria("Impressora");
        long chamadoId = abrirChamado(token(cliente), categoriaId);
        String admin = login(ADMIN_EMAIL, ADMIN_SENHA);

        mvc.perform(comentario(chamadoId, admin, "Vou verificar"))
            .andExpect(status().isCreated());

        mvc.perform(put("/chamados/" + chamadoId).header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Impressora\",\"descricao\":\"Não imprime\",\"prioridade\":\"ALTA\","
                    + "\"clienteId\":" + id(cliente) + ",\"categoriaId\":" + categoriaId
                    + ",\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isOk());

        mvc.perform(get("/chamados/" + chamadoId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(jsonPath("$.historico.length()").value(2))
            .andExpect(jsonPath("$.historico[1].statusAnterior").value("ABERTO"))
            .andExpect(jsonPath("$.historico[1].statusNovo").value("EM_ATENDIMENTO"))
            .andExpect(jsonPath("$.historico[1].responsavelNome").isNotEmpty());

        mvc.perform(delete("/chamados/" + chamadoId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isNoContent());
        mvc.perform(get("/chamados/" + chamadoId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
            .andExpect(status().isNotFound());
    }

    private long criarCategoria(String nome) throws Exception {
        String admin = login(ADMIN_EMAIL, ADMIN_SENHA);
        String corpo = mvc.perform(post("/categorias").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"" + nome + " " + System.nanoTime() + "\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return id(corpo);
    }

    private long abrirChamado(String token, long categoriaId) throws Exception {
        String corpo = mvc.perform(post("/chamados").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Problema\",\"descricao\":\"Descrição\",\"prioridade\":\"MEDIA\","
                    + "\"categoriaId\":" + categoriaId + "}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return id(corpo);
    }

    private org.springframework.test.web.servlet.RequestBuilder comentario(
            long chamadoId, String token, String texto) {
        return post("/chamados/" + chamadoId + "/comentarios")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"texto\":\"" + texto + "\"}");
    }
}
