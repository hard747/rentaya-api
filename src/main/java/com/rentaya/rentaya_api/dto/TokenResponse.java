package com.rentaya.rentaya_api.dto;

public record TokenResponse(String token, String tipo, long expiraEnSegundos, String rol) { }
