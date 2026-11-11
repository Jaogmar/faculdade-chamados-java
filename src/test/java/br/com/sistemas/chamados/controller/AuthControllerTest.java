package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class AuthControllerTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mvc;

    @Test
    void loginAdminRetornaToken() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@chamados.com\",\"senha\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@chamados.com\",\"senha\":\"errada\"}"))
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
        String token = login("admin@chamados.com", "admin123");
        mvc.perform(get("/chamados").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.perfil").value("ADMIN"))
            .andExpect(jsonPath("$.tecnicoId").isNotEmpty());
    }

    @Test
    void clienteNaoPodeCadastrarTecnico() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria\",\"email\":\"maria@teste.com\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isCreated());
        String token = login("maria@teste.com", "segredo1");

        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(jsonPath("$.perfil").value("CLIENTE"))
            .andExpect(jsonPath("$.clienteId").isNotEmpty());

        mvc.perform(post("/tecnicos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"X\",\"email\":\"x@teste.com\",\"perfil\":\"TECNICO\",\"senha\":\"segredo1\"}"))
            .andExpect(status().isForbidden());
    }

    private String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Matcher m = TOKEN.matcher(corpo);
        if (!m.find()) {
            throw new AssertionError("Token não encontrado: " + corpo);
        }
        return m.group(1);
    }
}
