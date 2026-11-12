package br.com.sistemas.chamados.controller.pagina;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import br.com.sistemas.chamados.support.ApiTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class ChamadoPaginaControllerTest extends ApiTestSupport {

    @Test
    void listaRenderizaSemToken() throws Exception {
        mvc.perform(get("/app/chamados").param("status", "ABERTO"))
            .andExpect(status().isOk())
            .andExpect(view().name("chamados/lista"));
    }

    @Test
    void novoRenderizaFormularioSemId() throws Exception {
        mvc.perform(get("/app/chamados/novo"))
            .andExpect(status().isOk())
            .andExpect(view().name("chamados/form"))
            .andExpect(model().attributeDoesNotExist("id"));
    }

    @Test
    void editarRenderizaFormularioComId() throws Exception {
        mvc.perform(get("/app/chamados/7/editar"))
            .andExpect(status().isOk())
            .andExpect(view().name("chamados/form"))
            .andExpect(model().attribute("id", 7L))
            .andExpect(content().string(containsString("Editar chamado #7")))
            .andExpect(content().string(containsString("data-id=\"7\"")));
    }

    @Test
    void detalheRenderizaComId() throws Exception {
        mvc.perform(get("/app/chamados/7"))
            .andExpect(status().isOk())
            .andExpect(view().name("chamados/detalhe"))
            .andExpect(model().attribute("id", 7L));
    }
}
