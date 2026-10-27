package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResponse(Long id, String username, String rol, Long clienteId) { }
