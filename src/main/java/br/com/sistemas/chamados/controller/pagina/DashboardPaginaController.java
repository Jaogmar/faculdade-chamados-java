package br.com.sistemas.chamados.controller.pagina;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardPaginaController {

    @GetMapping("/app")
    public String dashboard() {
        return "dashboard";
    }
}
