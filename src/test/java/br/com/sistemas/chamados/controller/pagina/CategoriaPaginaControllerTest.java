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
class CategoriaPaginaControllerTest extends ApiTestSupport {

    @Test
    void categoriasRenderizaSemToken() throws Exception {
        mvc.perform(get("/app/categorias"))
            .andExpect(status().isOk())
            .andExpect(view().name("categorias"));
    }
}
