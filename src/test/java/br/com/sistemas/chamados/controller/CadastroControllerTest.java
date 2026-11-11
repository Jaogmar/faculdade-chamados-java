package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CadastroControllerTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mvc;

    @Test
    void cadastroComoClienteRetornaTokenDeCliente() throws Exception {
        String corpo = mvc.perform(cadastro("Carla", "carla@cad.com", "CLIENTE"))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.startsWith("/clientes/")))
            .andExpect(jsonPath("$.tipo").value("CLIENTE"))
            .andExpect(jsonPath("$.autenticacao.perfil").value("CLIENTE"))
            .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token(corpo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").isNotEmpty())
            .andExpect(jsonPath("$.tecnicoId").doesNotExist());
    }

    @Test
    void cadastroComoTecnicoRetornaTokenDeTecnico() throws Exception {
        String corpo = mvc.perform(cadastro("Davi", "davi@cad.com", "TECNICO"))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.startsWith("/tecnicos/")))
            .andExpect(jsonPath("$.autenticacao.perfil").value("TECNICO"))
            .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token(corpo)))
            .andExpect(jsonPath("$.tecnicoId").isNotEmpty());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"davi@cad.com\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        mvc.perform(cadastro("Eva", "eva@cad.com", "CLIENTE")).andExpect(status().isCreated());
        mvc.perform(cadastro("Eva 2", "eva@cad.com", "TECNICO")).andExpect(status().isConflict());
    }

    @Test
    void tipoAusenteRetorna400() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Fabio\",\"email\":\"fabio@cad.com\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void tipoInvalidoOuAdminRetorna400() throws Exception {
        mvc.perform(cadastro("Gil", "gil@cad.com", "ADMIN")).andExpect(status().isBadRequest());
        mvc.perform(cadastro("Gil", "gil@cad.com", "QUALQUER")).andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.RequestBuilder cadastro(
            String nome, String email, String tipo) {
        return post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                + "\",\"senha\":\"segredo1\",\"tipo\":\"" + tipo + "\"}");
    }

    private static String token(String corpo) {
        Matcher m = TOKEN.matcher(corpo);
        if (!m.find()) {
            throw new AssertionError("Token não encontrado: " + corpo);
        }
        return m.group(1);
    }
}
