package br.com.sistemas.chamados.controller.pagina;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import br.com.sistemas.chamados.support.ApiTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class AuthPaginaControllerTest extends ApiTestSupport {

    @Test
    void loginEPublico() throws Exception {
        mvc.perform(get("/app/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    void cadastroEPublico() throws Exception {
        mvc.perform(get("/app/cadastro"))
            .andExpect(status().isOk())
            .andExpect(view().name("cadastro"));
    }
}
