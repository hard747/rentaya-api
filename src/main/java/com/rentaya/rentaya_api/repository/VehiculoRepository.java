package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query(value = """
            SELECT marca,
                   ROUND(AVG(kilometraje), 2) AS "promedioKilometraje",
                   ROUND(AVG(cilindrada), 2)  AS "promedioCilindrada",
                   COUNT(*)                   AS "cantidad"
            FROM vehiculos
            WHERE activo
            GROUP BY marca
            ORDER BY marca
            """, nativeQuery = true)
    List<EstadisticaMarcaProjection> obtenerEstadisticasPorMarca();

    // TODO (tu ejercicio): existsByPlaca, buscar activo por id, listar activos
}