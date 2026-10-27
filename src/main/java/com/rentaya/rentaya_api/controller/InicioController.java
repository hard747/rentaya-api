package com.rentaya.rentaya_api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** La raíz de la API no tiene contenido propio: lleva a la documentación interactiva. */
@Hidden
@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/swagger-ui.html";
    }
}
