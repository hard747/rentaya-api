package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlaca(String placa);

    Optional<Vehiculo> findByIdAndActivoTrue(Long id);

    List<Vehiculo> findByActivoTrueOrderByIdAsc();

    @Query("SELECT v FROM Vehiculo v WHERE v.activo = true AND TYPE(v) = :tipo ORDER BY v.id")
    List<Vehiculo> findActivosPorTipo(@Param("tipo") Class<? extends Vehiculo> tipo);

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
}
