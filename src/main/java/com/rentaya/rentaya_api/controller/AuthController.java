package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.CrearUsuarioRequest;
import com.rentaya.rentaya_api.dto.LoginRequest;
import com.rentaya.rentaya_api.dto.RegistroRequest;
import com.rentaya.rentaya_api.dto.TokenResponse;
import com.rentaya.rentaya_api.dto.UsuarioResponse;
import com.rentaya.rentaya_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        return authService.crearUsuario(request);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return authService.listarUsuarios();
    }
}
