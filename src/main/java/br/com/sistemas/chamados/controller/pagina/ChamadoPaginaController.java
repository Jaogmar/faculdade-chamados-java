package br.com.sistemas.chamados.controller.pagina;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/chamados")
public class ChamadoPaginaController {

    @GetMapping
    public String lista() {
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo() {
        return "chamados/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "chamados/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "chamados/detalhe";
    }
}
