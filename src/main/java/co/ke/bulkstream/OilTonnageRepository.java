package co.ke.bulkstream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface OilTonnageRepository extends JpaRepository<OilTonnage, Long> {
    // For search functionality:
    @Query("SELECT o FROM OilTonnage o WHERE " +
           "CAST(o.volume AS string) LIKE %:searchTerm% OR " +
           "CAST(o.density AS string) LIKE %:searchTerm% OR " +
           "CAST(o.temperature AS string) LIKE %:searchTerm% OR " +
           "CAST(o.vcf AS string) LIKE %:searchTerm% OR " +
           "CAST(o.tonnage AS string) LIKE %:searchTerm% OR " +
           "DATE_FORMAT(o.calculationDate, '%Y-%m-%d %H:%i:%s') LIKE %:searchTerm%")
    Page<OilTonnage> searchAllFields(@Param("searchTerm") String searchTerm, Pageable pageable);
}