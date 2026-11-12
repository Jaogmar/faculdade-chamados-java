package br.com.sistemas.chamados.controller.pagina;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPaginaController {

    @GetMapping("/app/login")
    public String login() {
        return "login";
    }

    @GetMapping("/app/cadastro")
    public String cadastro() {
        return "cadastro";
    }
}
