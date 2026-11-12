package br.com.sistemas.chamados.controller.pagina;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import br.com.sistemas.chamados.support.ApiTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class InicioPaginaControllerTest extends ApiTestSupport {

    @Test
    void raizRedirecionaParaApp() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/app"));
    }

    @Test
    void arquivosEstaticosSaoPublicos() throws Exception {
        mvc.perform(get("/js/app.js")).andExpect(status().isOk());
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
    }

    @Test
    void apiContinuaProtegida() throws Exception {
        mvc.perform(get("/chamados")).andExpect(status().isUnauthorized());
    }
}
