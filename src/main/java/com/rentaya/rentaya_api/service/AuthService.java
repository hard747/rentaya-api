package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.CrearUsuarioRequest;
import com.rentaya.rentaya_api.dto.LoginRequest;
import com.rentaya.rentaya_api.dto.RegistroRequest;
import com.rentaya.rentaya_api.dto.TokenResponse;
import com.rentaya.rentaya_api.dto.UsuarioResponse;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.exception.CredencialesInvalidasException;
import com.rentaya.rentaya_api.model.Cliente;
import com.rentaya.rentaya_api.model.Usuario;
import com.rentaya.rentaya_api.repository.ClienteRepository;
import com.rentaya.rentaya_api.repository.PersonaRepository;
import com.rentaya.rentaya_api.repository.UsuarioRepository;
import com.rentaya.rentaya_api.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                       PersonaRepository personaRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return token(usuario);
    }

    /** Crea el cliente y su cuenta en la misma transacción, y devuelve un token listo para usar. */
    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El usuario " + request.username() + " ya existe");
        }
        if (personaRepository.existsByDni(request.dni())) {
            throw new ConflictoException("Ya existe una persona con DNI " + request.dni());
        }
        if (clienteRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un cliente con email " + request.email());
        }
        Cliente cliente = clienteRepository.save(new Cliente(request.nombre(), request.dni(), request.email()));
        Usuario usuario = usuarioRepository.save(new Usuario(request.username(),
                passwordEncoder.encode(request.password()), Usuario.Rol.CLIENTE, cliente));
        return token(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El usuario " + request.username() + " ya existe");
        }
        Usuario usuario = usuarioRepository.save(new Usuario(request.username(),
                passwordEncoder.encode(request.password()), Usuario.Rol.valueOf(request.rol()), null));
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    private TokenResponse token(Usuario usuario) {
        return new TokenResponse(tokenService.generar(usuario), "Bearer", tokenService.expiracionSegundos(),
                usuario.getRol().name());
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getRol().name(),
                u.getCliente() == null ? null : u.getCliente().getId());
    }
}
