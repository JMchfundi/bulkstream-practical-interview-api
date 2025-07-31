package co.ke.bulkstream;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "oil_tonnages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OilTonnage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double volume;
    private Double density;
    private Double temperature;
    private Double vcf;
    private Double tonnage;
    private LocalDateTime calculationDate; // Mapped to TIMESTAMP in SQL
}