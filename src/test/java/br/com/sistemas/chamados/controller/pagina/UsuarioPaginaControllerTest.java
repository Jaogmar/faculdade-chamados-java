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
class UsuarioPaginaControllerTest extends ApiTestSupport {

    @Test
    void listaRenderizaSemToken() throws Exception {
        mvc.perform(get("/app/usuarios"))
            .andExpect(status().isOk())
            .andExpect(view().name("usuarios/lista"));
    }

    @Test
    void novoRenderizaFormularioSemId() throws Exception {
        mvc.perform(get("/app/usuarios/novo"))
            .andExpect(status().isOk())
            .andExpect(view().name("usuarios/form"))
            .andExpect(model().attributeDoesNotExist("id"))
            .andExpect(content().string(containsString("Novo usuário")));
    }

    @Test
    void editarRenderizaFormularioComId() throws Exception {
        mvc.perform(get("/app/usuarios/5/editar"))
            .andExpect(status().isOk())
            .andExpect(view().name("usuarios/form"))
            .andExpect(model().attribute("id", 5L))
            .andExpect(content().string(containsString("Editar usuário #5")))
            .andExpect(content().string(containsString("data-id=\"5\"")));
    }

    @Test
    void detalheRenderizaComId() throws Exception {
        mvc.perform(get("/app/usuarios/5"))
            .andExpect(status().isOk())
            .andExpect(view().name("usuarios/detalhe"))
            .andExpect(model().attribute("id", 5L));
    }
}
