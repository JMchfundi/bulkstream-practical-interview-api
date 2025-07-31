package co.ke.bulkstream;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "vcftable") // Or "table60b" if you named it that
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vcftable {
    @Id
    private Long id;
    private Double density;
    private Double temperature;
    private Double vcf;
    // You might need to add 'class' and 'vcf2' fields if they are actually used.
    // Based on the SQL snippet, they are NULL, so might not be critical.
    // private String class; // 'class' is a reserved keyword, use a different name if needed
    // private Double vcf2;
}