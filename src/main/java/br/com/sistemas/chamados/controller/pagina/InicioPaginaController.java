package br.com.sistemas.chamados.controller.pagina;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioPaginaController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/app";
    }
}
