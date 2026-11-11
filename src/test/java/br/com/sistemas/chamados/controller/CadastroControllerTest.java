package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CadastroControllerTest extends ApiTestSupport {

    @Test
    void cadastroComoClienteRetornaUsuarioETokenDeCliente() throws Exception {
        String corpo = mvc.perform(cadastro("Carla", "carla@cad.com", "CLIENTE"))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, Matchers.startsWith("/usuarios/")))
            .andExpect(jsonPath("$.usuario.perfil").value("CLIENTE"))
            .andExpect(jsonPath("$.usuario.senha").doesNotExist())
            .andExpect(jsonPath("$.autenticacao.perfil").value("CLIENTE"))
            .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token(corpo))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.perfil").value("CLIENTE"));
    }

    @Test
    void cadastroComoTecnicoPermiteLogin() throws Exception {
        mvc.perform(cadastro("Davi", "davi@cad.com", "TECNICO"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuario.perfil").value("TECNICO"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"davi@cad.com\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.perfil").value("TECNICO"));
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        mvc.perform(cadastro("Eva", "eva@cad.com", "CLIENTE")).andExpect(status().isCreated());
        mvc.perform(cadastro("Eva 2", "eva@cad.com", "TECNICO")).andExpect(status().isConflict());
    }

    @Test
    void perfilAusenteRetorna400() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Fabio\",\"email\":\"fabio@cad.com\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void perfilAdminOuInvalidoRetorna400() throws Exception {
        mvc.perform(cadastro("Gil", "gil@cad.com", "ADMIN")).andExpect(status().isBadRequest());
        mvc.perform(cadastro("Gil", "gil@cad.com", "QUALQUER")).andExpect(status().isBadRequest());
    }
}
