package co.ke.bulkstream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VcftableRepository extends JpaRepository<Vcftable, Long> {

    // Custom query to find the nearest VCF based on density and temperature
    @Query(value = "SELECT v FROM Vcftable v " +
                   "WHERE ABS(v.density - :density) = (SELECT MIN(ABS(v2.density - :density)) FROM Vcftable v2) " +
                   "AND ABS(v.temperature - :temperature) = (SELECT MIN(ABS(v3.temperature - :temperature)) FROM Vcftable v3 " +
                   "WHERE ABS(v3.density - :density) = (SELECT MIN(ABS(v4.density - :density)) FROM Vcftable v4)) " +
                   "ORDER BY ABS(v.density - :density) ASC, ABS(v.temperature - :temperature) ASC " +
                   "LIMIT 1")
    Optional<Vcftable> findNearestVcf(@Param("density") Double density, @Param("temperature") Double temperature);

    // Another approach for finding nearest, potentially more performant depending on data distribution
    // This finds the VCF with the closest density and then from those, the closest temperature
    @Query(value = "SELECT v FROM Vcftable v " +
            "WHERE v.density = (SELECT v2.density FROM Vcftable v2 ORDER BY ABS(v2.density - :density) ASC LIMIT 1) " +
            "ORDER BY ABS(v.temperature - :temperature) ASC " +
            "LIMIT 1")
    Optional<Vcftable> findVcfByClosestDensityAndTemperature(@Param("density") Double density, @Param("temperature") Double temperature);


}