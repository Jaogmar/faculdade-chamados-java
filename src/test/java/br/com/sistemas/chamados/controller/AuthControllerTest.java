package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import br.com.sistemas.chamados.support.ApiTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends ApiTestSupport {

    @Test
    void loginAdminRetornaToken() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + ADMIN_EMAIL + "\",\"senha\":\"" + ADMIN_SENHA + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + ADMIN_EMAIL + "\",\"senha\":\"errada\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mvc.perform(get("/chamados"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rotaProtegidaComTokenRetorna200() throws Exception {
        String token = login(ADMIN_EMAIL, ADMIN_SENHA);
        mvc.perform(get("/chamados").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk());
        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.perfil").value("ADMIN"))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.nome").value("Administrador"));
    }

    @Test
    void clienteNaoPodeCriarUsuarioPelaApiAdministrativa() throws Exception {
        String token = token(cadastrar("Maria", "maria@auth.com", "CLIENTE"));
        mvc.perform(post("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"X\",\"email\":\"x@auth.com\",\"perfil\":\"TECNICO\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isForbidden());
    }
}
