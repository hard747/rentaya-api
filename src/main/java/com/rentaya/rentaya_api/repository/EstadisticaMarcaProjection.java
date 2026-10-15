package com.rentaya.rentaya_api.repository;

import java.math.BigDecimal;

public interface EstadisticaMarcaProjection {
    String getMarca();
    BigDecimal getPromedioKilometraje();
    BigDecimal getPromedioCilindrada();
    Long getCantidad();
}