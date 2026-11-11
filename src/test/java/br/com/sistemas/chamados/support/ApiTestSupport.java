package br.com.sistemas.chamados.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

/** Helpers compartilhados pelos testes de integração da API. */
public abstract class ApiTestSupport {

    protected static final String ADMIN_EMAIL = "admin@chamados.com";
    protected static final String ADMIN_SENHA = "admin123";

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    protected MockMvc mvc;

    protected RequestBuilder cadastro(String nome, String email, String perfil) {
        return post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                + "\",\"senha\":\"segredo1\",\"perfil\":\"" + perfil + "\"}");
    }

    /** Faz o autocadastro e retorna o corpo da resposta. */
    protected String cadastrar(String nome, String email, String perfil) throws Exception {
        return mvc.perform(cadastro(nome, email, perfil))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
    }

    protected String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return token(corpo);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String token(String corpo) {
        return extrair(TOKEN, corpo);
    }

    protected static long id(String corpo) {
        return Long.parseLong(extrair(ID, corpo));
    }

    private static String extrair(Pattern p, String corpo) {
        Matcher m = p.matcher(corpo);
        if (!m.find()) {
            throw new AssertionError(p + " não encontrado em: " + corpo);
        }
        return m.group(1);
    }
}
