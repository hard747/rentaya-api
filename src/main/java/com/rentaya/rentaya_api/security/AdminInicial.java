package com.rentaya.rentaya_api.security;

import com.rentaya.rentaya_api.model.Usuario;
import com.rentaya.rentaya_api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer administrador al arrancar si se definen ADMIN_USERNAME y ADMIN_PASSWORD
 * y todavía no existe. Las credenciales nunca se guardan en el código.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        @Value("${rentaya.admin.username:}") String username,
                        @Value("${rentaya.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank() || usuarioRepository.existsByUsername(username)) {
            return;
        }
        usuarioRepository.save(new Usuario(username, passwordEncoder.encode(password), Usuario.Rol.ADMIN, null));
        log.info("Administrador inicial '{}' creado", username);
    }
}
