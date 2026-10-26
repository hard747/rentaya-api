package com.rentaya.rentaya_api.model;

import java.math.BigDecimal;

/** Lo que se puede calificar de 1 a 5 estrellas: conductores y taller externo. */
public interface Calificable {

    void calificar(int estrellas);

    BigDecimal getPromedioCalificacion();
}
