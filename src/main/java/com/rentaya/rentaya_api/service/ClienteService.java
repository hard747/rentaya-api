package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.ClienteResponse;
import com.rentaya.rentaya_api.dto.CrearClienteRequest;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.mapper.PersonaMapper;
import com.rentaya.rentaya_api.model.Cliente;
import com.rentaya.rentaya_api.repository.ClienteRepository;
import com.rentaya.rentaya_api.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final PersonaMapper mapper;

    public ClienteService(ClienteRepository clienteRepository, PersonaRepository personaRepository,
                          PersonaMapper mapper) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ClienteResponse crear(CrearClienteRequest request) {
        if (personaRepository.existsByDni(request.getDni())) {
            throw new ConflictoException("Ya existe una persona con DNI " + request.getDni());
        }
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new ConflictoException("Ya existe un cliente con email " + request.getEmail());
        }
        return mapper.toResponse(clienteRepository.save(mapper.toCliente(request)));
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + id));
    }
}
